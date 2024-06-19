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

package edu.rice.cs.util.newjvm;

import java.io.*;
import java.util.*;

/**
 * A utility class to allow executing another JVM.
 *
 * @version $Id: ExecJVM.java,v 1.1 2005/08/05 12:45:08 guehene Exp $
 */
public final class ExecJVM {
  private static final String PATH_SEPARATOR = System.getProperty("path.separator");
  private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.US);

  private ExecJVM() {}

  /**
   * Runs a new JVM.
   *
   * @param mainClass Class to run
   * @param classParams Parameters to pass to the main class
   * @param classPath Array of items to put in classpath of new JVM
   * @param jvmParams Array of additional command-line parameters to pass to JVM
   *
   * @return {@link Process} object corresponding to the executed JVM
   */
  public static Process runJVM(String mainClass,
                               String[] classParams,
                               String[] classPath,
                               String[] jvmParams) throws IOException {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < classPath.length; i++) {
      if (i != 0) {
        buf.append(PATH_SEPARATOR);
      }

      buf.append(classPath[i]);
    }

    return runJVM(mainClass, classParams, buf.toString(), jvmParams);
  }

  /**
   * Runs a new JVM.
   *
   * @param mainClass Class to run
   * @param classParams Parameters to pass to the main class
   * @param classPath Pre-formatted classpath parameter
   * @param jvmParams Array of additional command-line parameters to pass to JVM
   *
   * @return {@link Process} object corresponding to the executed JVM
   */
  public static Process runJVM(String mainClass,
                               String[] classParams,
                               String classPath,
                               String[] jvmParams) throws IOException {
    LinkedList<String> args = new LinkedList<String>();
    args.add("-classpath");
    args.add(classPath);
    _addArray(args, jvmParams);
    String[] jvmWithCP = args.toArray(new String[0]);

    return runJVM(mainClass, classParams, jvmWithCP);
  }

  /**
   * Runs a new JVM, propogating the present classpath.
   *
   * @param mainClass Class to run
   * @param classParams Parameters to pass to the main class
   * @param jvmParams Array of additional command-line parameters to pass to JVM
   *
   * @return {@link Process} object corresponding to the executed JVM
   */
  public static Process runJVMPropogateClassPath(String mainClass,
                                                 String[] classParams,
                                                 String[] jvmParams)
    throws IOException {
    String cp = System.getProperty("java.class.path");
    return runJVM(mainClass, classParams, cp, jvmParams);
  }

  /**
   * Runs a new JVM, propogating the present classpath.
   *
   * @param mainClass Class to run
   * @param classParams Parameters to pass to the main class
   *
   * @return {@link Process} object corresponding to the executed JVM
   */
  public static Process runJVMPropogateClassPath(String mainClass,
                                                 String[] classParams)
    throws IOException {
    return runJVMPropogateClassPath(mainClass, classParams, new String[0]);
  }

  /**
   * Runs a new JVM.
   *
   * @param mainClass Class to run
   * @param classParams Parameters to pass to the main class
   * @param jvmParams Array of additional command-line parameters to pass to JVM
   *
   * @return {@link Process} object corresponding to the executed JVM
   */
  public static Process runJVM(String mainClass,
                               String[] classParams,
                               String[] jvmParams) throws IOException {
    LinkedList<String> args = new LinkedList<String>();
    args.add(_getExecutable());
    _addArray(args, jvmParams);
    args.add(mainClass);
    _addArray(args, classParams);

    String[] argArray = args.toArray(new String[0]);

    //for (int i = 0; i < argArray.length; i++) {
    //System.err.println("arg #" + i + ": " + argArray[i]);
    //}

    return Runtime.getRuntime().exec(argArray);
  }

  /**
   * Empties BufferedReaders by copying lines into LinkedLists.
   * This is intended for use with the output streams from an ExecJVM process.
   * Source and destination objects are specified for stdout and for stderr.
   * @param theProc a Process object whose output will be handled
   * @param outLines the LinkedList of Strings to be filled with the lines read from outBuf
   * @param errLines the LinkedList of Strings to be filled with the lines read from errBuf
   */
  public static void ventBuffers(Process theProc, LinkedList<String> outLines,
                                 LinkedList<String> errLines) throws IOException {
    // getInputStream actually gives us the stdout from the Process.
    BufferedReader outBuf = new BufferedReader(new InputStreamReader(theProc.getInputStream()));
    BufferedReader errBuf = new BufferedReader(new InputStreamReader(theProc.getErrorStream()));
    String output;

    if (outBuf.ready()) {
      output = outBuf.readLine();

      while (output != null) {
        //        System.out.println("[stdout]: " + output);
        outLines.add(output);
        if (outBuf.ready()) {
          output = outBuf.readLine();
        }
        else {
          output = null;
        }
      }
    }

    if (errBuf.ready()) {
      output = errBuf.readLine();
      while (output != null) {
        //        System.out.println("[stderr] " + output);
        errLines.add(output);
        if (errBuf.ready()) {
          output = errBuf.readLine();
        }
        else {
          output = null;
        }
      }
    }
  }

  /**
   * Prints the stdout and stderr of the given process, line by line.  Adds a
   * message and tag to identify the source of the output.  Note that this code
   * will print all available stdout before all stderr, since it is impossible
   * to determine in which order lines were added to the respective buffers.
   * @param theProc a Process object whose output will be handled
   * @param msg an initial message to print before output
   * @param sourceName a short string to identify the process
   * @throws IOException if there is a problem with the streams
   */
  public static void printOutput(Process theProc, String msg, String sourceName)
    throws IOException {
    // First, write out our opening message.
    System.err.println(msg);

    LinkedList<String> outLines = new LinkedList<String>();
    LinkedList<String> errLines = new LinkedList<String>();

    ventBuffers(theProc, outLines, errLines);

    Iterator<String> it = outLines.iterator();
    String output;
    while (it.hasNext()) {
      output = it.next();
      System.err.println("    [" +sourceName + " stdout]: " + output);
    }

    it = errLines.iterator();
    while (it.hasNext()) {
      output = it.next();
      System.err.println("    [" +sourceName + " stderr]: " + output);
    }
  }

  private static void _addArray(LinkedList<String> list, String[] array) {
    if (array != null) {
      for (int i = 0; i < array.length; i++) {
        list.add(array[i]);
      }
    }
  }

  /** DOS/Windows family OS's use ; to separate paths. */
  private static boolean _isDOS() {
    return PATH_SEPARATOR.equals(";");
  }

  private static boolean _isNetware() {
    return OS_NAME.indexOf("netware") != -1;
  }

  /**
   * Find the java executable.
   * This logic comes from Ant.
   */
  private static String _getExecutable() {
    // this netware thing is based on comments from ant's code
    if (_isNetware()) {
      return "java";
    }

    File executable;

    String java_home = System.getProperty("java.home") + "/";

    String[] candidates = {
      java_home + "../bin/java",
        java_home + "bin/java",
        java_home + "java",
    };

    // search all the candidates to find java
    for (int i = 0; i < candidates.length; i++) {
      String current = candidates[i];

      // try javaw.exe first for dos, otherwise try java.exe for dos
      if (_isDOS()) {
        executable = new File(current + "w.exe");
        if (! executable.exists()) {
          executable = new File(current + ".exe");
        }
      }
      else {
        executable = new File(current);
      }

      //System.err.println("checking: " + executable);

      if (executable.exists()) {
        //System.err.println("JVM executable found: " + executable.getAbsolutePath());
        return executable.getAbsolutePath();
      }
    }

    // hope for the best using the system's path!
    //System.err.println("Could not find java executable, using 'java'!");
    return "java";
  }
}

