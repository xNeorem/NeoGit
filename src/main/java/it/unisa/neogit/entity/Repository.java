package it.unisa.neogit.entity;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

public class Repository implements Serializable {

  private String name;
  private final Stack<Commit> commits;
  private final HashSet<File> files;
  private final HashSet<File> stagedFiles;
  private boolean canCommit;
  private String userName;

  public Repository(String name, String userName) {
    this.name = name;
    this.userName = userName;
    this.commits = new Stack<>();
    this.files = new HashSet<>();
    this.stagedFiles = new HashSet<>();
    this.canCommit = false;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Stack<Commit> getCommits() {
    return commits;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public boolean isCanCommit() {
    return canCommit;
  }

  public void setCanCommit(boolean canCommit) {
    this.canCommit = canCommit;
  }

  public void addFile(List<File> files){
    this.stagedFiles.addAll(files);
    this.files.addAll(files);
  }

  public void commit(String message){
    this.commits.add(new Commit(message,userName,(HashSet<File>) this.stagedFiles.clone()));
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

    if (canCommit != that.canCommit) {
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
    return userName.equals(that.userName);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + commits.hashCode();
    result = 31 * result + files.hashCode();
    result = 31 * result + (canCommit ? 1 : 0);
    result = 31 * result + userName.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Repository{" +
        "name='" + name + '\'' +
        ", commits=" + commits +
        ", files=" + files +
        ", canCommit=" + canCommit +
        ", userName='" + userName + '\'' +
        '}';
  }
}
