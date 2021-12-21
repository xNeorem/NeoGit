package it.unisa.neogit;

import java.io.File;
import java.util.ArrayList;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
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
      TextIO textIO = TextIoFactory.getTextIO();
      TextTerminal terminal = textIO.getTextTerminal();

      dir = (dir == null) ? System.getProperty("user.dir") : dir;

      NeoGit neoGit = new NeoGit(id,master,dir);
      String name = "";
      String message = "";

      terminal.printf("\nStaring peer id: %d on master node: %s using as working dir: %s\n",
          id, master,dir);
      while(true) {
        printMenu(terminal);

        int option = textIO.newIntInputReader()
            .withMaxVal(6)
            .withMinVal(1)
            .read("Option");
        switch (option) {
          case 1:
            terminal.printf("\nENTER REPOSITORY NAME\n");
            name = textIO.newStringInputReader()
                .withDefaultValue("default-repo")
                .read("Name:");
            if(neoGit.createRepository(name,new File(dir)))
              terminal.printf("\nREPOSITORY %s SUCCESSFULLY CREATED\n",name);
            else
              terminal.printf("\nERROR IN REPOSITORY CREATION\n");
            break;

          case 2:
            terminal.printf("\nENTER REPOSITORY NAME\n");
            name = textIO.newStringInputReader()
                .withDefaultValue("default-topic")
                .read("Name:");
//            if(neoGit.addFilesToRepository())
//              terminal.printf("\n SUCCESSFULLY SUBSCRIBED TO %s\n",sname);
//            else
//              terminal.printf("\nERROR IN TOPIC SUBSCRIPTION\n");
            break;

          case 3:
            terminal.printf("\nENTER REPOSITORY NAME\n");
            name = textIO.newStringInputReader()
                .withDefaultValue("default-topic")
                .read("Name:");
            terminal.printf("\nENTER COMMIT MESSAGE\n");
            message = textIO.newStringInputReader()
                .withDefaultValue("default-commit")
                .read("commit:");
            if(neoGit.commit(name,message))
              terminal.printf("\n SUCCESSFULLY COMMIT CHANGES TO %s\n",name);
            else
              terminal.printf("\nERROR WHILE COMMITTING TO %s\n",name);
            break;

          case 4:
            terminal.printf("\nENTER REPOSITORY NAME\n");
            name = textIO.newStringInputReader()
                .withDefaultValue("default-topic")
                .read("Name:");
            terminal.printf(neoGit.push(name));
            break;
          case 5:
            terminal.printf("\nENTER REPOSITORY NAME\n");
            name = textIO.newStringInputReader()
                .withDefaultValue("default-topic")
                .read("Name:");
            terminal.printf(neoGit.pull(name));
            break;
          case 6:
            terminal.printf("\nARE YOU SURE TO LEAVE THE NETWORK?\n");
            boolean exit = textIO.newBooleanInputReader().withDefaultValue(false).read("exit?");
            if(exit) {
              neoGit.leaveNetwork();
              System.exit(0);
            }
            break;

          default:
            break;
        }
      }


//      neoGit.createRepository("maro",new File(dir));
//      neoGit.createRepository("patatern",new File(dir));
//
//      ArrayList<File> files = new ArrayList<>();
//      files.add(new File("aa.txt"));
//      files.add(new File("ab.txt"));
//      files.add(new File("ba.txt"));
//      neoGit.addFilesToRepository("maro",files);
//      neoGit.commit("maro","marooonnn");
//
//      neoGit.addFilesToRepository("patatern",files);
//      neoGit.commit("patatern","marooonnn");
//
//      neoGit.addFilesToRepository("maro",files);
//
//      System.out.println(neoGit.push("maro"));
//      System.out.println(neoGit.pull("maro"));


    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void printMenu(TextTerminal terminal) {
    terminal.printf("\n1 - CREATE REPOSITORY\n");
    terminal.printf("\n2 - ADD FILES\n");
    terminal.printf("\n3 - COMMIT\n");
    terminal.printf("\n4 - PUSH\n");
    terminal.printf("\n5 - PULL\n");
    terminal.printf("\n6 - EXIT\n");

  }
}
