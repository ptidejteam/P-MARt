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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.OptionEvent;

import  junit.framework.*;
import  java.util.Vector;
import  junit.extensions.*;
import  javax.swing.text.BadLocationException;
import  java.lang.RuntimeException;
import  java.io.File;


/**
 * Tests the functionality of the repl interpreter.
 * @version $Id: JavaInterpreterTest.java,v 1.1 2005/08/05 12:45:09 guehene Exp $
 */
public final class JavaInterpreterTest extends TestCase {
  private JavaInterpreter _interpreter;
  static public boolean testValue;

  /**
   * Constructs a new JavaInterpreterTest.
   * @param String name
   */
  public JavaInterpreterTest(String name) {
    super(name);
    testValue = false;
  }

  /**
   * The setup method run before each test.
   */
  protected void setUp() {
    _interpreter = new DynamicJavaAdapter();
    testValue = false;
  }

  /**
   * Creates a TestSuite of tests to be run.
   * @return the new TestSuite
   */
  public static Test suite() {
    return  new TestSuite(JavaInterpreterTest.class);
  }

  /**
   * Asserts that the results of interpreting the first of each
   * Pair is equal to the second.
   * @param cases an array of Pairs
   */
  private void tester(Pair[] cases) throws ExceptionReturnedException {
    for (int i = 0; i < cases.length; i++) {
      Object out = _interpreter.interpret(cases[i].first());
      assertEquals(cases[i].first() + " interpretation wrong!", cases[i].second(),
          out);
    }
  }

