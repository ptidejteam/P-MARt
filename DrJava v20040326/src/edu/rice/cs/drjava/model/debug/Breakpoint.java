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

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;

import java.util.Vector;
import java.util.List;
import java.util.Iterator;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import com.sun.jdi.*;
import com.sun.jdi.request.*;

/**
 * The breakpoint object which has references to its OpenDefinitionsDocument and its
 * BreakpointRequest
 */
public class Breakpoint extends DocumentDebugAction<BreakpointRequest> {

   private Position _startPos;
   private Position _endPos;

  /**
   * @throws DebugException if the document does not have a file
   */
  public Breakpoint(OpenDefinitionsDocument doc, int offset, int lineNumber, JPDADebugger manager)
    throws DebugException {

    super(manager, doc, offset);
    _suspendPolicy = EventRequest.SUSPEND_EVENT_THREAD;
    _lineNumber = lineNumber;

    DefinitionsDocument defDoc = doc.getDocument();

    try {
      _startPos = defDoc.createPosition(defDoc.getLineStartPos(offset));
      _endPos = defDoc.createPosition( defDoc.getLineEndPos(offset));
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }

    _initializeRequests(_manager.getReferenceTypes(_className, _lineNumber));
  }

  /**
   * Creates appropriate EventRequests from the EventRequestManager and
   * stores them in the _requests field.
   * @param refTypes All (identical) ReferenceTypes to which this action
   * applies.  (There may be multiple if a custom class loader is in use.)
   * @throws DebugException if the requests could not be created.
   */
  protected void _createRequests(Vector<ReferenceType> refTypes)
    throws DebugException
  {
    try {
      for (int i=0; i < refTypes.size(); i++) {
        ReferenceType rt = refTypes.get(i);

        if (!rt.isPrepared()) {
          // Not prepared, so skip this one
          continue;
        }

        // Get locations for the line number, use the first
        List lines = rt.locationsOfLine(_lineNumber);
        if (lines.size() == 0) {
          // Can't find a location on this line
          throw new DebugException("Could not find line number: " + _lineNumber);
        }
        Location loc = (Location) lines.get(0);
        BreakpointRequest request =
          _manager.getEventRequestManager().createBreakpointRequest(loc);
        _requests.add(request);
      }
    }
    catch (AbsentInformationException aie) {
      throw new DebugException("Could not find line number: " + aie);
    }
  }

  /**
   * Accessor for the offset of this breakpoint's start position
   * @return the start offset
   */
  public int getStartOffset() {
    return _startPos.getOffset();
  }

  /**
   * Accessor for the offset of this breakpoint's end position
   * @return the end offset
   */
  public int getEndOffset(){
    return _endPos.getOffset();
  }

  public String toString() {
    if (_requests.size() > 0) {
      // All BreakpointRequests are identical-- one copy for each loaded
      //  class.  So just print info from the first one, and how many there are.
      return "Breakpoint[class: " + getClassName() +
        ", lineNumber: " + getLineNumber() +
        ", method: " + _requests.get(0).location().method() +
        ", codeIndex: " + _requests.get(0).location().codeIndex() +
        ", numRefTypes: " + _requests.size() + "]";
    }
    else {
      return "Breakpoint[class: " + getClassName() +
        ", lineNumber: " + getLineNumber() + "]";
    }
  }
}
