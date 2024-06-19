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
import junit.extensions.*;
import javax.swing.text.BadLocationException;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.model.GlobalModelTestCase.WarningFileSelector;
import edu.rice.cs.drjava.model.GlobalModelTestCase.OverwriteException;
import edu.rice.cs.util.FileOps;

/**
 * Tests the functionality of the repl History.
 * @version $Id: HistoryTest.java,v 1.1 2005/08/05 12:45:09 guehene Exp $
 */
public final class HistoryTest extends TestCase implements OptionConstants{
  private History _history;
  private File _tempDir;
  
  /**
   * Create a new instance of this TestCase.
   * @param     String name
   */
  public HistoryTest(String name) {
    super(name);
  }
  
  /**
   * Initialize fields for each test.
   */
  public void setUp() throws IOException {
    String user = System.getProperty("user.name");
    _tempDir = FileOps.createTempDirectory("DrJava-test-" + user);
    _history = new History();
    DrJava.getConfig().resetToDefaults();
  }
  
  public void tearDown() throws IOException {
    boolean ret = FileOps.deleteDirectory(_tempDir);
    assertTrue("delete temp directory " + _tempDir, ret);
    _tempDir = null;
    _history = null;
  }
  
  /**
   * Return a new TestSuite for this class.
   * @return Test
   */
  public static Test suite() {
    return  new TestSuite(HistoryTest.class);
  }
  
  /**
   * Tests that the history doesn't overwrite files without prompting.
   */
  public void testSaveAsExistsForOverwrite()
    throws BadLocationException, IOException {
    
    _history.add("some text");
    final File file1 = File.createTempFile("DrJava-test", ".hist", _tempDir);
    file1.deleteOnExit();
    try {
      _history.writeToFile(new WarningFileSelector(file1));
      fail("Did not ask to verify overwrite as expected");
    }
    catch (OverwriteException e1) {
      // good behavior for file saving...
    }
  }
  
  public void testMultipleInsert() {
    _history.add("new Object()");
    _history.add("new Object()");
    assertEquals("Duplicate elements inserted", 2, _history.size());
  }
  
  public void testCanMoveToEmptyAtEnd() {
    String entry = "some text";
    _history.add(entry);
    
    _history.movePrevious("");
    assertEquals("Prev did not move to correct item", 
                 entry, 
                 _history.getCurrent());
    
    _history.moveNext(entry);
    assertEquals("Can't move to blank line at end",
                 "",
                 _history.getCurrent());
  }
  
  /**
   * Ensures that Histories are bound to 500 entries.
   */
  public void testHistoryIsBounded() {
    
    int maxLength = 500;
    DrJava.getConfig().setSetting(HISTORY_MAX_SIZE, new Integer(maxLength));
    
    int i;
    for (i = 0; i < maxLength + 100; i++) {
      _history.add("testing " + i);
    }
    for (; _history.hasPrevious(); i--) {
      _history.movePrevious("testing " + i);
    }

    assertEquals("History length is not bound to " + maxLength,
                 maxLength,
                 _history.size());
    assertEquals("History elements are not removed in FILO order",
                 "testing 100",
                 _history.getCurrent());
  }
  
  /**
   * Tests that the history size can be updated, both through
   * the config framework and the setMaxSize method.
   */
  public void testLiveUpdateOfHistoryMaxSize() {
    
    int maxLength = 20;
    DrJava.getConfig().setSetting(HISTORY_MAX_SIZE, new Integer(20));
    
    for (int i = 0; i < maxLength; i++) {
      _history.add("testing " + i);
    }
    
    assertEquals("History size should be 20",
                 20, _history.size());
    
    DrJava.getConfig().setSetting(HISTORY_MAX_SIZE, new Integer(10));
    
    assertEquals("History size should be 10",
                 10, _history.size());
    
    _history.setMaxSize(100);
    
    assertEquals("History size should still be 10",
                 10, _history.size());
    
    _history.setMaxSize(0);
    
    assertEquals("History size should be 0",
                 0, _history.size());
    
    DrJava.getConfig().setSetting(HISTORY_MAX_SIZE, new Integer(-1));
    
    assertEquals("History size should still be 0",
                 0, _history.size());
  }
  
