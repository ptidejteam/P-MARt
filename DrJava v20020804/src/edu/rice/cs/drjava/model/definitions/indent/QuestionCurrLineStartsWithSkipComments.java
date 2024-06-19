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
 * Determines whether or not the current line in the document starts
 * with a specific character sequence, skipping over any comments on that line.
 * The character sequence is passed to the constructor of the class as a String
 * argument.
 *
 * @version $Id: QuestionCurrLineStartsWithSkipComments.java,v 1.1 2005/03/03 19:29:46 toeivesb Exp $
 */
public class QuestionCurrLineStartsWithSkipComments extends IndentRuleQuestion
{
  /**
   * The String to be matched. This String may not contain whitespace characters
   * or comment-delimiting characters.
   */
  private String _prefix;
  
  /**
   * @param yesRule The decision subtree for the case that this rule applies
   * in the current context.
   * @param noRule The decision subtree for the case that this rule does not
   * apply in the current context.
   */
  public QuestionCurrLineStartsWithSkipComments(String prefix, IndentRule yesRule, IndentRule noRule)
  {
    super(yesRule, noRule);
    _prefix = prefix;
  }
  
  /**
   * Determines whether or not the current line in the document starts
   * with the character sequence specified by the String field _prefix,
   * skipping over any comments on that line.
   * @param doc The DefinitionsDocument containing the current line.
   * @return True iff the current line in the document starts with the
   * character sequence specified by the String field _prefix.
   */
  boolean applyRule(DefinitionsDocument doc)
  {
    try
    {
      // Find the first non-whitespace character on the current line.
      
      int currentPos = doc.getCurrentLocation(),
        startPos   = doc.getLineFirstCharPos(currentPos),
        endPos     = doc.getLineEndPos(currentPos),
        lineLength = endPos - startPos;
      
      char currentChar, previousChar = '\0';
      String text = doc.getText(startPos, lineLength);
      
      for (int i = 0; i < lineLength; i++)
      {
        // Get state for walker position.
        //BraceReduction reduced = doc.getReduced();
        
        synchronized(doc){
          doc.move( startPos - currentPos + i);
          ReducedModelState state = doc.getStateAtCurrent();
          doc.move(-startPos + currentPos - i);
          
          
          currentChar = text.charAt(i);
          
          if (state.equals(ReducedModelState.INSIDE_LINE_COMMENT)) 
          {
            return false;
          }
          if (state.equals(ReducedModelState.INSIDE_BLOCK_COMMENT))
          {
            // Handle case: ...*/*
            previousChar = '\0'; continue;
          }
          if (state.equals(ReducedModelState.FREE))
          {
            // Can prefix still fit on the current line?
            if (_prefix.length() > lineLength - i)
            {
              return false;
            }
            else if (text.substring(i, i+_prefix.length()).equals(_prefix) && previousChar != '/')
            {
              // '/' is the only non-WS character that we consume without
              // immediately returning false. When we try to match the prefix,
              // we also need to reflect this implicit lookahead mechanism.
              return true;
            }
            else if (currentChar == '/')
            {
              if (previousChar == '/') { return false; }
            }
            else if (currentChar == ' ' || currentChar == '\t')
            {
            }
            else if (!(currentChar == '*' && previousChar == '/'))
            {
              return false;
            }
          }
        }
        if (previousChar == '/' && currentChar != '*')
        {
          return false;
        }
        previousChar = currentChar;
      }
      return false;
    }
    catch (BadLocationException e)
    {
      // Control flow should never reach this point!
      throw new UnexpectedException(new RuntimeException("Bug in QuestionCurrLineStartsWithSkipComments"));
    }
  }
}
