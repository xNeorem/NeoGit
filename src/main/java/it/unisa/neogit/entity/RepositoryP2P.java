package it.unisa.neogit.entity;

import java.io.Serializable;
import java.util.HashSet;
import net.tomp2p.peers.PeerAddress;

public class RepositoryP2P extends Repository implements Serializable {

  private final HashSet<PeerAddress> contributors;
  private boolean hasIncomingChanges;



  public RepositoryP2P(String name, String userName) {
    super(name, userName);
    this.contributors = new HashSet<>();
    this.hasIncomingChanges = false;
  }

  public HashSet<PeerAddress> getContributors(){
    return (HashSet<PeerAddress>) this.contributors.clone();
  }

  public void addPeerAndress(PeerAddress peerAddress){
    this.contributors.add(peerAddress);
  }

  public boolean isHasIncomingChanges() {
    return hasIncomingChanges;
  }

  public void setHasIncomingChanges(boolean hasIncomingChanges) {
    this.hasIncomingChanges = hasIncomingChanges;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    RepositoryP2P that = (RepositoryP2P) o;

    if (hasIncomingChanges != that.hasIncomingChanges) {
      return false;
    }
    return contributors.equals(that.contributors);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + contributors.hashCode();
    result = 31 * result + (hasIncomingChanges ? 1 : 0);
    return result;
  }


  @Override
  public String toString() {
    return "RepositoryP2P{" +
        "contributors=" + contributors +
        ", hasIncomingChanges=" + hasIncomingChanges +
        "} " + super.toString();
  }
}
