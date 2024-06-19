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

package edu.rice.cs.drjava.model.debug;

import java.io.*;
import java.util.List;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.ListModel;

import gj.util.Enumeration;
import gj.util.Hashtable;
import gj.util.Vector;

// DrJava stuff
import edu.rice.cs.util.StringOps;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.GlobalModelListener;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.OperationCanceledException;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.config.OptionConstants;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

/**
 * An integrated debugger which attaches to the Interactions JVM using
 * Sun's Java Platform Debugger Architecture (JPDA/JDI) interface.
 * 
 * @version $Id: JPDADebugger.java,v 1.1 2005/04/27 20:30:32 elmoutam Exp $
 */
public class JPDADebugger implements Debugger {
  
  /**
   * Reference to DrJava's model.
   */
  private GlobalModel _model;
  
  /**
   * VirtualMachine of the interactions JVM.
   */
  private VirtualMachine _vm;
  
  /**
   * Manages all event requests in JDI.
   */
  private EventRequestManager _eventManager;

  /**
   * Vector of all current Breakpoints, with and without EventRequests.
   */
  private Vector<Breakpoint> _breakpoints;

  /**
   * Vector of all current Watches
   */
  private Vector<DebugWatchData> _watches;
  
  /**
   * Keeps track of any DebugActions whose classes have not yet been
   * loaded, so that EventRequests can be created when the correct
   * ClassPrepareEvent occurs.
   */
  private PendingRequestManager _pendingRequestManager;
  
  /**
   * Provides a way for the JPDADebugger to communicate with the view.
   */
  private LinkedList _listeners;
  
  /**
   * The Thread that the JPDADebugger is currently analyzing.
   */
  private ThreadReference _thread;
  
  /**
   * Builds a new JPDADebugger to debug code in the Interactions JVM,
   * using the JPDA/JDI interfaces.
   * Does not actually connect to the InteractionsJVM until startup().
   */
  public JPDADebugger(GlobalModel model) {
    _model = model;
    _vm = null;
    _eventManager = null;
    _thread = null;
    _listeners = new LinkedList();
    _breakpoints = new Vector<Breakpoint>();
    _watches = new Vector<DebugWatchData>();
    _pendingRequestManager = new PendingRequestManager(this);
  }
  
  /**
   * Returns whether the debugger is currently available in this JVM.
   * This does not indicate whether it is ready to be used.
   */
  public boolean isAvailable() {
    return true;
  }

  /**
   * Attaches the debugger to the Interactions JVM to prepare for debugging.
   */
  public synchronized void startup() throws DebugException {
    if (!isReady()) {
      // check if all open documents are in sync
      ListModel list = _model.getDefinitionsDocuments();
      for (int i = 0; i < list.getSize(); i++) {
        OpenDefinitionsDocument currDoc = (OpenDefinitionsDocument)list.getElementAt(i);
        currDoc.checkIfClassFileInSync();
      }
      _attachToVM();
      ThreadDeathRequest tdr = _eventManager.createThreadDeathRequest();
      tdr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
      tdr.enable();
      EventHandler eventHandler = new EventHandler(this, _vm);
      eventHandler.start();
    }
  }
  
  /**
   * Handles the details of attaching to the InteractionsJVM.
   */
  private void _attachToVM() throws DebugException {
    // Blocks until interpreter has registered itself
    _model.waitForInterpreter();
    
    // Get the connector
    VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
    List connectors = vmm.attachingConnectors();
    AttachingConnector connector = null;
    java.util.Iterator iter = connectors.iterator();
    while (iter.hasNext()) {
      AttachingConnector conn = (AttachingConnector)iter.next();
      if (conn.name().equals("com.sun.jdi.SocketAttach")) {
        connector = conn;
      }
    }
    if (connector == null) {
      throw new DebugException("Could not find an AttachingConnector!");
    }
    
    // Try to connect on our debug port
    Map args = connector.defaultArguments();
    Connector.Argument port = (Connector.Argument) args.get("port");
    try {
      int debugPort = _model.getDebugPort();
      port.setValue("" + debugPort);
      _vm = connector.attach(args);
      _eventManager = _vm.eventRequestManager();
    }
    catch (IOException ioe) {
      throw new DebugException("Could not connect to VM: " + ioe);
    }
    catch (IllegalConnectorArgumentsException icae) {
      throw new DebugException("Could not connect to VM: " + icae);
    }
  }
  
