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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import gj.util.Stack;
import gj.util.Vector;

/**
 * Keeps track of the true braces (i.e., "(){}[]").
 * This reduced sub-model is used to balance braces for both indenting
 * and highlighting purposes.  For example, when the user's caret is 
 * immediately after a closing brace, this allows the DefinitionsPane 
 * to produced a highlight extending from the closing brace to its match.
 * @version $Id: ReducedModelBrace.java,v 1.1 2005/04/27 20:30:32 elmoutam Exp $
 * @author JavaPLT
 */
public class ReducedModelBrace extends AbstractReducedModel {

  private ReducedModelControl _parent;
  
  public ReducedModelBrace(ReducedModelControl parent) {
    super();
    _parent = parent;
  }
  
  public void insertChar(char ch) {
    switch(ch) {
      case '{':
      case '}':
      case '[':
      case ']':
      case '(':
      case ')':
        _insertBrace("" + ch);
      break;
      default:
        _insertGap(1);
      break;
    }
  }
  

  /**
   * Helper function for top level brace insert functions.
   *
   * <OL>
   *  <li> at Head: not special case
   *  <li> at Tail: not special case
   *  <li> between two things (offset is 0):
   *      <ol>
   *       <li> insert brace
   *       <li> move next
   *       <li> offset = 0
   *      </ol>
   *  <li> inside gap:
   *      <ol>
   *       <li> shrink gap to size of gap - offset.
   *       <li> insert brace
   *       <li> insert gap the size of offset.
   *       <li> move next twice
   *       <li> offset = 0
   *      </ol>
   * <li> inside multiple char brace:
   *      <ol>
   *       <li> break
   *       <li> insert brace
   *      </ol>
   * </OL>
   * @param text the String type of the brace to insert
   */
  private void _insertBrace(String text) {
    if (_cursor.atStart() || _cursor.atEnd()) {
      _cursor.insertNewBrace(text); // inserts brace and goes to next
    }
    else if (_cursor.current().isGap()) {
      _cursor.insertBraceToGap(text);
    }
    
    else {
      _cursor.insertNewBrace(text);
    }
  }
  
  /**
   * Inserts a gap between the characters in a multiple character brace.
   * However, since ReducedModelBrace doesn't keep track of the comment
   * braces and escape sequences, we just throw an exception since the
   * condition in insertGap that spawns this method doesn't arise.
   */
  protected void insertGapBetweenMultiCharBrace(int length) {
    throw new RuntimeException("ReducedModelBrace does not keep " +
                               "track of multi-character braces.");
  }
  
  
  /**
   * Updates ReducedModelBrace to reflect cursor movement.
   * Negative values move left from the cursor, positive values move
   * right.  All functionality has been refactored into TokenList.
   * @param count indicates the direction and magnitude of cursor movement
   */
  public void move(int count) {
    _cursor.move(count);
  }
  
  /**
   * Updates ReducedModelBrace to reflect text deletion.
   * Negative values mean text left of the cursor, positive values mean
   * text to the right.  All functionality has been refactored into TokenList.
   */
  public void delete( int count ) {
    if (count == 0) {
      return;
    }
    _cursor.delete(count);
    return;
  }
  

  /** If the current brace is a /, a *, a // or a \n, it's not matchable.
  *  This means it is ignored on balancing and on next/prev brace finding.
  *  All other braces are matchable.
  */
  private boolean _isCurrentBraceMatchable()
  {
    String type = _cursor.current().getType();
    return (((type.equals("{")) ||
             (type.equals("}")) ||
             (type.equals("(")) ||
             (type.equals(")")) ||
             (type.equals("[")) ||
             (type.equals("]"))) &&
            (_parent.getStateAtCurrent() == FREE));
  }
    
