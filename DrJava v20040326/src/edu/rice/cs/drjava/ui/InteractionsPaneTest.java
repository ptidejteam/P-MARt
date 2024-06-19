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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.repl.InteractionsDocumentTest.TestBeep;
import edu.rice.cs.drjava.model.repl.InteractionsModelTest.TestInteractionsModel;
import edu.rice.cs.util.text.SwingDocumentAdapter;
import edu.rice.cs.util.text.DocumentAdapterException;

import junit.framework.*;
import junit.extensions.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.rmi.registry.Registry;

/**
 * Test functions of InteractionsPane.
 *
 * @version $Id: InteractionsPaneTest.java,v 1.1 2005/08/05 12:45:08 guehene Exp $
 */
public final class InteractionsPaneTest extends TestCase {
  
  protected SwingDocumentAdapter _adapter;
  protected InteractionsModel _model;
  protected InteractionsDocument _doc;
  protected InteractionsPane _pane;
  protected InteractionsController _controller;
  
  
  /**
   * Create a new instance of this TestCase.
   * @param     String name
   */
  public InteractionsPaneTest(String name) {
    super(name);
  }
  
  /**
   * Setup method for each JUnit test case.
   */
  public void setUp() {
    _adapter = new SwingDocumentAdapter();
    _model = new TestInteractionsModel(_adapter);
    _doc = _model.getDocument();
    _pane = new InteractionsPane(_adapter);
    // Make tests silent
    _pane.setBeep(new TestBeep());

    _controller = new InteractionsController(_model, _adapter, _pane);
  }
  
  public void tearDown() {
    _controller = null;
    _doc = null;
    _model = null;
    _pane = null;
    _adapter = null;
    System.gc();
  }
  
  /**
   * Tests that this.setUp() puts the caret in the correct position.
   */
  public void testInitialPosition() {
    assertEquals("Initial caret not in the correct position.",
                 _pane.getCaretPosition(),
                 _doc.getPromptPos());
  }
  
  /**
   * Tests that moving the caret left when it's already at the prompt will
   * cycle it to the end of the line.
   */
  public void testCaretMovementCyclesWhenAtPrompt() throws DocumentAdapterException {
    _doc.insertText(_doc.getDocLength(), "test text", InteractionsDocument.DEFAULT_STYLE);
    _controller.moveToPrompt();
    
    _controller.moveLeftAction.actionPerformed(null);
    assertEquals("Caret was not cycled when moved left at the prompt.",
                 _doc.getDocLength(),
                 _pane.getCaretPosition());
  }
  
  /**
   * Tests that moving the caret right when it's already at the end will
   * cycle it to the prompt.
   */
  public void testCaretMovementCyclesWhenAtEnd() throws DocumentAdapterException {
    _doc.insertText(_doc.getDocLength(), "test text", InteractionsDocument.DEFAULT_STYLE);
    _controller.moveToEnd();
    
    _controller.moveRightAction.actionPerformed(null);
    assertEquals("Caret was not cycled when moved right at the end.",
                 _doc.getPromptPos(),
                 _pane.getCaretPosition());
  }

  /**
   * Tests that moving the caret left when it's before the prompt will
   * cycle it to the prompt.
   */
  public void testLeftBeforePromptMovesToPrompt() {
    _pane.setCaretPosition(1);
    _controller.moveLeftAction.actionPerformed(null);
    assertEquals("Left arrow doesn't move to prompt when caret is before prompt.",
                 _doc.getPromptPos(),
                 _pane.getCaretPosition());
  }
  
  /**
   * Tests that moving the caret right when it's before the prompt will
   * cycle it to the end of the document.
   */
  public void testRightBeforePromptMovesToEnd() {
    _pane.setCaretPosition(1);
    _controller.moveRightAction.actionPerformed(null);
    assertEquals("Right arrow doesn't move to end when caret is before prompt.",
                 _doc.getDocLength(),
                 _pane.getCaretPosition());
  }
  
  /**
   * Tests that moving the caret up (recalling the previous command in the History)
   * will move the caret to the end of the document.
   */
  public void testHistoryRecallPrevMovesToEnd() {
    _pane.setCaretPosition(1);
    _controller.historyPrevAction.actionPerformed(null);
    assertEquals("Caret not moved to end on up arrow.",
                 _doc.getDocLength(),
                 _pane.getCaretPosition());
  }
  
  /**
   * Tests that moving the caret down (recalling the next command in the History)
   * will move the caret to the end of the document.
   */
  public void testHistoryRecallNextMovesToEnd() {
    _pane.setCaretPosition(1);
    _controller.historyNextAction.actionPerformed(null);
    assertEquals("Caret not moved to end on down arrow.",
                 _doc.getDocLength(),
                 _pane.getCaretPosition());
  }
  
