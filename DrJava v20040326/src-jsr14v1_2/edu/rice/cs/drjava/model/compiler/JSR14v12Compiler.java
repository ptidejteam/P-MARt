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

// Importing the Java 1.4.1 / JSR-14 v1.2 Compiler classes
import com.sun.tools.javac.v8.JavaCompiler;
import com.sun.tools.javac.v8.util.Name;
import com.sun.tools.javac.v8.util.Position;
import com.sun.tools.javac.v8.util.Hashtable;
import com.sun.tools.javac.v8.util.Context;
import com.sun.tools.javac.v8.util.Options;
import com.sun.tools.javac.v8.util.List;
import com.sun.tools.javac.v8.util.Log;
import com.sun.tools.javac.v8.code.ClassReader;

import edu.rice.cs.util.FileOps;

/**
 * An implementation of the CompilerInterface that supports compiling with
 * the JSR-14 prototype compiler.
 * It adds the collections classes signature to the bootclasspath
 * as requested by {@link Configuration}.
 * 
 * This class can only be compiled using the JSR-14 v1.2 compiler,
 * since the JSR-14 v1.0 compiler has incompatible classes.
 *
 * @version $Id: JSR14v12Compiler.java,v 1.2 2005/04/27 20:28:34 elmoutam Exp $
 */
public class JSR14v12Compiler extends Javac141Compiler {
  
  private File _collectionsPath;
  
  /**
   * Whether this is a version later than 1.2.
   */
  private boolean post12;
  
  /** Singleton instance. */
  public static final CompilerInterface ONLY = new JSR14v12Compiler();

  protected JSR14v12Compiler() {
    super();
    post12 = _isPost12();
  }
  
  /**
   * Returns whether this is a version of JSR-14 which is post-version 1.2.
   * (The checkLimits() method was added in version 1.3, and version 1.3
   * requires a different command line switch for generics.)
   */
  protected boolean _isPost12() {
    
    Class code = com.sun.tools.javac.v8.code.Code.class;
    // The JSR14 1.3 Code.checkLimits method
    Class[] validArgs1 = {
      int.class,
      com.sun.tools.javac.v8.util.Log.class
    };
    
    try { 
      code.getMethod("checkLimits", validArgs1);
      // succeeds, therefore must be correct version
      return true;
    }
    catch (NoSuchMethodException e) {
      // Didn't have expected method, so we must be using v1.2.
      return false;
    }
  }
  
  protected void updateBootClassPath() {

    // add collections path to the bootclasspath
    // Yes, we are mutating some other class's public variable.
    // But the docs for ClassReader say it's OK for others to mutate it!
    // And this way, we don't need to specify the entire bootclasspath,
    // just what we want to add on to it.
    
    if (_collectionsPath != null) {
      String ccp = _collectionsPath.getAbsolutePath();
    
      if (ccp != null && ccp.length() > 0) {
        ClassReader reader = ClassReader.instance(context);
        reader.bootClassPath = ccp +
          System.getProperty("path.separator")+
          reader.bootClassPath;
  
      }
    }
  }
    
  protected void initCompiler(File[] sourceRoots) {
    super.initCompiler(sourceRoots);
    updateBootClassPath();
  }

  /**
   * This method allows us to set the JSR14 collections path across a class loader.
   * (cannot cast a loaded class to a subclass, so all compiler interfaces must have this method)
   */ 
  public void addToBootClassPath( File cp) {
    _collectionsPath = cp;
  }
  
  /**
   * Adds the appropriate switch for generics, if available.
   */
  protected void _addGenericsOption(Options options) {
    if (!post12) {
      options.put("-gj", "");
    }
    else {
      // Uses "-source 1.5" and "-target 1.5" (see below)
    }
  }
  
  /**
   * Adds the appropriate values for the source and target arguments.
   */
  protected void _addSourceAndTargetOptions(Options options) {
    if (!post12) {
      // Set output classfile version to 1.1
      options.put("-target", "1.1");
    
      // Allow assertions in 1.4 if configured and in Java >= 1.4
      String version = System.getProperty("java.version");
      if ((_allowAssertions) && (version != null) &&
          ("1.4.0".compareTo(version) <= 0)) {
        options.put("-source", "1.4");
      }
    }
    else {
      // Version 1.3 or later needs "-source 1.5"
      options.put("-source", "1.5");
      options.put("-target", "1.5");
    }
  }

  /**
   * Both JSR-14 v1.2 and v1.3 have 12+ anonymous inner classes of Main, and
   * JDK1.4.2 tools.jar only has 10.  In JSR-14 v2.x there is no v8 in the
   * classnames, so hopefully this method is secure in all current platforms.
   */
  public boolean isAvailable() {
    try {
      Class.forName("com.sun.tools.javac.v8.Main$12");
      try {
        Class.forName("java.lang.Enum");
        // only available if jsr14 v2.x is on the boot classpath
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
    if (!post12) {
      return "JSR-14 v1.2";
    }
    else {
      return "JSR-14 v1.3";
    }
  }
}
