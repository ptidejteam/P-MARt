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

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.IOException;

import java.lang.reflect.Field;

import java.util.LinkedList;

// Importing the Java 1.3 / JSR-14 v1.0 Compiler classes
import com.sun.tools.javac.v8.JavaCompiler;
import com.sun.tools.javac.v8.util.Name;
import com.sun.tools.javac.v8.util.Position;
import com.sun.tools.javac.v8.util.Hashtable;
import com.sun.tools.javac.v8.util.List;
import com.sun.tools.javac.v8.util.Log;

import edu.rice.cs.drjava.DrJava;
import gj.util.Vector;
import gj.util.Enumeration;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.UnexpectedException;

/**
 * An implementation of the CompilerInterface that supports compiling with
 * javac, provided that this is a javac that is based on GJ. This is the case
 * for javac in JDK 1.3+, as well as when using the JSR14 prototype compiler.
 * 
 * This class can only be compiled using the JSR-14 v1.0 compiler,
 * since the JSR-14 v1.2 compiler has incompatible classes.
 *
 * @version $Id: JavacGJCompiler.java,v 1.2 2005/04/27 20:28:34 elmoutam Exp $
 */
public class JavacGJCompiler implements CompilerInterface {
  
  private String _extraClassPath = "";
  
  /**
   * Whether to allow 1.4 assertions.
   */
  private boolean _allowAssertions = false;
    
  /** Singleton instance. */
  public static final CompilerInterface ONLY = new JavacGJCompiler();

  public static final String COMPILER_CLASS_NAME =
    "com.sun.tools.javac.v8.JavaCompiler";

  /** A writer that discards its input. */
  private static final Writer NULL_WRITER = new Writer() {
    public void write(char cbuf[], int off, int len) throws IOException {}
    public void flush() throws IOException {}
    public void close() throws IOException {}
  };

  /**
   * A no-op printwriter to pass to the compiler to print error messages.
   */
  private static final PrintWriter NULL_PRINT_WRITER =
    new PrintWriter(NULL_WRITER);

  //private final boolean _supportsGenerics;
  protected JavaCompiler compiler;

  /**
   * We need to explicitly make the compiler's log and pass it
   * to JavaCompiler.make() so we can keep a pointer to the log,
   * since the log is not retrievable from the compiler. We
   * need to use the log to determine if any errors occurred.
   */
  protected OurLogI compilerLog;

  /**
   * Constructor for JavacGJCompiler will throw a RuntimeException if an invalid version
   * of the JDK is in use. 
   */ 
  protected JavacGJCompiler() {
    if (!_isValidVersion()) {
      throw new RuntimeException("Invalid version of Java compiler.");
    } 
  }
  
  /**
   * Uses reflection on the Log object to deduce which JDK is being used.
   * If the constructor for Log in this JDK does not match that of JDK13 or 
   * JDK14/JSR14v1.0, then the version is not yet supported.
   */
  protected boolean _isValidVersion() {
    
    Class log = com.sun.tools.javac.v8.util.Log.class;
    // The JDK13 version of the Log constructor
    Class[] validArgs1 = { boolean.class, boolean.class};
    
    // The JDK14 & JSR14v1.0 version of the Log constructor
    Class[] validArgs2 = { boolean.class, 
      boolean.class, 
      PrintWriter.class, 
      PrintWriter.class,
      PrintWriter.class};
    
    try { 
      log.getConstructor(validArgs1);
      // succeeds, therefore must be JDK13
      return true;
    }
    catch (NoSuchMethodException e) {
      try {
        log.getConstructor(validArgs2);
        // succeeds, therefore must be JDK14/JSR14v1.0
        return true;
      }
      catch (NoSuchMethodException e2) {
        //none of the above, so false
        return false;
      }
    }
  }
      
  
  /*
  private boolean _testIfSupportsGenerics() {
    try {
      // make a temp source file with generics
      File file = FileOps.writeStringToNewTempFile("drjava",
                                                   ".java",
                                                   "class x<T> {}");

      // then try to compile it
      CompilerError[] errors = compile(file.getParentFile(), new File[] { file });

      file.delete();
      File classFile = new File(file.getParentFile(), "x.class");
      classFile.delete();

      return (errors.length == 0);
    }
    catch (IOException ioe) {
      // should never happen, but temp file couldn't be created
      return false;
    }
  }
  */


  /**
   * Compile the given files.
   * @param files Source files to compile.
   * @param sourceRoot Source root directory, the base of the package structure.
   *
   * @return Array of errors that occurred. If no errors, should be zero
   * length array (not null).
   */
  public CompilerError[] compile(File sourceRoot, File[] files) {
    File[] sourceRoots = new File[] { sourceRoot };
    return compile(sourceRoots, files);
  }
  
