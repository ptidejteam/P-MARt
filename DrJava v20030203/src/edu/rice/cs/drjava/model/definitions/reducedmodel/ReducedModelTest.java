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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import  junit.framework.*;
import  java.util.Vector;
import  junit.extensions.*;


/**
 * Tests insertion and move and other non-delete functionality
 * of the reduced model.
 * @version $Id: ReducedModelTest.java,v 1.1 2005/04/27 20:30:32 elmoutam Exp $
 */
public class ReducedModelTest extends BraceReductionTestCase 
  implements ReducedModelStates 
{
   
  protected ReducedModelControl model0;
  protected ReducedModelControl model1;
  protected ReducedModelControl model2;

  /**
   * put your documentation comment here
   * @param   String name
   */
  public ReducedModelTest(String name) {
    super(name);
  }

  /**
   * put your documentation comment here
   */
  protected void setUp() {
    model0 = new ReducedModelControl();
    model1 = new ReducedModelControl();
    model2 = new ReducedModelControl();
  }

  /**
   * put your documentation comment here
   * @param model
   * @param size
   */
  protected void insertGap(BraceReduction model, int size) {
    for (int i = 0; i < size; i++)
      model.insertChar(' ');
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public static Test suite() {
    return  new TestSuite(ReducedModelTest.class);
  }

  /**
   * put your documentation comment here
   */
  public void testInsertGap() {
    insertGap(model1, 4);
    model1.move(-4);
    //checks to make sure it is a gap
    assertTrue("#0.0", model1.currentToken().isGap());
    assertEquals("#0.2", 4, model1.currentToken().getSize());
    model1.move(4);
    //inserts another gap after the afor mentioned gap
    insertGap(model2, 5);
    model2.move(-5);
    //makes sure they united to form an Uber gap.
    assertTrue("#1.0", model2.currentToken().isGap());
    assertEquals("#1.2", 5, model2.currentToken().getSize());
  }

  /**
   *Test that a gap inserted previous to a gap, unites with that gap.
   */
  public void testInsertGapBeforeGap() {
    insertGap(model1, 3);
    assertTrue("#0.0.0", model1.atEnd());
    model1.move(-3);
    insertGap(model1, 3);
    //insert two consecutive gaps and make sure they combine.
    assertTrue("#0.0", model1.currentToken().isGap());
    assertEquals("#0.1", 3, model1.absOffset());
    assertEquals("#0.2", 6, model1.currentToken().getSize());
    model1.move(-3);
    insertGap(model1, 2);
    assertTrue("#1.0", model1.currentToken().isGap());
    assertEquals("#1.1", 2, model1.absOffset());
    assertEquals("#1.2", 8, model1.currentToken().getSize());
  }

  /**
   * put your documentation comment here
   */
  public void testInsertGapAfterGap() {
    insertGap(model1, 3);
    assertTrue("#0.0", model1.atEnd());
    model1.move(-3);
    assertTrue("#0.1", model1.currentToken().isGap());
    assertEquals("#0.2", 3, model1.currentToken().getSize());
    insertGap(model1, 4);
    assertTrue("#1.1", model1.currentToken().isGap());
    assertEquals("#1.2", 7, model1.currentToken().getSize());
  }

  /**Inserts one gap inside of the other*/
  public void testInsertGapInsideGap() {
    insertGap(model1, 3);
    assertTrue("#0.0", model1.atEnd());
    model1.move(-3);
    assertTrue("#0.1", model1.currentToken().isGap());
    assertEquals("#0.2", 3, model1.currentToken().getSize());
    insertGap(model1, 3);
    assertTrue("#1.1", model1.currentToken().isGap());
    assertEquals("#1.2", 6, model1.currentToken().getSize());
    assertEquals("#1.3", 3, model1.absOffset());
    insertGap(model1, 4);
    assertTrue("#1.1", model1.currentToken().isGap());
    assertEquals("#1.2", 10, model1.currentToken().getSize());
    assertEquals("#1.3", 7, model1._offset);
  }

  /**
   * put your documentation comment here
   */
  public void testInsertBraceAtStartAndEnd() {
    model1.insertChar('(');
    assertTrue("#0.0", model1.atEnd());
    model1.move(-1);
    assertEquals("#0.1", "(", model1.currentToken().getType());
    assertEquals("#0.2", 1, model1.currentToken().getSize());
    model2.insertChar(')');
    assertTrue("#1.0", model2.atEnd());
    model2.move(-1);
    assertEquals("#1.1", ")", model2.currentToken().getType());
    assertEquals("#1.2", 1, model2.currentToken().getSize());
  }

  //**************
  public void testInsertBraceInsideGap() {
    insertGap(model1, 4);
    model1.move(-4);
    insertGap(model1, 3);
    assertEquals("#0.0", 3, model1.absOffset());
    assertEquals("#0.1", 7, model1.currentToken().getSize());
    model1.insertChar('{');
    assertEquals("#1.0", 4, model1.absOffset());
    assertEquals("#1.1", 4, model1.currentToken().getSize());
    assertTrue("#1.2", model1.currentToken().isGap());
    model1.move(-1);
    assertEquals("#2.0", 1, model1.currentToken().getSize());
    assertEquals("#2.1", "{", model1.currentToken().getType());
    model1.move(-3);
    assertEquals("#3.0", 0, model1.absOffset());
    assertEquals("#3.1", 3, model1.currentToken().getSize());
    assertTrue("#3.2", model1.currentToken().isGap());
  }

  /**
   * put your documentation comment here
   */
  public void testInsertBrace() {
    model1.insertChar('{');
    assertTrue("#0.0", model1.atEnd());
    model1.move(-1);
    assertEquals("#1.0", 1, model1.currentToken().getSize());
    assertEquals("#1.1", "{", model1.currentToken().getType());
    model1.insertChar('(');
    model1.insertChar('[');
    assertEquals("#2.0", 1, model1.currentToken().getSize());
    assertEquals("#2.1", "{", model1.currentToken().getType());
    model1.move(-1);
    assertEquals("#3.0", 1, model1.currentToken().getSize());
    assertEquals("#3.1", "[", model1.currentToken().getType());
    model1.move(-1);
    assertEquals("#3.0", 1, model1.currentToken().getSize());
    assertEquals("#3.1", "(", model1.currentToken().getType());
  }

  /**
   * put your documentation comment here
   */
  public void testInsertBraceAndBreakLineComment() {
    model1.insertChar('/');
    model1.insertChar('/');
    model1.move(-1);
    assertEquals("#0.0", 2, model1.currentToken().getSize());
    //move to the middle of the // and break it with a {
    model1.insertChar('{');
    assertEquals("#1.0", "/", model1.currentToken().getType());
    assertEquals("#1.1", 1, model1.currentToken().getSize());
    model1.move(-1);
    assertEquals("#2.0", "{", model1.currentToken().getType());
    assertEquals("#2.1", 1, model1.currentToken().getSize());
    model1.move(-1);
    assertEquals("#3.0", "/", model1.currentToken().getType());
    assertEquals("#3.1", 1, model1.currentToken().getSize());
  }

  /**
   * Tests the reduced model's ability to insert braces correctly
   */
  public void testInsertBraceAndBreakBlockCommentStart() {
    model1.insertChar('/');
    model1.insertChar('*');
    model1.move(-2);
    assertEquals("#0.0", 2, model1.currentToken().getSize());
    model1.move(1);
    model1.insertChar('{');
    assertEquals("#1.0", "*", model1.currentToken().getType());
    assertEquals("#1.1", 1, model1.currentToken().getSize());
    model1.move(-1);
    assertEquals("#2.0", "{", model1.currentToken().getType());
    assertEquals("#2.1", 1, model1.currentToken().getSize());
    model1.move(-1);
    assertEquals("#3.0", "/", model1.currentToken().getType());
    assertEquals("#3.1", 1, model1.currentToken().getSize());
  }

  //**************************
  public void testInsertMultipleBraces() {
    model1.insertChar('/');
    model1.insertChar('*');
    model1.insertChar('{');
    model1.move(-1);
    // /*#{
    assertEquals("#0.0", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.move(-2);
    assertEquals("#0.1", FREE, model1.currentToken().getState());
    model1.move(3);
    model1.insertChar('*');
    model1.insertChar('/');
    // /*{*/#
    model1.move(-2);
    assertEquals("#1.0", FREE, model1.currentToken().getState());
    model1.move(1);
    model1.insertChar('{');
    model1.move(0);
    // /*{*{#/
    model1.move(-1);
    assertEquals("#2.0", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
    assertEquals("#2.1", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#2.2", "/", model1.currentToken().getType());
  }

  /**
   * Test to ensure that a complex sequence of multi-lined Brace entries does not fail.
   * Originally, the insertBraceInGap() had the chance of inserting at the beginning 
   * of a gap, in which case the gap to be split was actually never shrunk and a new
   * gap of size 0 is added after the newly inserted Brace. This caused problems for 
   * brace-matching when new nested braces/parentheses piled up on top of each other.
   */
  public void testComplexBraceInsertion() {
    model1.insertChar('\n');
    model1.insertChar('\n');
    model1.move(-1);
    // \n#\n
    assertEquals("#0.0", false, model1.atEnd());
    model1.insertChar('{');
    model1.insertChar('\n');
    model1.insertChar('\n');
    model1.insertChar('}');
    model1.move(-2);
    // \n{\n#\n}\n
    assertEquals("#0.1", FREE, model1.currentToken().getState());
    model1.insertChar('{');
    model1.insertChar('{');
    model1.insertChar('}');
    model1.insertChar('}');
    // \n{\n{{}}#\n}\n
    assertEquals("#1.0", 4, model1.balanceBackward());
    model1.move(-1);
    assertEquals("#1.1", 2, model1.balanceBackward());
  }
  
  /**
   * put your documentation comment here
   */
  public void testCrazyCase1() {
    model1.insertChar('/');
    insertGap(model1, 4);
    model1.insertChar('*');
    model1.insertChar('/');
    //should not form an end block comment
    model1.move(-1);
    assertEquals("#0.0", "/", model1.currentToken().getType());
    model1.move(-1);
    assertEquals("#0.1", "*", model1.currentToken().getType());
    // /____#*/
    model1.move(1);
    model1.insertChar('/');
    // /____*/#/
    assertEquals("#1.0", 2, model1.currentToken().getSize());
    model1.move(-2);
    // /____#*//
    assertEquals("#1.0", "*", model1.currentToken().getType());
    model1.move(-4);
    model1.insertChar('*');
    // /*#____*//
    model1.move(-2);
    assertEquals("#2.0", "/*", model1.currentToken().getType());
    assertEquals("#2.1", FREE, model1.currentToken().getState());
    model1.move(6);
    // /*____#*//
    assertEquals("#2.2", "*/", model1.currentToken().getType());
    assertEquals("#2.3", FREE, model1.currentToken().getState());
    assertEquals("#2.4", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    // /*____#*//
  }

  /**Test sequences of inserts*/
  public void testCrazyCase2() {
    model1.insertChar('/');
    insertGap(model1, 4);
    model1.move(-2);
    model1.insertChar('/');
    model1.move(0);
    model1.move(-3);
    //check that double slash works.
    assertEquals("#0.0", 2, model1.currentToken().getSize());
    assertEquals("#0.3", FREE, model1.getStateAtCurrent());
    model1.move(2);
    assertEquals("#0.2", 1, model1.currentToken().getSize());
    assertEquals("#0.1", "/", model1.currentToken().getType());
    model1.move(-2);
    model1.insertChar('/');
    model1.move(-2);
    assertEquals("#1.1", "//", model1.currentToken().getType());
    assertEquals("#1.3", FREE, model1.currentToken().getState());
    model1.move(2);
    // //#__/__
    assertEquals("#1.2", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    assertEquals("1.4", INSIDE_LINE_COMMENT, model1.currentToken().getState());
    model1.move(2);
    assertEquals("1.5", INSIDE_LINE_COMMENT, model1.currentToken().getState());
    model1.move(-2);
  }

  /**
   * put your documentation comment here
   */
  public void testLineCommentBreakCrazy() {
    model1.insertChar('/');
    model1.insertChar('/');
    insertGap(model1, 4);
    model1.move(-2);
    model1.insertChar('/');
    // //#__/__
    //break line comment simultaneously forming a new line comment
    model1.move(-4);
    model1.insertChar('/');
    model1.move(0);
    // //#/__/__
    model1.move(-2);
    assertEquals("#2.0", "//", model1.currentToken().getType());
    assertEquals("#2.3", FREE, model1.currentToken().getState());
    model1.move(2);
    assertEquals("#2.1", "/", model1.currentToken().getType());
    assertEquals("#2.2", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    assertEquals("2.4", INSIDE_LINE_COMMENT, model1.currentToken().getState());
    model1.move(1);
    assertEquals("2.5", INSIDE_LINE_COMMENT, model1.currentToken().getState());
    //break line comment forming a block comment
    model1.move(-2);
    model1.insertChar('*');                     //  ///__/__ 
    model1.move(0);
    // /*#//__/__
    model1.move(-2);
    assertEquals("#3.0", "/*", model1.currentToken().getType());
    assertEquals("#3.3", FREE, model1.currentToken().getState());
    model1.move(2);
    assertEquals("#3.1", "/", model1.currentToken().getType());
    assertEquals("#3.3", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    assertEquals("3.4", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#3.2", "/", model1.currentToken().getType());
    assertEquals("3.5", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
  }

  /**
   * put your documentation comment here
   */
  public void testBreakBlockCommentWithStar() {
    // /*#//__/__
    model1.insertChar('/');
    model1.insertChar('*');
    model1.insertChar('/');
    model1.insertChar('/');
    insertGap(model1, 2);
    model1.insertChar('/');
    insertGap(model1, 2);
    //break block comment start with a star.
    model1.move(-8);
    model1.insertChar('*');
    // /*#*//__/__      
    model1.move(-2);
    assertEquals("#4.0", "/*", model1.currentToken().getType());
    assertEquals("#4.3", FREE, model1.currentToken().getState());
    model1.move(2);
    assertEquals("#4.1", "*/", model1.currentToken().getType());
    assertEquals("#4.3", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    assertEquals("4.4", FREE, model1.currentToken().getState());
    model1.move(2);
    assertEquals("#4.2", "/", model1.currentToken().getType());
    assertEquals("4.5", FREE, model1.currentToken().getState());
  }

  /**
   * put your documentation comment here
   */
  public void testBreakCloseBlockCommentWithStar() {
    model1.insertChar('/');
    model1.insertChar('*');
    model1.insertChar('*');
    model1.insertChar('/');
    model1.insertChar('/');
    insertGap(model1, 2);
    model1.insertChar('/');
    insertGap(model1, 2);
    model1.move(-7);
    insertGap(model1, 3);
    // /**___#//__/__
    model1.move(-3);
    assertEquals("#5.0", true, model1.currentToken().isGap());
    assertEquals("#5.4", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
    model1.move(3);
    assertEquals("#5.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    assertEquals("#5.2", "/", model1.currentToken().getType());
    assertEquals("5.5", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#5.3", "/", model1.currentToken().getType());
    assertEquals("5.6", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
  }

  /**
   * put your documentation comment here
   */
  public void testBasicBlockComment() {
    model1.insertChar('/');
    model1.insertChar('*');
    model1.insertChar('*');
    model1.insertChar('/');
    model1.move(-4);
    assertEquals("0.1", FREE, model1.currentToken().getState());
    assertEquals("0.2", "/*", model1.currentToken().getType());
    model1.move(2);
    assertEquals("0.3", FREE, model1.currentToken().getState());
    assertEquals("0.4", "*/", model1.currentToken().getType());
    model1.insertChar('/');
    model1.move(-1);
    assertEquals("1.1", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
    assertEquals("1.3", "/", model1.currentToken().getType());
    model1.move(1);
    assertEquals("1.0", FREE, model1.currentToken().getState());
    assertEquals("1.2", "*/", model1.currentToken().getType());
  }

  /**
   * put your documentation comment here
   */
  public void testInsertBlockInsideBlockComment() {
    model1.insertChar('/');
    model1.insertChar('*');
    model1.insertChar('/');
    model1.insertChar('*');
    model1.insertChar('/');
    ///*/*/#
    model1.move(-2);
    model1.insertChar('*');
    ///*/*#*/
    model1.move(-1);
    assertEquals("1.1", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
    assertEquals("1.3", "*", model1.currentToken().getType());
    model1.move(1);
    assertEquals("1.0", FREE, model1.currentToken().getState());
    assertEquals("1.2", "*/", model1.currentToken().getType());
  }

  /**
   * put your documentation comment here
   */
  public void testInsertBlockCommentEnd() { // should not form an end without a start.
    model1.insertChar('*');
    model1.insertChar('/');
    model1.move(-1);
    assertEquals("#3.0", "/", model1.currentToken().getType());
    assertEquals("#3.1", 1, model1.currentToken().getSize());
  }

  /**
   * put your documentation comment here
   */
  public void testGetStateAtCurrent() {
    assertEquals("#0.0", FREE, model1.getStateAtCurrent());
    assertEquals("#0.1", FREE, model1.getStateAtCurrent());
    model1.insertChar('(');
    model1.move(-1);
    assertEquals("#1.0", FREE, model1.currentToken().getState());
    model1.move(1);
    model1.insertChar('/');
    model1.insertChar('/');
    model1.move(-2);
    assertEquals("#2.0", FREE, model1.currentToken().getState());
    model1.move(2);
    assertEquals("#2.1", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    // {//#
    model1.move(-3);
    model1.insertChar('/');
    model1.insertChar('/');
    // //#{//
    model1.move(-2);
    assertEquals("#3.0", FREE, model1.currentToken().getState());
    model1.move(2);
    assertEquals("#3.1", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    assertEquals("#3.2", INSIDE_LINE_COMMENT, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#3.3", INSIDE_LINE_COMMENT, model1.currentToken().getState());
    assertEquals("#3.4", "/", model1.currentToken().getType());
    model1.move(1);
    assertEquals("#4.1", INSIDE_LINE_COMMENT, model1.currentToken().getState());
    assertEquals("#4.2", "/", model1.currentToken().getType());
  }

  /**
   * put your documentation comment here
   */
  public void testQuotesSimple() {
    model1.insertChar('\"');
    model1.insertChar('\"');
    model1.move(-2);
    assertEquals("#0.0", "\"", model1.currentToken().getType());
    assertEquals("#0.3", FREE, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#0.1", "\"", model1.currentToken().getType());
    assertEquals("#0.2", FREE, model1.currentToken().getState());
    assertEquals("#0.4", INSIDE_DOUBLE_QUOTE, model1.getStateAtCurrent());
  }

  /**
   * put your documentation comment here
   */
  public void testQuotesWithGap() {
    model1.insertChar('\"');
    model1.insertChar('\"');
    model1.move(-2);
    assertEquals("#0.1", "\"", model1.currentToken().getType());
    assertEquals("#0.3", FREE, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#0.0", "\"", model1.currentToken().getType());
    assertEquals("#0.2", FREE, model1.currentToken().getState());
    assertEquals("#0.4", INSIDE_DOUBLE_QUOTE, model1.getStateAtCurrent());
    insertGap(model1, 4);
    // "____#"
    model1.move(-4);
    assertEquals("#1.1", true, model1.currentToken().isGap());
    assertEquals("#1.3", INSIDE_DOUBLE_QUOTE, model1.currentToken().getState());
    model1.move(4);
    assertEquals("#1.0", "\"", model1.currentToken().getType());
    assertEquals("#1.2", FREE, model1.currentToken().getState());
    assertEquals("#1.4", INSIDE_DOUBLE_QUOTE, model1.getStateAtCurrent());
    model1.move(-2);
    model1.insertChar('/');
    // "__/__"
    model1.move(-1);
    assertEquals("#2.1", "/", model1.currentToken().getType());
    model1.move(1);
    assertEquals("#2.0", true, model1.currentToken().isGap());
    assertEquals("#2.4", INSIDE_DOUBLE_QUOTE, model1.currentToken().getState());
    assertEquals("#2.6", INSIDE_DOUBLE_QUOTE, model1.getStateAtCurrent());
    model1.move(2);
    assertEquals("#2.2", "\"", model1.currentToken().getType());
    assertEquals("#2.3", FREE, model1.currentToken().getState());
  }

  /**
   * put your documentation comment here
   */
  public void testInsertQuoteToQuoteBlock() {
    model1.insertChar('\"');
    insertGap(model1, 2);
    model1.insertChar('/');
    insertGap(model1, 2);
    model1.insertChar('\"');
    model1.move(-3);
    model1.insertChar('\"');
    // "__/"#__"
    model1.move(-1);
    assertEquals("#3.1", "\"", model1.currentToken().getType());
    assertEquals("#3.5", FREE, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#3.0", true, model1.currentToken().isGap());
    assertEquals("#3.4", FREE, model1.currentToken().getState());
    assertEquals("#3.6", FREE, model1.getStateAtCurrent());
    model1.move(2);
    assertEquals("#3.2", "\"", model1.currentToken().getType());
    assertEquals("#3.3", FREE, model1.currentToken().getState());
    // "__/"__"
    model1.move(-6);
    assertEquals("#4.1", true, model1.currentToken().isGap());
    assertEquals("#4.5", INSIDE_DOUBLE_QUOTE, model1.currentToken().getState());
    model1.move(2);
    assertEquals("#4.0", "/", model1.currentToken().getType());
    assertEquals("#4.4", INSIDE_DOUBLE_QUOTE, model1.currentToken().getState());
    assertEquals("#4.6", INSIDE_DOUBLE_QUOTE, model1.getStateAtCurrent());
    model1.move(1);
    assertEquals("#4.2", "\"", model1.currentToken().getType());
    assertEquals("#4.3", FREE, model1.currentToken().getState());
    model1.move(-1);
    // "__/#"__"
    //break quote with newline
    model1.insertChar('\n');
    // "__\n#/"__"
    model1.move(-1);
    assertEquals("#5.5", FREE, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#5.4", FREE, model1.currentToken().getState());
    assertEquals("#5.6", FREE, model1.getStateAtCurrent());
    model1.move(1);
    assertEquals("#5.3", FREE, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#5.7", INSIDE_DOUBLE_QUOTE, model1.currentToken().getState());
    assertEquals("#5.8", true, model1.currentToken().isGap());
  }

  /**
   * put your documentation comment here
   */
  public void testQuoteBreaksComment() {
    model1.insertChar('/');
    model1.insertChar('*');
    model1.insertChar('*');
    model1.insertChar('/');
    model1.move(-2);
    model1.insertChar('\"');
    model1.move(-1);
    // /*#"*/
    model1.move(-2);
    assertEquals("#1.1", FREE, model1.currentToken().getState());
    model1.move(2);
    assertEquals("#1.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    assertEquals("#1.2", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#1.2", FREE, model1.currentToken().getState());
    model1.move(-3);
    // #/*"*/
    model1.insertChar('\"');
    model1.move(-1);
    assertEquals("#2.2", FREE, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#2.0", INSIDE_DOUBLE_QUOTE, model1.getStateAtCurrent());
    assertEquals("#2.1", INSIDE_DOUBLE_QUOTE, model1.currentToken().getState());
    assertEquals("#2.3", "/", model1.currentToken().getType());
    model1.move(1);
    assertEquals("#2.4", "*", model1.currentToken().getType());
    // "/#*"*/
    model1.move(2);
    // "/*"#*/
    assertEquals("#5.0", FREE, model1.getStateAtCurrent());
    assertEquals("#5.1", FREE, model1.currentToken().getState());
    assertEquals("#5.3", "*", model1.currentToken().getType());
    model1.move(1);
    assertEquals("#5.4", "/", model1.currentToken().getType());
    assertEquals("#5.5", FREE, model1.currentToken().getState());
  }

  /**
   * put your documentation comment here
   */
  public void testQuoteBreakComment2() {
    model1.insertChar('/');
    model1.insertChar('*');
    model1.insertChar('*');
    model1.insertChar('/');
    model1.move(-4);
    assertEquals("#0.0", "/*", model1.currentToken().getType());
    model1.move(2);
    assertEquals("#0.1", "*/", model1.currentToken().getType());
    model1.move(-2);
    // "#/**/
    model1.insertChar('\"');
    model1.move(-1);
    assertEquals("#1.0", FREE, model1.currentToken().getState());
    assertEquals("#1.4", "\"", model1.currentToken().getType());
    model1.move(1);
    assertEquals("#1.1", INSIDE_DOUBLE_QUOTE, model1.getStateAtCurrent());
    assertEquals("#1.4", "/", model1.currentToken().getType());
    assertEquals("#1.2", INSIDE_DOUBLE_QUOTE, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#1.3", INSIDE_DOUBLE_QUOTE, model1.currentToken().getState());
    assertEquals("#1.4", "*", model1.currentToken().getType());
  }

  /**
   * put your documentation comment here
   */
  public void testInsertNewlineEndLineComment() {
    model1.insertChar('/');
    model1.insertChar('/');
    insertGap(model1, 5);
    model1.move(-2);
    model1.insertChar('\n');
    // //___\n#__
    model1.move(-1);
    assertEquals("#0.2", "\n", model1.currentToken().getType());
    assertEquals("#0.4", FREE, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#0.0", FREE, model1.getStateAtCurrent());
    assertTrue("#0.1", model1.currentToken().isGap());
    assertEquals("#0.3", 2, model1.currentToken().getSize());
    assertEquals("#0.5", FREE, model1.currentToken().getState());
  }

  /**
   * put your documentation comment here
   */
  public void testInsertNewlineEndQuote() {
    model1.insertChar('\"');
    insertGap(model1, 5);
    model1.move(-2);
    model1.insertChar('\n');
    // "___\n#__
    model1.move(-1);
    assertEquals("#0.4", FREE, model1.currentToken().getState());
    assertEquals("#0.2", "\n", model1.currentToken().getType());
    model1.move(1);
    assertEquals("#0.0", FREE, model1.getStateAtCurrent());
    assertTrue("#0.1", model1.currentToken().isGap());
    assertEquals("#0.3", 2, model1.currentToken().getSize());
    assertEquals("#0.5", FREE, model1.currentToken().getState());
  }

  /**
   * put your documentation comment here
   */
  public void testInsertNewlineChainReaction() {
    model1.insertChar('/');
    model1.insertChar('/');
    model1.insertChar('/');
    model1.insertChar('*');
    // ///*#
    model1.move(-1);
    // ///#*
    model1.move(-1);
    assertEquals("#0.2", "/", model1.currentToken().getType());
    assertEquals("#0.3", INSIDE_LINE_COMMENT, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#0.0", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    assertEquals("#0.1", "*", model1.currentToken().getType());
    assertEquals("#0.4", INSIDE_LINE_COMMENT, model1.currentToken().getState());
    model1.move(1);
    model1.insertChar('\n');
    model1.insertChar('\"');
    model1.insertChar('*');
    model1.insertChar('/');
    model1.move(-1);
    // ///*
    // "*#/
    assertEquals("#1.0", INSIDE_DOUBLE_QUOTE, model1.getStateAtCurrent());
    assertEquals("#1.1", "/", model1.currentToken().getType());
    assertEquals("#1.4", INSIDE_DOUBLE_QUOTE, model1.currentToken().getState());
    model1.move(-5);
    assertEquals("#2.1", "/", model1.currentToken().getType());
    model1.insertChar('\n');
    // //
    // #/*
    // "*/
    assertEquals("#3.0", FREE, model1.getStateAtCurrent());
    assertEquals("#3.4", FREE, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#3.1", "/*", model1.currentToken().getType());
    // //
    // /*
    // #"*/
    model1.move(2);
    assertEquals("#4.0", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    assertEquals("#4.1", "\"", model1.currentToken().getType());
    assertEquals("#4.4", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#4.6", "*/", model1.currentToken().getType());
  }

  /**
   * put your documentation comment here
   */
  public void testMoveWithinToken() {
    insertGap(model1, 10);
    assertTrue("#0.0", model1.atEnd());
    assertEquals("#0.1", 10, model1.absOffset());
    model1.move(-5);
    assertTrue("#1.0", model1.currentToken().isGap());
    assertEquals("#1.1", 5, model1.absOffset());
    model1.move(2);
    assertTrue("#2.0", model1.currentToken().isGap());
    assertEquals("#2.1", 7, model1.absOffset());
    model1.move(-4);
    assertTrue("#3.0", model1.currentToken().isGap());
    assertEquals("#3.1", 3, model1.absOffset());
    model1.move(-3);
    assertTrue("#4.0", model1.currentToken().isGap());
    assertEquals("#4.1", 0, model1.absOffset());
    model1.move(10);
    assertTrue("#5.0", model1.atEnd());
    assertEquals("#5.1", 10, model1.absOffset());
  }

  /**
   * put your documentation comment here
   */
  public void testMoveOnEmpty() {
    model1.move(0);
    assertTrue("#0.0", model1.atStart());
    try {
      model1.move(-1);
      assertTrue("#0.1", false);
    } catch (Exception e) {}
    try {
      model1.move(1);
      assertTrue("#0.2", false);
    } catch (Exception e) {}
  }

  /**
   * put your documentation comment here
   */
  public void testMove0StaysPut() {
    model0.insertChar('/');
    assertEquals("#1", 1, model0.absOffset());
    model0.move(0);
    assertEquals("#2", 1, model0.absOffset());
    model0.insertChar('/');
    assertEquals("#3", 2, model0.absOffset());
    model0.move(0);
    assertEquals("#4", 2, model0.absOffset());
    model0.move(-1);
    assertEquals("#5", 1, model0.absOffset());
    model0.move(0);
    assertEquals("#6", 1, model0.absOffset());
  }

  /** tests the function to test if something is inside comments */
  public void testInsideComment() {
    assertEquals("#0.0", FREE, model0.getStateAtCurrent());
    model0.insertChar('/');
    model0.insertChar('*');
    assertEquals("#0.1", INSIDE_BLOCK_COMMENT, model0.getStateAtCurrent());
    model1.insertChar('/');
    model1.insertChar('/');
    assertEquals("#0.2", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    model1.insertChar('(');
    assertEquals("#0.3", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    model1.insertChar('\n');
    assertEquals("#0.4", FREE, model1.getStateAtCurrent());
    model0.insertChar('*');
    model0.insertChar('/');
    assertEquals("#0.4", FREE, model0.getStateAtCurrent());
  }

  /** tests the function to test if something is inside quotes */
  public void testInsideString() {
    assertEquals("#0.0", FREE, model0.getStateAtCurrent());
    model0.insertChar('\"');
    assertEquals("#0.1", INSIDE_DOUBLE_QUOTE, model0.getStateAtCurrent());
    model1.insertChar('\"');
    assertEquals("#0.2", INSIDE_DOUBLE_QUOTE, model1.getStateAtCurrent());
    model1.insertChar('(');
    assertEquals("#0.3", INSIDE_DOUBLE_QUOTE, model1.getStateAtCurrent());
    model1.insertChar('\"');
    assertEquals("#0.4", FREE, model1.getStateAtCurrent());
  }

  /** tests inserting braces */
  public void testInsertBraces() {
    assertEquals("#0.0", 0, model0.absOffset());
    model0.insertChar('/');
    // /#
    assertEquals("#1.0", FREE, model0.getStateAtCurrent());
    model0.insertChar('*');
    // /*#
    assertEquals("#2.0", INSIDE_BLOCK_COMMENT, model0.getStateAtCurrent());
    assertEquals("#2.1", 2, model0.absOffset());
    model0.move(-1);
    // /#*
    assertEquals("#3.0", 1, model0.absOffset());
    model0.insertChar('(');
    // /(#*
    assertEquals("#4.0", FREE, model0.getStateAtCurrent());
    model0.move(-1);
    // /#(*
    model0.delete(1);
    // /#*
    model0.move(1);
    // /*#
    assertEquals("#5.0", INSIDE_BLOCK_COMMENT, model0.getStateAtCurrent());
    model0.insertChar('*');
    // /**#
    assertEquals("#6.0", INSIDE_BLOCK_COMMENT, model0.getStateAtCurrent());
    model0.insertChar('/');
    // /**/#
    assertEquals("#7.0", 4, model0.absOffset());
    assertEquals("#7.1", FREE, model0.getStateAtCurrent());
    model0.move(-2);
    // /*#*/
    assertEquals("#8.0", INSIDE_BLOCK_COMMENT, model0.getStateAtCurrent());
    assertEquals("#8.1", 2, model0.absOffset());
    model0.insertChar('(');
    assertEquals("#9.0", INSIDE_BLOCK_COMMENT, model0.getStateAtCurrent());
    // /*(#*/
    model0.move(1);
    // /*(*#/
    assertEquals("#10.0", INSIDE_BLOCK_COMMENT, model0.getStateAtCurrent());
    model0.move(-2);
    // /*#(*/
    assertEquals("#11.0", INSIDE_BLOCK_COMMENT, model0.getStateAtCurrent());
    model0.move(1);
    // /*(#*/
    // /*(#*/
    assertEquals("#12.0", INSIDE_BLOCK_COMMENT, model0.getStateAtCurrent());
    assertEquals("#12.1", 3, model0.absOffset());
    insertGap(model0, 4);
    // /*(____#*/
    model0.move(-2);
    // /*(__#__*/
    assertEquals("#13.0", 5, model0.absOffset());
    model0.insertChar(')');
    // /*(__)#__*/
    assertEquals("#14.0", 6, model0.absOffset());
    // move to the closed paren
    model0.move(-1);
    // /*(__#)__*/
    assertEquals("#12.0", INSIDE_BLOCK_COMMENT, model0.getStateAtCurrent());
  }

  /** tests inserting gaps */
  public void testInsertGap2() {
    model0.insertChar('/');
    model0.insertChar('*');
    insertGap(model0, 5);
    assertEquals("#0.0", 7, model0.absOffset());
    model0.insertChar('(');
    model0.move(-1);
    insertGap(model0, 3);
    assertEquals("#1.0", 10, model0.absOffset());
  }

  /** tests the cursor movement function */
  public void testMove() {
    model0.insertChar('(');
    insertGap(model0, 5);
    model0.insertChar(')');
    model0.insertChar('\n');
    insertGap(model0, 2);
    model0.insertChar('{');
    model0.insertChar('}');
    try {
      model0.move(-30);
      assertTrue("#0.0", false);
    } catch (Exception e) {}
    try {
      model0.move(1);
      assertTrue("#0.1", false);
    } catch (Exception e) {}
    assertEquals("#0.2", 12, model0.absOffset());
    model0.move(-2);
    assertEquals("#0.3", 10, model0.absOffset());
    model0.move(-8);
    assertEquals("#0.4", 2, model0.absOffset());
    model0.move(3);
    assertEquals("#0.5", 5, model0.absOffset());
    model0.move(4);
    assertEquals("#0.6", 9, model0.absOffset());
  }

  /** sets up example reduction for the following tests */
  protected ReducedModelControl setUpExample() {
    ReducedModelControl model = new ReducedModelControl();
    model.insertChar('{');
    model.insertChar('\n');
    insertGap(model, 3);
    model.insertChar('\n');
    model.insertChar('(');
    insertGap(model, 2);
    model.insertChar(')');
    model.insertChar('\n');
    insertGap(model, 3);
    model.insertChar('/');
    model.insertChar('/');
    insertGap(model, 3);
    model.insertChar('\n');
    model.insertChar('\"');
    insertGap(model, 1);
    model.insertChar('{');
    insertGap(model, 1);
    model.insertChar('\"');
    model.insertChar('/');
    model.insertChar('*');
    insertGap(model, 1);
    model.insertChar('(');
    insertGap(model, 1);
    model.insertChar(')');
    insertGap(model, 1);
    model.insertChar('*');
    model.insertChar('/');
    model.insertChar('\n');
    model.insertChar('}');
    // {
    // ___
    // (__)
    // ___//___
    // "_{_"/*_(_)_*/
    // }#
    return  model;
  }

  /** tests forward balancer, e.g., '(' balances with ')' */
  public void testBalanceForward() {
    assertEquals("#0.0", -1, model0.balanceForward());
    model0 = setUpExample();
    assertEquals("#1.0", -1, model0.balanceForward());
    model0.move(-1);
    assertEquals("#2.0", -1, model0.balanceForward());
    model0.move(-35);
    assertEquals("#3.0", 36, model0.balanceForward());
    model0.move(1);
    assertEquals("#4.0", -1, model0.balanceForward());
    model0.move(5);
    assertEquals("#5.0", 4, model0.balanceForward());
    model0.move(27);
    assertEquals("#6.0", -1, model0.balanceForward());
    model0.move(-20);
    assertEquals(-1, model0.balanceForward());
    model1.insertChar('(');
    model1.move(-1);
    assertEquals("#7.0", -1, model1.balanceForward());
    model1.move(1);
    model1.insertChar('}');
    model1.move(-1);
    assertEquals("#8.0", -1, model1.balanceForward());
  }

  /** tests backwards balancer, e.g., ')' balances with '(' */
  public void testBalanceBackward() {
    assertEquals("#0.0", -1, model0.balanceBackward());
    model0 = setUpExample();
    // {
    // ___
    // (__)
    // ___//___
    // "_{_"/*_(_)_*/
    // }#
    assertEquals("#1.0", 36, model0.balanceBackward());
    model0.move(-2);
    assertEquals("#2.0", -1, model0.balanceBackward());
    model0.move(-14);
    assertEquals("#3.0", -1, model0.balanceBackward());
    model0.move(-10);
    assertEquals("#4.0", 4, model0.balanceBackward());
    model0.move(-10);
    assertEquals("#5.0", -1, model0.balanceBackward());
    model1.insertChar(')');
    assertEquals("#6.0", -1, model1.balanceBackward());
    model1.move(-1);
    model1.insertChar('{');
    model1.move(1);
    assertEquals("#7.0", -1, model1.balanceBackward());
  }
}



;
 