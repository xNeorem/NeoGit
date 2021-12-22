package it.unisa.neogit.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Stack;

public class Repository implements Serializable {

  private String name;
  private final Stack<Commit> commits;
  private final HashSet<RepostitoryFile> files;
  private final HashSet<RepostitoryFile> stagedFiles;
  private boolean canPush;
  private String creator;

  public Repository(String name, String userName) {
    this.name = name;
    this.creator = userName;
    this.commits = new Stack<>();
    this.files = new HashSet<>();
    this.stagedFiles = new HashSet<>();
    this.canPush = false;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Stack<Commit> getCommits() {
    return (Stack<Commit>) this.commits.clone();
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public boolean isCanPush() {
    return canPush;
  }

  public void setCanPush(boolean canPush) {
    this.canPush = canPush;
  }

  public HashSet<RepostitoryFile> getFiles() {
    return (HashSet<RepostitoryFile>) this.files.clone();
  }

  public HashSet<RepostitoryFile> getStagedFiles() {
    return (HashSet<RepostitoryFile>) this.stagedFiles.clone();
  }

  public void addFile(HashSet<RepostitoryFile> files){
    this.stagedFiles.addAll(files);
    this.files.addAll(files);
  }

  public void addCommit(Commit commit){
    this.commits.add(commit);
  }

  public void commit(String message,String userName){
    this.commits.add(new Commit(message,userName,(HashSet<RepostitoryFile>) this.stagedFiles.clone()));
    this.stagedFiles.clear();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Repository that = (Repository) o;

    if (canPush != that.canPush) {
      return false;
    }
    if (!name.equals(that.name)) {
      return false;
    }
    if (!commits.equals(that.commits)) {
      return false;
    }
    if (!files.equals(that.files)) {
      return false;
    }
    return creator.equals(that.creator);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + commits.hashCode();
    result = 31 * result + files.hashCode();
    result = 31 * result + (canPush ? 1 : 0);
    result = 31 * result + creator.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Repository{" +
        "name='" + name + '\'' +
        ", commits=" + commits +
        ", files=" + files +
        ", canCommit=" + canPush +
        ", userName='" + creator + '\'' +
        '}';
  }
}
