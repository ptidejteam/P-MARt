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

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Event;
import java.awt.Cursor;
import java.lang.reflect.InvocationTargetException;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.model.InputListener;
import edu.rice.cs.drjava.model.repl.ConsoleDocument;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.InteractionsModel;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.SwingWorker;
import edu.rice.cs.util.text.DocumentEditCondition;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.util.text.SwingDocumentAdapter;

/**
 * This class installs listeners and actions between an InteractionsDocument
 * in the model and an InteractionsPane in the view.
 * 
 * We may want to refactor this class into a different package.
 * 
 * @version $Id: InteractionsController.java,v 1.1 2005/08/05 12:45:08 guehene Exp $
 */
public class InteractionsController extends AbstractConsoleController {
  /**
   * InteractionsModel to handle interpretation
   */
  protected InteractionsModel _model;
  
  /**
   * Document from the model.
   */
  protected InteractionsDocument _doc;
  
  /**
   * Style to use for error messages.
   */
  protected SimpleAttributeSet _errStyle;
  
  /**
   * Style to use for debug messages.
   */
  protected final SimpleAttributeSet _debugStyle;
  protected static final String INPUT_ENTERED_NAME = "Input Entered";
  protected static final String INSERT_NEWLINE_NAME = "Insert Newline";

  /**
   * Invoked when input is completed in an input box.
   */
  protected Action _inputEnteredAction = new AbstractAction() {
    public synchronized void actionPerformed(ActionEvent e) {
      _box.setEditable(false);
      _box.getCaret().setVisible(false);
      setEnabled(false);
//      _insertNewlineAction.setEnabled(false);
      _inputText = _box.getText();
      notify();
      _pane.setCaretPosition(_doc.getDocLength());
      _pane.requestFocus();
    }
  };

  /**
   * Shift-Enter action in a System.in box.  Inserts a newline.
   */
  protected Action _insertNewlineAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      _box.insert("\n", _box.getCaretPosition());
    }
  };

  /**
   * Current contents of the most recent InputBox.
   */
  private String _inputText;
  
  /**
   * The most recent graphical box used to request input for
   * System.in.
   */
  private InputBox _box;

  /**
   * Listens for input requests from System.in, displaying an input box as needed.
   */
  protected InputListener _inputListener = new InputListener() {
    public String getConsoleInput() {
      synchronized(_inputEnteredAction) {
        
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            _box = new InputBox();
            
            /*        if (_busy()) {
             _pane.setEditable(true);
             moveToEnd();
             _pane.insertComponent(_box);
             _pane.setEditable(false);
             }
             else {
             DocumentEditCondition ec = _doc.getEditCondition();
             _doc.setEditCondition(new DocumentEditCondition());
             int pos = _doc.getPositionBeforePrompt();
             _pane.setCaretPosition(pos);
             _pane.insertComponent(_box);
             _doc.setPromptPos(_doc.getPromptPos() + 1);
             //            _doc.insertBeforeLastPrompt("\n", _doc.DEFAULT_STYLE);
             _doc.setEditCondition(ec);
             }
             */
            int pos = _doc.getPositionBeforePrompt();
            _doc.insertBeforeLastPrompt(" ", _doc.DEFAULT_STYLE);
            SimpleAttributeSet att = new SimpleAttributeSet();
            StyleConstants.setComponent(att, _box);
            _adapter.setCharacterAttributes(pos, 1, att, false);
            _doc.insertBeforeLastPrompt("\n", _doc.DEFAULT_STYLE);
//            try {
//              int len = _doc.getDocLength();
//              _doc.forceInsertText(len, " ", _doc.DEFAULT_STYLE);
//              _doc.forceRemoveText(len, 1);
//            }
//            catch (DocumentAdapterException dae) {
//            }
            
            _inputEnteredAction.setEnabled(true);
            //          _insertNewlineAction.setEnabled(true);
            _box.requestFocus();
          }
        });
        
        try {
          _inputEnteredAction.wait();
        }
        catch (InterruptedException ie) {
        }
      }
      
      return _inputText + "\n";
    }
  };

  /**
   * Glue together the given model and a new view.
   * @param model An InteractionsModel
   * @param adapter SwingDocumentAdapter being used by the model's doc
   */
  public InteractionsController(InteractionsModel model, SwingDocumentAdapter adapter) {
    this(model, adapter, new InteractionsPane(adapter));
  }
  
  /**
   * Glue together the given model and view.
   * @param model An InteractionsModel
   * @param adapter SwingDocumentAdapter being used by the model's doc
   * @param pane An InteractionsPane
   */
  public InteractionsController(InteractionsModel model,
                                SwingDocumentAdapter adapter,
                                InteractionsPane pane) {
    super(adapter, pane);
    _model = model;
    _doc = model.getDocument();
    _errStyle = new SimpleAttributeSet();
    _debugStyle = new SimpleAttributeSet();
    
    _init();
  }

  /**
   * Gets the input listener for console input requests.
   */
  public InputListener getInputListener() {
    return _inputListener;
  }

  public void notifyInputEnteredAction() {
    synchronized(_inputEnteredAction) {
      _inputEnteredAction.notify();
    }
  }
  
  /**
   * Accessor method for the InteractionsModel.
   */
  public InteractionsModel getInteractionsModel() {
    return _model;
  }

  /**
   * Allows the abstract superclass to use the document.
   * @return the InteractionsDocument
   */
  public ConsoleDocument getConsoleDoc() {
    return _doc;
  }

  /**
   * Accessor method for the InteractionsDocument.
   */
  public InteractionsDocument getDocument() {
    return _doc;
  }
  
  /**
   * Notifies the inputEnteredAction. Called by DefaultGlobalModel when reset is called so
   * that this lock is released.
   */
