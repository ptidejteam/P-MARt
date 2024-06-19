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

import java.io.*;
import java.util.*;

/**
 * A class to provide some convenient file operations as static methods.
 * It's abstract to prevent (useless) instantiation, though it can be subclassed
 * to provide convenient namespace importation of its methods.
 *
 * @version $Id: FileOps.java,v 1.1 2005/08/05 12:45:09 guehene Exp $
 */
public abstract class FileOps {

  /**
   * This filter checks for files that end in .java
   */
  public static FileFilter JAVA_FILE_FILTER = new FileFilter(){
    public boolean accept(File f){
      //Do this runaround for filesystems that are case preserving
      //but case insensitive
      //Remove the last 5 letters from the file name, append ".java"
      //to the end, create a new file and see if its equivalent to the
      //original
      StringBuffer name = new StringBuffer(f.getAbsolutePath());
      String shortName = f.getName();
      if (shortName.length() < 6){
        return false;
      }
      name.delete(name.length() - 5, name.length());
      name.append(".java");
      File test = new File(new String(name));
      if (test.equals(f)){
        return true;
      } else {
        return false;
      }
    }
  };
  
  /**
   * Reads the stream until it reaches EOF, and then returns the read
   * contents as a byte array. This call may block, since it will not
   * return until EOF has been reached.
   *
   * @param stream Input stream to read.
   * @return Byte array consisting of all data read from stream.
   */
  public static byte[] readStreamAsBytes(final InputStream stream)
    throws IOException
  {
    BufferedInputStream buffered;

    if (stream instanceof BufferedInputStream) {
      buffered = (BufferedInputStream) stream;
    }
    else {
      buffered = new BufferedInputStream(stream);
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    int readVal = buffered.read();
    while (readVal != -1) {
      out.write(readVal);
      readVal = buffered.read();
    }

    stream.close();
    return out.toByteArray();
  }

  /**
   * Read the entire contents of a file and return them.
   */
  public static String readFileAsString(final File file) throws IOException {
    FileReader reader = new FileReader(file);
    StringBuffer buf = new StringBuffer();

    while (reader.ready()) {
      char c = (char) reader.read();
      buf.append(c);
    }

    reader.close();
    return buf.toString();
  }
  
  /**
   * Copies the text of one file into another.
   * @param source the file to be copied
   * @param dest the file to be copied to
   */
  public static void copyFile(File source, File dest) throws IOException {
    String text = readFileAsString(source);
    writeStringToFile(dest, text);
  }

  /**
   * Creates a new temporary file and writes the given text to it.
   * The file will be deleted on exit.
   *
   * @param prefix Beginning part of file name, before unique number
   * @param suffix Ending part of file name, after unique number
   * @param text Text to write to file
   *
   * @return name of the temporary file that was created
   */
  public static File writeStringToNewTempFile(final String prefix,
                                              final String suffix,
                                              final String text)
    throws IOException
  {
    File file = File.createTempFile(prefix, suffix);
    file.deleteOnExit();
    writeStringToFile(file, text);
    return file;
  }

  /**
   * Writes text to the file.
   *
   * @param file File to write to
   * @param text Text to write
   */
  public static void writeStringToFile(File file, String text)
    throws IOException
  {
    FileWriter writer = new FileWriter(file);
    writer.write(text);
    writer.close();
  }

  /**
   * Create a new temporary directory.
   * The directory will be deleted on exit, if empty.
   * (To delete it recursively on exit, use deleteDirectoryOnExit.)
   *
   * @param name Non-unique portion of the name of the directory to create.
   * @return File representing the directory that was created.
   */
  public static File createTempDirectory(final String name) throws IOException {
    return createTempDirectory(name, null);
  }

  /**
   * Create a new temporary directory.
   * The directory will be deleted on exit, if empty.
   * (To delete it recursively on exit, use deleteDirectoryOnExit.)
   *
   * @param name Non-unique portion of the name of the directory to create.
   * @param parent Parent directory to contain the new directory
   * @return File representing the directory that was created.
   */
  public static File createTempDirectory(final String name, final File parent)
    throws IOException
  {
    File file =  File.createTempFile(name, "", parent);
    file.delete();
    file.mkdir();
    file.deleteOnExit();

    return file;
  }

  /**
   * Delete the given directory including any files and directories it contains.
   *
   * @param dir File object representing directory to delete. If, for some
   *            reason, this file object is not a directory, it will still be
   *            deleted.
   *
   * @return true if there were no problems in deleting. If it returns
   *         false, something failed and the directory contents likely at least
   *         partially still exist.
   */
  public static boolean deleteDirectory(final File dir) {
    if (! dir.isDirectory()) {
      return dir.delete();
    }

    boolean ret = true;
    File[] childFiles = dir.listFiles();
    for (int i = 0; i < childFiles.length; i++) {
      ret = ret && deleteDirectory(childFiles[i]);
    }
    
    // Now we should have an empty directory
    ret = ret && dir.delete();
    return ret;
  }
  
  /**
   * Instructs Java to recursively delete the given directory and its contents
   * when the JVM exits.
   *
   * @param dir File object representing directory to delete. If, for some
   *            reason, this file object is not a directory, it will still be
   *            deleted.
   */
  public static void deleteDirectoryOnExit(final File dir) {
    // Delete this on exit, whether it's a directory or file
    dir.deleteOnExit();

    // If it's a directory, visit its children.
    //  For some reason, this has to be done after calling deleteOnExit
    //  on the directory itself.
    if (dir.isDirectory()) {
      File[] childFiles = dir.listFiles();
      for (int i = 0; i < childFiles.length; i++) {
        deleteDirectoryOnExit(childFiles[i]);
      }
    }
  }

  /**
   * This function starts from the given directory and finds all
   * packages within that directory
   * @param prefix the package name of files in the given root
   * @param root the directory to start exploring from
   * @return a list of valid packages, excluding the root ("") package
   */
  public static LinkedList<String> packageExplore(String prefix, File root) {
    /* Inner holder class. */
    class PrefixAndFile {
      public String prefix;
      public File root;
      public PrefixAndFile(String prefix, File root) {
        this.root = root;
        this.prefix = prefix;
      }
    }
    
    //This set makes sure we don't get caught in a loop if the filesystem has symbolic links
    //that form a circle by tracking the directories we have already explored
    final Set<File> exploredDirectories = new HashSet<File>();

    LinkedList<String> output = new LinkedList<String>();
    Stack<PrefixAndFile> working = new Stack<PrefixAndFile>();
    working.push(new PrefixAndFile(prefix, root));
    exploredDirectories.add(root);

    //This filter allows only directories, and accepts each directory
    //only once
    FileFilter directoryFilter = new FileFilter(){
      public boolean accept(File f){
        boolean toReturn = f.isDirectory() && !exploredDirectories.contains(f);
        exploredDirectories.add(f);
        return toReturn;
      }
    };

    //Explore each directory, adding (unique) subdirectories to the
    //working list.  If a directory has .java files, add the associated
    //package to the list of packages
    while (!working.empty()){
      PrefixAndFile current = working.pop();
      File [] subDirectories = current.root.listFiles(directoryFilter);
      for(int a = 0; a < subDirectories.length; a++){
        File dir = subDirectories[a];
        PrefixAndFile paf;
//         System.out.println("exploring " + dir);
        if (current.prefix == ""){
          paf = new PrefixAndFile(dir.getName(), dir);
        } else {
          paf = new PrefixAndFile(current.prefix + "." + dir.getName(), dir);
        }
        working.push(paf);
      }
      File [] javaFiles = current.root.listFiles(JAVA_FILE_FILTER);

      //Only add package names if they have java files and are not the root package
      if (javaFiles.length != 0 && current.prefix != ""){
        output.add(current.prefix);
//         System.out.println("adding " + current.prefix);
      }
    }

    return output;
  }

  /**
   * Renames the given file to the given destination.  Needed since Windows will
   * not allow a rename to overwrite an existing file.
   * @param file the file to rename
   * @param dest the destination file
   * @return true iff the rename was successful
   */
  public static boolean renameFile(File file, File dest) {
    if (dest.exists()) {
      dest.delete();
    }
    return file.renameTo(dest);
  }

  /**
   * This method writes files correctly; it takes care of catching errors and
   * making backups and keeping an unsuccessful file save from destroying the old
   * file (unless a backup is made).  It makes sure that the file to be saved is 
   * not read-only, throwing an IOException if it is.  Note: if saving fails and a 
   * backup was being created, any existing backup will be destroyed (this is 
   * because the backup is written before saving begins, and then moved back over 
   * the original file when saving fails).  As the old backup would have been destroyed 
   * anyways if saving had succeeded, I do not think that this is incorrect or 
   * unreasonable behavior.
   * @param fileSaver keeps track of the name of the file to write, whether to back
   * up the file, and has a method that actually performs the writing of the file
   * @throws IOException if the saving or backing up of the file fails for any reason
   */
  public static void saveFile(FileSaver fileSaver) throws IOException{
    boolean makeBackup = fileSaver.shouldBackup();
    boolean success = false;
    File file = fileSaver.getTargetFile();
    File backup = null;
    boolean tempFileUsed = true;
    // file.canWrite() is false if file.exists() is false
    // but we want to be able to save a file that doesn't
    // yet exist.
    if (file.exists() && !file.canWrite()) {
      throw new IOException("Permission denied");
    }
    /* First back up the file, if necessary */
    if (makeBackup){
      backup = fileSaver.getBackupFile();
      if (!renameFile(file, backup)){
        throw new IOException("Save failed: Could not create backup file "
                                + backup.getAbsolutePath() +
                              "\nIt may be possible to save by disabling file backups\n");
      }
      fileSaver.backupDone();
    }
    
    //Create a temp file in the same directory as the file to be saved.
    //From this point forward, enclose in try...finally so that we can clean
    //up the temp file and restore the file from its backup.
    File parent = file.getParentFile();
    File tempFile = File.createTempFile("drjava", ".temp", parent);
    try {
      /* Now, write your output to the temp file, then rename it to the correct
       name.  This way, if writing fails in the middle, the old file is not
       lost. */
      FileOutputStream fos;
      try {
        /* The next line will fail if we can't create the temp file.  This may mean that
         * the user does not have write permission on the directory the file they
         * are editing is in.  We may want to go ahead and try writing directly
         * to the target file in this case
         */
        fos = new FileOutputStream(tempFile);
      } catch (FileNotFoundException fnfe) {
        if (fileSaver.continueWhenTempFileCreationFails()){
          fos = new FileOutputStream(file);
          tempFileUsed = false;
        } else {
          throw new IOException("Could not create temp file " + tempFile +
                                " in attempt to save " + file);
        }
      }
      BufferedOutputStream bos = new BufferedOutputStream(fos);
      fileSaver.saveTo(bos);
      bos.close();

      if (tempFileUsed && !renameFile(tempFile, file)) {
        throw new IOException("Save failed: Another process may be using " + file + ".");
      }

      success = true;
    } 
    finally {
      if (tempFileUsed) {
        /* Delete the temp file */
        tempFile.delete();
      }
      if (makeBackup) {
        /* On failure, attempt to move the backup back to its original location if we
         made one.  On success, register that a backup was successfully made */
        if (success) {
          fileSaver.backupDone();
        } else {
          renameFile(backup, file);
        }
      }
    }
  }

  public interface FileSaver {
    
    /**
     * This method tells what to name the backup of the file, if a backup is to be made.
     * It may depend on getTargetFile(), so it can thrown an IOException
     */
    public abstract File getBackupFile() throws IOException;
    
    /**
     * This method indicates whether or not a backup of the file should be made.  It
     * may depend on getTargetFile(), so it can throw an IOException
     */
    public abstract boolean shouldBackup() throws IOException;

    /**
     * This method specifies if the saving process should continue trying to save
     * if it can not create the temp file that is written initially.  If you do
     * continue saving in this case, the original file may be lost if saving fails.
     */
    public abstract boolean continueWhenTempFileCreationFails();
    
    /**
     * This method is called to tell the file saver that a backup was successfully made
     */
    public abstract void backupDone();

    /**
     * This method actually writes info to a file.  NOTE: It is important that this
     * method write to the stream it is passed, not the target file.  If you write
     * directly to the target file, the target file will be destroyed if saving fails.
     * Also, it is important that when saving fails this method throw an IOException
     * @throws IOException when saving fails for any reason
     */
    public abstract void saveTo(OutputStream os) throws IOException;

    /**
     * This method tells what the file is that we want to save to.  It should
     * use the canonical name of the file (this means resolving symlinks).  Otherwise,
     * the saver would not deal correctly with symlinks.  Resolving symlinks may cause
     * an IOException, so this method throws an IOException
     */
    public abstract File getTargetFile() throws IOException;
  }

  /**
   * This class is a default implementation of FileSaver that makes only 1 backup
   * of each file per instantiation of the program (following Emacs' lead).  It 
   * backs up to files named <file>~.  It does not implement the saveTo method.
   */
  public abstract static class DefaultFileSaver implements FileSaver{

    private File outputFile = null;
    private static Set<File> filesNotNeedingBackup = new HashSet<File>();
    private static boolean backupsEnabled = true;

    /**
     * This keeps track of whether or not outputFile has been resolved to its canonical
     * name
     */
    private boolean isCanonical = false;
    
    /**
     * Globally enables backups for any DefaultFileSaver that does not override
     * the shouldBackup method
     */
    public static void setBackupsEnabled(boolean enabled){
      backupsEnabled = enabled;
    }
    
    public DefaultFileSaver(File file){
      outputFile = file.getAbsoluteFile();
    }
    
    public boolean continueWhenTempFileCreationFails(){
      return true;
    }
    
    public File getBackupFile() throws IOException{
      return new File(getTargetFile().getPath() + "~");
    }

    public boolean shouldBackup() throws IOException{
      if (!backupsEnabled){
        return false;
      }
      if (!getTargetFile().exists()){
        return false;
      }
      if(filesNotNeedingBackup.contains(getTargetFile())){
        return false;
      }
      return true;
    }
    
    public void backupDone() {
      try {
        filesNotNeedingBackup.add(getTargetFile());
      } catch (IOException ioe) {
        throw new UnexpectedException(ioe, "getTargetFile should fail earlier");
      }
    }

    public File getTargetFile() throws IOException{
      if (!isCanonical){
        outputFile = outputFile.getCanonicalFile();
        isCanonical = true;
      }
      return outputFile;
    }
  }
}
