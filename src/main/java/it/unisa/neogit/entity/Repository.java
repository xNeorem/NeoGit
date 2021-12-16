package it.unisa.neogit.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;

public class Repository implements Serializable {

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

  @Override
  public String toString() {
    return "Repository{" +
        "name='" + name + '\'' +
        ", commits=" + commits +
        ", files=" + files +
        ", userName='" + userName + '\'' +
        '}';
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
    result = 31 * result + userName.hashCode();
    return result;
  }
}
