package it.unisa.neogit;

import it.unisa.neogit.entity.Commit;
import it.unisa.neogit.entity.Repository;
import it.unisa.neogit.entity.RepositoryP2P;
import it.unisa.neogit.entity.RepostitoryFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    if(reposFile.exists() && !reposFile.isDirectory())
      this.repos = NeoGit.loadRepos(reposFile);
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

      String directory = _directory+"/"+_repo_name+"/data.ng";

      File file = new File(directory);
      if(!file.getParentFile().mkdirs()){
        return false;
      }

      FutureGet futureGet = dht.get(Number160.createHash(_repo_name)).start();
      futureGet.awaitUninterruptibly();
      if (futureGet.isSuccess()){
         if(futureGet.isEmpty()){
           this.cashedRepo = new RepositoryP2P(_repo_name,this.user);
           this.cashedRepo.addPeerAndress(peer.peerAddress());
         }
         else{
           this.cashedRepo = (RepositoryP2P) futureGet.dataMap().values().iterator().next().object();
           this.cashedRepo.addPeerAndress(this.peer.peerAddress());
         }

        this.dht.put(Number160.createHash(_repo_name)).data(new Data(this.cashedRepo)).start().awaitUninterruptibly();
        NeoGit.saveRepo(file,this.cashedRepo);
      }


      this.repos.put(_repo_name,file);
      NeoGit.saveRepos(new File(this.reposPath),this.repos);

    }catch (Exception e){
      e.printStackTrace();
    }
    return true;
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

    if(!this.cashedRepo.getName().equals(_repo_name)){
      this.cashedRepo = NeoGit.loadRepo(this.repos.get(_repo_name));
    }

    files.removeIf(file -> file.exists() && !file.isDirectory());

    this.cashedRepo.addFile(RepostitoryFile.fromList(files,this.user));
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

    this.cashedRepo.commit(_message);
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
    if(!this.repos.containsKey(_repo_name)) return "Can not push unknow repo\nCreate repository first.";

    this.cashedRepo = NeoGit.loadRepo(this.repos.get(_repo_name));

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
      if (futureGet.isSuccess())
        this.dht.put(Number160.createHash(_repo_name)).data(new Data(this.cashedRepo)).start().awaitUninterruptibly();

      for (PeerAddress peer : this.cashedRepo.getContributors()){
          FutureDirect futureDirect = this.dht.peer().sendDirect(peer).object(this.cashedRepo.getName()).start();
          futureDirect.awaitUninterruptibly();
      }

    }catch (Exception e){
      this.cashedRepo.setCanPush(true);
      this.cashedRepo.setCommitCount(commitCount);
      e.printStackTrace();
      return "Error during pushing "+_repo_name+".";
    }

    return commitCount+"commit pushed on "+_repo_name;
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
    if(!this.repos.containsKey(_repo_name)) return "Can not push unknow repo\nCreate repository first.";


    RepositoryP2P incoming = this.pullRepo(_repo_name);
    if(incoming == null) return "error while pulling "+_repo_name;

    int newCommits = this.addIncomingChanges(this.repos.get(_repo_name),incoming,this.user);
    if(newCommits == 0){
      return _repo_name+" it's up to date.";
    }
    return newCommits+" new commit on "+_repo_name;
  }

  private RepositoryP2P pullRepo(String _repo_name){
    try {
      FutureGet futureGet = this.dht.get(Number160.createHash(_repo_name)).start();
      futureGet.awaitUninterruptibly();
      if (futureGet.isSuccess())
        return (RepositoryP2P) futureGet.dataMap().values().iterator().next().object();
    }catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  private int addIncomingChanges(File local,RepositoryP2P incomingRepo ,String user){
    RepositoryP2P localRepo = loadRepo(local);
    HashSet<RepostitoryFile> localFiles = localRepo.getFiles();
    HashSet<RepostitoryFile> incomingFiles = incomingRepo.getFiles();

    for(RepostitoryFile file : incomingFiles){
      if(localFiles.contains(file) && !file.getLastContributor().equals(user)){
        //TODO: duplicate file
        System.out.println(file+"need to be duplicate");
      }
    }

    Commit lastCommitPushed = localRepo.getCommits().get(localRepo.getCommits().size() - localRepo
        .getCommitCount());

    Commit incomingCommit = incomingRepo.getCommits().pop();
    int count = 0;
    while(!lastCommitPushed.equals(incomingCommit)){
      localRepo.addCommit(incomingCommit);
      count++;
    }

    localRepo.setCommitCount(0);
    saveRepo(local,localRepo);
    this.cashedRepo = localRepo;

    return count;
  }

  public void leaveNetwork() {
    dht.peer().announceShutdown().start().awaitUninterruptibly();
  }

  private static RepositoryP2P loadRepo(File repoFile){

    RepositoryP2P result = null;

    try{
      FileInputStream fin = new FileInputStream(repoFile);
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
      FileOutputStream fout = new FileOutputStream(repoFile);
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
