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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

/**
 * Keeps track of newlines, comment blocks, and single and double-quoted strings.
 * This reduced sub-model is used for coloring purposes.  Given the information
 * contained here, the DefinitionsEditorKit can paint strings, comments, and
 * regular code in different colors.  DefinitionsEditorKit colors keywords
 * by directly reading DefinitionsDocument, the "full-scale" model.
 * @version $Id: ReducedModelComment.java,v 1.1 2005/08/05 12:45:09 guehene Exp $
 */

public class ReducedModelComment extends AbstractReducedModel {

  /**Can be used by other classes to walk through the list of comment chars*/
  TokenList.Iterator _walker;

  /**
  * Constructor.  Creates a new reduced model with the cursor
  * at the start of a blank "page."
  */
  public ReducedModelComment() {
    super();
    _walker = _cursor._copy();
  }

  public void insertChar(char ch) {
    switch(ch) {
      case '*': insertSpecial("*"); break;
      case '/': insertSpecial("/"); break;
      case '\n': insertNewline(); break;
      case '\\': insertSpecial("\\"); break;
      case '\'': insertQuote("'"); break;
      case '\"': insertQuote("\""); break;
      default:
        _insertGap(1); break;
    }
  }

  /**
  * Inserts one of three special chars, (*),(/), or (\).
  * <OL>
  *  <li> empty list: insert slash
  *  <li> atEnd: check previous and insert slash
  *  <li> inside multiple character brace:
  *   <ol>
  *    <li> break current brace
  *    <li> move next to make second part current
  *    <li> insert brace between broken parts of former brace
  *    <li> move previous twice to get before the broken first part
  *    <li> walk
  *    <li> current = multiple char brace? move next once<BR>
  *         current = single char brace?  move next twice<BR>
  *         We moved two previous, but if the broken part combined with
  *         the insert, there's only one brace where once were two.
  *   </ol>
  *  <li> inside a gap: use helper function
  *  <li> before a multiple char brace:
  *   <ol>
  *    <li> break the current brace
  *    <li> check previous and insert
  *   </ol>
  *  <li>otherwise, check previous and insert
  * </OL>
  */
  private void insertSpecial(String special) {
    // Check if empty.
    if (_tokens.isEmpty()) {
      _cursor.insertNewBrace(special); //now pointing to tail.
      return;
    }
    // Check if at start.
    if (_cursor.atStart()) {
      _cursor.next();
    }
    // Not empty, not at start, if at end check the previous brace
    if (_cursor.atEnd()) {
      _checkPreviousInsertSpecial(special);
    }
    // If inside a double character brace, break it.
    else if ((_cursor.getBlockOffset() > 0) && _cursor.current().isMultipleCharBrace()) {
      _cursor._splitCurrentIfCommentBlock(true,true);
      //leaving us at the start
      _cursor.next(); //leaving us after first char
      _cursor.insertNewBrace(special); //leaves us after the insert
      move(-2);
      _updateBasedOnCurrentState();
      move(2);
    }
    // inside a gap
    else if ((_cursor.getBlockOffset() > 0) && (_cursor.current().isGap())) {
      _cursor.insertBraceToGap(special);
      _cursor.prev();
      _cursor.prev();
      _updateBasedOnCurrentState();
      // restore cursor state
      _cursor.next();
      _cursor.next();
      // update based on current state
    }
    //if at start of double character brace, break it.
    else if ((_cursor.getBlockOffset() == 0) && _cursor.current().isMultipleCharBrace()) {
      //if we're free there won't be a block comment close so if there
      //is then we don't want to break it.  If the special character is
      // a backslash, we want to break the following escape sequence if there
      // is one.
      _cursor._splitCurrentIfCommentBlock(false,special.equals("\\"));
      //leaving us at start

      _checkPreviousInsertSpecial(special);
    }
    else {
      _checkPreviousInsertSpecial(special);
    }
    return;
  }

  /**
   * Checks before point of insertion to make sure we don't need to combine.
   * Delegates work to _checkPreviousInsertBackSlash and _checkPreviousInsertCommentChar,
   * depending on what's being inserted into the document.
   */
  private void _checkPreviousInsertSpecial(String special)
     {
       if (special.equals("\\"))
         _checkPreviousInsertBackSlash();
       else
         _checkPreviousInsertCommentChar(special);
     }

  /**
  * Checks before point of insertion to make sure we don't need to combine
  * backslash with another backslash (yes, they too can be escaped).
  */

