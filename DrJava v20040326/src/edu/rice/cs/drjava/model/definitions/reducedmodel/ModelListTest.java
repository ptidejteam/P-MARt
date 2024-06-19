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

import junit.framework.*;
import java.util.Vector;
import junit.extensions.*;

/**
 * Tests the functionality of the ModelList list class.
 * @version $Id: ModelListTest.java,v 1.1 2005/08/05 12:45:09 guehene Exp $
 */
public final class ModelListTest extends TestCase {
  protected ModelList<Integer> fEmpty;
  protected ModelList<Integer> fFull;
  
  public ModelListTest(String name) {
    super(name);
  }
  
  protected void setUp() {
    fFull = new ModelList<Integer>();
    fEmpty = new ModelList<Integer>();
  }
  
  public static Test suite() {
    return new TestSuite(ModelListTest.class);
  }
  
  public void testInsert() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    ModelList<Integer>.Iterator itEmpty = fEmpty.getIterator();
    assertTrue("#0.0", fEmpty.isEmpty());
    assertTrue("#0.1", fFull.isEmpty());
    assertEquals("#0.2", 0, fEmpty.length());
    assertEquals("#0.3", 0, fFull.length());
    assertTrue("#0.4", itEmpty.atStart());
    assertTrue("#0.5", itFull.atStart());
    itFull.insert(new Integer(5));
    assertTrue("#1.0", !itFull.atStart());
    assertEquals("#1.1", 1, fFull.length());
    assertEquals("#1.2", new Integer(5), itFull.current());
    assertTrue("#2.0", fEmpty.isEmpty());
    assertTrue("#2.1", !fFull.isEmpty());
    itFull.insert(new Integer(4));
    assertEquals("#2.2", 2, fFull.length());
    assertEquals("#2.3", new Integer(4), itFull.current());
    assertTrue("#2.4", !fFull.isEmpty());
  }
  
  public void testInsertFront() {
    fFull.insertFront(new Integer(3));
    fFull.insertFront(new Integer(2));
    fFull.insertFront(new Integer(1));
    fFull.insertFront(new Integer(0));
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    for (int i = 0; i < 4; i++) {
      itFull.next();
      assertEquals(new Integer(i), itFull.current());
    }
  }
  
  public void testRemove() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    ModelList<Integer>.Iterator itEmpty = fEmpty.getIterator();
    assertTrue("#0.0", fEmpty.isEmpty());
    assertEquals("#0.1", 0, fEmpty.length());
    assertEquals("#0.2", 0, fFull.length());
    try {
      itEmpty.remove();
      assertTrue("#1.0", false);
    }
    catch (Exception e) {
    }
    
    itFull.insert(new Integer(5));
    assertTrue("#2.0", !fFull.isEmpty());
    assertEquals("#2.1", 1, fFull.length());
    itFull.remove();
    assertTrue("#3.0", fFull.isEmpty());
    assertEquals("#3.1", 0, fFull.length());
  }
  
  public void testNext() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    ModelList<Integer>.Iterator itEmpty = fEmpty.getIterator();
    
    //test going past end of list
    try {
      itEmpty.next();
      itEmpty.next();
      assertTrue("#0.0", false);
    }
    catch (Exception e) {
    }
    
    itFull.insert(new Integer(6));
    itFull.insert(new Integer(5));
    itFull.insert(new Integer(4));
    //now at start of list, after head.
    assertEquals("#1.0", new Integer(4), itFull.current());
    itFull.next();
    assertEquals("#1.1", new Integer(5), itFull.current());
    itFull.next();
    assertEquals("#1.2", new Integer(6), itFull.current());
    itFull.next();
    try {
      itFull.next();
      assertTrue("#1.4", false);
    }
    catch (Exception f) {
    }
  }
  
  public void testPrev() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    ModelList<Integer>.Iterator itEmpty = fEmpty.getIterator();
    
    try {
      itEmpty.prev();
      assertTrue("#0.0", false);
    }
    catch (Exception e) {
    }
    
    itFull.insert(new Integer(6));
    itFull.insert(new Integer(5));
    itFull.insert(new Integer(4));
    itFull.next();
    itFull.next();
    itFull.next();
    
    itFull.prev();
    assertEquals("#1.1", new Integer(6), itFull.current());
    itFull.prev();
    assertEquals("#1.2", new Integer(5), itFull.current());
    itFull.prev();
    assertEquals("#1.3", new Integer(4), itFull.current());
    itFull.prev();
    
    try {
      itFull.prev();
      assertTrue("#1.5", false);
    }
    catch (Exception f) {
    }
  }
  
  public void testCurrent() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    try {
      itFull.current();
      throw new RuntimeException("Current call in initial position "+
                                 "did not fail.");
    }
    catch (Exception e) {
    }
  }
  
  public void testPrevItem() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    try {
      itFull.prevItem();
      assertTrue(false);
    }
    catch (Exception e) {
    }
    
    itFull.insert(new Integer(0));
    try {
      itFull.prevItem();
      assertTrue(false);
    }
    catch (Exception e) {
    }
    itFull.insert(new Integer(1));
    itFull.next();
    assertEquals("#0.0", new Integer(1), itFull.prevItem());
  }
  
  public void testNextItem() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    try {
      itFull.nextItem();
      assertTrue(false);
    }
    catch (Exception e) {
    }
    
    itFull.insert(new Integer(0));
    try {
      itFull.nextItem();
      assertTrue(false);
    }
    catch (Exception e) {
    }
    itFull.insert(new Integer(1));
    assertEquals("#0.0", new Integer(0), itFull.nextItem());
  }
  
  public void testCollapse() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    ModelList<Integer>.Iterator itEmpty = fEmpty.getIterator();
    ModelList<Integer>.Iterator itEmpty2 = itEmpty.copy();
    assertEquals("#0.0", 0, fEmpty.length());
    itEmpty.collapse(itEmpty2);
    assertEquals("#0.0", 0, fEmpty.length());
    
    itFull.insert(new Integer(6));
    ModelList<Integer>.Iterator itFull2 = itFull.copy();
    assertEquals("#1.0", 1, fFull.length());
    itFull.collapse(itFull2);
    assertEquals("#1.1", 1, fFull.length());
    
    itFull.insert(new Integer(5));
    assertEquals("#2.0", 2, fFull.length());
    itFull.collapse(itFull2);
    assertEquals("#2.1", 2, fFull.length());
    
    itFull.insert(new Integer(4));
    assertEquals("#3.0", 3, fFull.length());
    assertEquals("#3.0b",new Integer(4),itFull.current());
    assertEquals("#3.0a", new Integer(6), itFull2.current());
    itFull.collapse(itFull2);
    itFull.next();
    assertEquals("#3.0c",new Integer(6),itFull.current());
    assertEquals("#3.1", 2, fFull.length());
    itFull.prev();
    assertEquals("#4.0", new Integer(4), itFull.current());
    assertEquals("#4.1", new Integer(6), itFull2.current());
  }
  
  public void testNotifyInsert() {
    ModelList<Integer>.Iterator itFull2 = fFull.getIterator();
    
    itFull2.insert(new Integer(0));
    ModelList<Integer>.Iterator itFull = itFull2.copy();
    itFull2.insert(new Integer(1));
    assertEquals(new Integer(0), itFull.current());
  }
  
  public void testNotifyRemove() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    ModelList<Integer>.Iterator itFull2 = fFull.getIterator();
    
    itFull2.insert(new Integer(0));
    itFull2.insert(new Integer(1));
    itFull.next();
    itFull2.remove();
    assertEquals("#0.0", new Integer(0), itFull.current());
  }
  
  public void testNotifyCollapse() {
    ModelList<Integer>.Iterator itFull = fFull.getIterator();
    ModelList<Integer>.Iterator itFull2 = fFull.getIterator();
    ModelList<Integer>.Iterator itFull3 = fFull.getIterator();
    
    itFull2.insert(new Integer(0));
    itFull2.insert(new Integer(1));
    itFull2.insert(new Integer(2));
    itFull2.insert(new Integer(3));
    itFull2.insert(new Integer(4));
    
    assertTrue("#0.0.0",itFull.atStart());
    // we have (4 3 2 1 0), itFull2 points at 4, itFull and itFull3
    // point at 0.  We want to move itFull back to point at 2 to show
    // notifyCollapse works
    
    for (int i = 0; i < 3; i++) {
      itFull.next();
    }
    for (int j = 0; j < 5; j++) {
      itFull3.next();
    }
    assertEquals("#0.0", new Integer(2), itFull.current());
    assertEquals("#0.1", new Integer(0), itFull3.current());
    itFull2.collapse(itFull3);
    
    assertEquals("#1.0", new Integer(4), itFull2.current());
    assertEquals("#1.1", new Integer(0), itFull3.current());
    assertEquals("#1.2", new Integer(0), itFull.current());
  }
  
}








