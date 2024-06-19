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

package edu.rice.cs.drjava.model.definitions.indent;

import javax.swing.text.BadLocationException;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

/**
 * Determines if the current line is starting a new statement by
 * searching backwards to see if the previous line was the end
 * of a statement. Specifically,  checks if the previous
 * non-whitespace character not on this line is one of the
 * following: ';', '{', '}', or DOCSTART.
 * <p>
 * Note that characters in comments and quotes are disregarded. 
 *
 * @version $Id: QuestionStartingNewStmt.java,v 1.1 2005/08/05 12:45:06 guehene Exp $
 */
public class QuestionStartingNewStmt extends IndentRuleQuestion {
  
  /**
   * Constructs a new rule to determine if the current line is
   * the start of a new statement.
   * @param yesRule Rule to use if this rule holds
   * @param noRule Rule to use if this rule does not hold
   */
  public QuestionStartingNewStmt(IndentRule yesRule, IndentRule noRule) {
    super(yesRule, noRule);
  }
 
  /**
   * Determines if the previous non-whitespace character not on
   * this line was one of the following: ';', '{', '}' or DOCSTART.
   * Ignores characters in quotes and comments.
   * @param doc DefinitionsDocument containing the line to be indented.
   * @return true if this node's rule holds.
   */
  boolean applyRule(DefinitionsDocument doc, int reason) {
    
    char[] delims = {';', '{', '}'};
    int lineStart = doc.getLineStartPos(doc.getCurrentLocation());
    int prevDelimiterPos;
    
    try {
      prevDelimiterPos = doc.findPrevDelimiter(lineStart, delims);
    } catch (BadLocationException e) {
      // Should not happen
      throw new UnexpectedException(e);
    }
    
    // For DOCSTART, imaginary delimiter at position -1
    if(prevDelimiterPos == DefinitionsDocument.ERROR_INDEX) {
      prevDelimiterPos = -1;
    }
    
    // Delimiter must be at the end of its line (ignoring whitespace & comments)
    int firstNonWSAfterDelimiter;
    try {
      firstNonWSAfterDelimiter = doc.getFirstNonWSCharPos(prevDelimiterPos+1);
      // will return ERROR_INDEX if we hit the end of the document
    } catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
    
    // If the first non-WS character is after the beginning of the line
    // or we reached the end of the document, then we are starting a new statement.
    return (firstNonWSAfterDelimiter >= lineStart
              || firstNonWSAfterDelimiter == DefinitionsDocument.ERROR_INDEX);
  }
}

