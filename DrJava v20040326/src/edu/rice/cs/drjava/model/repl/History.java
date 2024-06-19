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

// TODO: Is synchronization used properly here?
import java.util.Vector;
import java.util.HashMap;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.ui.InteractionsHistoryFilter;
import edu.rice.cs.drjava.platform.PlatformFactory;
import java.io.*;
import javax.swing.*;

/**
 * Keeps track of what was typed in the interactions pane.
 * @version $Id: History.java,v 1.1 2005/08/05 12:45:09 guehene Exp $
 */
public class History implements OptionConstants {

  public static final String INTERACTION_SEPARATOR = "//End of Interaction//";
  
  // Not final because it may be updated by config
  private int MAX_SIZE;
  
  /**
   * Version flag at the beginning of saved history file format
   * If this is not present in a saved history, it is assumed to be the original format.
   */
  public static final String HISTORY_FORMAT_VERSION_2 = 
    "// DrJava saved history v2" + System.getProperty("line.separator");
    
  private Vector<String> _vector = new Vector<String>();
  private int _cursor = -1;
  
  /**
   * A hashmap for edited entries in the history.
   */
  private HashMap<Integer, String> _editedEntries = new HashMap<Integer, String>();
  
  /**
   * A placeholder for the current search string.
   */
  private String _currentSearchString = "";
  
  /**
   * Constructor, so we can add a listener to the Config item being used.
   */ 
  public History() {
    this(DrJava.getConfig().getSetting(HISTORY_MAX_SIZE).intValue());
    DrJava.getConfig().addOptionListener(HISTORY_MAX_SIZE, 
                                         new HistorySizeOptionListener());
  }
  
  /**
   * Creates a new History with the given size.  An option listener is not
   * added for the config framework.
   * @param maxSize Number of lines to remember in the history.
   */
  public History(int maxSize) {
    MAX_SIZE = maxSize;
    // Sanity check on MAX_SIZE
    if (MAX_SIZE < 0) MAX_SIZE = 0;
  }
  
  /**
   * Adds an item to the history and moves the cursor to point
   * to the place after it.
   * Note: Items are not inserted if they are empty. (This is in accordance with 
   * bug #522123, but in divergence from feature #522213 which originally excluded
   * sequential duplicate entries from ever being stored.)
   *
   * Thus, to access the newly inserted item, you must movePrevious first.
   */
  public void add(String item) {
    
    // for consistency in saved History files, WILL save sequential duplicate entries
    if (item.trim().length() > 0) {
      //if (_vector.isEmpty() || ! _vector.lastElement().equals(item)) {
      _vector.add(item);
        
        // If adding the new element has filled _vector to beyond max 
        // capacity, spill the oldest element out of the History.
      if (_vector.size() > MAX_SIZE) {
        _vector.remove(0);
        }
      //}
      moveEnd();
      _editedEntries.clear();
    }
  }

  /**
   * Move the cursor to just past the end. Thus, to access the last element,
   * you must movePrevious.
   */
  public void moveEnd() {
    _cursor = _vector.size();
  }

  /** 
   * Moves cursor back 1, or throws exception if there is none.
   * @param entry the current entry (perhaps edited from what is in history)
   */
  public void movePrevious(String entry) {
    if (!hasPrevious()) {
      throw  new ArrayIndexOutOfBoundsException();
    }
    setEditedEntry(entry);
    _cursor--;
  }

  /** 
   * Moves cursor forward 1, or throws exception if there is none. 
   * @param entry the current entry (perhaps edited from what is in history)
   */
  public void moveNext(String entry) {
    if (!hasNext()) {
      throw  new ArrayIndexOutOfBoundsException();
    }
    setEditedEntry(entry);
    _cursor++;
  }

  /** Returns whether moveNext() would succeed right now. */
  public boolean hasNext() {
    return  _cursor < (_vector.size());
  }

  /** Returns whether movePrevious() would succeed right now. */
  public boolean hasPrevious() {
    return  _cursor > 0;
  }

  /**
   * Returns item in history at current position, or throws exception if none.
   */
  public String getCurrent() {
    Integer cursor = new Integer(_cursor);
    if (_editedEntries.containsKey(cursor)) {
      return _editedEntries.get(cursor);
    }

    if (hasNext()) {
      return  _vector.get(_cursor);
    }
    else {
      return "";
    }
  }
  
  /**
   * Returns the number of items in this History.
   */
  public int size() {
    return _vector.size();
  }
  
  /**
   * Clears the vector
   */
  public void clear() {
    _vector.clear();
  }
  
  /**
   * Returns the history as a string by concatenating each string in the vector
   * separated by the delimiting character.
   * A semicolon is added to the end of every statement that didn't already
   * end with one.
   */
  public String getHistoryAsStringWithSemicolons() {
    StringBuffer s = new StringBuffer();
    String delimiter = INTERACTION_SEPARATOR + System.getProperty("line.separator");
    for (int i = 0; i < _vector.size(); i++) {
      String nextLine = _vector.get(i);
//      int nextLength = nextLine.length();
//      if ((nextLength > 0) && (nextLine.charAt(nextLength-1) != ';')) {
//        nextLine += ";";
//      }
//      s += nextLine + delimiter;
      s.append(nextLine);
      s.append(delimiter);
    }
    return s.toString();
  }