//  public void notifyInputEnteredAction() {
// _inputEnteredAction.notify();
//}
  
  /**
   * Adds AttributeSets as named styles to the document adapter.
   */
  protected void _addDocumentStyles() {
    // Add AbstractConsoleController styles
    super._addDocumentStyles();

    // Error
    _errStyle.addAttributes(_defaultStyle);
    _errStyle.addAttribute(StyleConstants.Foreground, DrJava.getConfig().getSetting(OptionConstants.INTERACTIONS_ERROR_COLOR));
    _errStyle.addAttribute(StyleConstants.Bold, Boolean.TRUE);
    _adapter.setDocStyle(InteractionsDocument.ERROR_STYLE, _errStyle);
    DrJava.getConfig().addOptionListener(OptionConstants.INTERACTIONS_ERROR_COLOR,
                                         new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oe) {
        _errStyle.addAttribute(StyleConstants.Foreground, oe.value);
      }
    });
    
    // Debug
    _debugStyle.addAttributes(_defaultStyle);
    _debugStyle.addAttribute(StyleConstants.Foreground, DrJava.getConfig().getSetting(OptionConstants.DEBUG_MESSAGE_COLOR));
    _debugStyle.addAttribute(StyleConstants.Bold, Boolean.TRUE);
    _adapter.setDocStyle(InteractionsDocument.DEBUGGER_STYLE, _debugStyle);
    DrJava.getConfig().addOptionListener(OptionConstants.DEBUG_MESSAGE_COLOR,
                                         new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oe) {
        _debugStyle.addAttribute(StyleConstants.Foreground, oe.value);
      }
    });
  }
  
  /**
   * Updates all document styles with the attributes contained in newSet.
   * This behavior is only used in Mac OS X, JDK 1.4.1, since
   * setFont() works fine on JTextPane on all other tested platforms.
   * @param newSet Style containing new attributes to use.
   */
  protected void _updateStyles(AttributeSet newSet) {
    super._updateStyles(newSet);
    _errStyle.addAttributes(newSet);
    StyleConstants.setBold(_errStyle, true);  // ensure err is always bold
    _debugStyle.addAttributes(newSet);
    StyleConstants.setBold(_debugStyle, true);  // ensure debug is always bold
  }
  
  /**
   * Adds listeners to the model.
   */
  protected void _setupModel() {
    _adapter.addDocumentListener(new CaretUpdateListener());
    _doc.setBeep(_pane.getBeep());
  }

  
  /**
   * Adds actions to the view.
   */
  protected void _setupView() {
    super._setupView();

    // Get proper cross-platform mask.
    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    // Add actions with keystrokes
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), 
                                evalAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 
                                                       java.awt.Event.SHIFT_MASK), 
                                newLineAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_B, mask), 
                                clearCurrentAction);

    // Up and down need to be bound both for keypad and not
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), 
                                historyPrevAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), 
                                historyPrevAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), 
                                historyNextAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), 
                                historyNextAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), 
                                historyReverseSearchAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 
                                                       java.awt.Event.SHIFT_MASK), 
                                historyForwardSearchAction);
    

    // Left needs to be prevented from rolling cursor back before the prompt.
    // Both left and right should lock when caret is before the prompt.
    // Caret is allowed before the prompt for the purposes of mouse-based copy-
    // and-paste.
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0),
                                moveLeftAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
                                moveLeftAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0),
                                moveRightAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
                                moveRightAction);

    // Prevent previous word action from going past the prompt
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_PREVIOUS_WORD),
                                prevWordAction);
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_PREVIOUS_WORD,
                                         new OptionListener<KeyStroke>() {
      public void optionChanged(OptionEvent<KeyStroke> oe) {
        _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_PREVIOUS_WORD),
                                    prevWordAction);
      }
    });

    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_NEXT_WORD),
                                nextWordAction);
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_NEXT_WORD, new OptionListener<KeyStroke>() {
      public void optionChanged(OptionEvent<KeyStroke> oe) {
        _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_NEXT_WORD),
                                    nextWordAction);
      }
    });
  }

  // The fields below were made package private for testing purposes.

  /**
   * Evaluates the interaction on the current line.
   */
  AbstractAction evalAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      SwingWorker worker = new SwingWorker() {
        public Object construct() {
          _model.interpretCurrentInteraction();
          return null;
        }
      };
      worker.start();
    }
  };
  
  /**
   * Recalls the previous command from the history.
   */
  AbstractAction historyPrevAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (!_busy()) {
        _doc.recallPreviousInteractionInHistory();
        moveToEnd();
      }
    }
  };

  /**
   * Recalls the next command from the history.
   */
  AbstractAction historyNextAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (!_busy()) {
        _doc.recallNextInteractionInHistory();
        moveToEnd();
      }
    }
  };

  /**
   * Reverse searches in the history.
   */
  AbstractAction historyReverseSearchAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (!_busy()) {
        _doc.reverseSearchInteractionsInHistory();
        moveToEnd();
      }
    }
  };

  /**
   * Forward searches in the history.
   */
  AbstractAction historyForwardSearchAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (!_busy()) {
        _doc.forwardSearchInteractionsInHistory();
        moveToEnd();
      }
    }
  };

  /**
   * Moves the caret left or wraps around.
   */
  AbstractAction moveLeftAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (!_busy()) {
        int position = _pane.getCaretPosition();
        if (position < _doc.getPromptPos()) {
          moveToPrompt();
        }
        else if (position == _doc.getPromptPos()) {
          // Wrap around to the end
          moveToEnd();
        }
        else { // position > _doc.getPromptPos()
          _pane.setCaretPosition(position - 1);
        }
      }
    }
  };

  /**
   * Moves the caret right or wraps around.
   */
  AbstractAction moveRightAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      int position = _pane.getCaretPosition();
      if (position < _doc.getPromptPos()) { 
        moveToEnd();
      }
      else if (position >= _doc.getDocLength()) {
        // Wrap around to the start
        moveToPrompt();
      }
      else { // position between prompt and end
        _pane.setCaretPosition(position + 1);
      }
    }
  };

  /**
   * Skips back one word.  Doesn't move past the prompt.
   */
  AbstractAction prevWordAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      int position = _pane.getCaretPosition();
      int promptPos = _doc.getPromptPos();
      if (position < promptPos) {
        moveToPrompt();
      }
      else if (position == promptPos) {
        // Wrap around to the end
        moveToEnd();
      }
     else {
        _pane.getActionMap().get(DefaultEditorKit.previousWordAction).actionPerformed(e);
      }
    }
  };

  /**
   * Skips forward one word.  Doesn't move past the prompt.
   */
  AbstractAction nextWordAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      int position = _pane.getCaretPosition();
      int promptPos = _doc.getPromptPos();
      if (position < promptPos) {
        moveToEnd();
      }
      else if (position >= _doc.getDocLength()) {
        // Wrap around to the start
        moveToPrompt();
      }
      else {
        _pane.getActionMap().get(DefaultEditorKit.nextWordAction).actionPerformed(e);
      }
    }
  };

  /**
   * A box that can be inserted into the interactions pane for separate input.
   */
  class InputBox extends JTextArea {
    private static final int BORDER_WIDTH = 1;
    private static final int INNER_BUFFER_WIDTH = 3;
    private static final int OUTER_BUFFER_WIDTH = 2;
    private Color _bgColor = DrJava.getConfig().getSetting(OptionConstants.DEFINITIONS_BACKGROUND_COLOR);
    private Color _fgColor = DrJava.getConfig().getSetting(OptionConstants.DEFINITIONS_NORMAL_COLOR);
    private Color _sysInColor = DrJava.getConfig().getSetting(OptionConstants.SYSTEM_IN_COLOR);
    public InputBox() {
      setForeground(_sysInColor);
      setBackground(_bgColor);
      setCaretColor(_fgColor);
      setBorder(_createBorder());
      setLineWrap(true);

      InputMap im = getInputMap(WHEN_FOCUSED);
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), INPUT_ENTERED_NAME);
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,Event.SHIFT_MASK), INSERT_NEWLINE_NAME);
      ActionMap am = getActionMap();
      am.put(INPUT_ENTERED_NAME, _inputEnteredAction);
      am.put(INSERT_NEWLINE_NAME, _insertNewlineAction);

      DrJava.getConfig().addOptionListener(OptionConstants.DEFINITIONS_NORMAL_COLOR,
                                           new OptionListener<Color>() {
        public void optionChanged(OptionEvent<Color> oe) {
          _fgColor = oe.value;
          setBorder(_createBorder());
          setCaretColor(oe.value);
        }
      });
      DrJava.getConfig().addOptionListener(OptionConstants.DEFINITIONS_BACKGROUND_COLOR,
                                           new OptionListener<Color>() {
        public void optionChanged(OptionEvent<Color> oe) {
          _bgColor = oe.value;
          setBorder(_createBorder());
          setBackground(oe.value);
        }
      });
      DrJava.getConfig().addOptionListener(OptionConstants.SYSTEM_IN_COLOR,
                                           new OptionListener<Color>() {
        public void optionChanged(OptionEvent<Color> oe) {
          _sysInColor = oe.value;
          setForeground(oe.value);
        }
      });
    }

    private Border _createBorder() {
      Border outerouter = BorderFactory.createLineBorder(_bgColor, OUTER_BUFFER_WIDTH);
      Border outer = BorderFactory.createLineBorder(_fgColor, BORDER_WIDTH);
      Border inner = BorderFactory.createLineBorder(_bgColor, INNER_BUFFER_WIDTH);
      Border temp = BorderFactory.createCompoundBorder(outer, inner);
      return BorderFactory.createCompoundBorder(outerouter, temp);
    }
  }
}
