package it.unisa.neogit.entity;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class Repository implements Serializable {

  private String name;
  private final Stack<Commit> commits;
  private final HashMap<File,String> files;
  private final HashSet<File> stagedFiles;
  private boolean canPush;
  private String creator;

  public Repository(String name, String userName) {
    this.name = name;
    this.creator = userName;
    this.commits = new Stack<>();
    this.files = new HashMap<>();
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

  public HashMap<File,String> getFiles() {
    return (HashMap<File, String>) this.files.clone();
  }

  public HashSet<File> getStagedFiles() {
    return (HashSet<File>) this.stagedFiles.clone();
  }

  public void addFile(HashMap<File,String> files){
    this.stagedFiles.addAll(files.keySet());
    this.files.putAll(files);
  }

  public void addCommit(Commit commit){
    this.commits.add(commit);
  }
  public void addCommit(Commit commit, int index) {
    this.commits.add(index,commit);
  }

  public void commit(String message,String userName){
    HashMap<File,String> files = new HashMap<>(this.stagedFiles.size());
    for(File file : this.stagedFiles)
      files.put(file,this.files.get(file));

    this.commits.add(new Commit(message,userName,files));
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
