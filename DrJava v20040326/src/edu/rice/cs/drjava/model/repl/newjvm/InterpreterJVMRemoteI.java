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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.List;
import java.io.File;
import edu.rice.cs.util.newjvm.*;

/**
 * This interface specifies the methods that the interpreter JVM exposes
 * for the MainJVM to call.
 *
 * @version $Id: InterpreterJVMRemoteI.java,v 1.1 2005/08/05 12:45:06 guehene Exp $
 */
public interface InterpreterJVMRemoteI extends SlaveRemote {
  public void interpret(String s) throws RemoteException;
  public void addClassPath(String s) throws RemoteException;
  public List<String> runTestSuite(List<String> classNames, List<File> files, boolean isTestAll)
    throws RemoteException;
  public void setPackageScope(String s) throws RemoteException;
  public void reset() throws RemoteException;
  
  /**
   * @param show Whether to show a message if a reset operation fails.
   */
  public void setShowMessageOnResetFailure(boolean show) throws RemoteException;
  
  /**
   * Adds a named DynamicJavaAdapter to the list of interpreters.
   * @param name the unique name for the interpreter
   * @throws IllegalArgumentException if the name is not unique
   */
  public void addJavaInterpreter(String name) throws RemoteException;
  
  /**
   * Adds a named JavaDebugInterpreter to the list of interpreters.
   * @param name the unique name for the interpreter
   * @param className the fully qualified class name of the class
   * the debug interpreter is in
   * @throws IllegalArgumentException if the name is not unique
   */
  public void addDebugInterpreter(String name, String className) throws RemoteException;

  /**
   * Removes the interpreter with the given name, if it exists.
   * @param name Name of the interpreter to remove
   */
  public void removeInterpreter(String name) throws RemoteException;
  
  /**
   * Sets the current interpreter to be the one specified by the given name
   * @param name the unique name of the interpreter to set active
   * @return Whether the new interpreter is currently in progress
   * with an interaction
   */
  public boolean setActiveInterpreter(String name) throws RemoteException;
  
  /**
   * Sets the default interpreter to be active.
   * @return Whether the new interpreter is currently in progress
   * with an interaction
   */
  public boolean setToDefaultInterpreter() throws RemoteException;
  
  /**
   * Returns a copy of the list of unique entries on the classpath.
   */
  public Vector<String> getAugmentedClasspath() throws RemoteException;
  
  /**
   * Gets the string representation of the value of a variable in the current interpreter.
   * @param var the name of the variable
   */
  public String getVariableToString(String var) throws RemoteException;
  
  /**
   * Gets the class name of a variable in the current interpreter.
   * @param var the name of the variable
   */
  public String getVariableClassName(String var) throws RemoteException;

  /**
   * Gets the classpath as a string.
   */
  public String getClasspathString() throws RemoteException;
  
  /**
   * Sets whether to allow private access.
   */
  public void setPrivateAccessible(boolean allow) throws RemoteException;
}
