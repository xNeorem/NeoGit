package it.unisa.neogit;

import it.unisa.neogit.entity.Commit;
import it.unisa.neogit.entity.Repository;
import it.unisa.neogit.entity.RepositoryP2P;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

public class NeoGit implements GitProtocol{

  private Peer peer;
  private PeerDHT dht;
  private HashMap<String,File> repos;
  private RepositoryP2P cashedRepo;
  private String user;
  private String reposPath;
  final static private int DEFAULT_MASTER_PORT=4000;


  public NeoGit(int _id, String _master_peer,String path) throws Exception {

    this.peer= new PeerBuilder(Number160.createHash(_id)).ports(DEFAULT_MASTER_PORT+_id).start();
    this.dht = new PeerBuilderDHT(peer).start();
    this.repos = new HashMap<>();
    this.user = Integer.toString(_id);
    this.reposPath = path + "/repos.ng";

    FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer)).ports(DEFAULT_MASTER_PORT).start();
    fb.awaitUninterruptibly();
    if(fb.isSuccess()) {
      peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
    }else {
      throw new Exception("Error in master peer bootstrap.");
    }

    peer.objectDataReply((sender, request) -> {
      if(!this.repos.containsKey(request)) return false;
      RepositoryP2P repo = loadRepo(this.repos.get(request));
      repo.setHasIncomingChanges(true);
      saveRepo(this.repos.get(request),repo);
      return true;
    });

    File reposFile = new File(this.reposPath);
    if(reposFile.exists() && !reposFile.isDirectory()){
      this.repos = NeoGit.loadRepos(reposFile);
      for(String repo : this.repos.keySet())
        joinRepo(repo);
    }
    else
      NeoGit.saveRepos(reposFile,this.repos);

  }

  /**
   * Creates new repository in a directory
   *
   * @param _repo_name a String, the name of the repository.
   * @param _directory a File, the directory where create the repository.
   * @return true if it is correctly created, false otherwise.
   */
  @Override
  public boolean createRepository(String _repo_name, File _directory) {
    if(_repo_name == null || _directory == null) return false;
    try{

      String directory = _directory+"/"+_repo_name;
      File directoryFile = new File(directory);

      if(!(new File(directory+"/data.ng").getParentFile().mkdirs())){
        return false;
      }

      FutureGet futureGet = dht.get(Number160.createHash(_repo_name)).start();
      futureGet.awaitUninterruptibly();

      if(!futureGet.isSuccess()) return false;
      if(!futureGet.isEmpty()) return false;

      this.cashedRepo = new RepositoryP2P(_repo_name,this.user);
      this.cashedRepo.addPeerAndress(this.peer.peerAddress());
      this.dht.put(Number160.createHash(_repo_name)).data(new Data(this.cashedRepo)).start().awaitUninterruptibly();
      NeoGit.saveRepo(directoryFile,this.cashedRepo);

      this.repos.put(_repo_name,directoryFile);
      NeoGit.saveRepos(new File(this.reposPath),this.repos);

    }catch (Exception e){
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Clone a remote repository in a directory
   *
   * @param _repo_name a String, the name of the repository.
   * @param _directory a File, the directory where create the repository.
   * @return true if it is correctly created, false otherwise.
   */
  @Override
  public boolean cloneRepository(String _repo_name, File _directory) {
    if(_repo_name == null || _directory == null) return false;
    try{

      String directory = _directory+"/"+_repo_name;
      File directoryFile = new File(directory);

      if(!(new File(directory+"/data.ng").getParentFile().mkdirs())){
        return false;
      }

      FutureGet futureGet = dht.get(Number160.createHash(_repo_name)).start();
      futureGet.awaitUninterruptibly();

      if(!futureGet.isSuccess()) return false;
      if(futureGet.isEmpty()) return false;

      this.cashedRepo = (RepositoryP2P) futureGet.dataMap().values().iterator().next()
          .object();
      HashMap<File,String> files = this.cashedRepo.getFiles();
      for(File file : files.keySet())
        NeoGit.writeFile(directory+"/"+file.getPath(),files.get(file));

      this.cashedRepo.addPeerAndress(this.peer.peerAddress());
      this.dht.put(Number160.createHash(_repo_name)).data(new Data(this.cashedRepo)).start().awaitUninterruptibly();
      NeoGit.saveRepo(directoryFile,this.cashedRepo);

      this.repos.put(_repo_name,directoryFile);
      NeoGit.saveRepos(new File(this.reposPath),this.repos);

    }catch (Exception e){
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Show commits from a local Repository
   *
   * @param _repo_name a String, the name of the repository.
   * @return List<Commit> if made a Commit, null otherwise.
   */
  @Override
  public List<Commit> showLocalHistory(String _repo_name) {
    if(_repo_name == null ) return null;
    if(!this.repos.containsKey(_repo_name)) return null;

    if(this.cashedRepo == null || !this.cashedRepo.getName().equals(_repo_name)){
      this.cashedRepo = NeoGit.loadRepo(this.repos.get(_repo_name));
    }

    return this.cashedRepo.getCommits();
  }

  /**
   * Show a file present in a Repository
   *
   * @param _repo_name a String, the name of the repository.
   * @return true if file are present, null otherwise.
   */
  @Override
  public List<File> showFileRepository(String _repo_name) {
    if(_repo_name == null ) return null;
    if(!this.repos.containsKey(_repo_name)) return null;

    if(this.cashedRepo == null || !this.cashedRepo.getName().equals(_repo_name)){
      this.cashedRepo = NeoGit.loadRepo(this.repos.get(_repo_name));
    }

    return new ArrayList<>(this.cashedRepo.getFiles().keySet());
  }

  /**
   * Adds a list of File to the given local repository.
   *
   * @param _repo_name a String, the name of the repository.
   * @param files a list of Files to be added to the repository.
   * @return true if it is correctly added, false otherwise.
   */
  @Override
  public boolean addFilesToRepository(String _repo_name, List<File> files) {
    if(_repo_name == null || files == null) return false;
    if(files.size() == 0) return false;
    if(!this.repos.containsKey(_repo_name)) return false;

    if(this.cashedRepo == null || !this.cashedRepo.getName().equals(_repo_name)){
      this.cashedRepo = NeoGit.loadRepo(this.repos.get(_repo_name));
    }

    String dir = this.repos.get(_repo_name).getPath();
    HashMap<File,String> filesToAdd = new HashMap<>();

    try{
      for (File file : files) {
        File checkFile = new File(dir + "/" + file.getPath());
        if (checkFile.exists() && !checkFile.isDirectory()) {
          filesToAdd.put(file,
              new String(Files.readAllBytes(Paths.get(checkFile.getPath())), StandardCharsets.UTF_8));
        }
      }
    }catch (Exception e){
      e.printStackTrace();
      return false;
    }

    if(filesToAdd.size() == 0) return false;


    this.cashedRepo.addFile(filesToAdd);
    NeoGit.saveRepo(this.repos.get(_repo_name),this.cashedRepo);

    return true;
  }

  /**
   * Apply the changing to the files in  the local repository.
   *
   * @param _repo_name a String, the name of the repository.
   * @param _message a String, the message for this commit.
   * @return true if it is correctly committed, false otherwise.
   */
  @Override
  public boolean commit(String _repo_name, String _message) {
    if(_repo_name == null || _message == null) return false;
    if(!this.repos.containsKey(_repo_name)) return false;

    if(this.cashedRepo == null || !this.cashedRepo.getName().equals(_repo_name)){
      this.cashedRepo = NeoGit.loadRepo(this.repos.get(_repo_name));
    }

    if(this.cashedRepo.getStagedFiles().size() == 0) return false;

    this.cashedRepo.commit(_message,this.user);
    this.cashedRepo.setCanPush(true);
    NeoGit.saveRepo(this.repos.get(_repo_name),this.cashedRepo);

    return true;
  }

  /**
   * Push all commits on the Network. If the status of the remote repository is changed, the push
   * fails, asking for a pull.
   *
   * @param _repo_name _repo_name a String, the name of the repository.
   * @return a String, operation message.
   */
  @Override
  public String push(String _repo_name) {
    if(_repo_name == null) return "Repository name can not be null.";
    if(!this.repos.containsKey(_repo_name)) return "Can not push unknown repo\nCreate repository first.";

    if(this.cashedRepo == null || !this.cashedRepo.getName().equals(_repo_name)){
      this.cashedRepo = NeoGit.loadRepo(this.repos.get(_repo_name));
    }

    if(!this.cashedRepo.isCanPush()) return "Nothing to commit.";
    if(this.cashedRepo.isHasIncomingChanges()) return _repo_name+" in not up to date\nPull new changes.";
    else{
      if(!this.cashedRepo.isUpToDate(this.pullRepo(_repo_name)))
        return _repo_name+" is not up to date\nPull new changes.";
    }

    this.cashedRepo.setCanPush(false);
    int commitCount = this.cashedRepo.getCommitCount();
    this.cashedRepo.setCommitCount(0);

    try {
      FutureGet futureGet = dht.get(Number160.createHash(_repo_name)).start();
      futureGet.awaitUninterruptibly();
      if (futureGet.isSuccess()) {
        this.dht.put(Number160.createHash(_repo_name)).data(new Data(this.cashedRepo)).start()
            .awaitUninterruptibly();

        for (PeerAddress peer : this.cashedRepo.getContributors()) {
          if(!peer.equals(this.peer.peerAddress())) {
            FutureDirect futureDirect = this.dht.peer().sendDirect(peer)
                .object(this.cashedRepo.getName()).start();
            futureDirect.awaitUninterruptibly();
          }
        }
      }

    }catch (Exception e){
      this.cashedRepo.setCanPush(true);
      this.cashedRepo.setCommitCount(commitCount);
      e.printStackTrace();
      return "Error during pushing "+_repo_name+".";
    }

    NeoGit.saveRepo(this.repos.get(_repo_name),this.cashedRepo);

    return commitCount+" commit pushed on "+_repo_name;
  }

  /**
   * Pull the files from the Network. If there is a conflict, the system duplicates the files and
   * the user should manually fix the conflict.
   *
   * @param _repo_name _repo_name a String, the name of the repository.
   * @return a String, operation message.
   */
  @Override
  public String pull(String _repo_name) {

    if(_repo_name == null) return "Repository name can not be null.";
    if(!this.repos.containsKey(_repo_name)) return "Can not pull unknown repo\nCreate repository first.";


    RepositoryP2P incoming = this.pullRepo(_repo_name);
    if(incoming == null) return "error while pulling, try push again "+_repo_name;

    if(this.cashedRepo == null || !this.cashedRepo.getName().equals(_repo_name)){
      this.cashedRepo = NeoGit.loadRepo(this.repos.get(_repo_name));
    }

    if(this.cashedRepo.isUpToDate(incoming)){
      this.cashedRepo.setHasIncomingChanges(false);
      return _repo_name+" it's up to date.";
    }

    int newCommits;
    try {
      newCommits = this.addIncomingChanges(_repo_name,incoming);
    } catch (IOException e) {
      e.printStackTrace();
      return "error while adding new changes from "+_repo_name;
    }

    return newCommits+" new commit on "+_repo_name;
  }

  private RepositoryP2P pullRepo(String _repo_name){
    try {
      FutureGet futureGet = this.dht.get(Number160.createHash(_repo_name)).start();
      futureGet.awaitUninterruptibly();
      if(!futureGet.isSuccess()) return null;
      if(futureGet.isEmpty()) return null;

      return (RepositoryP2P) futureGet.dataMap().values().iterator().next().object();
    }catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  private int addIncomingChanges(String _repo_name,RepositoryP2P remoteRepo) throws IOException {
    File repoFile = this.repos.get(_repo_name);
    RepositoryP2P localRepo = loadRepo(repoFile);
    HashMap<File,String> localFiles = localRepo.getFiles();
    HashMap<File,String> incomingFiles = remoteRepo.getFiles();
    HashMap<File,String> filesToAdd = new HashMap<>();
    String dir = repoFile.getPath();

    for(File file : incomingFiles.keySet()){
      if(localFiles.containsKey(file)){
        if(!localFiles.get(file).equals(incomingFiles.get(file))){
          System.out.println("FOUND CONFLIT : "+file.getPath()+" needs a manual fix.");
          int lastDot = file.getPath().lastIndexOf(".");
          String ext = (lastDot != -1) ? file.getPath().substring(lastDot) : "";
          String path = (lastDot != -1) ? file.getPath().substring(0,lastDot) : file.getPath();

          File remoteFile = new File(dir+"/"+path+"_remote"+ext);
          NeoGit.writeFile(remoteFile.getPath(),incomingFiles.get(file));

        }

      }else{
        NeoGit.writeFile(dir+"/"+file.getPath(),incomingFiles.get(file));
        filesToAdd.put(file,incomingFiles.get(file));
      }
    }

    localRepo.addFileFromRemote(filesToAdd);

    int count = 0;
    Stack<Commit> remoteCommits = remoteRepo.getCommits();
    int remoteCommitsSize = remoteCommits.size();
    int index = localRepo.getCommits().size() - localRepo.getCommitCount();
    Commit lastRemoteCommit = localRepo.getLastRemoteCommit();
    while(remoteCommitsSize != count){
      Commit commit = remoteCommits.pop();
      if(lastRemoteCommit != null && lastRemoteCommit.equals(commit))
        break;

      localRepo.addCommit(commit,index);
      count++;
//      index++;
    }
    localRepo.setHasIncomingChanges(false);

    saveRepo(repoFile,localRepo);
    this.cashedRepo = localRepo;

    return count;
  }

  public void leaveNetwork() {
    for(String repo: this.repos.keySet())
      leaveRepo(repo);

    dht.peer().announceShutdown().start().awaitUninterruptibly();
  }

  private void joinRepo(String _repo_name){
    try {
      RepositoryP2P repo = pullRepo(_repo_name);
      if(repo == null) return;

      repo.addPeerAndress(this.peer.peerAddress());
      FutureGet futureGet = dht.get(Number160.createHash(_repo_name)).start();
      futureGet.awaitUninterruptibly();
      if (futureGet.isSuccess()) {
        this.dht.put(Number160.createHash(_repo_name)).data(new Data(this.cashedRepo)).start()
            .awaitUninterruptibly();
      }

    }catch (Exception e){
      e.printStackTrace();
    }
  }

  private void leaveRepo(String _repo_name){
    try {
      RepositoryP2P repo = pullRepo(_repo_name);
      if(repo == null) return;

      repo.removePeerAndress(this.peer.peerAddress());
      FutureGet futureGet = dht.get(Number160.createHash(_repo_name)).start();
      futureGet.awaitUninterruptibly();
      if (futureGet.isSuccess()) {
        this.dht.put(Number160.createHash(_repo_name)).data(new Data(this.cashedRepo)).start()
            .awaitUninterruptibly();
      }

    }catch (Exception e){
      e.printStackTrace();
    }
  }

  private static void writeFile(String path, String data) throws IOException {

    File file = new File(path);
    file.getParentFile().mkdirs();

    FileWriter myWriter = new FileWriter(file);
    myWriter.write(data);
    myWriter.close();

  }

  private static RepositoryP2P loadRepo(File repoFile){

    RepositoryP2P result = null;

    try{
      FileInputStream fin = new FileInputStream(repoFile+"/data.ng");
      ObjectInputStream ois = new ObjectInputStream(fin);
      result = (RepositoryP2P) ois.readObject();
      ois.close();

    }catch (Exception e){
      e.printStackTrace();
    }

    return result;

  }

  private static boolean saveRepo(File repoFile,Repository repo){

    try{
      FileOutputStream fout = new FileOutputStream(repoFile+"/data.ng");
      ObjectOutputStream oos = new ObjectOutputStream(fout);
      oos.writeObject(repo);
      oos.close();
      return true;

    }catch (Exception e){
      e.printStackTrace();
    }

    return false;

  }
  private static boolean saveRepos(File reposFile, HashMap<String,File> repos){

    try{
      FileOutputStream fout = new FileOutputStream(reposFile);
      ObjectOutputStream oos = new ObjectOutputStream(fout);
      oos.writeObject(repos);
      oos.close();
      return true;

    }catch (Exception e){
      e.printStackTrace();
    }

    return false;

  }

  private static HashMap<String,File> loadRepos(File reposFile){

    HashMap<String,File> result = null;

    try{
      FileInputStream fin = new FileInputStream(reposFile);
      ObjectInputStream ois = new ObjectInputStream(fin);
      result = (HashMap<String,File>) ois.readObject();
      ois.close();

    }catch (Exception e){
      e.printStackTrace();
    }

    return result;

  }

}
