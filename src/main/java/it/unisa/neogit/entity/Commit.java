package it.unisa.neogit.entity;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

public class Commit implements Serializable {

  final static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

  private String uid;
  private String message;
  private String date;
  private String user;
  private HashSet<File> files;


  public Commit(String message, String user, HashSet<File> files) {
    this.message = message;
    this.user = user;
    this.uid = UUID.randomUUID().toString();
    this.date = formatter.format(new Date(System.currentTimeMillis()));
    this.files = files;
  }

  public String getUid() {
    return uid;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public HashSet<File> getFiles(){ return (HashSet<File>) this.files.clone();}

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Commit commit = (Commit) o;

    if (!uid.equals(commit.uid)) {
      return false;
    }
    if (!message.equals(commit.message)) {
      return false;
    }
    if (!date.equals(commit.date)) {
      return false;
    }
    if (!user.equals(commit.user)) {
      return false;
    }
    return files.equals(commit.files);
  }

  @Override
  public int hashCode() {
    int result = uid.hashCode();
    result = 31 * result + message.hashCode();
    result = 31 * result + date.hashCode();
    result = 31 * result + user.hashCode();
    result = 31 * result + files.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Commit{" +
        "uid='" + uid + '\'' +
        ", message='" + message + '\'' +
        ", date=" + date +
        ", user='" + user + '\'' +
        ", files=" + files +
        '}';
  }
}