  private void _checkPreviousInsertBackSlash() {
    if (!_cursor.atStart()  && !_cursor.atFirstItem()) {
      if (_cursor.prevItem().getType().equals("\\")) {
        _cursor.prevItem().setType("\\\\");
        _updateBasedOnCurrentState();
        return;
      }
    }
    // Here we know the / unites with nothing behind it.
    _cursor.insertNewBrace("\\"); //leaving us after the brace.
    _cursor.prev();
    _updateBasedOnCurrentState();
    if (_cursor.current().getSize() == 2) {
      _cursor.setBlockOffset(1);
    }
    else {
      _cursor.next();
    }
  }

  /**
  * Checks before the place of insert to make sure there are no preceding
  * slashes with which the inserted slash must combine.  It then performs
  * the insert of either (/), (/ /), (/ *) or (* /).
  */
  private void _checkPreviousInsertCommentChar(String special) {
    if (!_cursor.atStart()  && !_cursor.atFirstItem()) {
      if ((_cursor.prevItem().getType().equals("/")) &&
          (_cursor.prevItem().getState() == FREE))
          {
            _cursor.prevItem().setType("/" + special);
            _updateBasedOnCurrentState();
            return;
          }
      // if we're after a star,
      else if ((_cursor.prevItem().getType().equals("*")) &&
               (getStateAtCurrent() == INSIDE_BLOCK_COMMENT) &&
               special.equals("/"))
        {
          _cursor.prevItem().setType("*" + special);
          _cursor.prevItem().setState(FREE);
          _updateBasedOnCurrentState();
          return;
        }
    }
    //Here we know the / unites with nothing behind it.
    _cursor.insertNewBrace(special); //leaving us after the brace.
    _cursor.prev();
    _updateBasedOnCurrentState();
    if (_cursor.current().getSize() == 2)
      _cursor.setBlockOffset(1);
    else
      _cursor.next();
  }

  /**
  * Inserts an end-of-line character.
  * <OL>
  *  <li> atStart: insert
  *  <li> atEnd: insert
  *  <li> inside multiple character brace:
  *   <ol>
  *    <li> break current brace
  *    <li> move next to make second part current
  *    <li> insert brace between broken parts of former brace
  *    <li> move previous twice to get before the broken first part
  *    <li> walk
  *    <li> move next twice to be after newline insertion
  *   </ol>
  *  <li> inside a gap: use helper function
  *  <li>otherwise, just insert normally
  * </OL>
  */
  public void insertNewline() {
    if (_cursor.atStart()) {
      _insertNewEndOfLine();
    }
    else if (_cursor.atEnd()) {
      _insertNewEndOfLine();
    }
    else if ((_cursor.getBlockOffset() > 0) && _cursor.current().isMultipleCharBrace()) {
      _cursor._splitCurrentIfCommentBlock(true, true);
      _cursor.next();
      _cursor.insert(Brace.MakeBrace("\n", getStateAtCurrent()));
      _cursor.prev();
      _updateBasedOnCurrentState();
      _cursor.next();
      _cursor.next();
      _cursor.setBlockOffset(0);
    }
    else if ((_cursor.getBlockOffset() > 0) && _cursor.current().isGap()) {
      _cursor.insertBraceToGap("\n");
      _cursor.prev();
      _cursor.prev();
      _updateBasedOnCurrentState();
      // restore cursor state
      _cursor.next();
      _cursor.next();
    }
    else {
      _insertNewEndOfLine();
    }
    return;
  }

  private void _insertNewEndOfLine() {
    _cursor.insertNewBrace("\n");
    _cursor.prev();
    _updateBasedOnCurrentState();
    _cursor.next();
    _cursor.setBlockOffset(0);
  }

  /**
   * Inserts the specified quote character.
   * <OL>
   *  <li> atStart: insert
   *  <li> atEnd: insert
   *  <li> inside multiple character brace:
   *   <ol>
   *    <li> break current brace
   *    <li> move next to make second part current
   *    <li> insert brace between broken parts of former brace
   *    <li> walk
   *    <li> current = multiple char brace? move next once<BR>
   *         current = single char brace?  move next twice<BR>
   *         We moved two previous, but if the broken part combined with
   *         the insert, there's only one brace where once were two.
   *    <li> move next twice to be after newline insertion
   *   </ol>
   *  <li> inside a gap: use helper function
   *  <li> before a multiple char brace:
   *   <ol>
   *    <li> break the current brace
   *    <li> check previous and insert
   *   </ol>
   *  <li>otherwise, just insert normally
   * </OL>
   * @param quote the type of quote to insert
   */
  public void insertQuote(String quote) {
    if (_cursor.atStart()) {
      _insertNewQuote(quote);
    }
    else if (_cursor.atEnd()) {
      _insertNewQuote(quote);
    }
    // in the middle of a multiple character brace
    else if ((_cursor.getBlockOffset() > 0) && _cursor.current().isMultipleCharBrace()) {
      _cursor._splitCurrentIfCommentBlock(true,true);
      _cursor.next();
      _cursor.insert(Brace.MakeBrace(quote, getStateAtCurrent()));
      _cursor.prev();
      _updateBasedOnCurrentState();
      if (!_cursor.current().isMultipleCharBrace())
        _cursor.next();
      _cursor.next();
      _cursor.setBlockOffset(0);
    }
    // in the middle of a gap
    else if ((_cursor.getBlockOffset() > 0) && _cursor.current().isGap()) {
      _cursor.insertBraceToGap(quote);
      _cursor.prev();
      _cursor.prev();
      _updateBasedOnCurrentState();
      // restore cursor state
      _cursor.next();
      _cursor.next();

    }
    else {
      _insertNewQuote(quote);
    }
    return;
  }