  /**
   * Disconnects the debugger from the Interactions JVM and cleans up
   * any state.
   */
  public synchronized void shutdown() {
    if (isReady()) {
      try {
        removeAllBreakpoints();
        removeAllWatches();
        _vm.dispose();
      }
      catch (VMDisconnectedException vmde) {
        //VM was shutdown prematurely
      }
      finally {
        _vm = null;
        _eventManager = null;
      }
    }
  }
  
  /**
   * Returns the status of the debugger
   */
  public synchronized boolean isReady() {
    return _vm != null;
  }
  
  
  /**
   * Returns the current EventRequestManager from JDI, or null if 
   * startup() has not been called.
   */
  synchronized EventRequestManager getEventRequestManager() {
    return _eventManager;
  }
  
  /**
   * Returns the pending request manager used by the debugger.
   */
  synchronized PendingRequestManager getPendingRequestManager() {
    return _pendingRequestManager;
  }
  
  /**
   * Sets the debugger's currently active thread.
   */
  synchronized void setCurrentThread(ThreadReference thread) {
    _thread = thread;
  }
  
  /**
   * Returns the debugger's currently active thread.
   */
  synchronized ThreadReference getCurrentThread() {
    return _thread;
  }
  
  /**
   * Returns the loaded ReferenceTypes for the given class name, or null
   * if the class could not be found.  Makes no attempt to load the class
   * if it is not already loaded.
   * <p>
   * If custom class loaders are in use, multiple copies of the class may
   * be loaded, so all are returned.
   */
  synchronized Vector<ReferenceType> getReferenceTypes(String className) {
    return getReferenceTypes(className, DebugAction.ANY_LINE);
  }
  
  /**
   * Returns the loaded ReferenceTypes for the given class name, or null
   * if the class could not be found.  Makes no attempt to load the class
   * if it is not already loaded.  If the lineNumber is not DebugAction.ANY_LINE,
   * ensures that the returned ReferenceTypes contain the given lineNumber,
   * searching through inner classes if necessary.  If no inner classes
   * contain the line number, null is returned.
   * <p>
   * If custom class loaders are in use, multiple copies of the class
   * may be loaded, so all are returned.
   */
  synchronized Vector<ReferenceType> getReferenceTypes(String className, 
                                                       int lineNumber) {
    // Get all classes that match this name
    List classes = _vm.classesByName(className);
    
    // Assume first one is correct, for now
    //if (classes.size() > 0) {
    
    // Return each valid reference type
    Vector<ReferenceType> refTypes = new Vector<ReferenceType>();
    ReferenceType ref = null;
    for (int i=0; i < classes.size(); i++) {
      ref = (ReferenceType) classes.get(i);
      
      if (lineNumber > DebugAction.ANY_LINE) {
        List lines = new LinkedList();
        try {
          lines = ref.locationsOfLine(lineNumber);
        }
        catch (AbsentInformationException aie) {
          // try looking in inner classes
        }
        if (lines.size() == 0) {
          // The ReferenceType might be in an inner class
          List innerRefs = ref.nestedTypes();
          ref = null;
          for (int j = 0; j < innerRefs.size(); j++) {
            try {
              ReferenceType currRef = (ReferenceType) innerRefs.get(j);
              lines = currRef.locationsOfLine(lineNumber);
              if (lines.size() > 0) {
                ref =currRef;
                break;                
              }
            }
            catch (AbsentInformationException aie) {
              // skipping this inner class, look in another
            }
            catch (ClassNotPreparedException cnpe) {
              // skipping this inner class, look in another
            }
          }
        }
      }
      if ((ref != null) && ref.isPrepared()) {
        refTypes.addElement(ref);
      }
      //if (ref != null && !ref.isPrepared()) {
      //   return null;
      //}
    }
    return refTypes;
  }
  
  
  /**
   * Suspends execution of the currently running document.
   */
  public synchronized void suspend() {
    if (!isReady()) return;
    
    if (_thread == null)
      return;
    _thread.suspend();
    currThreadSuspended();
  }
  
