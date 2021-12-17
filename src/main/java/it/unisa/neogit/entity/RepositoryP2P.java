package it.unisa.neogit.entity;

import java.io.Serializable;
import java.util.HashSet;
import net.tomp2p.peers.PeerAddress;

public class RepositoryP2P extends Repository implements Serializable {

  private HashSet<PeerAddress> repoUsers;
  private boolean hasIncomingChanges;



  public RepositoryP2P(String name, String userName) {
    super(name, userName);
    this.repoUsers = new HashSet<>();
    this.hasIncomingChanges = false;
  }

}