  /**
   *Returns distance from current location of cursor to the location of the
   *previous significant brace.
   *ex. (...|)  where | signifies the cursor. previousBrace returns 4 because
   *it goes to the spot behind the (.
   * /|* returns this brace since you're in the middle of it and going
   *backward can find it.
   */
  public int previousBrace() {
    int relDistance = 0;
    int dist = 0;
    resetLocation();//reset the interface to the comment model
    
    TokenList.Iterator copyCursor = _cursor._copy();
    if (!copyCursor.atStart())
      copyCursor.prev();
    if (copyCursor.atStart()) {
      copyCursor.dispose();
      return -1;
    }
    //initialize the size.
    dist += _cursor.getBlockOffset();
    relDistance = dist;
    
    // if we're in the middle of the first brace element, we're
    // not going to find any previous braces
    
    while (!copyCursor.atStart()) {
      if (!copyCursor.current().isGap()) {
        if (stateAtRelLocation(-relDistance) == FREE) {
          copyCursor.dispose();
          return dist + copyCursor.current().getSize();
        }
        relDistance = 0;
      }
      
      dist += copyCursor.current().getSize();
      relDistance += copyCursor.current().getSize();
      copyCursor.prev();
    }
    copyCursor.dispose();
    return -1;
  }
  
  
  /**
   *Goes to the location before the brace. |...( where | is the cursor,
   *returns three since it is three moves to the location of the (
   *NOTE: /|* returns the next brace. It does not return this brace because
   *you are past it.
   */
  public int nextBrace() {
    int relDistance = 0;
    int dist = 0;
    TokenList.Iterator copyCursor = _cursor._copy();
    
    resetLocation();
    
    if ( copyCursor.atStart())
      copyCursor.next();
    if (_cursor.getBlockOffset() > 0){
      dist = copyCursor.current().getSize() - _cursor.getBlockOffset();
      relDistance = dist;
      copyCursor.next();
    }
    // there are no braces on the last brace element - it's empty
    while (!copyCursor.atEnd() ){
      if (!copyCursor.current().isGap()) {
        if (stateAtRelLocation(relDistance) ==
            FREE){
              copyCursor.dispose();
              return dist;
            }
        relDistance = 0;
      }
      relDistance += copyCursor.current().getSize();
      dist += copyCursor.current().getSize();
      copyCursor.next();
    }
    copyCursor.dispose();
    return -1;
  }
  
  /**
   * If the current ReducedToken is an open significant brace and the
   * offset is 0 (i.e., if we're immediately left of said brace),
   * push the current Brace onto a Stack and iterate forwards,
   * keeping track of the distance covered.
   * - For every closed significant Brace, if it matches the top of the Stack,
   *   pop the Stack.  Increase the distance by the size of the Brace.
   *   If the Stack is Empty, we have a balance.  Return distance.
   *   If the closed Brace does not match the top of the Stack, return -1;
   *   We have an unmatched open Brace at the top of the Stack.
   * - For every open significant Brace, push onto the Stack.
   *   Increase distance by size of the Brace, continue.
   * - Anything else, increase distance by size of the ReducedToken, continue.
   */
  public int balanceForward() {
    Stack<ReducedToken> braceStack = new Stack<ReducedToken>();
    TokenList.Iterator iter = _cursor._copy();
    int relDistance = 0;
    int distance = 0;
    
    resetLocation(); //resets location of ReducedModelComment
    
    if (iter.atStart())
      iter.next();
    
    // here we check to make sure there is an open significant brace
    // immediately to the right of the cursor
    if (!iter.atEnd() && openBraceImmediatelyRight()) {
      if (stateAtRelLocation(relDistance) == FREE) {
        relDistance = 0;
        // initialize the distance and the stack with the first brace,
        // the one we are balancing
        
        braceStack.push(iter.current());
        distance += iter.current().getSize();
        iter.next();
        
        // either we get a match and the stack is empty
        // or we reach the end of a file and haven't found a match
        // or we have a closed brace that doesn't have a match,
        //    so we abort
        while (!iter.atEnd() && !braceStack.isEmpty()) {
          if (!iter.current().isGap()) {
            if (stateAtRelLocation(relDistance) == FREE) {
              if (iter.current().isClosedBrace()) {
                ReducedToken popped = braceStack.pop();
                if (!iter.current().isMatch(popped)) {
                  iter.dispose();
                  return -1;
                }
              }
              //open
              else{
                braceStack.push(iter.current());
              }
            }
            relDistance = 0;
          }
          // no matter what, we always want to increase the distance
          // by the size of the token we have just gone over
          distance += iter.current().getSize();
          relDistance += iter.current().getSize();
          iter.next();
        }
        
        // we couldn't find a match
        if (!braceStack.isEmpty()) {
          iter.dispose();
          return -1;
        }
        // success
        else {
          iter.dispose();
          return distance;
        }
      }
      // not the right initial conditions 
    }
    iter.dispose();
    return -1;
  }
  
