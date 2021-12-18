package it.unisa.neogit.entity;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

public class RepostitoryFile implements Serializable {

  File file;
  String lastContributor;

  public RepostitoryFile(File file, String lastContributor) {
    this.file = file;
    this.lastContributor = lastContributor;
  }

  public static HashSet<RepostitoryFile> fromList(List<File> files,String lastContributor){
    HashSet<RepostitoryFile> list = new HashSet<>();
    for(File file : files){
      list.add(new RepostitoryFile(file,lastContributor));
    }
    return list;
  }

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public String getLastContributor() {
    return lastContributor;
  }

  public void setLastContributor(String lastContributor) {
    this.lastContributor = lastContributor;
  }
}
