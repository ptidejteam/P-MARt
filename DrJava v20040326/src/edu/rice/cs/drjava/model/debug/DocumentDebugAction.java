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

import com.sun.jdi.*;
import com.sun.jdi.request.*;

import java.util.Vector;
import java.io.File;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;

/**
 * Superclasses all DebugActions that are associated with specific
 * OpenDefinitionsDocuments.
 * @version $Id: DocumentDebugAction.java,v 1.1 2005/08/05 12:45:07 guehene Exp $
 */
public abstract class DocumentDebugAction<T extends EventRequest>
  extends DebugAction<T> {
  
  protected String _className;
  protected File _file;
  protected OpenDefinitionsDocument _doc;
  
  
  /**
   * Creates a new DocumentDebugAction.  Automatically tries to create the
   * EventRequest if a ReferenceType can be found, or else adds this object to the
   * PendingRequestManager. Any subclass should automatically call
   * _initializeRequest in its constructor.
   * @param manager JPDADebugger in charge
   * @param doc Document this action corresponds to
   * @param offset Offset into the document that the action affects
   */
  public DocumentDebugAction (JPDADebugger manager,
                              OpenDefinitionsDocument doc,
                              int offset)
    throws DebugException
  {
    super(manager);
    try {
      if (offset >= 0) {
        _className = doc.getDocument().getQualifiedClassName(offset);
      }
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Couldn't find class name at offset, use the first class name
      // found.
      try {
        _className = doc.getDocument().getQualifiedClassName();
      }
      catch (ClassNameNotFoundException cnnfe2) {
        // Still couldn't find a class name, use ""
        _className = "";
      }
    }
    try {
      _file = doc.getFile();
    }
    catch (FileMovedException fme) {
      throw new DebugException("This document's file no longer exists: " +
                               fme.getMessage());
    }
    catch (IllegalStateException ise) {
      throw new DebugException("This document has no file: " +
                               ise.getMessage());
    }
    _doc = doc;
  }
  
  /**
   * Returns the class name this DebugAction occurs in.
   */
  public String getClassName() {
    return _className;
  }
  
  /**
   * Returns the file this DebugAction occurs in.
   */
  public File getFile() {
    return _file;
  }
  
  /**
   * Returns the document this DebugAction occurs in.
   */
  public OpenDefinitionsDocument getDocument() {
    return _doc;
  }
  
  /**
   * Creates EventRequests corresponding to this DebugAction, using the
   * given ReferenceTypes.  This is called either from the DebugAction
   * constructor or the PendingRequestManager, depending on when the
   * ReferenceTypes become available.  (There may be multiple reference
   * types for the same class if a custom class loader is used.)
   * @return true if the EventRequest is successfully created
   */
  public boolean createRequests(Vector<ReferenceType> refTypes)
    throws DebugException
  {
    _createRequests(refTypes);
    if (_requests.size() > 0) {
      _prepareRequests(_requests);
      return true;
    }
    else {
      return false;
    }
  }
 
  /**
   * This should always be called from the constructor of the subclass.
   * Attempts to create EventRequests on the given ReferenceTypes, and
   * also adds this action to the pending request manager (so identical
   * classes loaded in the future will also have this action).
   */
  protected void _initializeRequests(Vector<ReferenceType> refTypes)
    throws DebugException
  {
    if (refTypes.size() > 0) {
      createRequests(refTypes);
    }
    //if (_request == null) {
      // couldn't create the request yet, add to the pending request manager
    
    // Experiment: always add to pending request, to deal with multpile class loads
    _manager.getPendingRequestManager().addPendingRequest(this);
    //}
  }
  
  /**
   * Creates appropriate EventRequests from the EventRequestManager and
   * stores them in the _requests field.
   * @param refTypes All (identical) ReferenceTypes to which this action
   * applies.  (There may be multiple if a custom class loader is in use.)
   * @throws DebugException if the requests could not be created.
   */
  protected abstract void _createRequests(Vector<ReferenceType> refTypes)
    throws DebugException;
  
  /**
   * Prepares this EventRequest with the current stored values.
   * @param request the EventRequest to prepare
   */
  protected void _prepareRequest(T request) {
    super._prepareRequest(request);
    request.putProperty("document", _doc);
  }
}