  public boolean openBraceImmediatelyRight() {
    if (_cursor.atEnd()) {
      return false;
    }
    else {
      return ((_cursor.getBlockOffset() == 0) && _cursor.current().isOpen() &&
              _isCurrentBraceMatchable());
    }
  }
  
  public boolean closedBraceImmediatelyLeft() {
    if (_cursor.atStart() || _cursor.atFirstItem()) {
      return false;
    }
    else {
      _cursor.prev();
      /*
      System.out.println("+ closedBraceImmediatelyLeft() {");
      System.out.println("  _cursor.getBlockOffset(): "+_cursor.getBlockOffset());
      System.out.println("  _cursor.current().isClosed(): "+_cursor.current().isClosed());
      System.out.println("  _isCurrentBraceMatchable(): "+_isCurrentBraceMatchable());
      System.out.println("  }");
      */
      boolean isLeft = ((_cursor.getBlockOffset() == 0) && _cursor.current().isClosed() &&
                        _isCurrentBraceMatchable());
      //System.out.println("= token to left: " + _cursor);
      _cursor.next();
      //String output = (_cursor.atEnd()) ? "<end>": _cursor.toString();
      //System.out.println("= current token: " + output);
      return isLeft;
    }
  }
  
  /* 
   * If the previous ReducedToken is a closed significant brace,
   * offset is 0 (i.e., if we're immediately right of said brace),
   * push the previous Brace onto a Stack and iterate backwards,
   * keeping track of the distance covered.
   * - For every open significant Brace, if it matches the top of the Stack,
   *   pop the Stack.  Increase the distance by the size of the Brace.
   *   If the Stack is Empty, we have a balance.  Return distance.
   *   If the open Brace does not match the top of the Stack, return -1;
   *   We have an unmatched closed Brace at the top of the Stack.
   * - For every closed significant Brace, push onto the Stack.
   *   Increase distance by size of the Brace, continue.
   * - Anything else, increase distance by size of the ReducedToken, continue.
   */
  public int balanceBackward() { 
    //System.out.println("-------------------------------------------");
    Stack<ReducedToken> braceStack = new Stack<ReducedToken>();
    TokenList.Iterator iter = _cursor._copy();
    resetLocation();
    int relDistance = 0;
    int distance = 0;
    if (iter.atStart() || 
        iter.atFirstItem() ||
        !closedBraceImmediatelyLeft()) 
    {
      //System.out.println("closedBraceImmediatelyLeft(): "+closedBraceImmediatelyLeft());
      iter.dispose();
      //System.out.println("! atStart, atFirstItem, or no closed brace");
      return -1;
    }
    
    iter.prev();
    relDistance = iter.current().getSize();
    // here we check to make sure there is an open significant brace
    // immediately to the right of the cursor
    if (iter.current().isClosedBrace()) {
      if(stateAtRelLocation(-relDistance) == FREE) {
        // initialize the distance and the stack with the first brace,
        // the one we are balancing
        
        braceStack.push(iter.current());
        distance += iter.current().getSize();
        iter.prev();
        if (!iter.atStart()) {
          distance += iter.current().getSize();
          relDistance = iter.current().getSize();
        }
      }
      else {
        iter.dispose();
        //System.out.println("! state at relative location != FREE");
        return -1;
      }
    }
    else {
      iter.dispose();
      //System.out.println("! no open brace to immediate right of cursor");
      return -1;
    }
    // either we get a match and the stack is empty
    // or we reach the start of a file and haven't found a match
    // or we have a open brace that doesn't have a match,
    // so we abort
    while (!iter.atStart() && !braceStack.isEmpty()) {
      if (!iter.current().isGap()) {
        if (stateAtRelLocation(-relDistance) ==
            FREE) {
              // open
              if (iter.current().isOpenBrace()) {
                ReducedToken popped = braceStack.pop();
                if (!iter.current().isMatch(popped)){
                  iter.dispose();
                  //System.out.println("! encountered open brace that didn't match");
                  return -1;
                }
              }
              // closed
              else {
                braceStack.push(iter.current());
              }
            }
        relDistance = 0;
      }
      // no matter what, we always want to increase the distance
      // by the size of the token we have just gone over
      iter.prev();
      if (!iter.atStart() && !braceStack.isEmpty()) {
        distance += iter.current().getSize();
        relDistance += iter.current().getSize();
      }
    }
    
    // we couldn't find a match
    if (!braceStack.isEmpty()) {
      iter.dispose();
      //System.out.println("! ran to end of brace stack");
      return -1;
    }
    // success
    else {
      iter.dispose();
      return distance;
    }
  }
  
