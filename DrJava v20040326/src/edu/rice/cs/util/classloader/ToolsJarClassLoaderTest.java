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

package edu.rice.cs.util.classloader;

import junit.framework.*;

/**
 * Test cases for {@link ToolsJarClassLoader}.
 *
 * @version $Id: ToolsJarClassLoaderTest.java,v 1.1 2005/08/05 12:45:09 guehene Exp $
 */
public class ToolsJarClassLoaderTest extends TestCase {

  /**
   * Constructor.
   * @param String name
   */
  public ToolsJarClassLoaderTest(String name) {
    super(name);
  }

  /**
   * Test that ToolsJarClassLoader can correctly guess the default
   * SDK installation directory on Windows.
   * Precondition: JAVA_HOME contains "Program Files"
   */
  public void testWindowsSDKDirectory() throws Throwable {
    String javahome1 = "C:\\Program Files\\Java\\j2re1.4.0_01";
    String javahome2 = "C:\\Program Files\\Java\\j2re1.4.1";
    String javahome3 = "C:\\Program Files\\JavaSoft\\JRE\\1.3.1_04";
    
    assertEquals("new versions of Windows J2SDK (1)",
                 "C:\\j2sdk1.4.0_01\\lib\\tools.jar",
                 ToolsJarClassLoader.getWindowsToolsJar(javahome1));
                 
    assertEquals("new versions of Windows J2SDK (2)",
                 "C:\\j2sdk1.4.1\\lib\\tools.jar",
                 ToolsJarClassLoader.getWindowsToolsJar(javahome2));
    
    assertEquals("old versions of Windows J2SDK",
                 "C:\\jdk1.3.1_04\\lib\\tools.jar",
                 ToolsJarClassLoader.getWindowsToolsJar(javahome3));
  }

}
