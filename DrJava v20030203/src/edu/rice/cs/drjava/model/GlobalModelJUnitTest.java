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

package edu.rice.cs.drjava.model;

import junit.framework.*;

import java.io.*;

import java.util.LinkedList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;

import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.util.UnexpectedException;

/**
 * A test on the GlobalModel for JUnit testing.
 *
 * @version $Id: GlobalModelJUnitTest.java,v 1.1 2005/04/27 20:30:32 elmoutam Exp $
 */
public class GlobalModelJUnitTest extends GlobalModelTestCase {
  final boolean printMessages = false;
  
  private static final String MONKEYTEST_PASS_TEXT =
    "import junit.framework.*; " + 
    "public class MonkeyTestPass extends TestCase { " +
    "  public MonkeyTestPass(String name) { super(name); } " +
    "  public void testShouldPass() { " +
    "    assertEquals(\"monkey\", \"monkey\"); " +
    "  } " +
    "}";
  
  private static final String MONKEYTEST_FAIL_TEXT =
    "import junit.framework.*; " + 
    "public class MonkeyTestFail extends TestCase { " +
    "  public MonkeyTestFail(String name) { super(name); } " +
    "  public void testShouldFail() { " +
    "    assertEquals(\"monkey\", \"baboon\"); " +
    "  } " +
    "}";
  
  private static final String MONKEYTEST_COMPILEERROR_TEXT =
    "import junit.framework.*; " + 
    "public class MonkeyTestCompileError extends TestCase { " +
    "  Object MonkeyTestFail(String name) { super(name); } " +
    "  public void testShouldFail() { " +
    "    assertEquals(\"monkey\", \"baboon\"); " +
    "  } " +
    "}";
  
  private static final String NONPUBLIC_TEXT =
    "import junit.framework.*; " + 
    "public class NonPublic extends TestCase { " +
    "  public NonPublic(String name) { super(name); } " +
    "  void testShouldFail() { " +
    "    assertEquals(\"monkey\", \"baboon\"); " +
    "  } " +
    "}";
  
  private static final String NON_TESTCASE_TEXT =
    "public class NonTestCase {}";
  
  private static final String MONKEYTEST_INFINITE_TEXT =
    "import junit.framework.*; " + 
    "public class MonkeyTestInfinite extends TestCase { " +
    "  public MonkeyTestInfinite(String name) { super(name); } " +
    "  public void testInfinite() { " +
    "    while(true){}" +
    "  } " +
    "}";
  