  /**
   * Compile the given files.
   * @param files Source files to compile.
   * @param sourceRoots Array of source root directories, the base of
   *  the package structure for all files to compile.
   *
   * @return Array of errors that occurred. If no errors, should be zero
   * length array (not null).
   */
  public CompilerError[] compile(File[] sourceRoots, File[] files) {
    // We must re-initialize the compiler on each compile. Otherwise
    // it gets very confused.
    //DrJava.consoleOut().println("-- In JavacGJCompiler: SourceRoots:");
    //for (int i = 0 ; i < sourceRoots.length; i ++) {
    //  DrJava.consoleOut().println(sourceRoots[i]);
    //}
    initCompiler(sourceRoots);
    List<String> filesToCompile = new List<String>();

    for (int i = 0; i < files.length; i++) {
      filesToCompile = filesToCompile.prepend(files[i].getAbsolutePath());
    }

    try {
      compiler.compile(filesToCompile);
    }
    catch (Throwable t) {
      // GJ defines the compile method to throw Throwable?!
      //System.err.println("Compile error: " + t);
      //t.printStackTrace();
      return new CompilerError[] {
        new CompilerError("Compile exception: " + t, false)
      };
    }

    CompilerError[] errors = compilerLog.getErrors();

    // null out things to not keep pointers to dead data
    compiler = null;
    compilerLog = null;
    return errors;
  }

  public boolean isAvailable() {
    try {
      Class.forName(COMPILER_CLASS_NAME);
      try {
        Class.forName("java.lang.Enum");
        // not available since jsr14 v2.0 is on the boot classpath
        return false;
      }
      catch (Throwable t) {
        // Good, java.lang.Enum is not available
        return true;
      }
    }
    catch (Exception e) {
      return false;
    }
  }

  public String getName() {
    return "javac";
    /*
    if (_supportsGenerics) {
      return "javac (GJ)";
    }
    else {
      return "javac";
    }
    */
  }

  public String toString() {
    return getName();
  }

  /**
   * Allows us to set the extra classpath for the compilers without referencing the
   * config object in a loaded class file
   */ 
  public void setExtraClassPath( String extraClassPath) {
      _extraClassPath = extraClassPath;
  }
  
  /**
   * Sets whether to allow assertions in Java 1.4.
   */
  public void setAllowAssertions(boolean allow) {
    _allowAssertions = allow;
  }
  
  /**
   * This method allows us to set the JSR14 collections path across a class loader.
   * (cannot cast a loaded class to a subclass, so all compiler interfaces must have this method)
   */
  public void addToBootClassPath( File cp) {
    throw new UnexpectedException( new Exception("Method only implemented in JSR14Compiler"));
  }
  
  protected Hashtable<String, String> createOptionsHashtable(File[] sourceRoots) {
    Hashtable<String, String> options = Hashtable.make();

    options.put("-warnunchecked", "");

    // Turn on debug -- maybe this should be setable some day?
    options.put("-g", "");

    // turn on generics, if we have em
    options.put("-gj", "");

    // Set output classfile version to 1.1
    options.put("-target", "1.1");

    // Allow assertions in 1.4 if configured and in Java >= 1.4
    String version = System.getProperty("java.version");
    if ((_allowAssertions) && (version != null) &&
        ("1.4.0".compareTo(version) <= 0)) {
      options.put("-source", "1.4");
    }

    String sourceRootString = getSourceRootString(sourceRoots);
    options.put("-sourcepath", sourceRootString /*sourceRoot.getAbsolutePath()*/);

    String cp = System.getProperty("java.class.path");
    // Adds extra.classpath to the classpath.
    cp += _extraClassPath;
    cp += File.pathSeparator + sourceRootString;
    
    options.put("-classpath", cp);

    return options;
  }
  
  /**
   * Utility method for getting a properly formatted
   * string with several source paths from an array of files.
   */
  protected String getSourceRootString(File[] sourceRoots) {
    StringBuffer roots = new StringBuffer();
    for (int i = 0; i < sourceRoots.length; i++) {
      roots.append(sourceRoots[i].getAbsolutePath());
      if (i < sourceRoots.length - 1) {
        roots.append(File.pathSeparator);
      }
    }
    return roots.toString();
  }

  protected void initCompiler(File[] sourceRoots) {
    Hashtable<String, String> options = createOptionsHashtable(sourceRoots);

    // sigh, the 1.4 log won't work on 1.3 so we need to try the 1.4 first
    // and fall back to the 1.3
    try {
      compilerLog = new OurLog14();
    }
    catch (NoSuchMethodError error) {
      compilerLog = new OurLog13();
    }

    //System.err.println(this + ": log=" + compilerLog);
    compiler = JavaCompiler.make((Log) compilerLog, options);
  }