  /**
   * Resumes execution of the currently loaded document.
   */
  public synchronized void resume() {
    if (!isReady()) return;
    
    if (_thread == null)
      return;
    
    int suspendCount = _thread.suspendCount();
    for (int i=suspendCount; i>0; i--) {
      _thread.resume();
    }
    currThreadResumed();
  }
    
  /** 
   * Steps into the execution of the currently loaded document.
   * @flag The flag denotes what kind of step to take. The following mark valid options:
   * StepRequest.STEP_INTO
   * StepRequest.STEP_OVER
   * StepRequest.STEP_OUT
   */
  public synchronized void step(int flag) throws DebugException {
    if (!isReady() || (_thread == null)) return;

    // don't allow the creation of a new StepRequest if there's already one on
    // the current thread
    List steps = _eventManager.stepRequests();    
    for (int i = 0; i < steps.size(); i++) {
      StepRequest step = (StepRequest)steps.get(i);
      if (step.thread().equals(_thread)) {
        if (!_thread.isSuspended())
          return;
        else {
          _eventManager.deleteEventRequest(step);
          break;
        }
      }
    }
        
    Step step = new Step(this, StepRequest.STEP_LINE, flag);
    notifyStepRequested();
    resume();
  }
  
  /**
   * Called from interactionsEnded in MainFrame in order to clear any current 
   * StepRequests that remain.
   */
  public synchronized void clearCurrentStepRequest() {   
    List steps = _eventManager.stepRequests();   
    for (int i = 0; i < steps.size(); i++) {
      StepRequest step = (StepRequest)steps.get(i);
      if (step.thread().equals(_thread)) {
        _eventManager.deleteEventRequest(step);
        return;
      }
    }
  }

  /**
   * Adds a watch on the given field or variable.
   * @param field the name of the field we will watch
   */
  public synchronized void addWatch(String field) {
    if (!isReady()) return;
    
    _watches.addElement(new DebugWatchData(field));
    _updateWatches();
  }
  
  /**
   * Removes any watches on the given field or variable.
   * @param field the name of the field we will watch
   */
  public synchronized void removeWatch(String field) {
    if (!isReady()) return;
    
    for (int i=0; i < _watches.size(); i++) {
      DebugWatchData watch = _watches.elementAt(i);
      if (watch.getName().equals(field)) {
        _watches.removeElementAt(i);
      }
    }
  }
  
  /**
   * Removes the watch at the given index.
   * @param index Index of the watch to remove
   */
  public synchronized void removeWatch(int index) {
    if (!isReady()) return;
    
    if (index < _watches.size()) {
      _watches.removeElementAt(index);
    }
  }
  
  /**
   * Removes all watches on existing fields and variables.
   */
  public synchronized void removeAllWatches() {
    _watches = new Vector<DebugWatchData>();
  }
  

  /**
   * Toggles whether a breakpoint is set at the given line in the given
   * document.
   * @param doc Document in which to set or remove the breakpoint
   * @param offset Start offset on the line to set the breakpoint
   * @param lineNumber Line on which to set or remove the breakpoint
   */
  public synchronized void toggleBreakpoint(OpenDefinitionsDocument doc, 
                                            int offset, int lineNum)
    throws DebugException
  {
    if (!isReady()) return;
    Breakpoint breakpoint = doc.getBreakpointAt(offset);
    if (breakpoint == null) {
      setBreakpoint(new Breakpoint (doc, offset, lineNum, this));
    }
    else {
      removeBreakpoint(breakpoint);
    }
  }
  
