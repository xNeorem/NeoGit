package it.unisa.neogit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

  private static NeoGit master,peer1,peer2,peer3;


  static void createTestingDir(){
    new File(testDirMaster).mkdirs();
    new File(testDirPeer1).mkdirs();
    new File(testDirPeer2).mkdirs();
    new File(testDirPeer3).mkdirs();
  }

  @BeforeAll
  static void createPeers() throws Exception{
    createTestingDir();
    master = new NeoGit(0,"127.0.0.1",testDirMaster);
    peer1 = new NeoGit(1,"127.0.0.1",testDirPeer1);
    peer2 = new NeoGit(2,"127.0.0.1",testDirPeer2);
    peer3 = new NeoGit(3,"127.0.0.1",testDirPeer3);
  }

  @AfterAll
  static void deleteTestingDir(){
    deleteDir(new File(testDir));
    master.leaveNetwork();
    peer1.leaveNetwork();
    peer2.leaveNetwork();
    peer3.leaveNetwork();

  }

  static void deleteDir(File file) {
    File[] contents = file.listFiles();
    if (contents != null) {
      for (File f : contents) {
        deleteDir(f);
      }
    }
    file.delete();
  }

  @Test
  void createRepository() {
    assertTrue(master.createRepository(repoName,new File(testDirMaster)));
    assertTrue(peer1.createRepository(repoName,new File(testDirPeer1)));
    assertTrue(peer2.createRepository(repoName,new File(testDirPeer2)));
    assertTrue(peer3.createRepository(repoName,new File(testDirPeer3)));
  }

  @Test
  void createExistingRepository() {
    assertFalse(master.createRepository(repoName,new File(testDirMaster)));
    assertFalse(peer1.createRepository(repoName,new File(testDirPeer1)));
    assertFalse(peer2.createRepository(repoName,new File(testDirPeer2)));
    assertFalse(peer3.createRepository(repoName,new File(testDirPeer3)));
  }

  @Test
  void addExistingFilesToRepositoryThatNotExist() {
    try {
      addFileToPeer(testDirMaster,"NotExist",files);

      ArrayList<File> fileList = new ArrayList<>(files.length);
      for (String file : files)
        fileList.add(new File(file));

      assertFalse(master.addFilesToRepository(repoName,fileList));

    } catch (IOException e) {
      fail();
    }
  }

  @Test
  void addExistingFilesToRepository() {
    try {
      addFileToPeer(testDirMaster,repoName,files);

      ArrayList<File> fileList = new ArrayList<>(files.length);
      for (String file : files)
        fileList.add(new File(file));

      assertTrue(master.addFilesToRepository(repoName,fileList));

    } catch (IOException e) {
      fail();
    }
  }

  @Test
  void addNotExistingFilesToRepository() {

    ArrayList<File> fileList = new ArrayList<>(files.length);
    for (String file : files)
      fileList.add(new File(file));

    assertFalse(peer1.addFilesToRepository(repoName,fileList));
  }


  static void addFileToPeer(String peer_dir,String repoName, String[] file_paths) throws IOException {

    for (String file_path : file_paths)
      new File(peer_dir +"/"+repoName+ "/" + file_path).createNewFile();

  }

  @Test
  void commitToRepositoryThatNotExist() {
    assertFalse(master.commit("NotExist",commitMessage));
  }

  @Test
  void commitToRepositoryWithZeroFiles() {
    assertFalse(peer1.commit(repoName,commitMessage));
  }

  @Test
  void commitToRepository() {
    assertTrue(master.commit(repoName,commitMessage));
  }

  @Test
  void push() {
  }

  @Test
  void pull() {
  }

}