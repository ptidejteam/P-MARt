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

package edu.rice.cs.drjava.config;

import junit.framework.*;

/**
 * Class according to the JUnit protocol. Tests
 * the proper functionality of the class IntegerOption.
 * @version $Id: IntegerOptionTest.java,v 1.1 2005/08/05 12:45:11 guehene Exp $
 */
public final class IntegerOptionTest extends TestCase
{
  /**
   * @param name The name of this test case.
   */
  public IntegerOptionTest(String name) { super(name); }
  
  public void setUp() {}
  
  public void testGetName()
  {
    IntegerOption io1 = new IntegerOption("indent_size",null);
    IntegerOption io2 = new IntegerOption("max_files",null);
    
    assertEquals("indent_size", io1.getName());
    assertEquals("max_files",   io2.getName());
  }
  
  public void testParse()
  {
    IntegerOption io = new IntegerOption("max_files",null);
    
    assertEquals(new Integer(3), io.parse("3"));
    assertEquals(new Integer(-3), io.parse("-3"));
    
    try { io.parse("true"); fail(); }
    catch (OptionParseException e) {}
    
    try { io.parse(".33"); fail(); }
    catch (OptionParseException e) {}
  }
  
  public void testFormat()
  {
    IntegerOption io1 = new IntegerOption("max_files",null);
    IntegerOption io2 = new IntegerOption("indent_size",null);
    
    assertEquals("33",  io1.format(new Integer(33)));
    assertEquals("33",  io2.format(new Integer(33)));
    assertEquals("-11", io1.format(new Integer(-11)));
    assertEquals("-11", io2.format(new Integer(-11)));
  }
}
