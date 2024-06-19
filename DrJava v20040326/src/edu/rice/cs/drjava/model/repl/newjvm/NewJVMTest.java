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

package edu.rice.cs.drjava.model.repl.newjvm;

import junit.framework.*;
import junit.extensions.*;

import java.rmi.*;

import edu.rice.cs.drjava.model.*;

/**
 * Tests the functionality of the new JVM manager.
 *
 * @version $Id: NewJVMTest.java,v 1.1 2005/08/05 12:45:06 guehene Exp $
 */
public final class NewJVMTest extends TestCase {
  final boolean printMessages = false;
  
  private static TestJVMExtension _jvm;

  public NewJVMTest(String name) {
    super(name);
  }

  protected void setUp() throws RemoteException {
    _jvm.resetFlags();
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(NewJVMTest.class);
    TestSetup setup = new TestSetup(suite) {
      protected void setUp() throws RemoteException {
        _jvm = new TestJVMExtension();
      }

      protected void tearDown() {
        _jvm.killInterpreter(false);
      }
    };

    return setup;
  }
  
  
  public void testPrintln() throws Throwable {
    if (printMessages) System.out.println("----testPrintln-----");
    synchronized(_jvm) {
      _jvm.interpret("System.err.print(\"err\");");
      _jvm.wait(); // wait for println
//      _jvm.wait(); // wait for void return
      assertEquals("system err buffer", "err", _jvm.errBuf);
      assertEquals("void return flag", true, _jvm.voidReturnFlag);
      _jvm.resetFlags();
    }

    synchronized(_jvm) {
      _jvm.interpret("System.err.print(\"err2\");");
      _jvm.wait(); // wait for println
//      _jvm.wait(); // wait for void return
      assertEquals("system err buffer", "err2", _jvm.errBuf);
      assertEquals("void return flag", true, _jvm.voidReturnFlag);
      _jvm.resetFlags();
    }

    synchronized(_jvm) {
      _jvm.interpret("System.out.print(\"out\");");
      _jvm.wait(); // wait for println
//      _jvm.wait(); // wait for void return
      assertEquals("system out buffer", "out", _jvm.outBuf);
      assertEquals("void return flag", true, _jvm.voidReturnFlag);
    }
  }

  public void testReturnConstant() throws Throwable {
    if (printMessages) System.out.println("----testReturnConstant-----");
    synchronized(_jvm) {
      _jvm.interpret("5");
      _jvm.wait();
      assertEquals("result", "5", _jvm.returnBuf);
    }
  }

  public void testWorksAfterRestartConstant() throws Throwable {
    if (printMessages) System.out.println("----testWorksAfterRestartConstant-----");
    
    // Check that a constant is returned
    synchronized(_jvm) {
      _jvm.interpret("5");
      _jvm.wait();
      assertEquals("result", "5", _jvm.returnBuf);
    }
    
    // Now restart interpreter
    synchronized(_jvm) {
      _jvm.killInterpreter(true);  // true: start back up
      _jvm.wait();
    }
    
    // Now evaluate another constant
    synchronized(_jvm) {
      _jvm.interpret("4");
      _jvm.wait();
      assertEquals("result", "4", _jvm.returnBuf);
    }
  }
  
  
  public void testThrowRuntimeException() throws Throwable {
    if (printMessages) System.out.println("----testThrowRuntimeException-----");
    synchronized(_jvm) {
      _jvm.interpret("throw new RuntimeException();");
      _jvm.wait();
      assertEquals("exception class",
                   "java.lang.RuntimeException",
                   _jvm.exceptionClassBuf);
    }
  }
  
  public void testToStringThrowsRuntimeException() throws Throwable {
    if (printMessages) System.out.println("----testToStringThrowsRuntimeException-----");
    synchronized(_jvm) {
      _jvm.interpret(
        "class A { public String toString() { throw new RuntimeException(); } };" +
        "new A()");
      _jvm.wait();
      assertTrue("exception should have been thrown by toString",
                 _jvm.exceptionClassBuf != null);
    }
  }

  public void testThrowNPE() throws Throwable {
    if (printMessages) System.out.println("----testThrowNPE-----");
    synchronized(_jvm) {
      _jvm.interpret("throw new NullPointerException();");

      while (_jvm.exceptionClassBuf == null) {
        _jvm.wait();
      }
      
      assertEquals("exception class",
                   "java.lang.NullPointerException",
                   _jvm.exceptionClassBuf);
    }
  }

  public void testStackTraceEmptyTrace() throws Throwable {
    if (printMessages) System.out.println("----testStackTraceEmptyTrace-----");
    synchronized(_jvm) {
      _jvm.interpret("null.toString()");

      while (_jvm.exceptionClassBuf == null) {
        _jvm.wait();
      }

      assertEquals("exception class",
                   "java.lang.NullPointerException",
                   _jvm.exceptionClassBuf);
      assertEquals("stack trace",
                   InterpreterJVM.EMPTY_TRACE_TEXT.trim(),
                   _jvm.exceptionTraceBuf.trim());
    }
  }
  
  
  /**
   * Ensure that switching to a non-existant interpreter throws an Exception.
   */
  public void testSwitchToNonExistantInterpreter() {
    try {
      _jvm.setActiveInterpreter("monkey");
      fail("Should have thrown an exception!");
    }
    catch (IllegalArgumentException e) {
      // good, that's what should happen
    }
  }
  
