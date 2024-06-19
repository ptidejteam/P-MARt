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

import javax.swing.text.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

/**
 * Question rule in the indentation decision tree.  Determines if the
 * current line starts a new "phrase" within a parenthesized expression.
 * Specifically, this rule determines if the previous line ends in a
 * comma, semicolon, open paren, or open bracket.  Note that whitespace,
 * blank lines, and comments are disregarded.
 * 
 * @version $Id: QuestionNewParenPhrase.java,v 1.1 2005/08/05 12:45:56 guehene Exp $
 */
public class QuestionNewParenPhrase extends IndentRuleQuestion {
  
  /**
   * Constructs a new rule to determine if the current line starts
   * new paren phrase.
   * @param yesRule Rule to use if this rule holds
   * @param noRule Rule to use if this rule does not hold
   */
  public QuestionNewParenPhrase(IndentRule yesRule, IndentRule noRule) {
    super(yesRule, noRule);
  }
 
  /**
   * Determines if the previous line ends in a comma, semicolon,
   * open paren, open bracket, operator, or comparator.
   * @param doc DefinitionsDocument containing the line to be indented.
   * @return true if this node's rule holds.
   */
  boolean applyRule(DefinitionsDocument doc) {

    try {
      // Find start of line
      int here = doc.getCurrentLocation();
      int startLine = doc.getLineStartPos(here);
      
      if (startLine > DefinitionsDocument.DOCSTART) {
        // Find previous delimiter (looking in paren phrases)
        char[] delims = {';', ',', '(', '[', 
          '&', '|', '+', '-', '*', '/', '%', 
          '=', '<', '>', '}'
        };
        int prevDelim = doc.findPrevDelimiter(startLine, delims, false);
        if (prevDelim == DefinitionsDocument.ERROR_INDEX) {
          return false;
        }
        
        // Make sure the delim is the previous non-WS char
        int nextNonWS = doc.getFirstNonWSCharPos(prevDelim + 1);
        if (nextNonWS == DefinitionsDocument.ERROR_INDEX) {
          nextNonWS = startLine;
        }
        return (nextNonWS >= startLine);
      }
    }
    catch (BadLocationException e) {
      // Shouldn't happen
      throw new UnexpectedException(e);
    }
    // On first line
    return false;
  }
}
