/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util;

import junit.framework.*;
import java.io.*;

import java.util.LinkedList;

import edu.rice.cs.util.newjvm.ExecJVM;

/**
 * Test cases for {@link FileOps}.
 *
 * @version $Id: FileOpsTest.java,v 1.1 2005/08/05 12:45:09 guehene Exp $
 */
public class FileOpsTest extends TestCase {
  public static final String TEXT = "hi, dude.";
  public static final String PREFIX = "prefix";
  public static final String SUFFIX = ".suffix";

  /**
   * Constructor.
   * @param  String name
   */
  public FileOpsTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return new TestSuite(FileOpsTest.class);
  }

  public void testCreateTempDirectory() throws IOException {
    File dir = FileOps.createTempDirectory(PREFIX);
    try {
      assertTrue("createTempDirectory result is a directory", dir.isDirectory());
      assertTrue("temp directory has correct prefix",
                 dir.getName().startsWith(PREFIX));
    }
    finally {
      assertTrue("delete directory", dir.delete());
    }
  }

  public void testReadAndWriteTempFile() throws IOException {
    File file = FileOps.writeStringToNewTempFile(PREFIX, SUFFIX, TEXT);
    try {
      assertTrue("temp file has correct prefix",
                 file.getName().startsWith(PREFIX));
      assertTrue("temp file has correct suffix",
                 file.getName().endsWith(SUFFIX));

      String read = FileOps.readFileAsString(file);
      assertEquals("contents after read", TEXT, read);
    }
    finally {
      assertTrue("delete file", file.delete());
    }
  }

  public void testRecursiveDirectoryDelete() throws IOException {
    final File baseDir = FileOps.createTempDirectory(PREFIX);

    File parentDir = baseDir;
    boolean ret;

    // create a bunch of subdirs and some files.
    for (int i = 0; i < 5; i++) {
      File subdir = new File(parentDir, "subdir" + i);
      ret = subdir.mkdir();
      assertTrue("create directory " + subdir, ret);

      for (int j = 0; j < 2; j++) {
        File file = new File(parentDir, "file" + i + "-" + j);
        FileOps.writeStringToFile(file,
                                  "Some text for file "+file.getAbsolutePath());
        assertTrue(file + " exists", file.exists());
      }

      parentDir = subdir;
    }

    // OK, now try to delete base.
    ret = FileOps.deleteDirectory(baseDir);
    assertTrue("delete directory result", ret);
    assertEquals("directory exists after deleting it", false, baseDir.exists());
  }

  
  /**
   * This method checks that backups are made correctly, that when a save fails,
   * no data is lost, and that when a save is attempted on a write-protected file,
   * the save fails (bug #782963).
   */
  public void testSaveFile() throws IOException {
    File writeTo = File.createTempFile("fileops", ".test");
    writeTo.deleteOnExit();
    File backup = new File(writeTo.getPath() + "~");
    
    FileOps.saveFile(new FileOps.DefaultFileSaver(writeTo) {
      public void saveTo(OutputStream os) throws IOException {
        String output = "version 1";
        os.write(output.getBytes());
      }
      public boolean shouldBackup() {
        return false;
      }
    });
    assertEquals("save w/o backup", "version 1", FileOps.readFileAsString(writeTo));
    assertEquals("save w/o backup did not backup", false, backup.exists());
    
    FileOps.saveFile(new FileOps.DefaultFileSaver(writeTo) {
      public void saveTo(OutputStream os) throws IOException {
        String output = "version 2";
        os.write(output.getBytes());
      }
    });
    assertEquals("save2 w backup", "version 2", FileOps.readFileAsString(writeTo));
    assertEquals("save2 w backup did backup", "version 1",
                 FileOps.readFileAsString(backup));
    
    FileOps.saveFile(new FileOps.DefaultFileSaver(writeTo) {
      public void saveTo(OutputStream os) throws IOException {
        String output =  "version 3";
        os.write(output.getBytes());
      }
    });
    assertEquals("save3 w backup on", "version 3", FileOps.readFileAsString(writeTo));
    assertEquals("save3 w backup on did not backup", "version 1",
                 FileOps.readFileAsString(backup));
    
    
    /* Now see what happens when saving fails and we were not making a backup
     * Nothing should change. */
    try {
      FileOps.saveFile(new FileOps.DefaultFileSaver(writeTo) {
        public void saveTo(OutputStream os) throws IOException {
          String output = "version 4";
          os.write(output.getBytes());
          throw new IOException();
        }
      });
      fail("IOException not propagated");
    } 
    catch (IOException ioe){}//do nothing, this is expected
    assertEquals("failed save4 w/o backup", "version 3",
                 FileOps.readFileAsString(writeTo));
    assertEquals("failed save4 w/o backup check original backup", "version 1",
                 FileOps.readFileAsString(backup));
    
    /* Now see what happens when saving fails and we were making a backup */
    try {
      FileOps.saveFile(new FileOps.DefaultFileSaver(writeTo) {
        public boolean shouldBackup () {
          return true;
        }
        public void saveTo(OutputStream os) throws IOException {
          String output =  "version 5";
          os.write(output.getBytes());
          throw new IOException();
        }
      });
      fail("IOException not propagated spot 2");
    }
    catch(IOException ioe){} //do nothing, we expected this
    assertEquals("failed save5 w backup", "version 3",
                 FileOps.readFileAsString(writeTo));
    
    // Make sure that the backup file no longer exists since it was 
    // copied over the original
    try {
      FileOps.readFileAsString(backup);
      fail("The backup file should no longer exist.");
    }
    catch(FileNotFoundException e) {} //do nothing, we expected this
    
    // Test that save fails if the file is write-protected.
    writeTo.setReadOnly();
    try {
      FileOps.saveFile(new FileOps.DefaultFileSaver(writeTo) {
        public boolean shouldBackup () {
          return true;
        }
        public void saveTo(OutputStream os) throws IOException {
          String output =  "version 6";
          os.write(output.getBytes());
        }
      });
      fail("The file to be saved was read-only!");
    }
    catch(IOException ioe){} //do nothing, we expected this
    assertEquals("failed save6 w backup", "version 3",
                 FileOps.readFileAsString(writeTo));
    
    // Make sure that the backup file still doesn't exist since the file
    // was read-only.
    try {
      FileOps.readFileAsString(backup);
      fail("The backup file should no longer exist.");
    }
    catch(FileNotFoundException e) {} //do nothing, we expected this
  }

  /**
   * This tests that packageExplore correctly runs through and returns
   * non-empty packages
   */
  public void testPackageExplore() throws IOException {
    File rootDir = FileOps.createTempDirectory("fileOpsTest");
    File subDir0 = new File(rootDir, "sub0");
    subDir0.mkdir();
    File subDir1 = new File(rootDir, "sub1");
    subDir1.mkdir();
    File subsubDir0 = new File(subDir0, "subsub0");
    subsubDir0.mkdir();
    File javasubsub = new File(subsubDir0, "aclass.java");
    FileOps.writeStringToFile(javasubsub, "contents of this file are unimportant");
    File javasub1 = new File(subDir1, "myclass.java");
    FileOps.writeStringToFile(javasub1, "this file is pretty much empty");
    File javaroot = new File(rootDir, "someclass.java");
    FileOps.writeStringToFile(javaroot, "i can write anything i want here");

    LinkedList packages = FileOps.packageExplore("hello", rootDir);
    assertEquals("package count a", 3, packages.size());
    assertTrue("packages contents a0", packages.contains("hello.sub0.subsub0"));
    assertTrue("packages contents a1", packages.contains("hello.sub1"));
    assertTrue("packages contents a2", packages.contains("hello"));

    //Now add a .java file to the root directory and check that the default directory
    //is not added
    packages = FileOps.packageExplore("", rootDir);
    assertEquals("package count b", 2, packages.size());
    assertTrue("packages contents b0", packages.contains("sub0.subsub0"));
    assertTrue("packages contents b1", packages.contains("sub1"));


    assertTrue("deleting temp directory", FileOps.deleteDirectory(rootDir));
  }
  
  
  /**
   * Tests that non-empty directories can be deleted on exit.
   */
  public void testDeleteDirectoryOnExit() 
    throws IOException, InterruptedException
  {
    // Create files:
    //  /tmp/DrJavaTestTempDir#/
    //   DrJavaTest-#.tmp
    //   TempDir#/
    //     DrJavaTest-#.tmp
    File dir1 = FileOps.createTempDirectory("DrJavaTestTempDir");
    assertTrue("dir1 exists", dir1.exists());
    File file1 = File.createTempFile("DrJavaTest-", ".temp", dir1);
    assertTrue("file1 exists", file1.exists());
    File dir2 = FileOps.createTempDirectory("TempDir", dir1);
    assertTrue("dir2 exists", dir2.exists());
    File file2 = File.createTempFile("DrJavaTest-", ".temp", dir2);
    assertTrue("file2 exists", file2.exists());
    
    String className = "edu.rice.cs.util.FileOpsTest";
    String[] args = new String[] { dir1.getAbsolutePath() };
    
    Process process = ExecJVM.
      runJVMPropogateClassPath(className, args);
    int status = process.waitFor();
    assertEquals("Delete on exit test exited with an error!", 0, status);
    
    assertTrue("dir1 should be deleted", !dir1.exists());
    assertTrue("file1 should be deleted", !file1.exists());
    assertTrue("dir2 should be deleted", !dir2.exists());
    assertTrue("file2 should be deleted", !file2.exists());
  }
  
  /**
   * Main method to be called by testDeleteDirectoryOnExit.  Runs in
   * a new JVM so the files can be deleted.
   * Exits with status 1 if wrong number of arguments.
   * Exits with status 2 if file doesn't exist
   * 
   * @params args should contain the file name of the directory
   * to delete on exit
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.exit(1);
    }
    
    File dir = new File(args[0]);
    if (!dir.exists()) {
      System.exit(2);
    }
    FileOps.deleteDirectoryOnExit(dir);
    
    // Ok, exit cleanly
    System.exit(0);
  }


}
