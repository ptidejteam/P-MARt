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

package edu.rice.cs.drjava.model;

import  junit.framework.*;

import java.io.*;

import java.util.LinkedList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Position;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.DocumentAdapterException;

/**
 * Tests to ensure that compilation interacts with files correctly.
 *
 * @version $Id: GlobalModelCompileIOTest.java,v 1.1 2005/08/05 12:45:06 guehene Exp $
 */
public final class GlobalModelCompileIOTest extends GlobalModelTestCase {
  
  /**
   * Constructor.
   * @param  String name
   */
  public GlobalModelCompileIOTest(String name) {
    super(name);
  }

  /**
   * After creating a new file, saving, and compiling it, this test checks
   * that the new document is in sync after compiling and is out of sync
   * after modifying and even saving it.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testClassFileSynchronization()
    throws BadLocationException, IOException, InterruptedException
  {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    
    doc.saveFile(new FileSelector(file));
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener);
    assertTrue("Class file should not exist before compile",
               doc.getDocument().getCachedClassFile() == null);
    assertTrue("should not be in sync before compile",
               !doc.checkIfClassFileInSync());
    doc.startCompile();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    _model.removeListener(listener);
    listener.checkCompileOccurred();
    assertTrue("should be in sync after compile",
               doc.checkIfClassFileInSync());
    doc.getDocument().insertString(0, "hi", null);
    assertTrue("should not be in sync after modification",
               !doc.checkIfClassFileInSync());

    // Have to wait 2 seconds so file will have a different timestamp
    Thread.sleep(2000);

    doc.saveFile(new FileSelector(file));
    assertTrue("should not be in sync after save",
               !doc.checkIfClassFileInSync());
    
    // Make sure .class exists
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertTrue(" Class file should exist after compile", compiled.exists());
  }
  
  /**
   * Ensure that renaming a file makes it out of sync with its class file.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testClassFileSynchronizationAfterRename()
    throws BadLocationException, IOException, IllegalStateException,
    InterruptedException
  {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    final File file2 = tempFile(2);
    
    doc.saveFile(new FileSelector(file));
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener);
    assertTrue("Class file should not exist before compile",
               doc.getDocument().getCachedClassFile() == null);
    assertTrue("should not be in sync before compile",
               !doc.checkIfClassFileInSync());
    doc.startCompile();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    _model.removeListener(listener);
    listener.checkCompileOccurred();
    assertTrue("should be in sync after compile",
               doc.checkIfClassFileInSync());
    
    // Have to wait 1 second so file will have a different timestamp
    Thread.sleep(2000);
    
    // Rename to a different file
    doc.saveFileAs(new FileSelector(file2));
    assertTrue("should not be in sync after renaming",
               !doc.checkIfClassFileInSync());
  }
  
  /**
   * Tests a compile after a file has unexpectedly been moved or deleted.
   */
  public void testCompileAfterFileMoved() throws BadLocationException, IOException {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    doc.saveFile(new FileSelector(file));
    TestListener listener = new TestListener();
    _model.addListener(listener);
    file.delete();
    try {
      doc.startCompile();
      fail("Compile should not have begun.");
    }
    catch (FileMovedException fme) {
      //compile should never have begun because the file was not where it was expected
      // to be on disk.
    }
    
    assertCompileErrorsPresent("compile should succeed", false);
    
    // Make sure .class exists
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertTrue("Class file shouldn't exist after compile", !compiled.exists());
    _model.removeListener(listener);
  }
  
}
