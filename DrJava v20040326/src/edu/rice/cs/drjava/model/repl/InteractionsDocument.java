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

package edu.rice.cs.drjava.model.repl;

import java.util.*;
import java.io.*;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.DocumentAdapter;
import edu.rice.cs.util.text.DocumentEditCondition;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.drjava.model.FileSaveSelector;

/**
 * Toolkit-independent document that provides console-like interaction
 * with a Java interpreter.
 * @version $Id: InteractionsDocument.java,v 1.1 2005/08/05 12:45:09 guehene Exp $
 */
public class InteractionsDocument extends ConsoleDocument {

  /** Default banner. */
  public static final String DEFAULT_BANNER = "Welcome to DrJava.\n";

  /** Default prompt. */
  public static final String DEFAULT_PROMPT = "> ";

  /** Style for error messages */
  public static final String ERROR_STYLE = "error";

  /** Style for debugger messages */
  public static final String DEBUGGER_STYLE = "debugger";

  /**
   * String to print when the document is reset.
   * Defaults to "Welcome to DrJava."
   */
  protected String _banner;

  /**
   * Command-line history. It's not reset when the interpreter is reset.
   */
  protected History _history;

  /**
   * Reset the document on startup.  Uses a history with configurable size.
   * @param document DocumentAdapter to use for the model
   */
  public InteractionsDocument(DocumentAdapter document) {
    this(document, new History());
  }

  /**
   * Reset the document on startup.  Uses a history with the given
   * maximum size.  This history will not use the config framework.
   * @param document DocumentAdapter to use for the model
   * @param maxHistorySize Number of commands to remember in the history
   */
  public InteractionsDocument(DocumentAdapter document, int maxHistorySize) {
    this(document, new History(maxHistorySize));
  }

  /**
   * Reset the document on startup.  Uses the given history.
   * @param document DocumentAdapter to use for the model
   * @param history History of commands
   */
  public InteractionsDocument(DocumentAdapter document, History history) {
    super(document);
    _history = history;

    _hasPrompt = true;
    _banner = DEFAULT_BANNER;
    _prompt = DEFAULT_PROMPT;

    reset();
  }


  /**
   * Accessor for the banner, which is printed when the document resets.
   */
  public String getBanner() {
    return _banner;
  }

  /**
   * Sets the string to use for the banner when the document resets.
   * @param banner String to be printed when the document resets.
   */
  public void setBanner(String banner) {
    _banner = banner;
  }

  /**
   * Lets this document know whether an interaction is in progress.
   * @param inProgress whether an interaction is in progress
   */
  public void setInProgress(boolean inProgress) {
    _hasPrompt = !inProgress;
  }

  /**
   * Returns whether an interaction is currently in progress.
   */
  public boolean inProgress() {
    return !_hasPrompt;
  }

  /**
   * Resets the document to a clean state.  Does not reset the history.
   */
  public void reset() {
    try {
      forceRemoveText(0, _document.getDocLength());
      forceInsertText(0, _banner, DEFAULT_STYLE);
      insertPrompt();
      _history.moveEnd();
      setInProgress(false);
    }
    catch (DocumentAdapterException e) {
      throw new UnexpectedException(e);
    }
  }

  /**
   * Replaces any text entered past the prompt with the current
   * item in the history.
   */
  protected void _replaceCurrentLineFromHistory() {
    try {
      _clearCurrentInputText();
      insertText(getDocLength(), _history.getCurrent(), DEFAULT_STYLE);
    }
    catch (DocumentAdapterException ble) {
      throw new UnexpectedException(ble);
    }
  }

  /**
   * Accessor method for the history of commands.
   */
  public History getHistory() {
    return _history;
  }

  /**
   * Adds the given text to the history of commands.
   */
  public void addToHistory(String text) {
    _history.add(text);
  }

  /**
   * Saves the unedited version of the current history to a file
   * @param selector File to save to
   */
  public void saveHistory(FileSaveSelector selector) throws IOException {
    _history.writeToFile(selector);
  }

  /**
   * Saves the edited version of the current history to a file
   * @param selector File to save to
   * @param editedVersion Edited verison of the history which will be
   * saved to file instead of the lines saved in the history. The saved
   * file will still include any tags needed to recognize it as a saved
   * interactions file.
   */
  public void saveHistory(FileSaveSelector selector, String editedVersion)
    throws IOException
  {
    _history.writeToFile(selector, editedVersion);
  }

  /**
   * Returns the entire history as a single string.  Commands should
   * be separated by semicolons. If an entire command does not end in a
   * semicolon, one is added.
   */
  public String getHistoryAsStringWithSemicolons() {
      return _history.getHistoryAsStringWithSemicolons();
  }

  /**
   * Returns the entire history as a single string.  Commands should
   * be separated by semicolons.
   */
  public String getHistoryAsString() {
    return _history.getHistoryAsString();
  }

  /**
   * Clears the history
   */
  public void clearHistory() {
    _history.clear();
  }

  /**
   * Puts the previous line from the history on the current line
   * and moves the history back one line.
   * @param entry the current entry (perhaps edited from what is in history)
   */
  public void moveHistoryPrevious(String entry) {
    _history.movePrevious(entry);
    _replaceCurrentLineFromHistory();
  }

  /**
   * Puts the next line from the history on the current line
   * and moves the history forward one line.
   * @param entry the current entry (perhaps edited from what is in history)
   */
  public void moveHistoryNext(String entry) {
    _history.moveNext(entry);
    _replaceCurrentLineFromHistory();
  }

  /**
   * Returns whether there is a previous command in the history.
   */
  public boolean hasHistoryPrevious() {
    return  _history.hasPrevious();
  }

  /**
   * Returns whether there is a next command in the history.
   */
  public boolean hasHistoryNext() {
    return _history.hasNext();
  }
  
