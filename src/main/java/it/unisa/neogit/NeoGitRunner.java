package it.unisa.neogit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NeoGitRunner {

  public static void main(String[] args) {
    System.out.println("Working Directory = " + System.getProperty("user.dir"));
    try {
      NeoGit neoGit = new NeoGit(0,"127.0.0.1");
      neoGit.createRepository("maro",new File(System.getProperty("user.dir")));
      neoGit.createRepository("patatern",new File(System.getProperty("user.dir")));

      NeoGit neoGit2 = new NeoGit(0,"127.0.0.1");
      neoGit2.createRepository("maro",new File(System.getProperty("user.dir")+"/neogit2"));
      neoGit2.createRepository("patatern",new File(System.getProperty("user.dir")+"/neogit2"));

      ArrayList<File> files = new ArrayList<>();
      files.add(new File("aa.txt"));
      files.add(new File("ab.txt"));
      files.add(new File("ba.txt"));
      neoGit.addFilesToRepository("maro",files);
      neoGit.commit("maro","marooonnn");

      neoGit.addFilesToRepository("patatern",files);
      neoGit.commit("patatern","marooonnn");

      neoGit.addFilesToRepository("maro",files);

      neoGit.push("maro");


      neoGit2.addFilesToRepository("maro",files);
      neoGit2.commit("maro","marooonnn");

      neoGit2.addFilesToRepository("patatern",files);
      neoGit2.commit("patatern","marooonnn");

      neoGit2.addFilesToRepository("maro",files);

      neoGit2.push("maro");


    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