  /**
   * Helper function for insertQuote.  Creates a new quote Brace and puts it in the
   * reduced model.
   * @param quote the quote to insert
   */
  private void _insertNewQuote(String quote) {
    String insert = _getQuoteType(quote);
    _cursor.insertNewBrace(insert);
    _cursor.prev();
    _updateBasedOnCurrentState();
    _cursor.next();
    _cursor.setBlockOffset(0);
  }

  /**
   * Helper function for insertNewQuote.  In the case where a backslash
   * precedes the point of insertion, it removes the backslash and returns
   * the text for an escaped quote.  The type of quote depends on the given
   * argument.
   * @param quote the type of quote to insert
   * @return a regular or escaped quote, depending on what was previous
   */
  private String _getQuoteType(String quote) {
    if (_cursor.atStart() || _cursor.atFirstItem()) {
      return quote;
    }
    else if (_cursor.prevItem().getType().equals("\\")) {
      _cursor.prev();
      _cursor.remove();
      return "\\" + quote;
    }
    else {
      return quote;
    }
  }

  /**
   * Inserts a gap between the characters in a multiple character brace.
   * This function is called by AbstractReducedModel's method insertGap
   * when a Gap is inserted between the characters in a comment brace
   * or an escape sequence.  It splits up the multiple character brace
   * into its component parts and inserts a Gap of size length in
   * between the resulting split parts.
   * @param length the size of the Gap to be inserted in characters
   */
  protected void insertGapBetweenMultiCharBrace(int length) {
    if (_cursor.getBlockOffset() > 1) {
      throw new IllegalArgumentException("OFFSET TOO BIG:  " + _cursor.getBlockOffset());
    }
    _cursor._splitCurrentIfCommentBlock(true, true);
    _cursor.next();
    _insertNewGap(length);  //inserts gap and goes to next item
    // we have to go back two tokens; we don't want to use move because it could
    // throw us past start if there was only one character before us and we went
    // the usual 2 spaces before.  There would have to be a check and a branch
    // depending on conditions that way.
    _cursor.prev();
    _cursor.prev();
    _updateBasedOnCurrentState();
    // restore cursor state
    _cursor.next();
    _cursor.next();
    return;
  }
  /**
  * USE RULES:
  * Inserting between braces: This should be called from between the two
  *                           characters of the broken double comment.
  * Deleting special chars: Start from previous char if it exists.
  * Begins updating at current character.  /./ would not become // because
  * current is in the middle.
  * Double character comments inside of a quote or a comment are broken.
  */

  private void _updateBasedOnCurrentState() {
    TokenList.Iterator copyCursor = _cursor._copy();
    copyCursor.updateBasedOnCurrentState();
    copyCursor.dispose();
  }

 /**
  * Updates the BraceReduction to reflect cursor movement.
  * Negative values move left from the cursor, positive values move
  * right.
  * @param count indicates the direction and magnitude of cursor movement
  */
  public void move(int count) {
    _cursor.move(count);
  }

  /**
  * <P>Update the BraceReduction to reflect text deletion.</P>
  * @param count indicates the size and direction of text deletion.
  * Negative values delete text to the left of the cursor, positive
  * values delete text to the right.
  * Always move count spaces to make sure we can delete.
  */
  public void delete(int count) {
    if (count == 0) {
      return;
    }
    _cursor.delete(count);

    // Changes in ReducedModelComment can entail state changes in the
    // document.  For this reason, we have to call
    // _updateBasedOnCurrentState because there is no need to call it
    // in ReducedModelBrace, and factoring it out would be stupid and
    // wasteful.

    // Move back 2 or as far back as the document will allow
    int absOff = this.absOffset();
    int movement;
    if (absOff < 2)
      movement = absOff;
    else
      movement = 2;
    _cursor.move(-movement);
    // update state information
    _updateBasedOnCurrentState();
    // restore the cursor
    _cursor.move(movement);
    return;
  }