  /**
   * Reverse searches the history for the given string.
   * @param searchString the string to search for
   */
  public void reverseHistorySearch(String searchString) {
    _history.reverseSearch(searchString);
    _replaceCurrentLineFromHistory();
  }

  /**
   * Forward searches the history for the given string.
   * @param searchString the string to search for
   */
  public void forwardHistorySearch(String searchString) {
    _history.forwardSearch(searchString);
    _replaceCurrentLineFromHistory();
  }

  /**
   * Gets the previous interaction in the history and
   * replaces whatever is on the current
   * interactions input line with this interaction.
   */
  public void recallPreviousInteractionInHistory() {
    if (hasHistoryPrevious()) {
      moveHistoryPrevious(getCurrentInteraction());
    }
    else {
      _beep.run();
    }
  }

  /**
   * Gets the next interaction in the history and
   * replaces whatever is on the current
   * interactions input line with this interaction.
   */
  public void recallNextInteractionInHistory() {
    if (hasHistoryNext()) {
      moveHistoryNext(getCurrentInteraction());
    }
    else {
      _beep.run();
    }
  }
  
  /**
   * Reverse searches the history for interactions that started with the
   * current interaction.
   */
  public void reverseSearchInteractionsInHistory() {
    if (hasHistoryPrevious()) {
      reverseHistorySearch(getCurrentInteraction());
    }
    else {
      _beep.run();
    }
  }

  /**
   * Forward searches the history for interactions that started with the
   * current interaction.
   */
  public void forwardSearchInteractionsInHistory() {
    if (hasHistoryNext()) {
      forwardHistorySearch(getCurrentInteraction());
    }
    else {
      _beep.run();
    }
  }

  /**
   * Inserts the given exception data into the document with the given style.
   * @param exceptionClass Name of the exception that was thrown
   * @param message Message contained in the exception
   * @param stackTrace String representation of the stack trace
   * @param styleName name of the style for formatting the exception
   */
  public void appendExceptionResult(String exceptionClass, String message,
                                    String stackTrace, String styleName) {
    //writeLock();
    try {

      if (null == message || "null".equals(message)) {
        message = "";
      }

      // Simplify the common error messages
      if ("koala.dynamicjava.interpreter.error.ExecutionError".equals(exceptionClass) ||
          "edu.rice.cs.drjava.model.repl.InteractionsException".equals(exceptionClass)) {
        exceptionClass = "Error";
      }

      insertText(getDocLength(),
                 exceptionClass + ": " + message + "\n", styleName);

      // An example stack trace:
      //
      // java.lang.IllegalMonitorStateException:
      // at java.lang.Object.wait(Native Method)
      // at java.lang.Object.wait(Object.java:425)
      if (! stackTrace.trim().equals("")) {
        BufferedReader reader=new BufferedReader(new StringReader(stackTrace));

        String line;
        // a line is parsable if it has ( then : then ), with some
        // text between each of those
        while ((line = reader.readLine()) != null) {
          String fileName = null;
          int lineNumber = -1;

          int openLoc = line.indexOf('(');

          if (openLoc != -1) {
            int closeLoc = line.indexOf(')', openLoc + 1);

            if (closeLoc != -1) {
              int colonLoc = line.indexOf(':', openLoc + 1);
              if ((colonLoc > openLoc) && (colonLoc < closeLoc)) {
                // ok this line is parsable!
                String lineNumStr = line.substring(colonLoc + 1, closeLoc);
                try {
                  lineNumber = Integer.parseInt(lineNumStr);
                  fileName = line.substring(openLoc + 1, colonLoc);
                }
                catch (NumberFormatException nfe) {
                  // do nothing; we failed at parsing
                }
              }
            }
          }

          insertText(getDocLength(), line, styleName);

          // OK, now if fileName != null we did parse out fileName
          // and lineNumber.
          // Here's where we'd add the button, etc.
          /*
          if (fileName != null) {
            JButton button = new JButton("go");
            button.addActionListener(new ExceptionButtonListener(fileName, lineNumber));
            SimpleAttributeSet buttonSet = new SimpleAttributeSet(set);
            StyleConstants.setComponent(buttonSet, button);
            insertString(getDocLength(), "  ", null);
            insertString(getDocLength() - 1, " ", buttonSet);
            JOptionPane.showMessageDialog(null, "button in");
            insertString(getDocLength(), " ", null);
            JOptionPane.showMessageDialog(null, "extra space");
          }*/

          //JOptionPane.showMessageDialog(null, "\\n");
          insertText(getDocLength(), "\n", styleName);

        } // end the while
      }
    }
    catch (IOException ioe) {
      // won't happen; we're readLine'ing from a String!
      throw new UnexpectedException(ioe);
    }
    catch (DocumentAdapterException ble) {
      throw new UnexpectedException(ble);
    }
//    finally {
//      writeUnlock();
//    }
  }

  public void appendSyntaxErrorResult(String message, int startRow, int startCol,
                                      int endRow, int endCol, String styleName) {
    //writeLock();
    try {
      if (null == message || "null".equals(message)) {
        message = "";
      }
      insertText(getDocLength(), message + "\n" , styleName );
    }
    catch (DocumentAdapterException ble) {
      throw new UnexpectedException(ble);
    }
//    finally {
//      writeUnlock();
//    }
  }

  /**
   * Clears the current input text and then moves
   * to the end of the command history.
   */
  public void clearCurrentInteraction() {
    super.clearCurrentInput();
    _history.moveEnd();
  }

  /**
   * Returns the string that the user has entered at the current prompt.
   * Forwards to getCurrentInput()
   */
  public String getCurrentInteraction() {
    return super.getCurrentInput();
  }
}
