package it.unisa.neogit;

import it.unisa.neogit.entity.Commit;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
      String name;
      String message;

      terminal.printf("\nStaring peer id: %d on master node: %s using as working dir: %s\n",
          id, master,dir);
      while(true) {
        printMenu(terminal);

        int option = textIO.newIntInputReader()
            .withMaxVal(9)
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
                .withDefaultValue("default-repo")
                .read("Name:");
            if(neoGit.cloneRepository(name,new File(dir)))
              terminal.printf("\nREPOSITORY %s SUCCESSFULLY CLONED\n",name);
            else
              terminal.printf("\nERROR WHILE CLONING REPOSITORY\n");
            break;

          case 3:
            terminal.printf("\nENTER REPOSITORY NAME\n");
            name = textIO.newStringInputReader()
                .withDefaultValue("default-repo")
                .read("Name:");
            List<File> files = addFiles(terminal,textIO,dir,name);
            if(neoGit.addFilesToRepository(name,files))
              terminal.printf("\nFILES ADDED TO %s\n",name);
            else
              terminal.printf("\nERROR WHILE ADDING FILE TO %s\n",name);
            break;

          case 4:
            terminal.printf("\nENTER REPOSITORY NAME\n");
            name = textIO.newStringInputReader()
                .withDefaultValue("default-repo")
                .read("Name:");
            terminal.printf("\nENTER COMMIT MESSAGE\n");
            message = textIO.newStringInputReader()
                .withDefaultValue("default-commit")
                .read("commit:");
            if(neoGit.commit(name,message))
              terminal.printf("\nSUCCESSFULLY COMMIT CHANGES TO %s\n",name);
            else
              terminal.printf("\nERROR WHILE COMMITTING TO %s\n",name);
            break;

          case 5:
            terminal.printf("\nENTER REPOSITORY NAME\n");
            name = textIO.newStringInputReader()
                .withDefaultValue("default-repo")
                .read("Name:");
            terminal.printf("\n%s\n",neoGit.push(name));
            break;
          case 6:
            terminal.printf("\nENTER REPOSITORY NAME\n");
            name = textIO.newStringInputReader()
                .withDefaultValue("default-repo")
                .read("Name:");
            terminal.printf("\n%s\n",neoGit.pull(name));
            break;
          case 7:
            terminal.printf("\nENTER REPOSITORY NAME\n");
            name = textIO.newStringInputReader()
                .withDefaultValue("default-repo")
                .read("Name:");
            terminal.printf("\nFILES:\n");
            for (File file : neoGit.showFileRepository(name))
              terminal.printf("%s\n",file.getPath());

            break;
          case 8:
            terminal.printf("\nENTER REPOSITORY NAME\n");
            name = textIO.newStringInputReader()
                .withDefaultValue("default-repo")
                .read("Name:");
            terminal.printf("\nCOMMITS:\n");
            for (Commit commit: neoGit.showLocalHistory(name))
              terminal.printf("%s %s %s files changed: %d\n",
                  commit.getUid(),commit.getUser(),commit.getDate(),commit.getFiles().size());
            break;
          case 9:
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
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void printMenu(TextTerminal terminal) {
    terminal.printf("\n1 - CREATE REPOSITORY\n");
    terminal.printf("\n2 - CLONE REPOSITORY\n");
    terminal.printf("\n3 - ADD FILES\n");
    terminal.printf("\n4 - COMMIT\n");
    terminal.printf("\n5 - PUSH\n");
    terminal.printf("\n6 - PULL\n");
    terminal.printf("\n7 - REPOSITORY STATUS\n");
    terminal.printf("\n8 - REPOSITORY LOG\n");

    terminal.printf("\n9 - EXIT\n");

  }

  private static List<File> addFiles(TextTerminal terminal, TextIO textIO,String dir,String repo_name ){

    HashSet<File> result = new HashSet<>();
    String path;
    while(true) {
      for (File file : result)
        terminal.printf(" - %s ADDED \n",file.getPath());

      terminal.printf("\n1 - ADD NEW FILE\n");
      terminal.printf("\n2 - BACK\n");

      int option = textIO.newIntInputReader()
          .withMaxVal(2)
          .withMinVal(1)
          .read("Option");
      switch (option) {
        case 1:
          terminal.printf("\nENTER FILE PATH\n");
          terminal.printf("\nWORKING PATH : %s/%s/\n",dir,repo_name);
          terminal.printf("\nFOR EXAMPLE TO ADD %s/%s/example.txt\nJUST WHRITE example.txt\n",dir,repo_name);
          path = textIO.newStringInputReader()
              .read("path:");
//          path = dir+"/"+repo_name+"/"+path;
          result.add(new File(path));
          break;
        case 2:
          return new ArrayList<>(result);
        default:
          break;
      }
    }



  }
}
