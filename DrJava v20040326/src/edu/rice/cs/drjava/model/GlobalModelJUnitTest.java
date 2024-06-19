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

import junit.framework.*;

import java.io.*;

import java.util.List;
import java.util.LinkedList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;

import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.drjava.model.junit.*;
import edu.rice.cs.util.UnexpectedException;

/**
 * A test on the GlobalModel for JUnit testing.
 *
 * @version $Id: GlobalModelJUnitTest.java,v 1.1 2005/08/05 12:45:06 guehene Exp $
 */
public final class GlobalModelJUnitTest extends GlobalModelTestCase {
  static final boolean printMessages = false;

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

  private static final String MONKEYTEST_ERROR_TEXT =
    "import junit.framework.*; " +
    "public class MonkeyTestError extends TestCase { " +
    "  public MonkeyTestError(String name) { super(name); } " +
    "  public void testThrowsError() { " +
    "    throw new Error(\"This is an error.\"); " +
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

  private static final String HAS_MULTIPLE_TESTS_PASS_TEXT =
    "import junit.framework.*; " +
    "public class HasMultipleTestsPass extends TestCase { " +
    "  public HasMultipleTestsPass(String name) { super(name); } " +
    "  public void testShouldPass() { " +
    "    assertEquals(\"monkey\", \"monkey\"); " +
    "  } " +
    "  public void testShouldAlsoPass() { " +
    "    assertTrue(true); " +
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

    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.java");
    doc.saveFile(new FileSelector(file));
    JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    if (printMessages) System.out.println("before compile");
    doc.startCompile();
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
                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    _model.removeListener(listener);
  }

  /**
   * Tests that a JUnit file with an error is reported to have an error.
   */
  public void testOneJUnitError() throws Exception {
    if (printMessages) System.out.println("----testOneJUnitError-----");

    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_FAIL_TEXT);
    final File file = new File(_tempDir, "MonkeyTestFail.java");
    doc.saveFile(new FileSelector(file));
    JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    if (printMessages) System.out.println("before compile");
    doc.startCompile();
    if (printMessages) System.out.println("after compile");
    synchronized(listener) {
      doc.startJUnit();
      if (printMessages) System.out.println("waiting for test");
      listener.wait();
    }
    if (printMessages) System.out.println("after test");

    assertEquals("test case has one error reported",
                 1,
                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    _model.removeListener(listener);
  }

  /**
   * Tests that a test class which throws a *real* Error (not an Exception)
   * is handled correctly.
   */
  public void testRealError() throws Exception {
    if (printMessages) System.out.println("----testRealError-----");

    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_ERROR_TEXT);
    final File file = new File(_tempDir, "MonkeyTestError.java");
    doc.saveFile(new FileSelector(file));
    JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    if (printMessages) System.out.println("before compile");   
    doc.startCompile();
    if (printMessages) System.out.println("after compile");
    synchronized(listener) {
      doc.startJUnit();
      if (printMessages) System.out.println("waiting for test");
      listener.wait();
    }
    if (printMessages) System.out.println("after test");

    assertEquals("test case has one error reported",
                 1,
                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    listener.assertJUnitEndCount(1);
    _model.removeListener(listener);
  }

  /**
   * Tests that the ui is notified to put up an error dialog if JUnit
   * is run on a non-TestCase.
   */
  public void testNonTestCaseError() throws Exception {
    if (printMessages) System.out.println("----testNonTestCaseError-----");

    final OpenDefinitionsDocument doc = setupDocument(NON_TESTCASE_TEXT);
    final File file = new File(_tempDir, "NonTestCase.java");
    doc.saveFile(new FileSelector(file));

    JUnitTestListener listener = new JUnitNonTestListener();

    _model.addListener(listener);
    if (printMessages) System.out.println("before compile");
    doc.startCompile();
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
    _model.removeListener(listener);
  }

  /**
   * Tests that the ui is notified to put up an error dialog if JUnit
   * is run on a non-public TestCase.
   */
  public void testResultOfNonPublicTestCase() throws Exception {
    if (printMessages) System.out.println("----testResultOfNonPublicTestCase-----");

    final OpenDefinitionsDocument doc = setupDocument(NONPUBLIC_TEXT);
    final File file = new File(_tempDir, "NonPublic.java");
    doc.saveFile(new FileSelector(file));

    JUnitTestListener listener = new JUnitTestListener();

    _model.addListener(listener);

    if (printMessages) System.out.println("before compile");
    doc.startCompile();
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
                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    _model.removeListener(listener);
  }

  public void testDoNotRunJUnitIfFileHasBeenMoved() throws Exception {
    if (printMessages) System.out.println("----testDoNotRunJUnitIfFileHasBeenMoved-----");

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
    _model.removeListener(listener);
  }

  /**
   * Tests a document that has no corresponding class file.
   */
  public void testNoClassFile() throws Exception {
    if (printMessages) System.out.println("----testNoClassFile-----");

    final OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.java");
    doc.saveFile(new FileSelector(file));

    JUnitTestListener listener = new JUnitNonTestListener();
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
    _model.removeListener(listener);
  }

  /**
   * Tests that an infinite loop in a test case can be aborted by clicking
   * the Reset button.
   */
  public void testInfiniteLoop() throws Exception {
    if (printMessages) System.out.println("----testInfiniteLoop-----");

    final OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_INFINITE_TEXT);
    final File file = new File(_tempDir, "MonkeyTestInfinite.java");
    doc.saveFile(new FileSelector(file));

    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    TestListener listener2 = new TestListener() {
      public void junitStarted(List<OpenDefinitionsDocument> odds) {
        assertEquals("Documents don't match", doc, odds.get(0));
        junitStartCount++;
      }
      public void junitSuiteStarted(int numTests) {
        assertEquals("should run 1 test", 1, numTests);
        junitSuiteStartedCount++;
        // kill the infinite test once the tests have started
        _model.resetInteractions();
      }
      public void junitTestStarted(String name) {
        assertEquals("running wrong test", "testInfinite", name);
        junitTestStartedCount++;
      }
      public void junitEnded() {
        synchronized(this) {
          assertInterpreterReadyCount(1);
          junitEndCount++;
          notify();
        }
      }
      public void interpreterResetting() {
        assertInterpreterReadyCount(0);
        interpreterResettingCount++;
      }
      public void interpreterReady() {
        assertInterpreterResettingCount(1);
        assertJUnitEndCount(0);
        interpreterReadyCount++;
      }

      public void consoleReset() {
        consoleResetCount++;
      }
    };
    _model.addListener(listener);
    if (printMessages) System.out.println("before compile");
      doc.startCompile();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    listener.checkCompileOccurred();
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
  }

  /**
   * Tests that when a JUnit file with no errors, after being saved and compiled,
   * has it's contents replaced by a test that should fail, will pass all tests.
   */
  public void testUnsavedAndUnCompiledChanges() throws Exception {
    if (printMessages) System.out.println("----testUnsavedAndUnCompiledChanges-----");

    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.java");
    doc.saveFile(new FileSelector(file));
    JUnitTestListener listener = new JUnitTestListener(true);
    _model.addListener(listener);
    if (printMessages) System.out.println("before compile");
    doc.startCompile();
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
                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    doc.saveFile(new FileSelector(file));

    listener = new JUnitTestListener();
    _model.addListener(listener);
    synchronized(listener) {
      doc.startJUnit();
      listener.wait();
    }

    assertEquals("test case should have no errors reported after saving",
                 0,
                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    _model.removeListener(listener);
  }

  /**
   * Verifies that we get a nonTestCase event
   */
  public void testJUnitAllWithNoValidTests() throws Exception {
    JUnitNonTestListener listener = new JUnitNonTestListener(true);
    _model.addListener(listener);
    synchronized (listener) {
      _model.getJUnitModel().junitAll();
      listener.wait();
    }
    listener.assertNonTestCaseCount(1);
    listener.assertJUnitSuiteStartedCount(0);
    listener.assertJUnitTestStartedCount(0);
    listener.assertJUnitTestEndedCount(0);
    _model.removeListener(listener);

    OpenDefinitionsDocument doc = setupDocument(NON_TESTCASE_TEXT);
    listener = new JUnitNonTestListener(true);
    File file = new File(_tempDir, "NonTestCase.java");
    doc.saveFile(new FileSelector(file));
    doc.startCompile();
    doc = setupDocument(MONKEYTEST_PASS_TEXT);
    file = new File(_tempDir, "MonkeyTestPass.java");
    doc.saveFile(new FileSelector(file));
    _model.addListener(listener);
    synchronized (listener) {
      _model.getJUnitModel().junitAll();
      listener.wait();
    }
    listener.assertNonTestCaseCount(1);
    listener.assertJUnitSuiteStartedCount(0);
    listener.assertJUnitTestStartedCount(0);
    listener.assertJUnitTestEndedCount(0);
    _model.removeListener(listener);
  }

  /**
   * Tests that junit all works with one or two test cases that should pass.
   */
  public void testJUnitAllWithNoErrors() throws Exception {
    OpenDefinitionsDocument doc = setupDocument(NON_TESTCASE_TEXT);
    JUnitNonTestListener listener = new JUnitNonTestListener(true);
    File file = new File(_tempDir, "NonTestCase.java");
    doc.saveFile(new FileSelector(file));
    doc.startCompile();
    doc = setupDocument(MONKEYTEST_PASS_TEXT);
    file = new File(_tempDir, "MonkeyTestPass.java");
    doc.saveFile(new FileSelector(file));
    doc.startCompile();
    _model.addListener(listener);
    synchronized (listener) {
      _model.getJUnitModel().junitAll();
      listener.wait();
    }
    listener.assertNonTestCaseCount(0);
    listener.assertJUnitSuiteStartedCount(1);
    listener.assertJUnitTestStartedCount(1);
    listener.assertJUnitTestEndedCount(1);
    _model.removeListener(listener);

    listener = new JUnitNonTestListener(true);
    doc = setupDocument(HAS_MULTIPLE_TESTS_PASS_TEXT);
    file = new File(_tempDir, "HasMultipleTestsPass.java");
    doc.saveFile(new FileSelector(file));
    doc.startCompile();
    _model.addListener(listener);
    synchronized (listener) {
      _model.getJUnitModel().junitAll();
      listener.wait();
    }
    listener.assertNonTestCaseCount(0);
    listener.assertJUnitSuiteStartedCount(1);
    listener.assertJUnitTestStartedCount(3);
    listener.assertJUnitTestEndedCount(3);
    _model.removeListener(listener);
  }

  /**
   * Tests that junit all works with test cases that do not pass.
   */
  public void testJUnitAllWithErrors() throws Exception {
    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_ERROR_TEXT);
    JUnitNonTestListener listener = new JUnitNonTestListener(true);
    File file = new File(_tempDir, "MonkeyTestError.java");
    doc.saveFile(new FileSelector(file));
    doc.startCompile();
    doc = setupDocument(MONKEYTEST_FAIL_TEXT);
    file = new File(_tempDir, "MonkeyTestFail.java");
    doc.saveFile(new FileSelector(file));
    doc.startCompile();
    _model.addListener(listener);
    synchronized (listener) {
      _model.getJUnitModel().junitAll();
      listener.wait();
    }
    listener.assertNonTestCaseCount(0);
    listener.assertJUnitSuiteStartedCount(1);
    listener.assertJUnitTestStartedCount(2);
    listener.assertJUnitTestEndedCount(2);
    _model.removeListener(listener);

    JUnitErrorModel jem = _model.getJUnitModel().getJUnitErrorModel();
    assertEquals("test case has one error reported", 2, jem.getNumErrors());
    assertTrue("first error should be an error", jem.getError(0).isWarning());
    assertFalse("second error should be a failure", jem.getError(1).isWarning());
  }

  public static class JUnitNonTestListener extends JUnitTestListener {
    private boolean _shouldBeTestAll;
    public JUnitNonTestListener() {
      _shouldBeTestAll = false;
    }
    public JUnitNonTestListener(boolean shouldBeTestAll) {
      _shouldBeTestAll = shouldBeTestAll;
    }
    public void nonTestCase(boolean isTestAll) {
      assertEquals("Non test case heard the wrong value for test current/test all",
                   _shouldBeTestAll, isTestAll);
      nonTestCaseCount++;
    }
  }

  public static class JUnitTestListener extends CompileShouldSucceedListener {
    public JUnitTestListener() {
      super(false);  // don't reset interactions after compile by default
    }
    public JUnitTestListener(boolean shouldResetAfterCompile) {
      super(shouldResetAfterCompile);
    }
    public void junitStarted(List<OpenDefinitionsDocument> odds) {
      junitStartCount++;
    }
    public void junitSuiteStarted(int numTests) {
      assertJUnitStartCount(1);
      junitSuiteStartedCount++;
    }
    public void junitTestStarted(String name) {
      junitTestStartedCount++;
    }
    public void junitTestEnded(String name, boolean wasSuccessful, boolean causedError) {
      junitTestEndedCount++;
      assertEquals("junitTestEndedCount should be same as junitTestStartedCount",
                   junitTestEndedCount, junitTestStartedCount);
    }
    public synchronized void junitEnded() {
      //assertJUnitSuiteStartedCount(1);
      if (printMessages) {
        System.out.println("junitEnded event!");
      }
      junitEndCount++;
      notify();
    }
  }
}