  /**
   * Replaces the standard compiler "log" so we can track the error
   * messages ourselves. This version will work for JDK 1.3.
   * (The other version only works on 1.4 and later since they
   * changed the constructors!)
   */
  private class OurLog13 extends Log implements OurLogI {
    // List of CompilerError
    private LinkedList _errors = new LinkedList();
    private String _sourceName = "";

    public OurLog13() {
      super(false, true);
    }

    /**
     * JSR14 uses this crazy signature on warning method because it localizes
     * the warning message.
     */
    public void warning(int pos, String key, String arg0, String arg1,
                        String arg2, String arg3)
    {
      super.warning(pos, key, arg0, arg1, arg2, arg3);

      String msg = getText("compiler.warn." + key,
        arg0, arg1, arg2, arg3, null, null, null);

      _errors.addLast(new CompilerError(new File(currentSource().toString()),
                                        Position.line(pos) - 1, // gj is 1 based
                                        Position.column(pos) - 1,
                                        msg,
                                        true));
    }

    /**
     * JSR14 uses this crazy signature on error method because it localizes
     * the error message.
     */
    public void error(int pos, String key, String arg0, String arg1,
                      String arg2, String arg3, String arg4, String arg5,
                      String arg6)
    {
      super.error(pos, key, arg0, arg1, arg2, arg3, arg4, arg5, arg6);

      String msg = getText("compiler.err." + key,
                           arg0, arg1, arg2, arg3,
                           arg4, arg5, arg6);

      _errors.addLast(new CompilerError(new File(currentSource().toString()),
                                        Position.line(pos) - 1, // gj is 1 based
                                        Position.column(pos) - 1,
                                        msg,
                                        false));
    }

    /**
     * Overridden to supress output.
     * This works on javac 1.3, but not later, since they switched from
     * using these methods to using the three PrintWriters.
     */
    public void print(String s) {}

    /**
     * Overridden to supress output.
     * This works on javac 1.3, but not later, since they switched from
     * using these methods to using the three PrintWriters.
     */
    public void println(String s) {}

    public CompilerError[] getErrors() {
      return (CompilerError[]) _errors.toArray(new CompilerError[0]);
    }
  }

  /**
   * Replaces the standard compiler "log" so we can track the error
   * messages ourselves. This version will work for JDK 1.4 and later.
   * It won't work on 1.3 since the 5-arg constructor didn't exist!
   */
  private class OurLog14 extends Log implements OurLogI {
    // List of CompilerError
    private LinkedList _errors = new LinkedList();
    private String _sourceName = "";

    public OurLog14() {
      super(false, true, NULL_PRINT_WRITER, NULL_PRINT_WRITER, NULL_PRINT_WRITER);
    }

    /**
     * JSR14 uses this crazy signature on warning method because it localizes
     * the warning message.
     */
    public void warning(int pos, String key, String arg0, String arg1,
                        String arg2, String arg3)
    {
      super.warning(pos, key, arg0, arg1, arg2, arg3);

      String msg = getText("compiler.warn." + key,
        arg0, arg1, arg2, arg3, null, null, null);

      _errors.addLast(new CompilerError(new File(currentSource().toString()),
                                        Position.line(pos) - 1, // gj is 1 based
                                        Position.column(pos) - 1,
                                        msg,
                                        true));
    }

    /**
     * JSR14 uses this crazy signature on error method because it localizes
     * the error message.
     */
    public void error(int pos, String key, String arg0, String arg1,
                      String arg2, String arg3, String arg4, String arg5,
                      String arg6)
    {
      super.error(pos, key, arg0, arg1, arg2, arg3, arg4, arg5, arg6);

      String msg = getText("compiler.err." + key,
                           arg0, arg1, arg2, arg3,
                           arg4, arg5, arg6);

      _errors.addLast(new CompilerError(new File(currentSource().toString()),
                                        Position.line(pos) - 1, // gj is 1 based
                                        Position.column(pos) - 1,
                                        msg,
                                        false));
    }

    /**
     * Overridden to supress output.
     * This works on javac 1.3, but not later, since they switched from
     * using these methods to using the three PrintWriters.
     */
    public void print(String s) {}

    /**
     * Overridden to supress output.
     * This works on javac 1.3, but not later, since they switched from
     * using these methods to using the three PrintWriters.
     */
    public void println(String s) {}

    public CompilerError[] getErrors() {
      return (CompilerError[]) _errors.toArray(new CompilerError[0]);
    }
  }

  private interface OurLogI {
    public CompilerError[] getErrors();
  }
}
