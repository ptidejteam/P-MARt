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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import edu.rice.cs.drjava.model.repl.InteractionsScriptModel;

/**
 * Controller for an interactions script.
 * @version $Id: InteractionsScriptController.java,v 1.1 2005/08/05 12:45:09 guehene Exp $
 */
public class InteractionsScriptController {
  /** Associated model. */
  private InteractionsScriptModel _model;
  /** Associated view. */
  private InteractionsScriptPane _pane;
  /** Interactions pane. */
  private InteractionsPane _interactionsPane;

  /**
   * Builds a new interactions script pane and links it to the given model.
   * @param model the InteractionsScriptModel to use
   * @param closeAction how to close this script.
   */
  public InteractionsScriptController(InteractionsScriptModel model, Action closeAction,
                                      InteractionsPane interactionsPane) {
    _model = model;
    _closeScriptAction = closeAction;
    _interactionsPane = interactionsPane;
    _pane = new InteractionsScriptPane(4, 1);
    
    // Previous
    _setupAction(_prevInteractionAction, "Previous", "Insert Previous Interaction from Script");
    _pane.addButton(_prevInteractionAction);
    // Next
    _setupAction(_nextInteractionAction, "Next", "Insert Next Interaction from Script");
    _pane.addButton(_nextInteractionAction);
    // Execute
    _setupAction(_executeInteractionAction, "Execute", "Execute Current Interaction");
    _pane.addButton(_executeInteractionAction, _interactionsPane);
    // Close
    _setupAction(_closeScriptAction, "Close", "Close Interactions Script");
    _pane.addButton(_closeScriptAction);
    setActionsEnabled();
  }

  /**
   * Sets the navigation actions to be enabled, if appropriate.
   */
  public void setActionsEnabled() {
    _nextInteractionAction.setEnabled(_model.hasNextInteraction());
    _prevInteractionAction.setEnabled(_model.hasPrevInteraction());
    _executeInteractionAction.setEnabled(true);
  }

  /**
   * Disables navigation actions
   */
  public void setActionsDisabled() {
    _nextInteractionAction.setEnabled(false);
    _prevInteractionAction.setEnabled(false);
    _executeInteractionAction.setEnabled(false);
  }

  /**
   * @return the interactions script pane controlled by this controller.
   */
  public InteractionsScriptPane getPane() {
    return _pane;
  }

  /** Action to go back in the script. */
  private Action _prevInteractionAction = new AbstractAction("Previous") {
    public void actionPerformed(ActionEvent e) {
      _model.prevInteraction();
      setActionsEnabled();
      _interactionsPane.requestFocus();
    }
  };
  /** Action to go forward in the script. */
  private Action _nextInteractionAction = new AbstractAction("Next") {
    public void actionPerformed(ActionEvent e) {
      _model.nextInteraction();
      setActionsEnabled();
      _interactionsPane.requestFocus();
    }
  };
  /** Action to execute the current interaction. */
  private Action _executeInteractionAction = new AbstractAction("Execute") {
    public void actionPerformed(ActionEvent e) {
      _model.executeInteraction();      
      _interactionsPane.requestFocus();
    }
  };
  /** Action to end the script.  (Defined in constructor.) */
  private Action _closeScriptAction; /* = new AbstractAction("<=Close=>") {
    public void actionPerformed(ActionEvent e) {
      _model.closeScript();
      _pane.setMaximumSize(new Dimension(0,0));
    }
  };*/
  
  /**
   * Sets up fields on the given Action, such as the name and tooltip.
   * @param a Action to modify
   * @param name Default name for the Action (for buttons)
   * @param desc Short description of the Action (for tooltips)
   */
  protected void _setupAction(Action a, String name, String desc) {
    a.putValue(Action.DEFAULT, name);
    a.putValue(Action.SHORT_DESCRIPTION, desc);
  }
}