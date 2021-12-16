package it.unisa.neogit.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Commit {

  final static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

  private String uid;
  private String message;
  private Date date;
  private String user;


  public Commit(String message, String user) {
    this.message = message;
    this.user = user;
    this.uid = "";
    this.date = new Date(System.currentTimeMillis());
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
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
}
