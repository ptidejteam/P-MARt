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
 * Tests to ensure that compilation behaves correctly in border cases.
 * 
 * @version $Id: GlobalModelCompileTest.java,v 1.1 2005/08/05 12:45:06 guehene Exp $
 */
public final class GlobalModelCompileTest extends GlobalModelTestCase {
  
  /**
   * Constructor.
   * @param  String name
   */
  public GlobalModelCompileTest(String name) {
    super(name);
  }

  
  /**
   * Tests calling compileAll with no source files works.
   * Doesn't reset interactions because Interactions Pane isn't used.
   */
  public void testCompileAllWithNoFiles()
    throws BadLocationException, IOException, InterruptedException
  {
    // Open one empty doc
    _model.newFile();
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener);
    _model.getCompilerModel().compileAll();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    assertCompileErrorsPresent("compile should succeed", false);
    listener.checkCompileOccurred();
    _model.removeListener(listener);
  }

  
  /**
   * Tests that the interactions pane is reset after a successful
   * compile if it has been used.
   */
  public void testCompileResetsInteractions()
    throws BadLocationException, IOException, InterruptedException,
    DocumentAdapterException
  {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = new File(_tempDir, "DrJavaTestFoo.java");
    doc.saveFile(new FileSelector(file));

    // Interpret something to force a reset
    // Note: the interpreter should reset now without any interactions.
//    interpret("Object o = new Object();");
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(true);
    _model.setResetAfterCompile(true);
    _model.addListener(listener);
    synchronized(listener) {
      _model.getCompilerModel().compileAll();
      if (_model.getCompilerModel().getNumErrors() > 0) {
        fail("compile failed: " + getCompilerErrorString());
      }
      listener.wait();
    }
    assertCompileErrorsPresent("compile should succeed", false);
    listener.checkCompileOccurred();
    _model.removeListener(listener);
  }


  /**
   * If we try to compile an unsaved file, and if we don't save when
   * asked to saveAllBeforeProceeding, it should not do the compile
   * or any other actions.
   */
  public void testCompileAbortsIfUnsaved()
    throws BadLocationException, IOException
  {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);

    TestListener listener = new TestListener() {
      public void saveBeforeCompile() {
        assertModified(true, doc);
        saveBeforeCompileCount++;
        // since we don't actually save the compile should abort
      }
    };

    _model.addListener(listener);
    doc.startCompile();
    listener.assertSaveBeforeCompileCount(1);
    assertModified(true, doc);
    assertContents(FOO_TEXT, doc);
    _model.removeListener(listener);
  }

  /**
   * If we try to compile while any files are unsaved, and if we don't
   * save when asked to saveAllBeforeProceeding, it should not do the compile
   * or any other actions.
   */
  public void testCompileAbortsIfAnyUnsaved()
    throws BadLocationException, IOException
  {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    
    TestListener listener = new TestListener() {
      public void saveBeforeCompile() {
        assertModified(true, doc);
        assertModified(true, doc2);
        saveBeforeCompileCount++;
        // since we don't actually save the compile should abort
      }
    };
    
    _model.addListener(listener);
    doc.startCompile();
    listener.assertSaveBeforeCompileCount(1);
    assertModified(true, doc);
    assertModified(true, doc2);
    assertContents(FOO_TEXT, doc);
    assertContents(BAR_TEXT, doc2);
    _model.removeListener(listener);
  }

  /**
   * If we try to compile while any files (including the active file) are
   * unsaved but we do save it from within saveAllBeforeProceeding, the
   * compile should occur happily.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testCompileAnyUnsavedButSaveWhenAsked()
    throws BadLocationException, IOException, InterruptedException
  {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    final File file = tempFile();
    final File file2 = tempFile(2);
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false) {
      public void saveBeforeCompile() {
        assertModified(true, doc);
        assertModified(true, doc2);
        assertSaveCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        assertInterpreterReadyCount(0);
        assertConsoleResetCount(0);
        
        try {
          doc.saveFile(new FileSelector(file));
          doc2.saveFile(new FileSelector(file2));
        }
        catch (IOException ioe) {
          fail("Save produced exception: " + ioe);
        }
        
        saveBeforeCompileCount++;
      }
      
      public void fileSaved(OpenDefinitionsDocument doc) {
        assertModified(false, doc);
        assertSaveBeforeCompileCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        assertInterpreterReadyCount(0);
        assertConsoleResetCount(0);
        
        //File f = null;
        try {
          //f = 
          doc.getFile();
        }
        catch (IllegalStateException ise) {
          // We know file should exist
          throw new UnexpectedException(ise);
        }
        catch (FileMovedException fme) {
          // We know file should exist
          fail("file does not exist");
        }
        //assertEquals("file saved", file, f);
        saveCount++;
      }
    };
    
    _model.addListener(listener);
    doc.startCompile();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }

    // Check events fired
    listener.assertSaveBeforeCompileCount(1);
    listener.assertSaveCount(2);
    assertCompileErrorsPresent("compile should succeed", false);
    listener.checkCompileOccurred();
    
    // Make sure .class exists
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertTrue("Class file doesn't exist after compile", compiled.exists());
    _model.removeListener(listener);
  }

  /**
   * If we try to compile while any files (but not the active file) are unsaved
   * but we do save it from within saveAllBeforeProceeding, the compile should
   * occur happily.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testCompileActiveSavedAnyUnsavedButSaveWhenAsked()
    throws BadLocationException, IOException, InterruptedException
  {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    final File file = tempFile();
    final File file2 = tempFile(1);
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false) {
      public void saveBeforeCompile() {
        assertModified(false, doc);
        assertModified(true, doc2);
        assertSaveCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        assertInterpreterReadyCount(0);
        assertConsoleResetCount(0);
        
        try {
          doc2.saveFile(new FileSelector(file2));
        }
        catch (IOException ioe) {
          fail("Save produced exception: " + ioe);
        }
        
        saveBeforeCompileCount++;
        assertModified(false, doc);
        assertModified(false, doc2);
        assertTrue(!_model.hasModifiedDocuments());
      }
      
      public void fileSaved(OpenDefinitionsDocument doc) {
        assertModified(false, doc);
        assertSaveBeforeCompileCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        assertInterpreterReadyCount(0);
        assertConsoleResetCount(0);
        
        File f = null;
        try {
          f = doc.getFile();
        }
        catch (IllegalStateException ise) {
          // We know file should exist
          throw new UnexpectedException(ise);
        }
        catch (FileMovedException fme) {
          // We know file should exist
          fail("file does not exist");
        }
        assertEquals("file saved", file2, f);
        saveCount++;
      }
    };
    
    assertModified(true, doc);
    doc.saveFile(new FileSelector(file));
    assertModified(false, doc);
    assertModified(true, doc2);
    _model.addListener(listener);
    doc.startCompile();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    assertTrue(!_model.hasModifiedDocuments());
    
    // Check events fired
    listener.assertCompileStartCount(1);
    listener.assertSaveBeforeCompileCount(1);
    listener.assertSaveCount(1);
    assertCompileErrorsPresent("compile should succeed", false);
    listener.checkCompileOccurred();
    
    // Make sure .class exists
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertTrue("Class file doesn't exist after compile", compiled.exists());
    _model.removeListener(listener);
  }

  
}
