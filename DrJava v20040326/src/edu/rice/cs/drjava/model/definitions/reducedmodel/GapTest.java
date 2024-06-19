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
 * Tests the functionality of the Gap class.
 * @version $Id: GapTest.java,v 1.1 2005/08/05 12:45:09 guehene Exp $
 */
public final class GapTest extends TestCase {

  /**
   * put your documentation comment here
   * @param     String name
   */
  public GapTest(String name) {
    super(name);
  }

  /**
   * put your documentation comment here
   */
  public void setUp() {}

  /**
   * put your documentation comment here
   * @return 
   */
  public static Test suite() {
    return  new TestSuite(GapTest.class);
  }

  /**
   * put your documentation comment here
   */
  public void testGrow() {
    Gap gap0 = new Gap(0, ReducedToken.FREE);
    Gap gap1 = new Gap(1, ReducedToken.FREE);
    gap0.grow(5);
    assertEquals(5, gap0.getSize());
    gap0.grow(0);
    assertEquals(5, gap0.getSize());
    gap1.grow(-6);
    assertEquals(1, gap1.getSize());
  }

  /**
   * put your documentation comment here
   */
  public void testShrink() {
    Gap gap0 = new Gap(5, ReducedToken.FREE);
    Gap gap1 = new Gap(1, ReducedToken.FREE);
    gap0.shrink(3);
    assertEquals(2, gap0.getSize());
    gap0.shrink(0);
    assertEquals(2, gap0.getSize());
    gap1.shrink(3);
    assertEquals(1, gap1.getSize());
    gap1.shrink(-1);
    assertEquals(1, gap1.getSize());
  }
}



