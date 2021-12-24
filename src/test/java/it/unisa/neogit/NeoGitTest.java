package it.unisa.neogit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NeoGitTest {

  final static String testDir = System.getProperty("user.dir")+"/testing";
  final static String testDirMaster = testDir+"/master";
  final static String testDirPeer1 = testDir+"/peer1";
  final static String testDirPeer2 = testDir+"/peer2";
  final static String testDirPeer3 = testDir+"/peer3";

  final static String[] files = new String[]{"test.txt","hello.txt","text.txt","adc.txt"};
  static String repoName = "test";
  private static int count = 0;

  final static String commitMessage = "test commit";

  private static NeoGit master,peer1,peer2,peer3;

  @BeforeEach
  void setUpTest(){
    NeoGitTest.repoName = "test"+count++;
  }

  @BeforeAll
  static void createSandBox() throws Exception{
    boolean result = new File(testDirMaster).mkdirs();
    result = result && new File(testDirPeer1).mkdirs();
    result = result && new File(testDirPeer2).mkdirs();
    result = result && new File(testDirPeer3).mkdirs();
    if(!result) fail();


    master = new NeoGit(0,"127.0.0.1",testDirMaster);
    peer1 = new NeoGit(1,"127.0.0.1",testDirPeer1);
    peer2 = new NeoGit(2,"127.0.0.1",testDirPeer2);
    peer3 = new NeoGit(3,"127.0.0.1",testDirPeer3);
  }

  @AfterAll
  static void clearSandBox() throws Exception{
    master.leaveNetwork();
    peer1.leaveNetwork();
    peer2.leaveNetwork();
    peer3.leaveNetwork();

    master = null;
    peer1 = null;
    peer2 = null;
    peer3 = null;

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
  void createAndCloneRepository() {
    peer1.createRepository(repoName,new File(testDirMaster));

    assertFalse(master.createRepository(repoName,new File(testDirMaster)));
    assertFalse(peer1.createRepository(repoName,new File(testDirPeer1)));

    assertTrue(peer2.cloneRepository(repoName,new File(testDirPeer2)));
    assertTrue(peer3.cloneRepository(repoName,new File(testDirPeer3)));
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

    assertFalse(master.commit("NotExist",commitMessage));

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

  @Test
  void pushToRepositoryThatNotExist() {

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
    master.commit(repoName,commitMessage);

    assertEquals("Can not push unknown repo\nCreate repository first.",master.push("NotExist"));

  }

  @Test
  void pushToRepositoryWithNoCommit() {

    master.createRepository(repoName,new File(testDirMaster));
    assertEquals("Nothing to commit.",master.push(repoName));

  }

  @Test
  void pushToRepository() {

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
    master.commit(repoName,commitMessage);

    assertEquals("1 commit pushed on "+repoName,master.push(repoName));

  }

  @Test
  void pullFromRepository() {

    master.createRepository(repoName,new File(testDirMaster));
    peer1.cloneRepository(repoName,new File(testDirPeer1));
    peer2.cloneRepository(repoName,new File(testDirPeer2));
    peer3.cloneRepository(repoName,new File(testDirPeer3));

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
    master.commit(repoName,commitMessage);

    master.push(repoName);

    assertEquals("1 new commit on "+repoName,peer1.pull(repoName));
    assertEquals("1 new commit on "+repoName,peer2.pull(repoName));
    assertEquals("1 new commit on "+repoName,peer3.pull(repoName));

  }

  @Test
  void pushWithIncomingChanges() {

    peer1.createRepository(repoName,new File(testDirMaster));
    master.cloneRepository(repoName,new File(testDirPeer1));

    try {
      addFileToPeer(testDirMaster,repoName,files);
      addFileToPeer(testDirPeer1,repoName,files);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }

    ArrayList<File> filesToAdd = new ArrayList<>();
    for(String name : files)
      filesToAdd.add(new File(name));

    master.addFilesToRepository(repoName,filesToAdd);
    master.commit(repoName,commitMessage);
    master.push(repoName);

    peer1.addFilesToRepository(repoName,filesToAdd);
    peer1.commit(repoName,commitMessage);

    assertEquals(repoName+" is not up to date\nPull new changes.",peer1.push(repoName));

  }

  @Test
  void pullWithConflict() {

    peer1.createRepository(repoName,new File(testDirMaster));
    master.cloneRepository(repoName,new File(testDirPeer1));

    try {
      addFileToPeer(testDirMaster,repoName,files);
      addFileToPeer(testDirPeer1,repoName,files);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }

    ArrayList<File> filesToAdd = new ArrayList<>();
    for(String name : files)
      filesToAdd.add(new File(name));

    master.addFilesToRepository(repoName,filesToAdd);
    master.commit(repoName,commitMessage);
    master.push(repoName);

    peer1.addFilesToRepository(repoName,filesToAdd);
    peer1.commit(repoName,commitMessage);

    assertEquals("1 new commit on "+repoName,peer1.pull(repoName));

  }

  @Test
  void operationUsing4peer(){

    peer1.createRepository(repoName,new File(testDirPeer1));
    master.cloneRepository(repoName,new File(testDirMaster));
    peer2.cloneRepository(repoName,new File(testDirPeer2));
    peer3.cloneRepository(repoName,new File(testDirPeer3));

    String[] masterFile = new String[]{NeoGitTest.files[0]};
    String[] peer1file = new String[]{NeoGitTest.files[1]};
    String[] peer2file = new String[]{NeoGitTest.files[2]};
    String[] peer3file = new String[]{NeoGitTest.files[3]};

    try {
      addFileToPeer(testDirMaster,repoName,masterFile);
      addFileToPeer(testDirPeer1,repoName,peer1file);
      addFileToPeer(testDirPeer2,repoName,peer2file);
      addFileToPeer(testDirPeer3,repoName,peer3file);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }

    ArrayList<File> filesToAddMaster = new ArrayList<>();
    for(String name : masterFile)
      filesToAddMaster.add(new File(name));

    master.addFilesToRepository(repoName,filesToAddMaster);
    master.commit(repoName,commitMessage);


    ArrayList<File> filesToAddPeer1 = new ArrayList<>();
    for(String name : peer1file)
      filesToAddPeer1.add(new File(name));

    peer1.addFilesToRepository(repoName,filesToAddPeer1);
    peer1.commit(repoName,commitMessage);

    ArrayList<File> filesToAddPeer2 = new ArrayList<>();
    for(String name : peer2file)
      filesToAddPeer2.add(new File(name));

    peer2.addFilesToRepository(repoName,filesToAddPeer2);
    peer2.commit(repoName,commitMessage);

    ArrayList<File> filesToAddPeer3 = new ArrayList<>();
    for(String name : peer3file)
      filesToAddPeer3.add(new File(name));

    peer3.addFilesToRepository(repoName,filesToAddPeer3);
    peer3.commit(repoName,commitMessage);


    assertEquals("1 commit pushed on "+repoName,master.push(repoName));
    assertEquals(repoName+" is not up to date\nPull new changes.",peer1.push(repoName));
    assertEquals(repoName+" is not up to date\nPull new changes.",peer2.push(repoName));
    assertEquals(repoName+" is not up to date\nPull new changes.",peer3.push(repoName));

    assertEquals("1 new commit on "+repoName,peer1.pull(repoName));
    assertEquals("1 commit pushed on "+repoName,peer1.push(repoName));

    assertEquals("2 new commit on "+repoName,peer2.pull(repoName));
    assertEquals("1 commit pushed on "+repoName,peer2.push(repoName));

    assertEquals("3 new commit on "+repoName,peer3.pull(repoName));
    assertEquals("1 commit pushed on "+repoName,peer3.push(repoName));

    assertEquals("Nothing to commit.",master.push(repoName));

    assertEquals("2 new commit on "+repoName,peer1.pull(repoName));

  }

  @Test
  void operationUsing4peer2(){

    peer1.createRepository(repoName,new File(testDirPeer1));
    master.cloneRepository(repoName,new File(testDirMaster));
    peer2.cloneRepository(repoName,new File(testDirPeer2));
    peer3.cloneRepository(repoName,new File(testDirPeer3));

    String[] masterFile = new String[]{NeoGitTest.files[0]};
    String[] peer1file = new String[]{NeoGitTest.files[0], NeoGitTest.files[1]};
    String[] peer2file = new String[]{NeoGitTest.files[1],NeoGitTest.files[2]};
    String[] peer3file = new String[]{NeoGitTest.files[2],NeoGitTest.files[3]};

    try {
      addFileToPeer(testDirMaster,repoName,masterFile);
      addFileToPeer(testDirPeer1,repoName,peer1file);
      addFileToPeer(testDirPeer2,repoName,peer2file);
      addFileToPeer(testDirPeer3,repoName,peer3file);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }

    ArrayList<File> filesToAddMaster = new ArrayList<>();
    for(String name : masterFile)
      filesToAddMaster.add(new File(name));

    master.addFilesToRepository(repoName,filesToAddMaster);
    master.commit(repoName,commitMessage);


    ArrayList<File> filesToAddPeer1 = new ArrayList<>();
    for(String name : peer1file)
      filesToAddPeer1.add(new File(name));

    peer1.addFilesToRepository(repoName,filesToAddPeer1);
    peer1.commit(repoName,commitMessage);

    ArrayList<File> filesToAddPeer2 = new ArrayList<>();
    for(String name : peer2file)
      filesToAddPeer2.add(new File(name));

    peer2.addFilesToRepository(repoName,filesToAddPeer2);
    peer2.commit(repoName,commitMessage);

    ArrayList<File> filesToAddPeer3 = new ArrayList<>();
    for(String name : peer3file)
      filesToAddPeer3.add(new File(name));

    peer3.addFilesToRepository(repoName,filesToAddPeer3);
    peer3.commit(repoName,commitMessage);


    assertEquals("1 commit pushed on "+repoName,master.push(repoName));
    assertEquals(repoName+" is not up to date\nPull new changes.",peer1.push(repoName));
    assertEquals(repoName+" is not up to date\nPull new changes.",peer2.push(repoName));
    assertEquals(repoName+" is not up to date\nPull new changes.",peer3.push(repoName));

    assertEquals("1 new commit on "+repoName,peer1.pull(repoName));
    assertEquals("1 commit pushed on "+repoName,peer1.push(repoName));

    assertEquals("2 new commit on "+repoName,peer2.pull(repoName));
    assertEquals("1 commit pushed on "+repoName,peer2.push(repoName));

    assertEquals("3 new commit on "+repoName,peer3.pull(repoName));
    assertEquals("1 commit pushed on "+repoName,peer3.push(repoName));

    assertEquals("Nothing to commit.",master.push(repoName));

    assertEquals("2 new commit on "+repoName,peer1.pull(repoName));

  }

  @Test
  void operationUsing4peer3(){

    master.createRepository(repoName,new File(testDirMaster));
    peer1.cloneRepository(repoName,new File(testDirPeer1));


    String[] masterFile = new String[]{NeoGitTest.files[0]};
    String[] peer1file = new String[]{NeoGitTest.files[0], NeoGitTest.files[1]};
    String[] peer2file = new String[]{NeoGitTest.files[1],NeoGitTest.files[2]};
    String[] peer3file = new String[]{NeoGitTest.files[2],NeoGitTest.files[3]};

    try {
      addFileToPeer(testDirMaster,repoName,masterFile);
      addFileToPeer(testDirPeer1,repoName,peer1file);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }

    ArrayList<File> filesToAddMaster = new ArrayList<>();
    for(String name : masterFile)
      filesToAddMaster.add(new File(name));

    master.addFilesToRepository(repoName,filesToAddMaster);
    master.commit(repoName,commitMessage);


    ArrayList<File> filesToAddPeer1 = new ArrayList<>();
    for(String name : peer1file)
      filesToAddPeer1.add(new File(name));

    peer1.addFilesToRepository(repoName,filesToAddPeer1);
    peer1.commit(repoName,commitMessage);


    assertEquals("1 commit pushed on "+repoName,master.push(repoName));
    assertEquals(repoName+" is not up to date\nPull new changes.",peer1.push(repoName));

    peer2.cloneRepository(repoName,new File(testDirPeer2));
    try {
      addFileToPeer(testDirPeer2,repoName,peer2file);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }
    ArrayList<File> filesToAddPeer2 = new ArrayList<>();
    for(String name : peer2file)
      filesToAddPeer2.add(new File(name));

    peer2.addFilesToRepository(repoName,filesToAddPeer2);
    peer2.commit(repoName,commitMessage);

    assertEquals("1 commit pushed on "+repoName,peer2.push(repoName));

    peer3.cloneRepository(repoName,new File(testDirPeer3));
    try {
      addFileToPeer(testDirPeer3,repoName,peer3file);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }
    ArrayList<File> filesToAddPeer3 = new ArrayList<>();
    for(String name : peer3file)
      filesToAddPeer3.add(new File(name));

    peer3.addFilesToRepository(repoName,filesToAddPeer3);
    peer3.commit(repoName,commitMessage);

    assertEquals("2 new commit on "+repoName,peer1.pull(repoName));
    assertEquals("1 commit pushed on "+repoName,peer1.push(repoName));

    assertEquals(repoName+" is not up to date\nPull new changes.",peer3.push(repoName));

    assertEquals("1 new commit on "+repoName,peer3.pull(repoName));
    assertEquals("1 commit pushed on "+repoName,peer3.push(repoName));

    assertEquals("Nothing to commit.",master.push(repoName));


  }
  @Test
  void operationUsing4peer4(){

    master.createRepository(repoName,new File(testDirMaster));
    peer1.cloneRepository(repoName,new File(testDirPeer1));


    String[] masterFile = new String[]{NeoGitTest.files[0]};
    String[] peer1file = new String[]{NeoGitTest.files[0], NeoGitTest.files[1]};
    String[] peer2file = new String[]{NeoGitTest.files[1],NeoGitTest.files[2]};
    String[] peer3file = new String[]{NeoGitTest.files[2],NeoGitTest.files[3]};

    try {
      addFileToPeer(testDirMaster,repoName,masterFile);
      addFileToPeer(testDirPeer1,repoName,peer1file);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }

    ArrayList<File> filesToAddMaster = new ArrayList<>();
    for(String name : masterFile)
      filesToAddMaster.add(new File(name));

    master.addFilesToRepository(repoName,filesToAddMaster);
    master.commit(repoName,commitMessage);

    assertEquals("1 commit pushed on "+repoName,master.push(repoName));
    peer2.cloneRepository(repoName,new File(testDirPeer2));
    try {
      addFileToPeer(testDirPeer2,repoName,peer2file);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }
    ArrayList<File> filesToAddPeer2 = new ArrayList<>();
    for(String name : peer2file)
      filesToAddPeer2.add(new File(name));

    peer2.addFilesToRepository(repoName,filesToAddPeer2);
    peer2.commit(repoName,commitMessage);

    assertEquals("1 commit pushed on "+repoName,peer2.push(repoName));

    peer3.cloneRepository(repoName,new File(testDirPeer3));
    try {
      addFileToPeer(testDirPeer3,repoName,peer3file);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }
    ArrayList<File> filesToAddPeer3 = new ArrayList<>();
    for(String name : peer3file)
      filesToAddPeer3.add(new File(name));

    peer3.addFilesToRepository(repoName,filesToAddPeer3);
    peer3.commit(repoName,commitMessage);

    assertEquals(repoName+" it's up to date.",peer3.pull(repoName));
    assertEquals("1 commit pushed on "+repoName,peer3.push(repoName));

    ArrayList<File> filesToAddPeer1 = new ArrayList<>();
    for(String name : peer1file)
      filesToAddPeer1.add(new File(name));

    peer1.addFilesToRepository(repoName,filesToAddPeer1);
    peer1.commit(repoName,commitMessage);
    peer1.addFilesToRepository(repoName,filesToAddPeer1);
    peer1.commit(repoName,commitMessage);
    assertEquals(repoName+" is not up to date\nPull new changes.",peer1.push(repoName));
    assertEquals("3 new commit on "+repoName,peer1.pull(repoName));
    assertEquals("2 commit pushed on "+repoName,peer1.push(repoName));

    assertEquals("Nothing to commit.",master.push(repoName));


  }






  static void addFileToPeer(String peer_dir,String repoName, String[] file_paths) throws IOException {

    for (String file_path : file_paths)
      new File(peer_dir +"/"+repoName+ "/" + file_path).createNewFile();

  }

}