  /**
   * Sets a breakpoint.
   *
   * @param breakpoint The new breakpoint to set
   */
  public synchronized void setBreakpoint(final Breakpoint breakpoint)
  {
    if (!isReady()) return;
    
    breakpoint.getDocument().checkIfClassFileInSync();
    // update UI back in MainFrame
    
    _breakpoints.addElement(breakpoint);
    breakpoint.getDocument().addBreakpoint(breakpoint);
    
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.breakpointSet(breakpoint);
      }
    });
  }
  
 /**
  * Removes a breakpoint.
  * Called from ToggleBreakpoint -- even with BPs that are not active.
  *
  * @param breakpoint The breakpoint to remove.
  * @param className the name of the class the BP is being removed from.
  */
  public synchronized void removeBreakpoint(final Breakpoint breakpoint) {
    if (!isReady()) return;
    
    _breakpoints.removeElement(breakpoint);
    
    Vector<BreakpointRequest> requests = breakpoint.getRequests();
    if ( requests.size() > 0 && _eventManager != null) {
      try {
        for (int i=0; i < requests.size(); i++) {
          _eventManager.deleteEventRequest(requests.elementAt(i));
        }
      }
      catch (VMMismatchException vme) {
        // Not associated with this VM; probably from a previous session.
        // Ignore and make sure it gets removed from the document.
      }
      catch (VMDisconnectedException vmde) {
        // The VM has already disconnected for some reason
        // Ignore it and make sure the breakpoint gets removed from the document
      }
    }
    //else {
    // Now always remove from pending request, since it's always there
    _pendingRequestManager.removePendingRequest(breakpoint);
    //}
    breakpoint.getDocument().removeBreakpoint(breakpoint);
    
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.breakpointRemoved(breakpoint);
      }
    });
  }

  /**
   * Removes all the breakpoints from the manager's vector of breakpoints.
   */
  public synchronized void removeAllBreakpoints() {
    while (_breakpoints.size() > 0) {
      removeBreakpoint( _breakpoints.elementAt(0));
    }
  }

  /**
   * Called when a breakpoint is reached.  The Breakpoint object itself
   * should be stored in the "debugAction" property on the request.
   * @param request The BreakPointRequest reached by the debugger
   */
  synchronized void reachedBreakpoint(BreakpointRequest request) {
    Object property = request.getProperty("debugAction");
    if ( (property!=null) && (property instanceof Breakpoint)) {
      final Breakpoint breakpoint = (Breakpoint)property;
      _model.printDebugMessage("Breakpoint hit in class " + 
                               breakpoint.getClassName() + "  [line " +
                               breakpoint.getLineNumber() + "]");
      notifyListeners(new EventNotifier() {
        public void notifyListener(DebugListener l) {
          l.breakpointReached(breakpoint);
        }
      });
    }
    else {
      // A breakpoint we didn't set??
    }
  }
  
  /**
   * Returns a Vector<Breakpoint> that contains all of the Breakpoint objects that
   * all open documents contain.
   */
  public synchronized Vector<Breakpoint> getBreakpoints() {
    Vector<Breakpoint> sortedBreakpoints = new Vector<Breakpoint>();
    ListModel docs = _model.getDefinitionsDocuments();
    for (int i = 0; i < docs.getSize(); i++) {
      Vector<Breakpoint> docBreakpoints = 
        ((OpenDefinitionsDocument)docs.getElementAt(i)).getBreakpoints();
      for (int j = 0; j < docBreakpoints.size(); j++) {
        sortedBreakpoints.addElement(docBreakpoints.elementAt(j));
      }      
    }
    return sortedBreakpoints;
  }
  
  /**
   * Prints the list of breakpoints in the current session of DrJava, both pending
   * resolved Breakpoints are listed
   */
  public synchronized void printBreakpoints() {
    Enumeration<Breakpoint> breakpoints = getBreakpoints().elements();
    if (breakpoints.hasMoreElements()) {
      _model.printDebugMessage("Breakpoints: ");
      while (breakpoints.hasMoreElements()) {
        Breakpoint breakpoint = breakpoints.nextElement();
        _model.printDebugMessage("  " + breakpoint.getClassName() +
                                 "  [line " + breakpoint.getLineNumber() + "]");
      }
    }
    else {
      _model.printDebugMessage("No breakpoints set.");
    }
  }
  
  /**
   * Returns all currently watched fields and variables.
   */
  public synchronized Vector<DebugWatchData> getWatches() {
    return _watches;
  }
  
  /**
   * Returns a Vector of DebugThreadData or null if the vm is null
   */
  public synchronized Vector<DebugThreadData> getCurrentThreadData() {
    if (!isReady()) return null;

    Iterator iter = _vm.allThreads().iterator();
    Vector<DebugThreadData> threads = new Vector<DebugThreadData>();
    while (iter.hasNext()) {      
      threads.addElement(new DebugThreadData((ThreadReference)iter.next()));                                                  
    }
    return threads;
  }
  
  /**
   * Returns a Vector of DebugStackData for the current thread or null if the 
   * current thread is null
   * TO DO: Config option for hiding DrJava subset of stack trace
   */
  public synchronized Vector<DebugStackData> getCurrentStackFrameData() {
    if (!isReady() || (_thread == null) || !_thread.isSuspended()) {
      return null;
    }
    
    Iterator iter = null;
    try {
      iter = _thread.frames().iterator();
      Vector<DebugStackData> frames = new Vector<DebugStackData>();
      while (iter.hasNext()) {
        frames.addElement(new DebugStackData((StackFrame)iter.next()));
      }
      return frames;
    }
    catch (IncompatibleThreadStateException itse) {
      return null;
    }
  }
  
  /**
   * Takes the location of event e, opens the document corresponding to its class
   * and centers the definition pane's view on the appropriate line number
   * @param e should be a LocatableEvent
   */
  synchronized void scrollToSource(LocatableEvent e) {
    Location location = e.location();
    OpenDefinitionsDocument doc = null;
    
    // First see if doc is stored
    EventRequest request = e.request();
    Object docProp = request.getProperty("document");
    if ((docProp != null) && (docProp instanceof OpenDefinitionsDocument)) {
      doc = (OpenDefinitionsDocument) docProp;
    }
    else {
      // No stored doc, look on the source root set (later, also the sourcepath)
      ReferenceType rt = location.declaringType();
      String filename = "";
      try {
        filename = rt.sourceName();
        filename = getPackageDir(rt.name()) + filename;
      }
      catch (AbsentInformationException aie) {
        // Don't know real source name:
        //   assume source name is same as file name
        String className = rt.name();
        String ps = System.getProperty("file.separator");
        // replace periods with the System's file separator
        className = StringOps.replace(className, ".", ps);
        
        // crop off the $ if there is one and anything after it
        int indexOfDollar = className.indexOf('$');    
        if (indexOfDollar > -1) {
          className = className.substring(0, indexOfDollar);
        }
      
        filename = className + ".java";
      }
        
      // Check source root set (open files)
      File[] sourceRoots = _model.getSourceRootSet();
      Vector<File> roots = new Vector<File>();
      for (int i=0; i < sourceRoots.length; i++) {
        roots.addElement(sourceRoots[i]);
      }
      File f = _model.getSourceFileFromPaths(filename, roots);
      if (f == null) {
        Vector<File> sourcepath = 
          DrJava.getConfig().getSetting(OptionConstants.DEBUG_SOURCEPATH);
        f = _model.getSourceFileFromPaths(filename, sourcepath);
      }
      
      if (f != null) {
        // Get a document for this file, forcing it to open
        try {
          doc = _model.getDocumentForFile(f);
        }
        catch (IOException ioe) {
          // No doc, so don't notify listener
        }
        catch (OperationCanceledException oce) {
          // No doc, so don't notify listener
        }
      }
    }
    
    // Open and scroll if doc was found
    if (doc != null) {
      doc.checkIfClassFileInSync();
      // change UI if in sync in MainFrame listener
      
      final OpenDefinitionsDocument docF = doc;
      final Location locationF = location;
      
      notifyListeners(new EventNotifier() {
        public void notifyListener(DebugListener l) {
          l.threadLocationUpdated(docF, locationF.lineNumber());
        }
      });
    }
    else {
      String className = location.declaringType().name();
      printMessage("  (Source for " + className + " not found.)");
    }
  }  
  
  /**
   * Returns the relative directory (from the source root) that the source
   * file with this qualifed name will be in, given its package.
   * Returns the empty string for classes without packages.
   * @param className The fully qualified class name
   */
  String getPackageDir(String className) {
    // Only keep up to the last dot
    int lastDotIndex = className.lastIndexOf(".");
    if (lastDotIndex == -1) {
      // No dots, so no package
      return "";
    }
    else {
      String packageName = className.substring(0, lastDotIndex);
      // replace periods with the System's file separator
      String ps = System.getProperty("file.separator");
      packageName = StringOps.replace(packageName, ".", ps);
      return packageName + ps;
    }
  }
  
  /**
   * Prints a message in the Interactions Pane.
   * @param message Message to display
   */
  synchronized void printMessage(String message) {
    _model.printDebugMessage(message);
  }

  private void _updateWatches() {
    if (!isReady() || (_thread == null)) return;
    
    try {
      int stackIndex = 0;
      StackFrame currFrame = null;
      List frames = null;
      frames = _thread.frames();
      currFrame = (StackFrame) frames.get(stackIndex);
      stackIndex++;
      Location location = currFrame.location();
      ReferenceType rt = location.declaringType();
      for (int i = 0; i < _watches.size(); i++) {
        DebugWatchData currWatch = _watches.elementAt(i);
        String currName = currWatch.getName();
        String currValue = currWatch.getValue();
        // check for "this"
        if (currName.equals("this")) {
          ObjectReference obj = currFrame.thisObject();
          if (obj != null) {
            currWatch.setValue(_getValue(obj));
            currWatch.setType(obj.type());
          }
          else {
            currWatch.setValue(DebugWatchUndefinedValue.ONLY);
            currWatch.setType(null);
          }
          continue;
        } 
        //List frames = null;
        LocalVariable localVar = null;
        try {
          localVar = currFrame.visibleVariableByName(currName);
        }
        catch (AbsentInformationException aie) {
        }
        
        ReferenceType outerRt = rt;
        // if the variable being watched is not a local variable, check if it's a field
        if (localVar == null) {
          Field field = outerRt.fieldByName(currName);
          
          // if the variable is not a field either, it's not defined in this 
          // ReferenceType's scope, keep going further out in scope.
          String className = outerRt.name();
          while (field == null) {
            
            // crop off the $ if there is one and anything after it
            int indexOfDollar = className.lastIndexOf('$');    
            if (indexOfDollar > -1) {
              className = className.substring(0, indexOfDollar);
            }
            else {
              // There is no $ in the className, we're at the outermost class and the
              // field still was not found
              break;
            }
            outerRt = (ReferenceType)_vm.classesByName(className).get(0);
            if (outerRt == null) {
              break;
            }
            field = outerRt.fieldByName(currName);
          }
          if (field != null) {
            // check if the field is static
            if (field.isStatic()) {
              currWatch.setValue(_getValue(outerRt.getValue(field)));
              try {
                currWatch.setType(field.type());
              }
              catch (ClassNotLoadedException cnle) {
                currWatch.setType(null);
              }
            }
            else {
              StackFrame outerFrame = currFrame;
              // the field is not static
              // Check if the frame represents a native or static method and
              // keep going down the stack frame looking for the frame that
              // has the same ReferenceType that we found the Field in.
              // This is a hack, remove it to slightly improve performance but
              // at the loss of ever being able to watch outer instance
              // fields. If unremoved, this will work sometimes, but not always.
              while (outerFrame.thisObject() != null && 
                     !outerFrame.thisObject().referenceType().equals(outerRt) &&
                     stackIndex < frames.size()) {
                outerFrame = (StackFrame) frames.get(stackIndex);
                stackIndex++;
              }
              if (stackIndex < frames.size() && outerFrame.thisObject() != null) { 
                // then we found the right stack frame
                currWatch.setValue(_getValue(outerFrame.thisObject().getValue(field)));
                try {
                  currWatch.setType(field.type());
                }
                catch (ClassNotLoadedException cnle) {
                  currWatch.setType(null);
                }
              }
              else {
                currWatch.setValue(DebugWatchUndefinedValue.ONLY);
                currWatch.setType(null);
              }
            }
          }
          else {
            currWatch.setValue(DebugWatchUndefinedValue.ONLY);
            currWatch.setType(null);
          }
        }
        else {
          currWatch.setValue(_getValue(currFrame.getValue(localVar)));
          try {
            currWatch.setType(localVar.type());
          }
          catch (ClassNotLoadedException cnle) {
            currWatch.setType(null);
          }
        }
      }
    }
    catch (IncompatibleThreadStateException itse) {
      return;
    }
    catch (InvalidStackFrameException isfe) {
      return;
    }
  }

  /**
   * Takes a jdi Value and gets its String representation
   * @param value the Value whose value is requested
   * @return the String representation of the Value
   */
  private String _getValue(Value value) {
    // Most types work as they are; for the rest, for now, only care about getting
    // accurate toString for Objects
    if (value == null) {
      return "null";
    }
    
    if (!(value instanceof ObjectReference)) {
      return value.toString();
    }
    ObjectReference object = (ObjectReference) value;
    ReferenceType rt = object.referenceType();
    ThreadReference thread = null;
    thread = _thread;
    /*try {
      thread = object.owningThread();
    }
    catch (IncompatibleThreadStateException itse) {
      DrJava.consoleOut().println("thread is not suspended");
      return DebugWatchUndefinedValue.ONLY.toString();
    }*/
    List toStrings = rt.methodsByName("toString");
    if (toStrings.size() == 0) {
      // not sure how an Object can't have a toString method, but it happens
      return value.toString();
    }
    // Assume that there's only one method named toString
    Method method = (Method)toStrings.get(0);
    Value stringValue = null;
    try {
      stringValue = object.invokeMethod(thread, method, new LinkedList(), ObjectReference.INVOKE_SINGLE_THREADED);
    }
    catch (InvalidTypeException ite) {
      // shouldn't happen, not passing any arguments to toString()
    }
    catch (ClassNotLoadedException cnle) {
      // once again, no arguments
    }
    catch (IncompatibleThreadStateException itse) {
      DrJava.consoleOut().println("thread is not suspended");
      return DebugWatchUndefinedValue.ONLY.toString();
    }
    catch (InvocationException ie) {
      DrJava.consoleOut().println("invocation exception");
      return DebugWatchUndefinedValue.ONLY.toString();
    }
    return stringValue.toString();
  }
  
  /**
   * Notifies all listeners that the current thread has been suspended.
   */
  synchronized void currThreadSuspended() {
    _updateWatches();
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.currThreadSuspended();
      }
    });
  }
  
  
  /**
   * Notifies all listeners that the current thread has been resumed.
   */
  synchronized void currThreadResumed() {
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.currThreadResumed();
      }
    });
  }
  
  /**
   * Notifies all listeners that the current thread has died.
   */
  synchronized void currThreadDied() {
    _model.printDebugMessage("The current thread has finished.");
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.currThreadDied();
      }
    });
  }
  
  /**
   * Notifies all listeners that the debugger has shut down.
   */
  synchronized void notifyDebuggerShutdown() {
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.debuggerShutdown();
      }
    });
  }

  /**
   * Notifies all listeners that the debugger has started.
   */
  synchronized void notifyDebuggerStarted() {
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.debuggerStarted();
      }
    });
  }
  
  /**
   * Notifies all listeners that a step has been requested.
   */
  synchronized void notifyStepRequested() {
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.stepRequested();
      }
    });
  }
  
  /**
   * Adds a listener to this JPDADebugger.
   * @param listener a listener that reacts on events generated by the JPDADebugger
   */
  public synchronized void addListener(DebugListener listener) {
    _listeners.addLast(listener);
  }

  /**
   * Removes a listener to this JPDADebugger.
   * @param listener listener to remove
   */
  public synchronized void removeListener(DebugListener listener){
    _listeners.remove(listener);
  }
  
  /**
   * Lets the listeners know some event has taken place.
   * @param EventNotifier n tells the listener what happened
   */
  protected void notifyListeners(EventNotifier n) {
    synchronized(_listeners) {
      ListIterator i = _listeners.listIterator();

      while(i.hasNext()) {
        DebugListener cur = (DebugListener) i.next();
        n.notifyListener(cur);
      }
    }
  }
  
  /**
   * Class model for notifying listeners of an event.
   */
  protected abstract class EventNotifier {
    public abstract void notifyListener(DebugListener l);
  }
  
}
