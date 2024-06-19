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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.util.swing.FileSelectorComponent;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.Configuration;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.DirectorySelector;
import edu.rice.cs.drjava.model.OperationCanceledException;

import javax.swing.*;
import java.io.File;

/**
 * Manages a dialog box that can select a destination directory for generating
 * Javadoc.  The getDirectory method should be called to show the dialog,
 * using the suggested location for the Javadoc as the "start" file.  If the
 * user modifies the selection once, the user's choice will be remembered and
 * no further suggestions will be used.
 * 
 * @version $Id: JavadocDialog.java,v 1.1 2005/08/05 12:45:08 guehene Exp $
 */
public class JavadocDialog implements DirectorySelector {
  /** Parent frame of the dialog. */
  private final JFrame _frame;
  
  /** File field and button. */
  private final FileSelectorComponent _selector;
  
  /** Whether to always prompt for destination. */
  private final JCheckBox _checkBox;
  
  /** OptionPane from which to get the results. */
  private final JOptionPane _optionPane;
  
  /** Dialog to show. */
  private final JDialog _dialog;
  
  /** Whether to use the suggested directory each time the dialog is shown. */
  private boolean _useSuggestion;
  
  /** Current suggestion for the destination directory, or null. */
  private File _suggestedDir;
  
  /**
   * Creates a new JavadocDialog to show from the given frame.
   * @param frame Parent frame of this dialog
   */
  public JavadocDialog(JFrame frame) {
    _frame = frame;
    _useSuggestion = true;
    _suggestedDir = null;
    
    // Create file chooser
    JFileChooser chooser = new JFileChooser();
    chooser.setMultiSelectionEnabled(false);
    // We use FILES_AND_DIRECTORIES because it behaves better than
    //  DIRECTORIES_ONLY.  (The latter will return the parent of a
    //  pre-selected directory unless the user chooses something else first.)
    //  I hate JFileChooser.
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    chooser.setApproveButtonText("Select");
    chooser.setFileFilter(new DirectoryFilter());
    
    // Create components for dialog
    String msg = "Select a destination directory for the Javadoc files:";
    _selector = new FileSelectorComponent(_frame, chooser);
    _checkBox = new JCheckBox("Always Prompt For Destination");
    Object[] components = new Object[] { msg, _selector, _checkBox };
    
    _optionPane = new JOptionPane(components,
                                  JOptionPane.QUESTION_MESSAGE,
                                  JOptionPane.OK_CANCEL_OPTION);
    _dialog = _optionPane.createDialog(_frame, "Select Javadoc Destination");
  }
  
  
  /**
   * Shows the dialog prompting the user for a destination directory
   * in which to generate Javadoc.
   * 
   * @param start The directory to display in the text box.  If null,
   * the most recent suggested directory (passed in via setSuggestedDir)
   * is displayed, unless the user has modified a previous suggestion.
   * @return A directory to use for the Javadoc (which might not exist)
   * @throws OperationCanceledException if the selection request is canceled
   */
  public File getDirectory(File start) throws OperationCanceledException {
    if (start != null) {
      // We were given a default - use it.
      _selector.setFileField(start);
    }
    else if (_useSuggestion && (_suggestedDir != null)) {
      // We weren't given one, so we need to use our suggestion.
      _selector.setFileField(_suggestedDir);
    }
    
    Configuration config = DrJava.getConfig();
    boolean ask = config.getSetting(OptionConstants.JAVADOC_PROMPT_FOR_DESTINATION).booleanValue();
    
    if (ask) {
      // The "always prompt" checkbox should be checked
      _checkBox.setSelected(true);
      
      // Prompt the user
      _dialog.show();
      
      // Get result
      if (!_isPositiveResult()) {
        throw new OperationCanceledException();
      }
      
      // See if the user wants to suppress this dialog in the future.
      if (!_checkBox.isSelected()) {
        config.setSetting(OptionConstants.JAVADOC_PROMPT_FOR_DESTINATION,
                          Boolean.FALSE);
      }
      
      // Check if the user disagreed with the suggestion
      if ((start == null) &&
          (_useSuggestion && (_suggestedDir != null)) &&
          !_selector.getFileFromField().equals(_suggestedDir)) {
        _useSuggestion = false;
      }
    }
    return _selector.getFileFromField();
  }
  
  /**
   * Asks the user a yes/no question.
   * @return true if the user responded affirmatively, false if negatively
   */
  public boolean askUser(String message, String title) {
    int choice = JOptionPane.showConfirmDialog(_frame, message, title,
                                               JOptionPane.YES_NO_OPTION);
    return (choice == JOptionPane.YES_OPTION);
  }
  
  /**
   * Warns the user about an error condition.
   */
  public void warnUser(String message, String title) {
    JOptionPane.showMessageDialog(_frame, message, title,
                                  JOptionPane.ERROR_MESSAGE);
  }
  
  /**
   * Sets the suggested destination directory for Javadoc generation.
   * This directory will be displayed in the file field if the user
   * has not modified the suggestion in the past.
   * @param dir Suggested destination directory
   */
  public void setSuggestedDir(File dir) {
    _suggestedDir = dir;
  }
  
  /**
   * Sets whether the dialog should use the suggested directory provided
   * to the getDirectory method as the default location.
   * @param use Whether to use the suggested directory
   */
  public void setUseSuggestion(boolean use) {
    _useSuggestion = use;
  }
  
  /**
   * Returns whether the JOptionPane currently has the OK_OPTION result.
   */
  private boolean _isPositiveResult() {
    Object result = _optionPane.getValue();
    if ((result != null) && (result instanceof Integer)) {
      int rc = ((Integer)result).intValue();
      return rc == JOptionPane.OK_OPTION;
    }
    else {
      return false;
    }
  }
}