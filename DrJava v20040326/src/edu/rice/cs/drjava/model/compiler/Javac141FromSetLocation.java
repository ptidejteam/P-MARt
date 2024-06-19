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
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.FileOption;
import edu.rice.cs.util.classloader.ToolsJarClassLoader;

/**
 * A compiler interface to find Javac (1.4.1+) from the location
 * specified in Configuration.
 *
 * @version $Id: Javac141FromSetLocation.java,v 1.1 2005/08/05 12:45:17 guehene Exp $
 */
public class Javac141FromSetLocation extends CompilerProxy
  implements OptionConstants {
  // To implement #523222, we had to make this not a singleton,
  // to allow it to re-determine the location of the compiler multiple times.
  //public static final CompilerInterface ONLY = new JavacFromSetLocation();
  
  /** Private constructor due to singleton. */
  public Javac141FromSetLocation() {
    super("edu.rice.cs.drjava.model.compiler.Javac141Compiler",
          _getClassLoader());
  }
  
  private static ClassLoader _getClassLoader() {
    File loc = DrJava.getConfig().getSetting(JAVAC_LOCATION);
    if (loc == FileOption.NULL_FILE) {
      throw new RuntimeException("javac location not set");
    }
    
    try {
      //URL url = new File(loc).toURL();
      URL url = loc.toURL();
      return new URLClassLoader(new URL[] { url });
    }
    catch (MalformedURLException e) {
      throw new RuntimeException("malformed url exception");
    }
  }
  
  /**
   * Returns the name of this compiler, appropriate to show to the user.
   */
  public String getName() {
    return super.getName() + " (user)";
  }
}
