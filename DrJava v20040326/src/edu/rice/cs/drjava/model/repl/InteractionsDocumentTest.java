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

package edu.rice.cs.drjava.model.repl;

import junit.framework.*;

import java.util.Vector;
import javax.swing.text.BadLocationException;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;

import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.CodeStatus;

import edu.rice.cs.util.text.DocumentAdapter;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.util.text.SwingDocumentAdapter;

/**
 * Tests the functionality of the AbstractInteractionsDocument.
 * Most history functionality is tested in HistoryTest.
 * @version $Id: InteractionsDocumentTest.java,v 1.1 2005/08/05 12:45:09 guehene Exp $
 */
public final class InteractionsDocumentTest extends TestCase {
  protected InteractionsDocument _doc;
  
  /**
   * Create a new instance of this TestCase.
   * @param     String name
   */
  public InteractionsDocumentTest(String name) {
    super(name);
  }
  
  /**
   * Initialize fields for each test.
   */
  protected void setUp() {
    _doc = new InteractionsDocument(new SwingDocumentAdapter());
  }
  
  /**
   * Return a new TestSuite for this class.
   * @return Test
   */
  public static Test suite() {
    return new TestSuite(InteractionsDocumentTest.class);
  }
  
  
  /**
   * Tests that the document prevents editing before the
   * prompt, and beeps if you try.
   */
  public void testCannotEditBeforePrompt() throws DocumentAdapterException {
    TestBeep testBeep = new TestBeep();
    _doc.setBeep(testBeep);
    int origLength = _doc.getDocLength();
    
    // Try to insert into the banner
    _doc.insertText(1, "text", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("Number of beeps", 1, testBeep.numBeeps);
    assertEquals("Doc length", origLength, _doc.getDocLength());
  }
  
  /**
   * Tests that clear current interaction works.
   */
  public void testClearCurrent() throws DocumentAdapterException {
    int origLength = _doc.getDocLength();
    _doc.insertText(origLength, "text", InteractionsDocument.DEFAULT_STYLE);
    _doc.insertBeforeLastPrompt("before", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("Length after inserts", 
                 origLength + 10,  // orig + "before" + "text"
                 _doc.getDocLength());
    _doc.clearCurrentInteraction();
    assertEquals("Length after clear", 
                 origLength + 6,  // orig + "before"
                 _doc.getDocLength());
  }
  
  /**
   * Tests that initial contents are the banner and prompt, and that
   * reset works.
   */
  public void testContentsAndReset() throws DocumentAdapterException {
    String origText = _doc.getBanner() + _doc.getPrompt();
    assertEquals("Contents before insert", 
                 origText, _doc.getDocText(0, _doc.getDocLength()));
    
    // Insert some text
    _doc.insertText(_doc.getDocLength(), "text", InteractionsDocument.DEFAULT_STYLE);
    _doc.insertBeforeLastPrompt("before", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("Contents before reset", 
                 _doc.getBanner() + "before" + _doc.getPrompt() + "text",
                 _doc.getDocText(0, _doc.getDocLength()));
    
    // Reset should clear
    _doc.reset();
    assertEquals("Contents after reset", 
                 origText, _doc.getDocText(0, _doc.getDocLength()));
  }
  
  /**
   * Tests that inserting a newline works.
   */
  public void testInsertNewLine() throws DocumentAdapterException {
    int origLength = _doc.getDocLength();
    _doc.insertText(origLength, "command", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("current interaction before newline",
                 "command",
                 _doc.getCurrentInteraction());
    _doc.insertNewLine(origLength + 2);
    assertEquals("current interaction after newline",
                 "co" + System.getProperty("line.separator") + "mmand",
                 _doc.getCurrentInteraction());
  }
  
  /**
   * Tests that recalling commands from the history works.
   */
  public void testRecallFromHistory() throws DocumentAdapterException {
    String origText = _doc.getDocText(0, _doc.getDocLength());
    _doc.addToHistory("command");
    assertEquals("Contents before recall prev", 
                 origText, _doc.getDocText(0, _doc.getDocLength()));
    
    _doc.recallPreviousInteractionInHistory();
    assertEquals("Contents after recall prev", 
                 origText + "command", _doc.getDocText(0, _doc.getDocLength()));
    
    _doc.recallNextInteractionInHistory();
    assertEquals("Contents after recall next", 
                 origText, _doc.getDocText(0, _doc.getDocLength()));
  }
  
  
  /**
   * Silent beep for a test class.
   */
  public static class TestBeep implements Runnable {
    int numBeeps = 0;
    public void run() {
      numBeeps++;
    }
  }
}



