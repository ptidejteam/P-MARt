/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import  junit.framework.*;
import  junit.extensions.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.File;
import java.io.IOException;
import java.rmi.registry.Registry;

import gj.util.Vector;

import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.util.FileOps;

/**
 * Test functions of RecentFileManager.
 *
 * @version $Id: RecentFileManagerTest.java,v 1.1 2005/04/27 20:30:32 elmoutam Exp $
 */
public class RecentFileManagerTest extends TestCase {
  
  protected static final String FOO_TEXT = "class DrJavaTestFoo {}";
  protected static final String BAR_TEXT = "class DrJavaTestBar {}";
  private RecentFileManager _rfm;
  private JMenu _menu;
  private DefaultGlobalModel _model = new DefaultGlobalModel();
  protected File _tempDir;
  
  /**
   * Constructor.
   * @param  String name
   */
  public RecentFileManagerTest(String name) {
    super(name);
  }
  
  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(RecentFileManagerTest.class);
  }
  
  /**
   * Setup method for each JUnit test case.
   */
  public void setUp() throws IOException {
    _menu = new JMenu();
    _rfm = new RecentFileManager(0, _menu, null);
    String user = System.getProperty("user.name");
    _tempDir = FileOps.createTempDirectory("DrJava-test-" + user);
  }

  /** 
   * Create a new temporary file in _tempDir.  Calls with the same
   * int will return the same filename, while calls with different
   * ints will return different filenames.
   */
  protected File tempFile() throws IOException {
    return File.createTempFile("DrJava-test", ".java", _tempDir);
  }
  
  /**
   * Creates a new temporary file and writes the given text to it.
   * The File object for the new file is returned.
   */
  protected File writeToNewTempFile(String text) throws IOException {
    File temp = tempFile();
    FileOps.writeStringToFile(temp, text);
    return temp;
  }
  
  /**
   * Tests that the size of the recent files list doesn't get bigger than
   * the maximum size.
   */
  public void testAddMoreThanMaxSize() throws IOException, AlreadyOpenException, OperationCanceledException {

    
    final File tempFile = writeToNewTempFile(BAR_TEXT);
    final File tempFile2 = writeToNewTempFile(FOO_TEXT);
    _rfm.updateMax(1);
    _rfm.updateOpenFiles(tempFile);
    _rfm.updateOpenFiles(tempFile2);
    Vector<File> vector = _rfm.getFileVector();
    assertEquals("number of recent files", 1, vector.size());
    assertEquals("text of recent file", 
                 FOO_TEXT,
                 FileOps.readFileAsString(vector.elementAt(0)));
  }
  
  /**
   * Tests that the size of the recent files list is reduced in response to a 
   * decrease in max size. 
   */
  public void testShrinksToMaxSize() throws IOException, AlreadyOpenException, OperationCanceledException {
    
    final File tempFile = writeToNewTempFile(BAR_TEXT);
    final File tempFile2 = writeToNewTempFile(FOO_TEXT);
    _rfm.updateMax(2);
    /*TestListener listener = new TestListener() {
      public void fileOpened(OpenDefinitionsDocument doc) {
        File file = null;
        try {
          file = doc.getFile();
        }
        catch (IllegalStateException ise) {
          // We know file should exist
          fail("file does not exist");
        }
        assertEquals("file to open", tempFile, file);
        openCount++;
      }
    };

    _model.addListener(listener);
    OpenDefinitionsDocument doc = _model.openFile(new FileSelector(tempFile));
    listener.assertOpenCount(1);
    assertModified(false, doc);
    assertContents(BAR_TEXT, doc);
    OpenDefinitionsDocument doc2 = _model.openFile(new FileSelector(tempFile2));
    listener.assertOpenCount(2);
    assertModified(false, doc2);
    assertContents(FOO_TEXT, doc2);*/
    _rfm.updateOpenFiles(tempFile);
    _rfm.updateOpenFiles(tempFile2);
    Vector<File> vector = _rfm.getFileVector();
    assertEquals("number of recent files", 2, vector.size());
    assertEquals("text of most-recent file", 
                 FOO_TEXT,
                 FileOps.readFileAsString(vector.elementAt(0)));
    assertEquals("text of second-most recent file", 
                 BAR_TEXT,
                 FileOps.readFileAsString(vector.elementAt(1)));
    _rfm.updateMax(1);
    _rfm.numberItems();
    vector = _rfm.getFileVector();
    assertEquals("number of recent files", 1, vector.size());
    assertEquals("text of recent file", 
                 FOO_TEXT,
                 FileOps.readFileAsString(vector.elementAt(0)));
    
  }
}