  /**
   * Tests the getHistoryAsString() method
   */
  public void testGetHistoryAsString(){
    DrJava.getConfig().setSetting(HISTORY_MAX_SIZE, new Integer(20));
    assertEquals("testGetHistoryAsString:", "", _history.getHistoryAsString());
    
    String newLine = System.getProperty("line.separator");
    
    _history.add("some text");
    assertEquals("testGetHistoryAsString:", "some text" + newLine, _history.getHistoryAsString());
    
    _history.add("some more text");
    _history.add("some text followed by a newline" + newLine);
    assertEquals("testGetHistoryAsString:",  
                 "some text" + newLine + "some more text" + newLine +
                 "some text followed by a newline" + newLine + newLine,
                 _history.getHistoryAsString());
  }
  
  /**
   * Tests that the history remembers one edited entry for the given command.
   */
  public void testRemembersOneEditedEntry() {
    _history.add("some text");
    _history.movePrevious("");
    
    String newEntry = "some different text";
   
    _history.moveNext(newEntry);
    _history.movePrevious("");
    
    assertEquals("Did not remember the edited entry correctly.", 
                 newEntry, 
                 _history.getCurrent());
  }
  
  /**
   * Tests that the history remembers multiple edited entries for the given command.
   */
  public void testRemembersMultipleEditedEntries() {
    _history.add("some text");
    _history.add("some more text");
    _history.movePrevious("");
    
    String newEntry1 = "some more different text";
    String newEntry2 = "some different text";
   
    _history.movePrevious(newEntry1);
    _history.moveNext(newEntry2);
    
    assertEquals("Did not remember the edited entry correctly.", 
                 newEntry1, 
                 _history.getCurrent());
    
    _history.movePrevious(newEntry1);
    assertEquals("Did not remember the edited entry correctly.",
                 newEntry2,
                 _history.getCurrent());
  }
  
  /**
   * Tests that the original copy of an edited entry remains in the history.
   */
  public void testOriginalCopyRemains() {
    String entry = "some text";
    String newEntry = "some different text";

    _history.add(entry);
    _history.movePrevious("");
    _history.moveNext(newEntry);
    _history.movePrevious("");
    _history.add(newEntry);
    
    _history.movePrevious("");
    assertEquals("Did not add edited entry to end of history.",
                 newEntry,
                 _history.getCurrent());
    
    _history.movePrevious(newEntry);
    assertEquals("Did not keep a copy of the original entry.",
                 entry,
                 _history.getCurrent());
  }
  
  /**
   * Tests that the tab completion of the most recent entry is correct.
   */
  public void testSearchHistory() {
    String entry1 = "some text";
    String entry2 = "blah";
    
    _history.add(entry1);
    _history.add(entry2);
    
    _history.reverseSearch("s");
    assertEquals("Did not find the correct entry in history.",
                 entry1,
                 _history.getCurrent());
    
    _history.forwardSearch("b");
    assertEquals("Did not find the correct entry in history.",
                 entry2,
                 _history.getCurrent());
  }
  
  /**
   * Tests that if "tab completion" does not find a match, then cursor goes back to "end".
   */
  public void testNoMatch() {
    String entry1 = "some text";
    String entry2 = "blah";
    
    _history.add(entry1);
    _history.add(entry2);
    
    _history.reverseSearch("a");
    
    assertEquals("Did not reset cursor correctly.",
                 "a",
                 _history.getCurrent());    
  }
  
  /**
   * Tests reverse searching twice.
   */
  public void testReverseSearchTwice() {
    String entry1 = "same";
    String entry2 = "some";
    
    _history.add(entry1);
    _history.add(entry2);
    
    _history.reverseSearch("s");
    assertEquals("Did not reset cursor correctly.",
                 entry2,
                 _history.getCurrent());    

    _history.reverseSearch(_history.getCurrent());
    assertEquals("Did not reset cursor correctly.",
                 entry1,
                 _history.getCurrent());    
  }
}
