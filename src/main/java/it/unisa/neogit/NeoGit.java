package it.unisa.neogit;

import it.unisa.neogit.entity.Repository;
import it.unisa.neogit.entity.RepositoryP2P;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.HashMap;
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
  final static private int DEFAULT_MASTER_PORT=4000;


  public NeoGit(int _id, String _master_peer) throws Exception {

    this.peer= new PeerBuilder(Number160.createHash(_id)).ports(DEFAULT_MASTER_PORT+_id).start();
    this.dht = new PeerBuilderDHT(peer).start();
    this.repos = new HashMap<>();

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
      this.cashedRepo = new RepositoryP2P(_repo_name,peer.peerAddress().inetAddress().toString());
      this.cashedRepo.addPeerAndress(peer.peerAddress());
      NeoGit.saveRepo(file,this.cashedRepo);

      FutureGet futureGet = dht.get(Number160.createHash(_repo_name)).start();
      futureGet.awaitUninterruptibly();
      if (futureGet.isSuccess() && futureGet.isEmpty())
        this.dht.put(Number160.createHash(_repo_name)).data(new Data(this.cashedRepo)).start().awaitUninterruptibly();

      this.repos.put(_repo_name,file);
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
    if(!this.repos.containsKey(_repo_name)) return false;

    if(!this.cashedRepo.getName().equals(_repo_name)){
      this.cashedRepo = NeoGit.loadRepo(this.repos.get(_repo_name));
    }

    this.cashedRepo.addFile(files);
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

    if(!this.cashedRepo.getName().equals(_repo_name)){
      this.cashedRepo = NeoGit.loadRepo(this.repos.get(_repo_name));
    }

    this.cashedRepo.commit(_message);
    this.cashedRepo.setCanCommit(true);
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
    if(_repo_name == null) return null;

    if(!this.repos.containsKey(_repo_name)) return null;

    if(!this.cashedRepo.getName().equals(_repo_name)){
      this.cashedRepo = NeoGit.loadRepo(this.repos.get(_repo_name));
    }
    if(!this.cashedRepo.isCanCommit()) return null;
    if(this.cashedRepo.isHasIncomingChanges()) return null;

    this.cashedRepo.setCanCommit(false);

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
      this.cashedRepo.setCanCommit(true);
      e.printStackTrace();
    }

    return "done";
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
    try {
      FutureGet futureGet = this.dht.get(Number160.createHash(_repo_name)).start();
      futureGet.awaitUninterruptibly();
      if (futureGet.isSuccess()) {
        Repository repo;
        repo = (Repository) futureGet.dataMap().values().iterator().next().object();
        System.out.println(repo);
      }
    }catch (Exception e) {
      e.printStackTrace();
    }
    return null;
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


}