  public void testCaretStaysAtEndDuringInteraction() throws DocumentAdapterException {
    _doc.setInProgress(true);
    _doc.insertText(_doc.getDocLength(), "simulated output", InteractionsDocument.DEFAULT_STYLE);
    _doc.setInProgress(false);
    assertEquals("Caret is at the end after output while in progress.",
                 _doc.getDocLength(),
                 _pane.getCaretPosition());
  }
  
  /**
   * Tests that the caret catches up to the prompt if it is before it and
   * output is displayed.
   */
  public void testCaretMovesUpToPromptAfterInsert() throws DocumentAdapterException {
    _doc.insertText(_doc.getDocLength(), "typed text", InteractionsDocument.DEFAULT_STYLE);
    _pane.setCaretPosition(1);
    _doc.insertBeforeLastPrompt("simulated output", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("Caret is at the prompt after output inserted.",
                 _doc.getPromptPos(),
                 _pane.getCaretPosition());
    
    _doc.insertPrompt();
    _pane.setCaretPosition(1);
    _doc.insertBeforeLastPrompt("simulated output", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("Caret is at the end after output inserted.",
                 _doc.getPromptPos(),
                 _pane.getCaretPosition());
  }
  
  /**
   * Tests that the caret is moved properly when the current interaction
   * is cleared.
   */
  public void testClearCurrentInteraction() throws DocumentAdapterException {
    _doc.insertText(_doc.getDocLength(), "typed text", InteractionsDocument.DEFAULT_STYLE);
    _controller.moveToEnd();
    
    _doc.clearCurrentInteraction();
    assertEquals("Caret is at the prompt after output cleared.",
                 _doc.getPromptPos(),
                 _pane.getCaretPosition());
    assertEquals("Prompt is at the end after output cleared.",
                 _doc.getDocLength(),
                 _doc.getPromptPos());
  }
  
  /**
   * Tests that the InteractionsPane cannot be edited before the prompt.
   */
  public void testCannotEditBeforePrompt() throws DocumentAdapterException {
    int origLength = _doc.getDocLength();
    _doc.insertText(1, "typed text", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("Document should not have changed.",
                 origLength,
                 _doc.getDocLength());
  }
  
  /**
   * Tests that the caret is put in the correct position after an insert.
   */
  public void testCaretUpdatedOnInsert() throws DocumentAdapterException {
    _doc.insertText(_doc.getDocLength(), "typed text",
                    InteractionsDocument.DEFAULT_STYLE);
    int pos = _doc.getDocLength() - 5;
    _pane.setCaretPosition(pos);
    
    // Insert text before the prompt
    _doc.insertBeforeLastPrompt("aa", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("caret should be in correct position",
                 pos + 2, _pane.getCaretPosition());
    
    // Move caret to prompt and insert more text
    _pane.setCaretPosition(_doc.getPromptPos());
    _doc.insertBeforeLastPrompt("b", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("caret should be at prompt",
                 _doc.getPromptPos(), _pane.getCaretPosition());
    
    // Move caret before prompt and insert more text
    _pane.setCaretPosition(0);
    _doc.insertBeforeLastPrompt("ccc", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("caret should be at prompt",
                 _doc.getPromptPos(), _pane.getCaretPosition());
    
    // Move caret after prompt and insert more text
    pos = _doc.getPromptPos();
    // simulate a keystroke by putting caret just *after* pos of insert
    _pane.setCaretPosition(pos+1);
    _doc.insertText(pos, "d", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("caret should be immediately after the d",
                 pos + 1, _pane.getCaretPosition());
  }

  public void testSystemIn() {
    final Object lock = new Object();
    final StringBuffer buf = new StringBuffer();
    synchronized (_controller._inputEnteredAction) {
      new Thread("Testing System.in") {
        public void run() {
          synchronized (_controller._inputEnteredAction) {
            _controller._inputEnteredAction.notify();
            synchronized (lock) {
              buf.append(_controller._inputListener.getConsoleInput());
            }
          }
        }
      }.start();
      try {
        _controller._inputEnteredAction.wait();
      }
      catch (InterruptedException ie) {
      }
    }
    try {
      Thread.sleep(2000);
    }
    catch (InterruptedException ie) {
    }
    _controller._insertNewlineAction.actionPerformed(null);
    _controller._inputEnteredAction.actionPerformed(null);
    synchronized (lock) {
      assertEquals("Should have returned the correct text.", "\n\n", buf.toString());
    }
  }
}
