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

package edu.rice.cs.drjava.model.definitions.indent;

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.util.UnexpectedException;

import javax.swing.text.BadLocationException;

/**
 * Indents the current line in the document to the indent level of the
 * start of the statement previous to the one the cursor is currently on,
 * plus the given suffix string.
 *
 * @version $Id: ActionStartPrevStmtPlus.java,v 1.1 2005/04/27 20:30:32 elmoutam Exp $
 */
public class ActionStartPrevStmtPlus extends IndentRuleAction {
  private String _suffix;
  private boolean _useColon;
  
  /**
   * Constructs a new rule with the given suffix string.
   * @param suffix String to append to indent level of brace
   * @param colonIsDelim whether to include colons as statement delimiters
   */
  public ActionStartPrevStmtPlus(String suffix, boolean colonIsDelim) {
    super();
    _suffix = suffix;
    _useColon = colonIsDelim;
  }
  
  /**
   * Properly indents the line that the caret is currently on.
   * Replaces all whitespace characters at the beginning of the
   * line with the appropriate spacing or characters.
   *
   * @param doc DefinitionsDocument containing the line to be indented.
   */
  public void indentLine(DefinitionsDocument doc) {
    String indent = "";
    int here = doc.getCurrentLocation();
    
    // Find end of previous statement (or end of case statement)
    char[] delims = {';', '{', '}'};
    int lineStart = doc.getLineStartPos(here);
    int prevDelimiterPos;
    try {
      prevDelimiterPos = doc.findPrevDelimiter(lineStart, delims);
    } catch (BadLocationException e) {
      // Should not happen
      throw new UnexpectedException(e);
    }

    // For DOCSTART, align to left margin
    if(prevDelimiterPos <= DefinitionsDocument.DOCSTART) {
      doc.setTab(_suffix, here);
      return;
    }

    try {
      char delim = doc.getText(prevDelimiterPos, 1).charAt(0);
      char[] ws = {' ', '\t', '\n', ';'};
      if (delim == ';') {
        int testPos = doc.findPrevCharPos(prevDelimiterPos, ws);
        if (doc.getText(testPos,1).charAt(0) == '}') {
          prevDelimiterPos = testPos;
        }
      }
    } catch (BadLocationException e) {
      //do nothing
    }
    try {
      // Jump over {-} region if delimiter was a close brace.
      char delim = doc.getText(prevDelimiterPos, 1).charAt(0);

      if (delim == '}') {       
        //BraceReduction reduced = doc.getReduced();
        //we're pretty sure the doc is in sync.
        doc.resetReducedModelLocation();
        
        int dist = prevDelimiterPos - here + 1;
        synchronized(doc){
          doc.move(dist);
          prevDelimiterPos -= doc.balanceBackward() - 1;
          doc.move(-dist);
        }
      }
    }
    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
    
    // Get indent of prev statement
    try {
      // Include colons as end of statement (ie. "case")
      char[] indentDelims;
      char[] indentDelimsWithColon = {';', '{', '}', ':'};
      char[] indentDelimsWithoutColon = {';', '{', '}'};
      if (_useColon) {
        indentDelims = indentDelimsWithColon;
      } else {
        indentDelims = indentDelimsWithoutColon;
      }
      indent = doc.getIndentOfCurrStmt(prevDelimiterPos, indentDelims);

    } catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }

    indent = indent + _suffix;
    doc.setTab(indent, here);
  }
  
  private boolean _isPrevNonWSCharEqualTo(DefinitionsDocument doc,int pos,char c) {
    try {
      int prevPos = doc.findPrevNonWSCharPos(pos);
      if (prevPos <0) return false;
      return (doc.getText(prevPos,1).charAt(0) == c);
    }
    catch (BadLocationException e) {
      return false;
    }
  }
}