  /**
   * Constructor.
   * @param  String name
   */
  public GlobalModelJUnitTest(String name) {
    super(name);
  }
  
  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(GlobalModelJUnitTest.class);
  }  
  
  /**
   * Tests that a JUnit file with no errors is reported to have no errors.
   */
  public void testNoJUnitErrors() throws Exception {
    if (printMessages) System.out.println("----testNoJUnitErrors-----");
    _model.setResetAfterCompile(false);
    
    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.java");
    doc.saveFile(new FileSelector(file));
    TestShouldSucceedListener listener = new TestShouldSucceedListener();
    _model.addListener(listener);
    if (printMessages) System.out.println("before compile");
    //synchronized(listener) {
      doc.startCompile();
      //listener.wait();
    //}
    if (printMessages) System.out.println("after compile");
    listener.checkCompileOccurred();
    synchronized(listener) {
      doc.startJUnit();
      listener.assertJUnitStartCount(1);
      if (printMessages) System.out.println("waiting for test");
      listener.wait();
    }
    if (printMessages) System.out.println("after test");
    assertEquals("test case should have no errors reported",
                 0,
                 doc.getJUnitErrorModel().getNumErrors());
    
    _model.setResetAfterCompile(true);
  }
  
  /**
   * Tests that a JUnit file with an error is reported to have an error.
   */
  public void testOneJUnitError() throws Exception {
    if (printMessages) System.out.println("----testOneJUnitError-----");
    _model.setResetAfterCompile(false);
    
    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_FAIL_TEXT);
    final File file = new File(_tempDir, "MonkeyTestFail.java");
    doc.saveFile(new FileSelector(file));
    TestShouldSucceedListener listener = new TestShouldSucceedListener();
    _model.addListener(listener);
    if (printMessages) System.out.println("before compile");
    //synchronized(listener) {
      doc.startCompile();
      //listener.wait();
    //}
    if (printMessages) System.out.println("after compile");
    synchronized(listener) {
      doc.startJUnit();
      if (printMessages) System.out.println("waiting for test");
      listener.wait();
    }
    if (printMessages) System.out.println("after test");
    
    assertEquals("test case has one error reported",
                 1,
                 doc.getJUnitErrorModel().getNumErrors());
    
    _model.setResetAfterCompile(true);
  }
 
  /**
   * Tests that the ui is notified to put up an error dialog if JUnit
   * is run on a non-TestCase.
   */
  public void testNonTestCaseError() throws Exception {
    if (printMessages) System.out.println("----testNonTestCaseError-----");
    _model.setResetAfterCompile(false);
    
    final OpenDefinitionsDocument doc = setupDocument(NON_TESTCASE_TEXT);
    final File file = new File(_tempDir, "NonTestCase.java");
    doc.saveFile(new FileSelector(file));
    
    TestShouldSucceedListener listener = new TestShouldSucceedListener() {
      public void nonTestCase() { nonTestCaseCount++; }
    };
    
    _model.addListener(listener);
    if (printMessages) System.out.println("before compile");
    //synchronized(listener) {
      doc.startCompile();
      //listener.wait();
    //}
    if (printMessages) System.out.println("after compile");
    synchronized(listener) {
      doc.startJUnit();
      listener.assertJUnitStartCount(1);
      if (printMessages) System.out.println("waiting for test");
      listener.wait();
    }
    if (printMessages) System.out.println("after test");
    
    // Check events fired
    listener.assertJUnitEndCount(1);
    listener.assertNonTestCaseCount(1);
    listener.assertJUnitSuiteStartedCount(0);
    listener.assertJUnitTestStartedCount(0);
    listener.assertJUnitTestEndedCount(0);
    
    _model.setResetAfterCompile(true);
  }
  
  /**
   * Tests that the ui is notified to put up an error dialog if JUnit
   * is run on a non-public TestCase.
   */
  public void testResultOfNonPublicTestCase() throws Exception {
    if (printMessages) System.out.println("----testResultOfNonPublicTestCase-----");
    _model.setResetAfterCompile(false);
    
    final OpenDefinitionsDocument doc = setupDocument(NONPUBLIC_TEXT);
    final File file = new File(_tempDir, "NonPublic.java");
    doc.saveFile(new FileSelector(file));
    
    TestShouldSucceedListener listener = new TestShouldSucceedListener();
    
    _model.addListener(listener);
    
    if (printMessages) System.out.println("before compile");
    //synchronized(listener) {
      doc.startCompile();
      //listener.wait();
    //}
    if (printMessages) System.out.println("after compile");
    
    synchronized(listener) {
      doc.startJUnit();
      listener.assertJUnitStartCount(1);
      if (printMessages) System.out.println("waiting for test");
      listener.wait();
    }
    if (printMessages) System.out.println("after test");
    
    //System.err.println(testResults.toString());
    
    // Check events fired
    listener.assertJUnitEndCount(1);
   
    assertEquals("test case has one error reported",
                 1,
                 doc.getJUnitErrorModel().getNumErrors());
                 
    _model.setResetAfterCompile(true);
  }
  
  public void testDoNotRunJUnitIfFileHasBeenMoved() throws Exception {
    if (printMessages) System.out.println("----testDoNotRunJUnitIfFileHasBeenMoved-----");
    _model.setResetAfterCompile(false);
    
    final OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.java");
    doc.saveFile(new FileSelector(file));
    
    TestListener listener = new TestListener();
    
    _model.addListener(listener);
    file.delete();
    try {
      doc.startJUnit();
      fail("JUnit should not have started.");
    }
    catch (FileMovedException fme) {
      //JUnit should not have started, because the documents file is not
      // where it should be on the disk.
    }
    
    _model.setResetAfterCompile(true);
  }
  
  /**
   * Tests a document that has no corresponding class file.
   */
  public void testNoClassFile() throws Exception {
    if (printMessages) System.out.println("----testNoClassFile-----");
    _model.setResetAfterCompile(false);
    
    final OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.java");
    doc.saveFile(new FileSelector(file));
    
    TestShouldSucceedListener listener = new TestShouldSucceedListener() {
      public void nonTestCase() {
        nonTestCaseCount++;
      }
    };
    _model.addListener(listener);
    synchronized(listener) {
      doc.startJUnit();
      if (printMessages) System.out.println("waiting for test");
      listener.wait();
    }
    if (printMessages) System.out.println("after test");
    listener.assertNonTestCaseCount(1);
    listener.assertJUnitStartCount(1);
    listener.assertJUnitEndCount(1);
    listener.assertJUnitSuiteStartedCount(0);
    listener.assertJUnitTestStartedCount(0);
    listener.assertJUnitTestEndedCount(0);
    
    _model.setResetAfterCompile(true);
  }
  
  /**
   * Tests that an infinite loop in a test case can be aborted by clicking
   * the Reset button.
   */
  public void testInfiniteLoop() throws Exception {
    if (printMessages) System.out.println("----testInfiniteLoop-----");
    _model.setResetAfterCompile(false);
    
    final OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_INFINITE_TEXT);
    final File file = new File(_tempDir, "MonkeyTestInfinite.java");
    doc.saveFile(new FileSelector(file));
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    TestListener listener2 = new TestListener() {
      public void junitStarted(OpenDefinitionsDocument odd) {
        assertEquals("Documents don't match", doc, odd);
        junitStartCount++;
      }
      
      public void junitSuiteStarted(int numTests) {
        assertEquals("should run 1 test", 1, numTests);
        junitSuiteStartedCount++;
        // kill the infinite test once the tests have started
        _model.resetInteractions();
      }
      
      public void junitTestStarted(OpenDefinitionsDocument doc, String name) {
        assertEquals("running wrong test", "testInfinite", name);
        junitTestStartedCount++;
      }
      
      public void junitEnded() {
        synchronized(this) {
          assertInteractionsResetCount(1);
          junitEndCount++;
          notify();
        }
      }
      
      public void interactionsResetting() {
        assertInteractionsResetCount(0);
        interactionsResettingCount++;
      }
      
      public void interactionsReset() {
        assertInteractionsResettingCount(1);
        assertJUnitEndCount(0);
        interactionsResetCount++;
      }
    };
    _model.addListener(listener);
    if (printMessages) System.out.println("before compile");
    //synchronized(listener) {
      doc.startCompile();
      if (_model.getNumErrors() > 0) {
        fail("compile failed: " + doc.getCompilerErrorModel());
      }
      //listener.wait();
    //}
    if (printMessages) System.out.println("after compile");
    _model.removeListener(listener);
    _model.addListener(listener2);
    synchronized(listener2) {
      doc.startJUnit();
      listener2.assertJUnitStartCount(1);
      if (printMessages) System.out.println("waiting for test");
      listener2.wait();
    }
    if (printMessages) System.out.println("after test");
    _model.removeListener(listener2);
    listener2.assertJUnitEndCount(1);
    
    _model.setResetAfterCompile(true);
  }  
  
  /**
   * Tests that when a JUnit file with no errors, after being saved and compiled,
   * has it's contents replaced by a test that should fail, will pass all tests.
   */
  public void testUnsavedAndUnCompiledChanges() throws Exception {
    if (printMessages) System.out.println("----testUnsavedAndUnCompiledChanges-----");
    _model.setResetAfterCompile(false);
    
    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.java");
    doc.saveFile(new FileSelector(file));
    TestShouldSucceedListener listener = new TestShouldSucceedListener(true);
    _model.addListener(listener);
    if (printMessages) System.out.println("before compile");
    //synchronized(listener) {
      doc.startCompile();
      //listener.wait();
    //}
    if (printMessages) System.out.println("after compile");
    changeDocumentText(MONKEYTEST_FAIL_TEXT, doc);
    synchronized(listener) {
      doc.startJUnit();
      if (printMessages) System.out.println("waiting for test");
      listener.wait();
    }
    if (printMessages) System.out.println("after test");
    _model.removeListener(listener);
    
    assertEquals("test case should have no errors reported after modifying",
                 0,
                 doc.getJUnitErrorModel().getNumErrors());
    doc.saveFile(new FileSelector(file));
    
    listener = new TestShouldSucceedListener();
    _model.addListener(listener);
    synchronized(listener) {
      doc.startJUnit();
      listener.wait();
    }
    
    assertEquals("test case should have no errors reported after saving",
                 0,
                 doc.getJUnitErrorModel().getNumErrors());
    
    _model.setResetAfterCompile(false);
  }
  
  protected class TestShouldSucceedListener extends CompileShouldSucceedListener {
    public TestShouldSucceedListener() {
      super(false);  // don't reset interactions after compile by default
    }
    public TestShouldSucceedListener(boolean shouldResetAfterCompile) {
      super(shouldResetAfterCompile);
    }
    public void junitStarted(OpenDefinitionsDocument odd) {
      junitStartCount++;
    }
    public void junitSuiteStarted(int numTests) {
      assertJUnitStartCount(1);
      junitSuiteStartedCount++;
    }
    public void junitTestStarted(OpenDefinitionsDocument doc, String name) {
      junitTestStartedCount++;
    }
    public void junitTestEnded(OpenDefinitionsDocument doc, String name,
                               boolean wasSuccessful, boolean causedError) {
      junitTestEndedCount++;
      assertTrue("junitTestEndedCount should be same as junitTestStartedCount",
                 junitTestStartedCount == junitTestEndedCount);
    }
    public void junitEnded() {
      synchronized(this) {
        //assertJUnitSuiteStartedCount(1);
        if (printMessages) System.out.println("junitEnded event!");
        junitEndCount++;
        notify();
      }
    }
  }
    
}

