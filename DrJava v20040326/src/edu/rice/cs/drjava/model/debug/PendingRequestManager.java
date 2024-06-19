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

package edu.rice.cs.drjava.model.debug;

import edu.rice.cs.drjava.DrJava;

import com.sun.jdi.*;
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

import java.util.Hashtable;
import java.util.List;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Keeps track of DocumentDebugActions that are waiting to be resolved when the
 * classes they corresponed to are prepared.  (Only DocumentDebugActions have
 * reference types which can be prepared.)
 * @version $Id: PendingRequestManager.java,v 1.1 2005/08/05 12:45:07 guehene Exp $
 */

public class PendingRequestManager {
  private JPDADebugger _manager;
  private Hashtable<String, Vector<DocumentDebugAction>> _pendingActions;

  public PendingRequestManager(JPDADebugger manager) {
    _manager = manager;
    _pendingActions = new Hashtable<String, Vector<DocumentDebugAction>>();
  }

  /**
   * Called if a breakpoint is set before its class is prepared
   * @param action The DebugAction that is pending
   */
  public void addPendingRequest (DocumentDebugAction action) {
    Vector<DocumentDebugAction> actions = null;
    String className = action.getClassName();
    actions = _pendingActions.get(className);
    if (actions == null) {
      actions = new Vector<DocumentDebugAction>();

      // only create a ClassPrepareRequest once per class
      ClassPrepareRequest request =
        _manager.getEventRequestManager().createClassPrepareRequest();
      // Listen for events from the class, and also its inner classes
      request.addClassFilter(className + "*");
      request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
      request.enable();
      //System.out.println("Creating prepareRequest in class " + className);
    }
    actions.add(action);
    _pendingActions.put(className, actions);
  }

  /**
   * Called if a breakpoint is set and removed before its class is prepared
   * @param action The DebugAction that was set and removed
   */
  public void removePendingRequest (DocumentDebugAction action) {
    Vector<DocumentDebugAction> actions = null;
    String className = action.getClassName();
    actions = _pendingActions.get(className);
    if (actions == null) {
      return;
    }
    actions.remove(action);
    // check if the vector is empty
    if (actions.size() == 0) {
      _pendingActions.remove(className);
    }
  }

  /**
   * Called by the EventHandler whenever a ClassPrepareEvent occurs.
   * This will take the event, get the class that was prepared, lookup
   * the Vector of DebugAction that was waiting for this class's preparation,
   * iterate through this Vector, and attempt to create the Breakpoints that
   * were pending. Since the keys to the HashTable are the names of the
   * outer class, the $ and everything after it must be cropped off from the
   * class name in order to do the lookup. During the lookup, however, the line
   * number of each action is checked to see if the line number is contained
   * in the given event's ReferenceType. If not, we ignore that pending action
   * since it is not in the class that was just prepared, but may be in one of its
   * inner classes.
   * @param event The ClassPrepareEvent that just occured
   */
  public void classPrepared (ClassPrepareEvent event) throws DebugException {
    ReferenceType rt = event.referenceType();
    //DrJava.consoleOut().println("In classPrepared. rt: " + rt);
    //DrJava.consoleOut().println("equals getReferenceType: " +
    //                   rt.equals(_manager.getReferenceType(rt.name())));
    String className = rt.name();

    // crop off the $ if there is one and anything after it
    int indexOfDollar = className.indexOf('$');
    if (indexOfDollar > 1) {
      className = className.substring(0, indexOfDollar);
    }

    // Get the pending actions for this class (and inner classes)
    Vector<DocumentDebugAction> actions = _pendingActions.get(className);
    Vector<DocumentDebugAction> failedActions =
      new Vector<DocumentDebugAction>();
    //DrJava.consoleOut().println("pending actions: " + actions);
    if (actions == null) {
      // Must have been a different class with a matching prefix, ignore it
      // since we're not interested in this class.
      return;
    }
    else if (actions.isEmpty()) {
      // any actions that were waiting for this class to be prepared have been
      // removed
      _manager.getEventRequestManager().deleteEventRequest(event.request());
      return;
    }
    for (int i = 0; i < actions.size(); i++) {
      int lineNumber = actions.get(i).getLineNumber();
      if (lineNumber != DebugAction.ANY_LINE) {
        try {
          List lines = rt.locationsOfLine(lineNumber);
          if (lines.size() == 0) {
            // Requested line number not in reference type, skip this action
            //i++;
            continue;
          }
        }
        catch (AbsentInformationException aie) {
          // outer class has no line number info, skip this action
          continue;
        }
      }
      // check if the action was successfully created
      try {
        Vector<ReferenceType> refTypes = new Vector<ReferenceType>();
        refTypes.add(rt);
        // next line was in condition for if
        actions.get(i).createRequests(refTypes);  // the second receiver has raw type DocumentDebugAction
                                                  // it should not generate a type warning;
        
//        if (!) {
          // if no request created, skip this action
          //i++;
//        }
//        else {
          // Experiment: try never removing the action or event request.
          //  This way, multiple classloads of this class will always have
          //  the DebugActions set properly
          /*

          // if request created, remove the current action and keep i here
          actions.remove(i);
          // check if the vector is empty
          if (actions.size() == 0) {
            _pendingActions.remove(className);
            _manager.getEventRequestManager().deleteEventRequest(event.request());
          }
        */
//        }
      }
      catch (DebugException e) {
        failedActions.add(actions.get(i));
        //i++;
       // DrJava.consoleOut().println("Exception preparing request!! " + e);
      }
    }

    // For debugging purposes
    /*
    List l = _manager.getEventRequestManager().breakpointRequests();
    System.out.println("list of eventrequestmanager's breakpointRequests: " +
                       l);
    for (int i = 0; i < l.size(); i++) {
      BreakpointRequest br = (BreakpointRequest)l.get(i);
      System.out.println("isEnabled(): " + br.isEnabled() +
                         " suspendPolicy(): " + br.suspendPolicy() +
                         " location(): " + br.location());
    }
    */
    if (failedActions.size() > 0) {
      // need to create an exception framework
      throw new DebugException("Failed actions: " + failedActions);
    }
  }
}
