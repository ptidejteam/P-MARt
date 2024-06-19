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

package edu.rice.cs.drjava.model.repl.newjvm;

import java.util.*;
import java.io.*;
import java.rmi.server.*;
import java.rmi.*;
import java.net.MalformedURLException;

import edu.rice.cs.util.newjvm.*;
import edu.rice.cs.util.OutputStreamRedirector;
import edu.rice.cs.drjava.model.junit.JUnitTestManager;
import edu.rice.cs.drjava.model.junit.JUnitError;
import edu.rice.cs.drjava.model.repl.*;

// For Windows focus fix
import javax.swing.JDialog;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * This is the main class for the interpreter JVM.
 * Note that this class is specific to using DynamicJava. It would need
 * to be subclassed to use with another interpreter. (Really, there would
 * need to be an abstract base class, but since we don't need it yet I'm
 * not making one.)
 *
 * @version $Id: InterpreterJVM.java,v 1.2 2005/08/23 23:35:56 huynhduc Exp $
 */
public class InterpreterJVM extends AbstractSlaveJVM
                            implements InterpreterJVMRemoteI
{
  public static final String EMPTY_TRACE_TEXT = "";
  //public static final String EMPTY_TRACE_TEXT = "  at (the interactions window)";

  private MainJVMRemoteI _mainJVM;
  private JavaInterpreter _interpreter;
  private String _classpath;
  private JUnitTestManager _junitTestManager;

  public InterpreterJVM() {
    reset();
    _junitTestManager = new JUnitTestManager(this);
  }

  protected void handleStart(MasterRemote mainJVM) 
  {
    //_log.log("handleStart...");
    _mainJVM = (MainJVMRemoteI) mainJVM;

    // redirect stdout
    System.setOut(new PrintStream(new OutputStreamRedirector() {
      public void print(String s) {
        try {
          //_log.log("out.print: " + s);
          _mainJVM.systemOutPrint(s);
        }
        catch (RemoteException re) {
          // nothing to do
          _log.log(re.toString());
        }
      }
    }));

    // redirect stderr
    System.setErr(new PrintStream(new OutputStreamRedirector() {
      public void print(String s) {
        try {
          //_log.log("err.print: " + s);
          _mainJVM.systemErrPrint(s);
        }
        catch (RemoteException re) {
          // nothing to do
          _log.log(re.toString());
        }
      }
    }));
    
    // On Windows, any frame or dialog opened from Interactions pane will
    // appear *behind* DrJava's frame, unless a previous frame or dialog 
    // is shown here.  Not sure what the difference is, but this hack 
    // seems to work.  (I'd be happy to find a better solution, though.)
    // Only necessary on Windows, since frames and dialogs on other 
    // platforms appear correctly in front of DrJava.
    if (_isWindowsPlatform()) {
      JDialog d = new JDialog();
      d.setSize(0,0);
      d.show();
      d.hide();
    }
  }
  
  /**
   * Returns if the current platform is Windows.
   */
  private boolean _isWindowsPlatform() {
    String os = System.getProperty("os.name");
    if (os != null) {
      return os.toLowerCase().indexOf("windows") == 0;
    }
    else {
      return false;
    }
  }

  //public InterpretResult interpret(final String s) {
  public void interpret(final String s) {
    Thread thread = new Thread("interpret thread: " + s) {
      public void run() {
        try {
          try {
            _dialog("to interp: " + s);
            Object result = _interpreter.interpret(s);
            
            if (result == JavaInterpreter.NO_RESULT) {
              //return new VoidResult();
              _dialog("void interp ret: " + result);
              _mainJVM.interpretResult(new VoidResult());
            }
            else {
              // we use String.valueOf because it deals with result = null!
              _dialog("about to tell main result was " + result);
              //return new ValueResult(String.valueOf(result));
              _mainJVM.interpretResult(new ValueResult(String.valueOf(result)));
            }
          }
          catch (ExceptionReturnedException e) {
            Throwable t = e.getContainedException();
            _dialog("interp exception: " + t);
            
            //_dialog("before call to threwException");
            //return new ExceptionResult(t.getClass().getName(),
            //                           t.getMessage(),
            //                           getStackTrace(t));
            _mainJVM.interpretResult(new ExceptionResult(t.getClass().getName(),
                                                         t.getMessage(),
                                                         //getStackTrace(t));
                                                         t.getStackTrace().toString()));
          }
          catch (Throwable t) {
            // A user's toString method might throw anything, so we need to be careful
            //_dialog("thrown by toString: " + t);
            //return new ExceptionResult(t.getClass().getName(),
            //                           t.getMessage(),
            //                           getStackTrace(t));
            _mainJVM.interpretResult(new ExceptionResult(t.getClass().getName(),
                                                         t.getMessage(),
                                                         //getStackTrace(t));
                                                         t.getStackTrace().toString()));
          }
        }
        catch (RemoteException re) {
          // Can't communicate with MainJVM?  Nothing to do...
          _log.log(re.toString());
        }
      }
    };

    thread.setDaemon(true);
    thread.start();
  }

  private static final Log _log = new Log("IntJVM");
  private static void _dialog(String s) {
    //javax.swing.JOptionPane.showMessageDialog(null, s);
    _log.log(s);
  }

  /**
   * Gets the stack trace from the given exception, stripping off
   * the bottom parts of the trace that are internal to the interpreter.
   * This would be much easier to do in JDK 1.4, since you can get the
   * stack trace frames directly, instead of having to parse this!
   */
  protected static String getStackTrace(Throwable t) {
    StringWriter writer = new StringWriter();
    t.printStackTrace(new PrintWriter(writer));

    //_dialog("before creating reader");
    BufferedReader reader
      = new BufferedReader(new StringReader(writer.toString()));

    //_dialog("after creating reader");
    LinkedList traceItems = new LinkedList();
    try {
      // we will generate list of trace items
      // skip the first one since it's just the message
      //_dialog("before first readLine");
      reader.readLine();
      //_dialog("after first readLine");

      String s;
      while ((s = reader.readLine()) != null) {
        //_dialog("read: " + s);
        traceItems.add(s);
      }
    }
    catch (IOException ioe) {
      return "Unable to get stack trace";
    }

    // OK, now we crop off everything after the first "koala.dynamicjava." or
    //  "edu.rice.cs.drjava.", if there is one.
    
    //  First, find the index of an occurrence.
    int index = -1;
    for (int i=0; i < traceItems.size(); i++) {
      String item = (String) traceItems.get(i);
      item = item.trim();
      if (item.startsWith("at edu.rice.cs.drjava.") ||
          item.startsWith("at koala.dynamicjava."))
      {
        index = i;
        break;
      }
    }
    
    // Now crop off the rest
    if (index > -1) {
      while (traceItems.size() > index) {
        traceItems.removeLast();
      }
    }

    // Last check: See if there are no items left. If there are none,
    // put one in to say it happened at top-level.
    if (traceItems.isEmpty()) {
      traceItems.add(EMPTY_TRACE_TEXT);
    }
    

    // OK, now rebuild string
    StringBuffer buf = new StringBuffer();
    ListIterator itor = traceItems.listIterator();
    boolean first = true;
    while (itor.hasNext()) {
      if (first) {
        first = false;
      }
      else {
        buf.append("\n");
      }

      buf.append("  " + ((String) itor.next()).trim());
    }

    return buf.toString();
  }

  public void addClassPath(String s) {
    //_dialog("add classpath: " + s);
    _interpreter.addClassPath(s);
    _classpath += s;
    _classpath += System.getProperty("path.separator");
  }
  
  public String getClasspath() {
    return _classpath;
  }

  public void setPackageScope(String s) {
    _interpreter.setPackageScope(s);
  }
  
  /**
   * Runs a JUnit Test class in the Interpreter JVM.
   * @param className Name of the TestCase class
   * @param fileName Name of the file for the TestCase class
   */
  public void runTestSuite(String className, String fileName) throws RemoteException {
    _junitTestManager.runTest(className, fileName);
  }
  
  /**
   * Notifies the main JVM if JUnit is invoked on a non TestCase class.
   */
  public void nonTestCase() {
    try {
      _mainJVM.nonTestCase();
    }
    catch (RemoteException re) {
      // nothing to do
      _log.log(re.toString());
    }
  }
  
  /**
   * Notifies that a suite of tests has started running.
   * @param numTests The number of tests in the suite to be run.
   */
  public void testSuiteStarted(int numTests) {
    try {
      _mainJVM.testSuiteStarted(numTests);
    }
    catch (RemoteException re) {
      // nothing to do
      _log.log(re.toString());
    }
  }
  
  /**
   * Notifies that a particular test has started.
   * @param testName The name of the test being started.
   */
  public void testStarted(String testName) {
    try {
      _mainJVM.testStarted(testName);
    }
    catch (RemoteException re) {
      // nothing to do
      _log.log(re.toString());
    }
  }
  
  /**
   * Notifies that a particular test has ended.
   * @param testName The name of the test that has ended.
   * @param wasSuccessful Whether the test passed or not.
   * @param causedError If not successful, whether the test caused an error
   *  or simply failed.
   */
  public void testEnded(String testName, boolean wasSuccessful, boolean causedError) {
    try {
      _mainJVM.testEnded(testName, wasSuccessful, causedError);
    }
    catch (RemoteException re) {
      // nothing to do
      _log.log(re.toString());
    }
  }
  
  /**
   * Notifies that a full suite of tests has finished running.
   * @param errors The array of errors from all failed tests in the suite.
   */
  public void testSuiteFinished(JUnitError[] errors) {
    try {
      _mainJVM.testSuiteEnded(errors);
    }
    catch (RemoteException re) {
      // nothing to do
      _log.log(re.toString());
    }
  }

  public void reset() {
    _interpreter = new DynamicJavaAdapter();
    _classpath = "";
    
    // do an interpretation to get the interpreter loaded fully
    try {
      _interpreter.interpret("0");
    }
    catch (ExceptionReturnedException e) {
      throw new edu.rice.cs.util.UnexpectedException(e);
    }
  }
}
