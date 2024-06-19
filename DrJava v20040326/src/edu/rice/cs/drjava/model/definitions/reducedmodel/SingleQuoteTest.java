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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import  junit.framework.*;
import  java.util.Vector;
import  junit.extensions.*;


/**
 * Tests the interaction between quotes and backslashes.
 * @version $Id: SingleQuoteTest.java,v 1.1 2005/08/05 12:45:09 guehene Exp $
 */
public final class SingleQuoteTest extends BraceReductionTestCase 
  implements ReducedModelStates 
{

  protected ReducedModelControl model0;
  protected ReducedModelControl model1;
  protected ReducedModelControl model2;

  /**
   * Constructor.
   * @param name a name for the test.
   */
  public SingleQuoteTest(String name) {
    super(name);
  }

  /**
   * Initializes the reduced models used in the tests.
   */
  protected void setUp() {
    model0 = new ReducedModelControl();
    model1 = new ReducedModelControl();
    model2 = new ReducedModelControl();
  }

  /**
   * Creates a test suite for JUnit to use.
   * @return a test suite for JUnit
   */
  public static Test suite() {
    return  new TestSuite(SingleQuoteTest.class);
  }

  /**
   * Convenience function to insert a number of non-special characters into a reduced model.
   * @param model the model being modified
   * @param size the number of characters being inserted
   */
  protected void insertGap(BraceReduction model, int size) {
    for (int i = 0; i < size; i++) {
      model.insertChar(' ');
    }
  }

  /**
   * Tests the relationship between backslash characters and quote characters.
   * It focuses on the case where the backslash is inserted first before the quote.
   */
  public void testInsideQuotePrevious() {
    model1.insertChar('\'');
    model1.insertChar('\\');
    model1.insertChar('\'');
    model1.move(-2);
    // "#\"
    assertEquals("#0.0", "\\'", model1.currentToken().getType());
    assertEquals("#0.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model1));
    model1.move(2);
    model1.insertChar('\'');
    model1.move(-1);
    // '\'#'
    assertEquals("#1.0", "\'", model1.currentToken().getType());
    assertEquals("#1.1", FREE, stateOfCurrentToken(model1));
    assertTrue("#1.2", model1.currentToken().isClosed());
    model1.move(1);
    model1.insertChar('\'');     
    model1.insertChar('\\');
    model1.insertChar('\\');
    model1.move(-2);
    // '\'''#\\
    assertEquals("#2.0", "\\\\", model1.currentToken().getType());
    assertEquals("#2.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model1));
    model1.move(2);
    model1.insertChar('\\');
    model1.move(-1);
    // '\'''\\#\
    assertEquals("#3.0", "\\", model1.currentToken().getType());
    assertEquals("#3.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model1));
    model1.move(1);
    model1.insertChar('\'');
    model1.move(-1);
    // '\'''\\\#"
    assertEquals("#4.0", "\\'", model1.currentToken().getType());
    assertEquals("#4.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model1));
  }

  /**
   * Tests the relationship between backslashes and quotes.
   * Focuses on the case where a backslash is inserted and turns a regular quote
   * into an escaped quote.
   */
  public void testInsideQuoteNext() {
    model1.insertChar('\'');
    model1.insertChar('\'');
    model1.move(-1);
    model1.insertChar('\\');
    assertEquals("#0.0", "\\'", model1.currentToken().getType());
    assertEquals("#0.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model1));
    assertEquals("#0.2", 1, model1.getBlockOffset());
    model1.move(1);     
    model1.insertChar('\'');
    model1.move(-1);
    assertEquals("#1.0", "\'", model1.currentToken().getType());
    assertEquals("#1.1", FREE, stateOfCurrentToken(model1));
    assertTrue("#1.2", model1.currentToken().isClosed());
    model1.move(1);
    model1.insertChar('\'');     
    model1.insertChar('\\');
    model1.move(-1);
    model1.insertChar('\\');
    assertEquals("#2.0", "\\\\", model1.currentToken().getType());
    assertEquals("#2.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model1));
    assertEquals("#2.2", 6, model1.absOffset());
    model1.move(-2);
    model1.insertChar('{');
    model1.move(-1);
    assertEquals("#3.0", "{", model1.currentToken().getType());
    assertEquals("#3.1", FREE, stateOfCurrentToken(model1));
    model1.move(1);
    model1.move(3);
    model1.insertChar('\'');
    model1.move(-1);
    assertEquals("#4.0", "\'", model1.currentToken().getType());
    assertEquals("#4.1", FREE, stateOfCurrentToken(model1));
    assertTrue("#4.2", model1.currentToken().isClosed());
    model1.insertChar('\\');
    assertEquals("#5.0", "\\'", model1.currentToken().getType());
    assertEquals("#5.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model1));
    assertEquals("#5.2", 1, model1.getBlockOffset());
  }

  /**
   * Tests the case when a backslash is inserted before two backslashes.
   * The existing double escape is broken and the first two backslashes become
   * a double escape with the third backslash ending up alone.
   */
  public void testBackSlashBeforeDoubleEscape() {
    model1.insertChar('\\');
    model1.insertChar('\\');
    model1.move(-2);
    model1.insertChar('\\');
    assertEquals("#0.0", "\\\\", model1.currentToken().getType());
    assertEquals("#0.1", 2, model1.currentToken().getSize());
    model1.move(1);
    assertEquals("#0.2", "\\", model1.currentToken().getType());
    model2.insertChar('\\');
    model2.insertChar('\'');
    model2.move(-2);
    model2.insertChar('\\');
    assertEquals("#1.0", "\\\\", model2.currentToken().getType());
    assertEquals("#1.1", 1, model2.absOffset());
    model2.move(1);
    assertEquals("#1.2", "'", model2.currentToken().getType());
  }

  /**
   * Tests the case where a backslash breaks up two backslashes together.
   * The newly inserted backslash and the first backslash form a new double escape
   * and the second backslash in the previous double escape becomes free.
   */
  public void testInsertBetweenDoubleEscape() {
    model1.insertChar('\\');
    model1.insertChar('\\');
    model1.move(-1);
    model1.insertChar('\\');
    model1.move(-2);
    assertEquals("#0.0", "\\\\", model1.currentToken().getType());
    model1.move(2);
    assertEquals("#0.1", "\\", model1.currentToken().getType());
    model2.insertChar('\\');
    model2.insertChar('\'');
    model2.move(-1);
    model2.insertChar('\\');
    model2.move(-2);
    assertEquals("#1.0", "\\\\", model2.currentToken().getType());
    model2.move(2);
    assertEquals("#1.1", "\'", model2.currentToken().getType());
    model0.insertChar('\\');
    model0.insertChar('\\');
    model0.move(-1);
    model0.insertChar(')');
    model0.move(-2);
    assertEquals("#2.0", "\\", model0.currentToken().getType());
    model0.move(1);
    assertEquals("#2.1", ")", model0.currentToken().getType());
    model0.move(1);
    assertEquals("#2.2", "\\", model0.currentToken().getType());
    model0.move(1);
    model0.delete(-3);
    model0.insertChar('\\');
    model0.insertChar('\'');
    model0.move(-1);
    model0.insertChar(')');
    model0.move(-2);
    assertEquals("#3.0", "\\", model0.currentToken().getType());
    model0.move(1);
    assertEquals("#3.1", ")", model0.currentToken().getType());
    model0.move(1);
    assertEquals("#3.2", "'", model0.currentToken().getType());
  }

  /**
   * Tests the case where deletion combines a backslash and a quote or two backslashes.
   * The deletion of characters in between the two special characters brings them together
   * and unites them into a 2-character special token.
   */
  public void testDeleteAndCombine() {
    model0.insertChar('\\');
    insertGap(model0, 2);
    model0.insertChar('\'');
    model0.move(-1);
    assertEquals("#0.0", "\'", model0.currentToken().getType());
    model0.delete(-2);
    assertEquals("#1.0", "\\'", model0.currentToken().getType());
    assertEquals("#1.1", 1, model0.absOffset());
    model0.delete(1);
    insertGap(model0, 2);
    model0.insertChar('\\');
    model0.move(-1);
    assertEquals("#2.0", "\\", model0.currentToken().getType());
    model0.delete(-2);
    assertEquals("#3.0", "\\\\", model0.currentToken().getType());
    assertEquals("#3.1", 2, model0.currentToken().getSize());
  }

  /**
   * Tests more of the same sort of cases as found in testDeleteAndCombine().
   */
  public void testDeleteAndCombine2() {
    model0.insertChar('\\');
    model0.insertChar('\'');
    model0.move(-1);
    model0.delete(-1);
    assertEquals("#0.0", "\'", model0.currentToken().getType());
    assertEquals("#0.1", FREE, model0.getStateAtCurrent());
    model1.insertChar('\\');
    model1.insertChar('\\');
    model1.delete(-1);
    model1.move(-1);
    assertEquals("#1.0", "\\", model1.currentToken().getType());
    model1.move(1);
    model1.insertChar('\\');
    model1.move(-1);
    model1.delete(-1);
    assertEquals("#2.0", "\\", model1.currentToken().getType());
  }

  /**
   * More of the same sort of cases as found in testDeleteAndCombine().
   */
  public void testDeleteAndCombine3() {
    model0.insertChar('\\');
    model0.insertChar('\\');
    insertGap(model0, 3);
    model0.insertChar('\\');
    model0.move(-1);
    model0.delete(-4);
    assertEquals("#0.0", "\\\\", model0.currentToken().getType());
    assertEquals("#0.1", 1, model0.absOffset());
    model1.insertChar('\\');
    insertGap(model1, 3);
    model1.insertChar('\\');
    model1.insertChar('\'');
    model1.move(-1);
    model1.delete(-4);
    assertEquals("#1.0", "\\\'", model1.currentToken().getType());
    assertEquals("#1.1", 1, model1.absOffset());
  }

  /**
   * Tests cases where a long chain of backslashes and quotes can be all altered with a simple
   * insertion or deletion of a special character.
   */
  public void testChainEffect() {
    model0.insertChar('\'');
    model0.insertChar('\\');
    model0.insertChar('\'');
    model0.insertChar('\'');
    model0.insertChar('\'');
    model0.insertChar('\\');
    model0.insertChar('\'');
    model0.insertChar('\'');
    model0.insertChar('\'');
    model0.insertChar('\\');
    model0.insertChar('\'');
    model0.insertChar('\'');
    // '\'''\'''\''#
    model0.move(-1);
    assertEquals("#0.0", "\'", model0.currentToken().getType());
    assertTrue("#0.1", model0.currentToken().isClosed());
    model0.move(-2);
    // '\'''\'''#\''
    assertEquals("#1.0", "\\'", model0.currentToken().getType());
    assertEquals("#1.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model0));
    model0.move(-1);
    assertEquals("#1.2", "'", model0.currentToken().getType());
    assertEquals("#1.3", FREE, stateOfCurrentToken(model0));
    assertTrue("#1.4", model0.currentToken().isOpen());
    model0.move(1);
    model0.insertChar('\\');
    // '\'''\'''\#\''
    assertEquals("#2.0", "\\\\", model0.currentToken().getType());
    assertEquals("#2.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model0));
    assertEquals("#2.2", 10, model0.absOffset());
    model0.move(-2);
    assertEquals("#2.3", "'", model0.currentToken().getType());
    assertEquals("#2.4", FREE, stateOfCurrentToken(model0));
    assertTrue("#2.5", model0.currentToken().isOpen());
    model0.move(3);
    assertEquals("#2.6", "'", model0.currentToken().getType());
    assertEquals("#2.7", FREE, stateOfCurrentToken(model0));
    assertTrue("#2.8", model0.currentToken().isClosed());
    model0.move(-1);
    model0.insertChar('\'');
    // '\'''\'''\'#\''
    assertEquals("#3.0", "\\'", model0.currentToken().getType());
    assertEquals("#3.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model0));
    assertEquals("#3.2", 11, model0.absOffset());
    model0.move(-2);
    assertEquals("#3.3", "\\'", model0.currentToken().getType());
    assertEquals("#3.4", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model0));
    model0.move(4);
    assertEquals("#3.5", "'", model0.currentToken().getType());
    assertEquals("#3.6", FREE, stateOfCurrentToken(model0));
    assertTrue("#3.7", model0.currentToken().isClosed());
    model0.move(-12);
    // '#\'''\'''\'\''
    model0.delete(1);
    // '#'''\'''\'\''
    model0.move(-1);
    // #''''\'''\'\''
    assertEquals("#4.0", "'", model0.currentToken().getType());
    assertTrue("#4.1", model0.currentToken().isOpen());
    assertEquals("#4.2", FREE, stateOfCurrentToken(model0));
    model0.move(1);
    // '#'''\'''\'\''
    assertEquals("#4.3", "'", model0.currentToken().getType());
    assertTrue("#4.4", model0.currentToken().isClosed());
    assertEquals("#4.5", FREE, stateOfCurrentToken(model0));
    model0.move(1);
    // ''#''\'''\'\''
    assertEquals("#5.0", "'", model0.currentToken().getType());
    assertTrue("#5.1", model0.currentToken().isOpen());
    assertEquals("#5.2", FREE, stateOfCurrentToken(model0));
    model0.move(1);
    // '''#'\'''\'\''
    assertEquals("#5.3", "'", model0.currentToken().getType());
    assertTrue("#5.4", model0.currentToken().isClosed());
    assertEquals("#5.5", FREE, stateOfCurrentToken(model0));
    model0.move(1);
    // ''''#\'''\'\''
    assertEquals("#5.6", "\\'", model0.currentToken().getType());
    assertEquals("#5.7", FREE, stateOfCurrentToken(model0));
    model0.move(2);
    // ''''\'#''\'\''
    assertEquals("#6.0", "'", model0.currentToken().getType());
    assertTrue("#6.1", model0.currentToken().isOpen());
    assertEquals("#6.2", FREE, stateOfCurrentToken(model0));
    model0.move(1);
    // ''''\''#'\'\''
    assertEquals("#6.3", "'", model0.currentToken().getType());
    assertTrue("#6.4", model0.currentToken().isClosed());
    assertEquals("#6.5", FREE, stateOfCurrentToken(model0));
    model0.move(1);
    // ''''\'''#\'\''
    assertEquals("#6.6", "\\'", model0.currentToken().getType());
    assertEquals("#6.7", FREE, stateOfCurrentToken(model0));
    model0.move(2);
    // ''''\'''\'#\''
    assertEquals("#6.0", "\\'", model0.currentToken().getType());
    assertEquals("#6.1", FREE, stateOfCurrentToken(model0));
    model0.move(2);
    // ''''\'''\'\'#'
    assertEquals("#6.2", "'", model0.currentToken().getType());
    assertTrue("#6.3", model0.currentToken().isOpen());
    assertEquals("#6.4", FREE, stateOfCurrentToken(model0));
  }
}
