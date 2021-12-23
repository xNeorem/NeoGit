package it.unisa.neogit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NeoGitTest {

  final static String testDir = System.getProperty("user.dir")+"/testing";
  final static String testDirMaster = testDir+"/master";
  final static String testDirPeer1 = testDir+"/peer1";
  final static String testDirPeer2 = testDir+"/peer2";
  final static String testDirPeer3 = testDir+"/peer3";

  final static String[] files = new String[]{"test.txt","hello.txt","text.txt","adc.txt"};
  private static final String repoName = "test";

  private static final String commitMessage = "test commit";

  private NeoGit master,peer1,peer2,peer3;

  public NeoGitTest() throws Exception{

    if(!createSandBox())
      fail();

    master = new NeoGit(0,"127.0.0.1",testDirMaster);
    peer1 = new NeoGit(1,"127.0.0.1",testDirPeer1);
    peer2 = new NeoGit(2,"127.0.0.1",testDirPeer2);
    peer3 = new NeoGit(3,"127.0.0.1",testDirPeer3);

  }

  boolean createSandBox(){
    boolean result = new File(testDirMaster).mkdirs();
    result = result && new File(testDirPeer1).mkdirs();
    result = result && new File(testDirPeer2).mkdirs();
    result = result && new File(testDirPeer3).mkdirs();
    return result;
  }

  @AfterEach
  void clearSandBox(){
    master.leaveNetwork();
    peer1.leaveNetwork();
    peer2.leaveNetwork();
    peer3.leaveNetwork();
    if(!deleteDir(new File(testDir)))
      fail();

  }

  static boolean deleteDir(File file) {
    File[] contents = file.listFiles();
    boolean result = true;
    if (contents != null) {
      for (File f : contents) {
        result = result && deleteDir(f);
      }
    }
    return result && file.delete();
  }

  @Test
  void createRepository() {
    assertTrue(master.createRepository(repoName,new File(testDirMaster)));
  }

  @Test
  void createExistingRepository() {
    master.createRepository(repoName,new File(testDirMaster));

    assertFalse(master.createRepository(repoName,new File(testDirMaster)));
    assertFalse(peer1.createRepository(repoName,new File(testDirPeer1)));
    assertFalse(peer2.createRepository(repoName,new File(testDirPeer2)));
    assertFalse(peer3.createRepository(repoName,new File(testDirPeer3)));
  }

  @Test
  void addFilesToRepositoryThatNotExist() {
    master.createRepository(repoName,new File(testDirMaster));

    ArrayList<File> filesToAdd = new ArrayList<>();
    for(String name : files)
      filesToAdd.add(new File(name));

   assertFalse(master.addFilesToRepository("NotExist",filesToAdd));
  }

  @Test
  void addExistingFilesToRepository() {

    master.createRepository(repoName,new File(testDirMaster));
    try {
      addFileToPeer(testDirMaster,repoName,files);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }

    ArrayList<File> filesToAdd = new ArrayList<>();
    for(String name : files)
      filesToAdd.add(new File(name));

    assertTrue(master.addFilesToRepository(repoName,filesToAdd));


  }

  @Test
  void addNotExistingFilesToRepository() {

    master.createRepository(repoName,new File(testDirMaster));

    ArrayList<File> filesToAdd = new ArrayList<>();
    for(String name : files)
      filesToAdd.add(new File(name));

    assertFalse(master.addFilesToRepository(repoName,filesToAdd));

  }

  @Test
  void commitToRepositoryThatNotExist() {

    master.createRepository(repoName,new File(testDirMaster));
    try {
      addFileToPeer(testDirMaster,repoName,files);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }

    ArrayList<File> filesToAdd = new ArrayList<>();
    for(String name : files)
      filesToAdd.add(new File(name));

    master.addFilesToRepository(repoName,filesToAdd);

    assertTrue(master.commit("NotExist",commitMessage));

  }

  @Test
  void commitToRepository() {

    master.createRepository(repoName,new File(testDirMaster));
    try {
      addFileToPeer(testDirMaster,repoName,files);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }

    ArrayList<File> filesToAdd = new ArrayList<>();
    for(String name : files)
      filesToAdd.add(new File(name));

    master.addFilesToRepository(repoName,filesToAdd);

    assertTrue(master.commit(repoName,commitMessage));

  }



  static void addFileToPeer(String peer_dir,String repoName, String[] file_paths) throws IOException {

    for (String file_path : file_paths)
      new File(peer_dir +"/"+repoName+ "/" + file_path).createNewFile();

  }
//
//  @Test
//  void commitToRepositoryThatNotExist() {
//  }
//
//  @Test
//  void commitToRepositoryWithZeroFiles() {
//  }
//
//  @Test
//  void commitToRepository() {
//  }
//
//  @Test
//  void pushWithZeroCommits() {
//  }
//
//  @Test
//  void pushRepositoryThatNotExist() {
//  }
//
//  @Test
//  void pushRepository() {
//  }
//
//  @Test
//  void pull() {
//  }

}