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

package edu.rice.cs.drjava.model.junit;

import java.util.List;
import edu.rice.cs.drjava.model.EventNotifier;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

/**
 * Keeps track of all listeners to a JUnitModel, and has the ability
 * to notify them of some event.
 * <p>
 *
 * This class has a specific role of managing JUnitListeners.  Other
 * classes with similar names use similar code to perform the same function for
 * other interfaces, e.g. InteractionsEventNotifier and GlobalEventNotifier.
 * These classes implement the appropriate interface definition so that they
 * can be used transparently as composite packaging for a particular listener
 * interface.
 * <p>
 *
 * Components which might otherwise manage their own list of listeners use
 * EventNotifiers instead to simplify their internal implementation.  Notifiers
 * should therefore be considered a private implementation detail of the
 * components, and should not be used directly outside of the "host" component.
 * <p>
 *
 * All methods in this class must use the synchronization methods
 * provided by ReaderWriterLock.  This ensures that multiple notifications
 * (reads) can occur simultaneously, but only one thread can be adding
 * or removing listeners (writing) at a time, and no reads can occur
 * during a write.
 * <p>
 *
 * <i>No</i> methods on this class should be synchronized using traditional
 * Java synchronization!
 * <p>
 *
 * @version $Id: JUnitEventNotifier.java,v 1.1 2005/08/05 12:45:13 guehene Exp $
 */
class JUnitEventNotifier extends EventNotifier<JUnitListener>
    implements JUnitListener {

  /**
   * Called when trying to test a non-TestCase class.
   * @param isTestAll whether or not it was a use of the test all button
   */
  public void nonTestCase(boolean isTestAll) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).nonTestCase(isTestAll);
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called after JUnit is started by the GlobalModel.
   */
  public void junitStarted(List<OpenDefinitionsDocument> docs) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).junitStarted(docs);
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called to indicate that a suite of tests has started running.
   * @param numTests The number of tests in the suite to be run.
   */
  public void junitSuiteStarted(int numTests) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).junitSuiteStarted(numTests);
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when a particular test is started.
   * @param testName The name of the test being started.
   */
  public void junitTestStarted(String name) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).junitTestStarted(name);
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called when a particular test has ended.
   * @param testName The name of the test that has ended.
   * @param wasSuccessful Whether the test passed or not.
   * @param causedError If not successful, whether the test caused an error
   *  or simply failed.
   */
  public void junitTestEnded(String name, boolean wasSuccesful, boolean causedError) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).junitTestEnded(name, wasSuccesful, causedError);
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called after JUnit is finished running tests.
   */
  public void junitEnded() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).junitEnded();
      }
    }
    finally {
      _lock.endRead();
    }
  }
}

