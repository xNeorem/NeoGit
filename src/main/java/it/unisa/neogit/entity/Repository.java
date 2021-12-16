package it.unisa.neogit.entity;

import java.util.ArrayList;
import java.util.Stack;

public class Repository {

  private String name;
  private Stack<Commit> commits;
  private ArrayList<String> files;
  private String userName;

  public Repository(String name, String userName) {
    this.name = name;
    this.userName = userName;
    this.commits = new Stack<>();
    this.files = new ArrayList<>();
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

  public void setCommits(Stack<Commit> commits) {
    this.commits = commits;
  }

  public ArrayList<String> getFiles() {
    return files;
  }

  public void setFiles(ArrayList<String> files) {
    this.files = files;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }
}
