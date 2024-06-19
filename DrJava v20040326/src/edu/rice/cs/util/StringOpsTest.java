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

package edu.rice.cs.util;

import junit.framework.*;
import junit.extensions.*;
import java.io.PrintWriter;

/**
 * Test functions of StringOps.
 */
public class StringOpsTest extends TestCase {
  /**
   *  Test the replace() method of StringOps class
   */
  public void testReplace() {
    String test = "aabbccdd";
    assertEquals("testReplace:", "aab12cdd", StringOps.replace(test, "bc", "12"));
    test = "cabcabc";
    assertEquals("testReplace:", "cabc", StringOps.replace(test, "cabc", "c"));
  }
  
  /**
   *  Test the getOffsetAndLength() method of StringOps class
   */
  public void testGetOffsetAndLength() {
    String test = "123456789\n123456789\n123456789\n";
    
    // The offset is always one less than the first row/col
    // The length includes the start and end positions
    Pair<Integer,Integer> oAndL = StringOps.getOffsetAndLength(test, 1, 1, 1, 9);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(0), oAndL.getFirst());
    assertEquals("testGetOffsetAndLength- length:", new Integer(9), oAndL.getSecond());
   
    oAndL = StringOps.getOffsetAndLength(test, 1, 1, 2, 3);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(0), oAndL.getFirst());
    assertEquals("testGetOffsetAndLength- length:", new Integer(13), oAndL.getSecond());
    
    oAndL = StringOps.getOffsetAndLength(test, 1, 5, 2, 3);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(4), oAndL.getFirst());
    assertEquals("testGetOffsetAndLength- length:", new Integer(9), oAndL.getSecond());

    oAndL = StringOps.getOffsetAndLength(test, 1, 1, 1, 1);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(0), oAndL.getFirst());
    assertEquals("testGetOffsetAndLength- length:", new Integer(1), oAndL.getSecond());

    oAndL = StringOps.getOffsetAndLength(test, 3, 5, 3, 5);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(24), oAndL.getFirst());
    assertEquals("testGetOffsetAndLength- length:", new Integer(1), oAndL.getSecond());

    oAndL = StringOps.getOffsetAndLength(test, 2, 3, 3, 6);
    assertEquals("testGetOffsetAndLength- offSet:", new Integer(12), oAndL.getFirst());
    assertEquals("testGetOffsetAndLength- length:", new Integer(14), oAndL.getSecond());
    
    try {
      StringOps.getOffsetAndLength(test, 3, 2, 2, 3);
      fail("Should not have been able to compute offset where startRow > endRow");
    }
    catch (IllegalArgumentException ex) {
      // correct behavior
    }

    try {
      StringOps.getOffsetAndLength(test, 2, 4, 2, 3);
      fail("Should not have been able to compute offset where start > end");
    }
    catch (IllegalArgumentException ex) {
      // correct behavior
    }

    try {
      StringOps.getOffsetAndLength(test, 4, 4, 5, 5);
      fail("Should not have been able to compute offset where the\n" +
           "given coordinates are not contained within the string");
    }
    catch (IllegalArgumentException ex) {
      // correct behavior
    }

    try {
      StringOps.getOffsetAndLength(test, 3, 4, 3, 12);
      fail("Should not have been able to compute offset where the\n" +
           "given coordinates are not contained within the string");
    }
    catch (IllegalArgumentException ex) {
      // correct behavior
    }

    try {
      StringOps.getOffsetAndLength(test, 2, 15, 3, 1);
      fail("Should not have been able to compute offset where the\n" +
           "given coordinates are not contained within the string");
    }
    catch (IllegalArgumentException ex) {
      // correct behavior
    }
  }

  /**
   * Tests that getting the stack trace of a throwable works correctly.
   */
  public void testGetStackTrace() {
    final String trace = "hello";
    Throwable t = new Throwable() {
      public void printStackTrace(PrintWriter w) {
        w.print(trace);
      }
    };
    assertEquals("Should have returned the correct stack trace!", trace, StringOps.getStackTrace(t));
  }
}