  /**
   * Ensure that MainJVM can correctly switch the active interpreter used by
   * the interpreter JVM.
   */
  public void testSwitchActiveInterpreter() throws InterruptedException {
    synchronized(_jvm) {
      _jvm.interpret("x = 6;");
      _jvm.wait();
    }
    _jvm.addJavaInterpreter("monkey");
    
    // x should be defined in active interpreter
    synchronized(_jvm) {
      _jvm.interpret("x");
      _jvm.wait();
      assertEquals("result", "6", _jvm.returnBuf);
    }
    
    // switch interpreter
    _jvm.setActiveInterpreter("monkey");
    synchronized(_jvm) {
      _jvm.interpret("x");
      _jvm.wait();
      assertTrue("exception was thrown",
                 !_jvm.exceptionClassBuf.equals(""));
    }
    
    // define x to 3 and switch back
    synchronized(_jvm) {
      _jvm.interpret("x = 3;");
      _jvm.wait();
    }
    _jvm.setToDefaultInterpreter();
    
    // x should have its old value
    synchronized(_jvm) {
      _jvm.interpret("x");
      _jvm.wait();
      assertEquals("result", "6", _jvm.returnBuf);
    }
    
    // test syntax error handling
    //  (temporarily disabled until bug 750605 fixed)
//     synchronized(_jvm) {
//       _jvm.interpret("x+");
//       _jvm.wait();
//       assertTrue("syntax error was reported",
//                  ! _jvm.syntaxErrorMsgBuf.equals("") );
//     }
    
  }

  private static class TestJVMExtension extends MainJVM {
    public String outBuf;
    public String errBuf;
    public String returnBuf;
    public String exceptionClassBuf;
    public String exceptionMsgBuf;
    public String exceptionTraceBuf;
    public String syntaxErrorMsgBuf;
    public int syntaxErrorStartRow;
    public int syntaxErrorStartCol;
    public int syntaxErrorEndRow;
    public int syntaxErrorEndCol;
    public boolean voidReturnFlag;
    
    private InterpretResultVisitor<Object> _testHandler;

    public TestJVMExtension() throws RemoteException { 
      super();
      _testHandler = new TestResultHandler();
      startInterpreterJVM();
      ensureInterpreterConnected();
    }
    
    protected InterpretResultVisitor<Object> getResultHandler() {
      return _testHandler;
    }

    public void resetFlags() {
      outBuf = null;
      errBuf = null;
      returnBuf = null;
      exceptionClassBuf = null;
      exceptionMsgBuf = null;
      exceptionTraceBuf = null;
      voidReturnFlag = false;
      syntaxErrorMsgBuf = null;
      syntaxErrorStartRow = 0;
      syntaxErrorStartCol = 0;
      syntaxErrorEndRow = 0;
      syntaxErrorEndCol = 0;      
    }
    
    protected void handleSlaveQuit(int status) {
      synchronized(this) {
        this.notify();
        super.handleSlaveQuit(status);
      }
    }

    public void systemErrPrint(String s) throws RemoteException {
      synchronized(this) {
        //System.out.println("notify err: " + s);
        errBuf = s;
//        this.notify();
      }
    }

    public void systemOutPrint(String s) throws RemoteException {
      synchronized(this) {
        //System.out.println("notify out: " + s);
        outBuf = s;
//        this.notify();
      }
    }

    private class TestResultHandler implements InterpretResultVisitor<Object> {
      public Object forVoidResult(VoidResult that) {
        synchronized(TestJVMExtension.this) {
          voidReturnFlag = true;
          //System.out.println("notify void");
          TestJVMExtension.this.notify();
          return null;
        }
      }
      public Object forValueResult(ValueResult that) {
        synchronized(TestJVMExtension.this) {
          returnBuf = that.getValueStr();
          //System.out.println("notify returned");
          TestJVMExtension.this.notify();
          return null;
        }
      }
      public Object forExceptionResult(ExceptionResult that) {
        synchronized(TestJVMExtension.this) {
          exceptionClassBuf = that.getExceptionClass();
          exceptionTraceBuf = that.getStackTrace();
          exceptionMsgBuf = that.getExceptionMessage();
          
          //System.out.println("notify threw");
          TestJVMExtension.this.notify();
          return null;
        }
      }
      
      public Object forSyntaxErrorResult(SyntaxErrorResult that) {
        synchronized(TestJVMExtension.this) {
          syntaxErrorMsgBuf = that.getErrorMessage();
          syntaxErrorStartRow = that.getStartRow();   
          syntaxErrorStartCol = that.getStartCol();  
          syntaxErrorEndRow = that.getEndRow();            
          syntaxErrorEndCol = that.getEndCol();  
          //System.out.println("notify threw");
          TestJVMExtension.this.notify();
          return null;
        }
      }
      
    }
  }
}