  protected ReducedModelState stateAtRelLocation(int relDistance) {
    return _parent.stateAtRelLocation(relDistance);
  }
  
  protected void resetLocation() {
    _parent.resetLocation();
  }
 
  /*
   *The braceInfo.distToNewline holds the distance to the previous newline.
   *To find the enclosing brace one must first move past this newline.
   *The distance held in this variable is only to the space in front of the
   *newline hence you must move back that distance + 1.
   */
  protected void getDistToEnclosingBrace(IndentInfo braceInfo) {
    Stack<ReducedToken> braceStack = new Stack<ReducedToken>();
    TokenList.Iterator iter = _cursor._copy();
    resetLocation();
    //this is the distance to in front of the previous newline.
    int relDistance = braceInfo.distToNewline + 1;
    int distance = relDistance;
    
    if (braceInfo.distToNewline == -1) {
      iter.dispose();
      return;
    }
    //move to the proper location, then add the rest of the block
    // and go to the previous.
    iter.move(-braceInfo.distToNewline - 1);
    relDistance += iter.getBlockOffset();
    distance += iter.getBlockOffset();
    
    //reset the value of braceInfo signiling the necessary newline has
    //not been found.
    braceInfo.distToNewline = -1;
    
    if (iter.atStart() || iter.atFirstItem()) {
      iter.dispose();
      return;
    }
    
    iter.prev();
    
    // either we get a match and the stack is empty
    // or we reach the start of a file and haven't found a match
    // or we have a open brace that doesn't have a match,
    // so we abort
    while (!iter.atStart()) {
      
      distance += iter.current().getSize();
      relDistance += iter.current().getSize();
      
      if (!iter.current().isGap()) {
        
        if (stateAtRelLocation(-relDistance) == FREE) {
              // open
              if (iter.current().isOpenBrace()) {
                if (braceStack.isEmpty()) {
                  braceInfo.braceType = iter.current().getType();
                  braceInfo.distToBrace = distance;
                  iter.dispose();
                  return;
                }
                ReducedToken popped = braceStack.pop();
                if (!iter.current().isMatch(popped)){
                  iter.dispose();
                  return;
                }
              }
              // closed
              else {
                braceStack.push(iter.current());
              }
            }
        relDistance = 0;
      }
      // no matter what, we always want to increase the distance
      // by the size of the token we have just gone over
      iter.prev();
    }
    
    iter.dispose();
    return;
  }
  
 
  /**
   * Find the enclosing brace enclosing our current location.
   */
  protected void getDistToEnclosingBraceCurrent(IndentInfo braceInfo) {
    Stack<ReducedToken> braceStack = new Stack<ReducedToken>();
    TokenList.Iterator iter = _cursor._copy();
    resetLocation();
    int relDistance = 0;
    int distance = relDistance;

    
    //move to the proper location, then add the rest of the block
    // and go to the previous.

    relDistance += iter.getBlockOffset();
    distance += iter.getBlockOffset();
    
    //reset the value of braceInfo signiling the necessary newline has
    //not been found.
    braceInfo.distToNewlineCurrent = -1;
    
    if (iter.atStart() || iter.atFirstItem()) {
      iter.dispose();
      return;
    }
    
    iter.prev();
    
    // either we get a match and the stack is empty
    // or we reach the start of a file and haven't found a match
    // or we have a open brace that doesn't have a match,
    // so we abort
    while (!iter.atStart()) {
      
      distance += iter.current().getSize();
      relDistance += iter.current().getSize();
      
      if (!iter.current().isGap()) {
        
        if (stateAtRelLocation(-relDistance) == FREE) {
              // open
              if (iter.current().isOpenBrace()) {
                if (braceStack.isEmpty()) {
    braceInfo.braceTypeCurrent = iter.current().getType();
                  braceInfo.distToBraceCurrent = distance;
                  iter.dispose();
                  return;
                }
                ReducedToken popped = braceStack.pop();
                if (!iter.current().isMatch(popped)){
                  iter.dispose();
                  return;
                }
              }
              // closed
              else {
                braceStack.push(iter.current());
              }
            }
        relDistance = 0;
      }
      // no matter what, we always want to increase the distance
      // by the size of the token we have just gone over
      iter.prev();
    }
    
    iter.dispose();
    return;
  }
}