  /**
   * Returns the history as a string by concatenating each string in the vector
   * separated by the delimiting character
   */
  public String getHistoryAsString() {
    String s = "";
    String delimiter = System.getProperty("line.separator");
    for (int i = 0; i < _vector.size(); i++) {
      s +=_vector.get(i) + delimiter;
    }
    return s;
  }
  
  /**
   * Writes this (unedited) History to the file selected in the FileSaveSelector
   * @param selector File to save to
   */
  public void writeToFile(FileSaveSelector selector) throws IOException {
    writeToFile(selector, getHistoryAsStringWithSemicolons());
  }
  
  /**
   * Writes this History to the file selected in the FileSaveSelector
   * @param selector File to save to
   * @param editedVersion The edited version of the text to be saved.
   * The saved file will still include
   * any tags or extensions needed to recognize it as a saved interactions file.
   */
  public void writeToFile(FileSaveSelector selector, final String editedVersion) 
    throws IOException
  {
    
    File c = null;
    
    try {
      c = selector.getFile();
    }
    catch (OperationCanceledException oce) {
      return; // don't need to do anything
    }
    
    // Make sure we ask before overwriting
    if (c != null) {
      if (!c.exists() || selector.verifyOverwrite()) {
        if (c.getName().indexOf('.') == -1) {
          c = new File(c.getAbsolutePath() + "." +
                       InteractionsHistoryFilter.HIST_EXTENSION);
        }
        FileOps.DefaultFileSaver saver = new FileOps.DefaultFileSaver(c) {
          public void saveTo(OutputStream os) throws IOException {
            
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            String file = HISTORY_FORMAT_VERSION_2 + editedVersion;
//            if (PlatformFactory.ONLY.isWindowsPlatform()) {
//              StringBuffer buf = new StringBuffer();
//              String lineSep = System.getProperty("line.separator");
//              int last = 0;
//              for (int i = file.indexOf('\n'); i != -1; i = file.indexOf('\n', last)) {
//                buf.append(file.substring(last, i));
//                buf.append(lineSep);
//                last = i+1;
//              }
//              file = buf.toString();
//            }
            bw.write(file, 0, file.length());
            bw.close();
          }
        };
        FileOps.saveFile(saver);
      }
    }
  }
  
  /**
   * Changes the maximum number of interactions remembered by this History.
   * @param newSize New number of interactions to remember.
   */
  public void setMaxSize(int newSize) {
    // Sanity check
    if (newSize < 0) newSize = 0;
    
    // Remove old elements if the new size is less than current size
    if (size() > newSize) {
      
      int numToDelete = size() - newSize;
      
      for (int i=0; i< numToDelete; i++) {
        _vector.remove(0);
      }
      
      moveEnd();
    }
    MAX_SIZE = newSize;
  }
  
  /**
   * The OptionListener for HISTORY_MAX_SIZE
   */
  public class HistorySizeOptionListener implements OptionListener<Integer> {
    public void optionChanged (OptionEvent<Integer> oce) {
      int newSize = oce.value.intValue();
      setMaxSize(newSize);
    }
  }
  
  /**
   * Sets the edited entry to the given value.
   * @param entry the string to set
   */
  public void setEditedEntry(String entry) {
    if (!entry.equals(getCurrent())) {
      _editedEntries.put(new Integer(_cursor), entry);
    }
  }
  
  /**
   * Reverse-searches the history for the previous matching string.
   * @param currentInteraction the current interaction
   */
  public void reverseSearch(String currentInteraction) {
    if (_currentSearchString.equals("") || 
        !currentInteraction.startsWith(_currentSearchString)) {
      _currentSearchString = currentInteraction;
    }
    
    setEditedEntry(currentInteraction);
    while (hasPrevious()) {
      movePrevious(getCurrent());
      
      if (getCurrent().startsWith(_currentSearchString, 0)) {
        break;
      }
    }
    if (!getCurrent().startsWith(_currentSearchString, 0)) {
      moveEnd();
    }
  }
  
  /**
   * Forward-searches the history for the next matching string.
   * @param currentInteraction the current interaction
   */
  public void forwardSearch(String currentInteraction) {
    if (_currentSearchString.equals("") || 
        !currentInteraction.startsWith(_currentSearchString)) {
      _currentSearchString = currentInteraction;
    }
    
    setEditedEntry(currentInteraction);
    while (hasNext()) {
      moveNext(getCurrent());
      
      if (getCurrent().startsWith(_currentSearchString, 0)) {
        break;
      }
    }
    if (!getCurrent().startsWith(_currentSearchString, 0)) {
      moveEnd();
    }
  }
}