  /**
   * Make sure interpreting simple constants works.
   * Note that strings and characters are quoted.
   */
  public void testConstants() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
      Pair.make("5", new Integer(5)), Pair.make("1356", new Integer(1356)), Pair.make("true",
          Boolean.TRUE), Pair.make("false", Boolean.FALSE), Pair.make("\'c\'", "'" + new Character('c') + "'"),
          Pair.make("1.345", new Double(1.345)), Pair.make("\"buwahahahaha!\"",
          new String("\"buwahahahaha!\"")), Pair.make("\"yah\\\"eh\\\"\"", new String("\"yah\"eh\"\"")),
          Pair.make("'\\''", "'" + new Character('\'') + "'"),
    };
    tester(cases);
  }

  /** Test simple operations with Booleans */
  public void testBooleanOps() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
      //and
      Pair.make("true && false", Boolean.FALSE), Pair.make("true && true",
          Boolean.TRUE),
      //or
      Pair.make("true || true", Boolean.TRUE), Pair.make("false || true", Boolean.TRUE),
          Pair.make("false || false", Boolean.FALSE),
      // not
      Pair.make("!true", Boolean.FALSE), Pair.make("!false", Boolean.TRUE),
          //equals
      Pair.make("true == true", Boolean.TRUE), Pair.make("false == true", Boolean.FALSE),
          Pair.make("false == false", Boolean.TRUE),
      // xor
      Pair.make("false ^ false", new Boolean(false ^ false)), Pair.make("false ^ true ",
          new Boolean(false ^ true))
    };
    tester(cases);
  }

  /** Tests short circuiting */
  public void testShortCircuit() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
      Pair.make("false && (3 == 1/0)", Boolean.FALSE),
        Pair.make("true || (1/0 != 43)", Boolean.TRUE)
    };
    tester(cases);
  }

  /**
   * put your documentation comment here
   */
  public void testIntegerOps() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
      // plus
      Pair.make("5+6", new Integer(5 + 6)),
      // minus
      Pair.make("6-5", new Integer(6 - 5)),
      // times
      Pair.make("6*5", new Integer(6*5)),
      // divide
      Pair.make("6/5", new Integer(6/5)),
      // modulo
      Pair.make("6%5", new Integer(6%5)),
      // bit and
      Pair.make("6&5", new Integer(6 & 5)),
      // bit or
      Pair.make("6 | 5", new Integer(6 | 5)),
      // bit xor
      Pair.make("6^5", new Integer(6 ^ 5)),
      // bit complement
      Pair.make("~6", new Integer(~6)),
      // unary plus
      Pair.make("+5", new Integer(+5)),
      // unary minus
      Pair.make("-5", new Integer(-5)),
      // left shift
      Pair.make("400 << 5", new Integer(400 << 5)),
      // right shift
      Pair.make("400 >> 5", new Integer(400 >> 5)),
      // unsigned right shift
      Pair.make("400 >>> 5", new Integer(400 >>> 5)),
      // less than
      Pair.make("5 < 4", new Boolean(5 < 4)),
      // less than or equal to
      Pair.make("4 <= 4", new Boolean(4 <= 4)), Pair.make("4 <= 5", new Boolean(4 <= 5)),
          // greater than
      Pair.make("5 > 4", new Boolean(5 > 4)), Pair.make("5 > 5", new Boolean(5 > 5)),
          // greater than or equal to
      Pair.make("5 >= 4", new Boolean(5 >= 4)), Pair.make("5 >= 5", new Boolean(5 >= 5)),
          // equal to
      Pair.make("5 == 5", new Boolean(5 == 5)), Pair.make("5 == 6", new Boolean(
          5 == 6)),
      // not equal to
      Pair.make("5 != 6", new Boolean(5 != 6)), Pair.make("5 != 5", new Boolean(
          5 != 5))
    };
    tester(cases);
  }

  /**
   * put your documentation comment here
   */
  public void testDoubleOps() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
      // less than
      Pair.make("5.6 < 6.7", new Boolean(5.6 < 6.7)),
      // less than or equal to
      Pair.make("5.6 <= 5.6", new Boolean(5.6 <= 5.6)),
      // greater than
      Pair.make("5.6 > 4.5", new Boolean(5.6 > 4.5)),
      // greater than or equal to
      Pair.make("5.6 >= 56.4", new Boolean(5.6 >= 56.4)),
      // equal to
      Pair.make("5.4 == 5.4", new Boolean(5 == 5)),
      // not equal to
      Pair.make("5.5 != 5.5", new Boolean(5 != 5)),
      // unary plus
      Pair.make("+5.6", new Double(+5.6)),
      // unary minus
      Pair.make("-5.6", new Double(-5.6)),
      // times
      Pair.make("5.6 * 4.5", new Double(5.6*4.5)),
      // divide
      Pair.make("5.6 / 3.4", new Double(5.6/3.4)),
      // modulo
      Pair.make("5.6 % 3.4", new Double(5.6%3.4)),
      // plus
      Pair.make("5.6 + 6.7", new Double(5.6 + 6.7)),
      // minus
      Pair.make("4.5 - 3.4", new Double(4.5 - 3.4)),
    };
    tester(cases);
  }

  /**
   * put your documentation comment here
   */
  public void testStringOps() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
      // concatenation
      Pair.make("\"yeah\" + \"and\"", new String("\"yeah" + "and\"")),
      // equals
      Pair.make("\"yeah\".equals(\"yeah\")", new Boolean("yeah".equals("yeah"))),

    };
    tester(cases);
  }

  /**
   * put your documentation comment here
   */
  public void testCharacterOps()  throws ExceptionReturnedException{
    Pair[] cases = new Pair[] {
      // equals
      Pair.make("'c' == 'c'", new Boolean('c' == 'c'))
    };
    tester(cases);
  }

  /**
   * Tests that String and character declarations do not return
   * a result, while the variables themselves return a quoted result.
   */
  public void testSemicolon() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
      Pair.make("'c' == 'c'", new Boolean('c' == 'c')),
      Pair.make("'c' == 'c';", JavaInterpreter.NO_RESULT),
      Pair.make("String s = \"hello\"", JavaInterpreter.NO_RESULT),
      Pair.make("String x = \"hello\";", JavaInterpreter.NO_RESULT),
      Pair.make("char c = 'c'", JavaInterpreter.NO_RESULT),
      Pair.make("Character d = new Character('d')", JavaInterpreter.NO_RESULT),
      Pair.make("s", "\"hello\""), Pair.make("s;", JavaInterpreter.NO_RESULT),
      Pair.make("x", "\"hello\""), Pair.make("x;", JavaInterpreter.NO_RESULT),
      Pair.make("c", "'c'"), Pair.make("d", "'d'")
    };
    tester(cases);
  }

  /**
   * Tests that null can be used in instanceof expressions.
   */
  public void testNullInstanceOf() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
      Pair.make("null instanceof Object", new Boolean(null instanceof Object)),
      Pair.make("null instanceof String", new Boolean(null instanceof String))
    };
    tester(cases);
  }

  /**
   * Tests simple variable definitions which broke the initial implementation
   * of variable redefinition (tested by testVariableRedefinition).
   */
  public void testVariableDefinition() throws ExceptionReturnedException {
    _interpreter.interpret("int a = 5;");
    _interpreter.interpret("int b = a;");

    _interpreter.interpret("int c = a++;");
  }
  
  /** 
   * Tests that variables are assigned default values.
   */
  public void testVariableDefaultValues() throws ExceptionReturnedException {
    _interpreter.interpret("byte b");
    _interpreter.interpret("short s");
    _interpreter.interpret("int i");
    _interpreter.interpret("long l");
    _interpreter.interpret("float f");
    _interpreter.interpret("double d");
    _interpreter.interpret("char c");
    _interpreter.interpret("boolean bool");
    _interpreter.interpret("String str");
    Pair[] cases = new Pair[] {
      Pair.make("b", new Byte((byte)0)),
      Pair.make("s", new Short((short)0)),
      Pair.make("i", new Integer(0)),
      Pair.make("l", new Long(0L)),
      Pair.make("f", new Float(0.0f)),
      Pair.make("d", new Double(0.0d)),
      Pair.make("c", "'" + new Character('\u0000') + "'"), // quotes are added around chars
      Pair.make("bool", new Boolean(false)),        
      Pair.make("str", null)
    };
    tester(cases);
  }

  /**
   * Tests that variable declarations with errors will not allow the interpreter
   * to not define the variable. This will get rid of annoying "Error:
   * Redefinition of 'variable'" messages after fixing the error. Note that if
   * the error occurs during the evaluation of the right hand side then the
   * variable is defined. This is for two reasons: The compiler would have
   * accepted this variable declaration so that no more variables could have
   * been defined with the same name afterwards, and we don't know how to make
   * sure the evaluation doesn't return errors without actually evaluating which
   * may have side-effects.
   */
  public void testVariableRedefinition() {
    // test error in NameVisitor
    try {
      _interpreter.interpret("String s = abc;");
      fail("variable definition should have failed");
    }
    catch (ExceptionReturnedException e) {
      // Correct; it should fail
    }
    // test error in TypeChecker
    try {
      _interpreter.interpret("Vector v = new Vector();");
      fail("variable definition should have failed");
    }
    catch (ExceptionReturnedException e) {
      // Correct; it should fail
    }
    try {
      _interpreter.interpret("File f;");
      fail("variable definition should have failed");
    }
    catch (ExceptionReturnedException e) {
      // Correct; it should fail
    }
    try {
      // make sure we can redefine
      _interpreter.interpret("import java.util.Vector;");
      _interpreter.interpret("Vector v = new Vector();");
      _interpreter.interpret("String s = \"abc\";");
      _interpreter.interpret("import java.io.File;");
      _interpreter.interpret("File f = new File(\"\");");
    }
    catch (ExceptionReturnedException e) {
      fail("These interpret statements shouldn't cause errors");
    }
    // test error in EvaluationVisitor

    // Integer.getInteger("somebadproperty") should be null
    try {
      _interpreter.interpret("String z = new String(Integer.getInteger(\"somebadproperty\").toString());");
      fail("variable definition should have failed");
    }
    catch (ExceptionReturnedException e) {
    }
    try {
      _interpreter.interpret("String z = \"z\";");
      fail("variable redefinition should have failed");
    }
    catch (ExceptionReturnedException e) {
    }
  }

  /**
   * Ensure that the interpreter rejects assignments where the right type
   * is not a subclass of the left type.
   */
  public void testIncompatibleAssignment() throws ExceptionReturnedException {
    try {
      _interpreter.interpret("Integer i = new Object()");
      fail("incompatible assignment should have failed");
    }
    catch (ExceptionReturnedException e) {
      // Correct; it should fail
    }
    try {
      _interpreter.interpret("Integer i2 = (Integer)new Object();");
      fail("incompatible assignment should have failed");
    }
    catch (ExceptionReturnedException e) {
      // Correct; it should fail
    }

    // Check that a correct assignment doesn't fail
    _interpreter.interpret("Object o = new Integer(3)");
  }

 /**
  * Tests the operation of the TypeCheckerExtension by performing the
  * operations ((false) ? 2/0 : 1) and ((false) ? 2%0 : 1), which should
  * not throw Exceptions in the Java interpreter.
  */
  public void testTypeCheckerExtension() {
    try{
      _interpreter.interpret("(false) ? 2/0 : 1 ");
    }
    catch(ExceptionReturnedException e){
      if( e.getContainedException() instanceof ArithmeticException ){
        fail("testTypeCheckerExtension failed to prevent short circuit DivideByZeroException");
      }
    }

    try{
      _interpreter.interpret("(false) ? 2%0 : 1 ");
    }
    catch(ExceptionReturnedException e){
      if( e.getContainedException() instanceof ArithmeticException ){
        fail("testTypeCheckerExtension failed to prevent short circuit DivideByZeroException");
      }
    }
  }

  /**
  * Tests the operation of the EvaluationVisitorExtension by
  * Performing a computation with no results (interpreter
  * should return NO_RESULT and not null)
  */
  public void testEvaluationVisitorExtensionNO_RESULT() {
    try{
      Object out = _interpreter.interpret("true;");
      assertEquals("testEvaluationVisitorExtension", JavaInterpreter.NO_RESULT, out);
    }
    catch(ExceptionReturnedException e){
      fail("testEvaluationVisitorExtension Exception returned for none exceptional code!" + e);
    }
  }

  /**
   * Tests that a variable can be defined in the interpreter by an external source.
   */
  public void testDefineVariableExternally() throws ExceptionReturnedException {
    _interpreter.defineVariable("foo", new String("hello"));
    assertEquals("manipulated externally defined variable",
                 "\"ello\"", _interpreter.interpret("foo.substring(1,5)"));
    _interpreter.defineVariable("x", 3);
    assertEquals("externally defined variable x",
                 new Integer(3), _interpreter.interpret("x"));
    assertEquals("incremented externally defined variable x",
                 new Integer(4), _interpreter.interpret("++x"));
  }

  /**
   * Tests that the value of a variable can be queried externally.
   */
  public void testQueryVariableExternally() throws ExceptionReturnedException {
    _interpreter.defineVariable("x", 7);
    // Get value of variable externally
    assertEquals("external query for x",
                 new Integer(7), _interpreter.getVariable("x"));

    // Undefined variable
    try {
      _interpreter.getVariable("undefined");
      fail("Should have thrown IllegalStateException");
    }
    catch (IllegalStateException e) {
      // good, that's what we want
    }
  }

  /**
   * Tests that a constant can be defined in the interpreter by an external source.
   */
  public void testDefineConstantExternally() throws ExceptionReturnedException {
    _interpreter.defineConstant("y", 3);
    try {
      _interpreter.interpret("y = 4");
      fail("should not be able to assign to a constant");
    }
    catch (ExceptionReturnedException e) {
      // correct, it should fail
    }
  }
  
  /** 
   * Tests that arrays initializers are accepted.
   */
  public void testInitializeArrays() throws ExceptionReturnedException {    
    try {
      _interpreter.interpret("int i[] = new int[]{1,2,3};");
      _interpreter.interpret("int j[][] = new int[][]{{1}, {2,3}};");
      _interpreter.interpret("int k[][][][] = new int[][][][]{{{{1},{2,3}}}};");
    }
    catch(IllegalArgumentException iae) {
      fail("Legal array initializations were not accepted.");
    }
  }
  
  /**
   * Tests that the Interactions Pane will or won't allow access to private members
   * given the value of the ALLOW_PRIVATE_ACCESS configuration option.
   */
  public void testAllowPrivateAccess() throws ExceptionReturnedException {
    // The real option listener is in DefaultGlobalModel, so add one here.
    DrJava.getConfig().addOptionListener(OptionConstants.ALLOW_PRIVATE_ACCESS, new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oce) {
        _interpreter.setPrivateAccessible(oce.value.booleanValue());
      }
    });
    DrJava.getConfig().setSetting(OptionConstants.ALLOW_PRIVATE_ACCESS, new Boolean(false));
    try {
      _interpreter.interpret("class A { private int i = 0; }");
      _interpreter.interpret("new A().i");
      fail("Should not have access to the private field i inside class A.");
    }
    catch (ExceptionReturnedException ere) {
      assertTrue(ere.getContainedException() instanceof IllegalAccessException);
    }
    DrJava.getConfig().setSetting(OptionConstants.ALLOW_PRIVATE_ACCESS, new Boolean(true));
    assertEquals("Should be able to access private field i whose value should be 0", 
                 new Integer(0), 
                 _interpreter.interpret("new A().i"));
  }
  
  /**
   * Tests that declaring a void method in the Interactions Pane won't cause a bad type
   * exception. Tests bug #915906 "Methods in Interactions no longer work".
   */
  public void testDeclareVoidMethod() {
    try {
      _interpreter.interpret("void method() {}");
    }
    catch (ExceptionReturnedException ere) {
      fail("Should be able to declare void methods.");
    }
  }

  /**
   * Tests that a call to user-defined void method returns NO_RESULT, instead of null.
   * This test does not pass, it is currently broken.
   */
  public void testUserDefinedVoidMethod() throws ExceptionReturnedException {
     Object result = _interpreter.interpret("public void foo() {}; foo()");
     assertSame("Should have returned NO_RESULT.", Interpreter.NO_RESULT, result);
   }
}

/**
 * A structure to contain a String and an Object pair.
 *  This class is used to help test the JavaInterpreter.
 */
class Pair {
  private String _first;
  private Object _second;

  /**
   * put your documentation comment here
   * @param     String f
   * @param     Object s
   */
  Pair(String f, Object s) {
    this._first = f;
    this._second = s;
  }

  /**
   * put your documentation comment here
   * @param first
   * @param second
   * @return
   */
  public static Pair make(String first, Object second) {
    return  new Pair(first, second);
  }

  /**
   * put your documentation comment here
   * @return
   */
  public String first() {
    return  this._first;
  }

  /**
   * put your documentation comment here
   * @return
   */
  public Object second() {
    return  this._second;
  }
}



