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
 * Tests the interaction between double and single quotes.
 * @version $Id: MixedQuoteTest.java,v 1.1 2005/08/05 12:45:57 guehene Exp $
 */
public class MixedQuoteTest extends BraceReductionTestCase 
  implements ReducedModelStates 
{

  protected ReducedModelControl model;

  /**
   * Constructor.
   * @param name a name for the test.
   */
  public MixedQuoteTest(String name) {
    super(name);
  }

  /**
   * Initializes the reduced models used in the tests.
   */
  protected void setUp() {
    model = new ReducedModelControl();
  }

  /**
   * Creates a test suite for JUnit to use.
   * @return a test suite for JUnit
   */
  public static Test suite() {
    return  new TestSuite(MixedQuoteTest.class);
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
   * Tests how a single quote can eclipse the effects of a double quote by inserting
   * the single quote before the double quote.  This test caught an error with 
   * getStateAtCurrent(): the check for double quote status checks if there is a double
   * quote immediately preceding, but it didn't make sure the double quote was FREE.
   * I fixed that, so now the test passes.
   */
  public void testSingleEclipsesDouble() {
    model.insertChar('\"');
    assertEquals("#0.0", INSIDE_DOUBLE_QUOTE, model.getStateAtCurrent());
    model.move(-1);
    assertEquals("#0.1", FREE, stateOfCurrentToken(model));
    model.move(1);
    model.insertChar('A');    
    model.move(-1);
    assertEquals("#1.0", INSIDE_DOUBLE_QUOTE, model.getStateAtCurrent());    
    assertEquals("#1.1", INSIDE_DOUBLE_QUOTE, stateOfCurrentToken(model));
    assertTrue("#1.2", model.currentToken().isGap());
    model.move(-1);
    model.insertChar('\'');
    assertEquals("#2.0", INSIDE_SINGLE_QUOTE, model.getStateAtCurrent());
    assertEquals("#2.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model));
    assertEquals("#2.2", "\"", model.currentToken().getType());
    model.move(1);
    assertEquals("#3.0", INSIDE_SINGLE_QUOTE, model.getStateAtCurrent());
    assertEquals("#3.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model));
    assertTrue("#3.2", model.currentToken().isGap());    
  }

  /**
   * Tests how a double quote can eclipse the effects of a single quote by inserting
   * the double quote before the single quote. 
   */
  public void testDoubleEclipsesSingle() {
    model.insertChar('\'');
    assertEquals("#0.0", INSIDE_SINGLE_QUOTE, model.getStateAtCurrent());
    model.move(-1);
    assertEquals("#0.1", FREE, stateOfCurrentToken(model));
    model.move(1);
    model.insertChar('A');    
    model.move(-1);
    assertEquals("#1.0", INSIDE_SINGLE_QUOTE, model.getStateAtCurrent());    
    assertEquals("#1.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model));
    assertTrue("#1.2", model.currentToken().isGap());
    model.move(-1);
    model.insertChar('\"');
    assertEquals("#2.0", INSIDE_DOUBLE_QUOTE, model.getStateAtCurrent());
    assertEquals("#2.1", INSIDE_DOUBLE_QUOTE, stateOfCurrentToken(model));
    assertEquals("#2.2", "\'", model.currentToken().getType());
    model.move(1);
    assertEquals("#3.0", INSIDE_DOUBLE_QUOTE, model.getStateAtCurrent());
    assertEquals("#3.1", INSIDE_DOUBLE_QUOTE, stateOfCurrentToken(model));
    assertTrue("#3.2", model.currentToken().isGap());    
  }  
}