  /**In order to interface with the ReducedModelComment two functions are
  provided. One resets the walker and the other will both move the cursor
  by x and return the state at that new location.
  Once the new value has returned all new calculations will be relative to
  that spot until the walker is reset to the _cursor.
  */

  /**
   * Returns the state at the relLocation, where relLocation is the location
   * relative to the walker
   * @param relLocation distance from walker to get state at.
   */
  protected ReducedModelState moveWalkerGetState(int relLocation) {
    _walker.move(relLocation);
    return _walker.getStateAtCurrent();
  }

  /**
   * Resets the walker to the current position in document
   */
  protected void resetWalkerLocationToCursor() {
    _walker.dispose();
    _walker = _cursor._copy();
  }

  /**
   * Dist to Previous newline will be -1 if no newline.
   */
  void getDistToPreviousNewline(IndentInfo braceInfo) {
    braceInfo.distToPrevNewline = _getDistToPreviousNewline(_cursor._copy());
    braceInfo.distToNewline = braceInfo.distToPrevNewline;
    return;
  }

  /**
   *returns distance to after newline
   */
  private int _getDistToPreviousNewline(TokenList.Iterator copyCursor) {
    int walkcount = copyCursor.getBlockOffset();
    if (!copyCursor.atStart()) {
      copyCursor.prev();
    }
    while ((!copyCursor.atStart()) &&
           (!(copyCursor.current().getType().equals("\n"))))
           {
             //  copyCursor.current().getState() == FREE))){
             walkcount += copyCursor.current().getSize();
             copyCursor.prev();
           }

    if (copyCursor.atStart()) {
      return -1;
    }
    return walkcount;
  }

  void getDistToIndentNewline(IndentInfo braceInfo) {
    int walkcount = -1;
    TokenList.Iterator copyCursor = _cursor._copy();

    if (braceInfo.distToBrace == -1 || copyCursor.atStart()) { // no brace
      return;
    }

    copyCursor.move(-braceInfo.distToBrace);
    walkcount = _getDistToPreviousNewline(copyCursor);

    if (walkcount == -1) {
      braceInfo.distToNewline = -1;
    }
    else {
      braceInfo.distToNewline = walkcount + braceInfo.distToBrace;
    }
    return;
  }

/**
 * Computes the distance to the beginning of the line containing the brace enclosing
 * the current location.  Stores this info in the IndentInfo field distToNewlineCurrent.
 */
  void getDistToCurrentBraceNewline(IndentInfo braceInfo) {
    int walkcount = -1;
    TokenList.Iterator copyCursor = _cursor._copy();

    if (braceInfo.distToBraceCurrent == -1 || copyCursor.atStart()) { // no brace
      return;
    }

    copyCursor.move(-braceInfo.distToBraceCurrent);
    walkcount = _getDistToPreviousNewline(copyCursor);

    if (walkcount == -1) {
      braceInfo.distToNewlineCurrent = -1;
    }
    else {
      braceInfo.distToNewlineCurrent = walkcount + braceInfo.distToBraceCurrent;
    }
    return;
  }

  /**
  * Gets distance to previous newline, relLoc is the distance
  * back from the cursor that we want to start searching.
  */
  public int getDistToPreviousNewline(int relLoc) {
    TokenList.Iterator copyCursor = _cursor._copy();
    copyCursor.move(-relLoc);
    int dist = _getDistToPreviousNewline(copyCursor);
    copyCursor.dispose();
    if(dist == -1) {
      return -1;
    }
    return dist + relLoc;
  }

  /**
  * returns the distance to the space before the next newline
  * returns the distance to the end of the document if there is no newline
  */
  public int getDistToNextNewline() {
    TokenList.Iterator copyCursor = _cursor._copy();
    if(copyCursor.atStart()) {
      copyCursor.next();
    }
    if(copyCursor.atEnd() || copyCursor.current().getType().equals("\n")) {
      return 0;
    }
    int walkcount = copyCursor.current().getSize() - _cursor.getBlockOffset();
    copyCursor.next();

    while ((!copyCursor.atEnd()) &&
           (!(copyCursor.current().getType().equals("\n"))))
    {
      //copyCursor.current().getState() == FREE))){
      walkcount += copyCursor.current().getSize();
      copyCursor.next();
    }
    return walkcount;
  }
}
