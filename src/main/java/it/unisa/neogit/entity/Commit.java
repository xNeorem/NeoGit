package it.unisa.neogit.entity;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Commit implements Serializable {

  final static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

  private String uid;
  private String message;
  private Date date;
  private String user;
  private ArrayList<File> files;


  public Commit(String message, String user, ArrayList<File> files) {
    this.message = message;
    this.user = user;
    this.uid = UUID.randomUUID().toString();
    this.date = new Date(System.currentTimeMillis());
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

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public ArrayList<File> getFiles(){ return (ArrayList<File>) this.files.clone();}

  @Override
  public String toString() {
    return "Commit{" +
        "uid='" + uid + '\'' +
        ", message='" + message + '\'' +
        ", date=" + date +
        ", user='" + user + '\'' +
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
    return user.equals(commit.user);
  }

  @Override
  public int hashCode() {
    int result = uid.hashCode();
    result = 31 * result + message.hashCode();
    result = 31 * result + date.hashCode();
    result = 31 * result + user.hashCode();
    return result;
  }
}
