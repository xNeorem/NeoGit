package it.unisa.neogit.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Stack;
import net.tomp2p.peers.PeerAddress;

public class RepositoryP2P extends Repository implements Serializable {

  private final HashSet<PeerAddress> contributors;
  private boolean hasIncomingChanges;
  private int commitCount;



  public RepositoryP2P(String name, String userName) {
    super(name, userName);
    this.contributors = new HashSet<>();
    this.hasIncomingChanges = false;
    this.commitCount = 0;
  }

  @Override
  public void commit(String message,String userName){
    super.commit(message,userName);
    this.commitCount += 1;
  }

  public int getCommitCount() {
    return commitCount;
  }

  public void setCommitCount(int commitCount) {
    this.commitCount = commitCount;
  }

  public HashSet<PeerAddress> getContributors(){
    return (HashSet<PeerAddress>) this.contributors.clone();
  }

  public void addPeerAndress(PeerAddress peerAddress){
    this.contributors.add(peerAddress);
  }

  public void removePeerAndress(PeerAddress peerAddress){ this.contributors.remove(peerAddress);}

  public boolean isHasIncomingChanges() {
    return hasIncomingChanges;
  }

  public void setHasIncomingChanges(boolean hasIncomingChanges) {
    this.hasIncomingChanges = hasIncomingChanges;
  }

  public boolean isUpToDate(RepositoryP2P that){
    if(that == null)
      return true;

    Stack<Commit> thatCommits = that.getCommits();
    Stack<Commit> thisCommits = this.getCommits();

    if(thatCommits.size() == 0) // local repo is up to date because remote repo haven't got any commit.
      return true;

    if(thisCommits.size() - this.commitCount == 0) // local repo is NOT up to date because remote repo have at least one commit.
      return false;

    //get first commit pushed and check if is equals to remote last commit.
    for(int i = 0; i < this.commitCount; i++)
      thisCommits.pop();

    return thisCommits.pop().equals(that.getCommits().pop());

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
    if (commitCount != that.commitCount) {
      return false;
    }
    return contributors.equals(that.contributors);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + contributors.hashCode();
    result = 31 * result + (hasIncomingChanges ? 1 : 0);
    result = 31 * result + commitCount;
    return result;
  }

  @Override
  public String toString() {
    return "RepositoryP2P{" +
        "contributors=" + contributors +
        ", hasIncomingChanges=" + hasIncomingChanges +
        ", commitCount=" + commitCount +
        "} " + super.toString();
  }
}
