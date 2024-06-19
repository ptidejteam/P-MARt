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

/**
 * Interface for an interpreter of Java source code.
 * @version $Id: JavaInterpreter.java,v 1.1 2005/08/05 12:45:09 guehene Exp $
 */
public interface JavaInterpreter extends Interpreter {

  /**
   * Adds the given path to the interpreter's classpath.
   * @param path Path to add
   */
  public void addClassPath(String path);

  /**
   * Set the scope for unqualified names to be the given package.
   * @param packageName Package to use for the current scope.
   */
  public void setPackageScope(String packageName);
  
  /**
   * Returns the value of the variable with the given name in
   * the interpreter.
   * @param name Name of the variable
   * @return Value of the variable
   */
  public Object getVariable(String name);
  
  /**
   * Returns the class of the variable with the given name in
   * the interpreter.
   * @param name Name of the variable
   * @return class of the variable
   */
  public Class getVariableClass(String name);
  
    /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value Value to assign
   */
  public void defineVariable(String name, Object value);

  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value boolean to assign
   */
  public void defineVariable(String name, boolean value);

  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value byte to assign
   */
  public void defineVariable(String name, byte value);

  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value char to assign
   */
  public void defineVariable(String name, char value);

  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value double to assign
   */
  public void defineVariable(String name, double value);

  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value float to assign
   */
  public void defineVariable(String name, float value);


  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value int to assign
   */
  public void defineVariable(String name, int value);

  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value long to assign
   */
  public void defineVariable(String name, long value);

  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value short to assign
   */
  public void defineVariable(String name, short value);

  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value Value to assign
   */
  public void defineConstant(String name, Object value);

  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value boolean to assign
   */
  public void defineConstant(String name, boolean value);

  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value byte to assign
   */
  public void defineConstant(String name, byte value);

  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value char to assign
   */
  public void defineConstant(String name, char value);

  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value double to assign
   */
  public void defineConstant(String name, double value);

  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value float to assign
   */
  public void defineConstant(String name, float value);

  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value int to assign
   */
  public void defineConstant(String name, int value);

  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value long to assign
   */
  public void defineConstant(String name, long value);

  /**
   * Assigns the given value to the given name as a constant in the interpreter.
   * @param name Name of the variable
   * @param value short to assign
   */
  public void defineConstant(String name, short value);
  
  /**
   * Sets whether protected and private variables should be accessible in
   * the interpreter.
   * @param accessible Whether protected and private variable are accessible
   */
  public void setPrivateAccessible(boolean accessible);
}
