package it.unisa.neogit;

import java.io.File;
import java.util.ArrayList;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class NeoGitRunner {

  @Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
  private static String master;

  @Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
  private static int id;

  @Option(name="-wd", aliases="--working_dir", usage="working directory that will be used to store file", required=false)
  private static String dir;

  public static void main(String[] args) {
    final CmdLineParser parser = new CmdLineParser( new NeoGitRunner());
    try {
      parser.parseArgument(args);
      NeoGit neoGit = new NeoGit(id,master);

      dir = (dir == null) ? System.getProperty("user.dir") : dir;
      System.out.println("Working Directory = " + dir);

      neoGit.createRepository("maro",new File(dir));
      neoGit.createRepository("patatern",new File(dir));

//      NeoGit neoGit2 = new NeoGit(0,"127.0.0.1");
//      neoGit2.createRepository("maro",new File(System.getProperty("user.dir")+"/neogit2"));
//      neoGit2.createRepository("patatern",new File(System.getProperty("user.dir")+"/neogit2"));

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


    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
