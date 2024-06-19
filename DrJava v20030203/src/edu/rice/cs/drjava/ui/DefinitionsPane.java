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

package edu.rice.cs.drjava.ui;

import  javax.swing.*;
import  javax.swing.undo.*;
import  javax.swing.event.*;
import  javax.swing.text.*;
import  java.awt.*;
import  java.awt.event.*;
import  java.io.*;
import  java.util.*;

import gj.util.Stack;
import gj.util.Hashtable;
import edu.rice.cs.util.Pair;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.HighlightManager;
import edu.rice.cs.util.swing.SwingWorker;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.DefinitionsEditorKit;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.model.debug.DebugException;
import edu.rice.cs.drjava.model.debug.Breakpoint;

/**
 * The pane in which work on a given OpenDefinitionsDocument occurs.
 * A DefinitionsPane is tied to a single document, which cannot be
 * changed.
 * @version $Id: DefinitionsPane.java,v 1.1 2005/04/27 20:30:32 elmoutam Exp $
 */
public class DefinitionsPane extends JEditorPane 
  implements OptionConstants {
  private static final EditorKit EDITOR_KIT = new DefinitionsEditorKit();
  private static final int UNDO_LIMIT = 1000;

  /**
   * Our parent window.
   */
  private MainFrame _mainFrame;
  private GlobalModel _model;
  private final OpenDefinitionsDocument _doc;
  private OurUndoManager _undoManager;
  private UndoAction _undoAction;
  private RedoAction _redoAction;
  private HighlightManager _highlightManager;

  /**
   * Flag used to determine if the user has already been warned about debugging
   * when the document within this defpane has been modified since its last save.
   */
  private boolean _hasWarnedAboutModified = false;
  
  /**
   * Our current paren/brace/bracket matching highlight.
   */
  private HighlightManager.HighlightInfo _matchHighlight = null;
  
  /**
   * Used by the centering source mechanism to ensure paints
   */
  private boolean _updatePending = false;
  
  /**
   * Paren/brace/bracket matching highlight color.
   */
  public static DefaultHighlighter.DefaultHighlightPainter
    MATCH_PAINTER;
    
  static {
    Color highColor = DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR);
    
    MATCH_PAINTER = 
      new DefaultHighlighter.DefaultHighlightPainter(highColor);
  }

  /**
   * Our current compiler error matching highlight.
   */
  private HighlightManager.HighlightInfo _errorHighlightTag = null;
  
  /**
   * Our current test error matching highlight.
   */
  private HighlightManager.HighlightInfo _testErrorHighlightTag = null;
  
  /**
   * Highlight painter for selected errors in the defs doc.
   */
  public static DefaultHighlighter.DefaultHighlightPainter
    ERROR_PAINTER =
    new DefaultHighlighter.DefaultHighlightPainter(DrJava.getConfig().getSetting(COMPILER_ERROR_COLOR));
  
  /**
   *  Highlight painter for breakpoints
   */
  public static DefaultHighlighter.DefaultHighlightPainter 
    BREAKPOINT_PAINTER =
    new DefaultHighlighter.DefaultHighlightPainter(DrJava.getConfig().getSetting(DEBUG_BREAKPOINT_COLOR));
  
  /**
   * Highlight painter for thread's current location
   */
  public static DefaultHighlighter.DefaultHighlightPainter 
    THREAD_PAINTER =
    new DefaultHighlighter.DefaultHighlightPainter(DrJava.getConfig().getSetting(DEBUG_THREAD_COLOR));
  
  /**
   * The OptionListener for DEFINITIONS_BACKGROUND_COLOR 
   */
  private class BackgroundColorOptionListener implements OptionListener<Color> { 
    public void optionChanged(OptionEvent<Color> oce) {
      setBackground(oce.value);
    }
  }
  
  /**
   * The OptionListener for DEFINITIONS_MATCH_COLOR 
   */
  private class MatchColorOptionListener implements OptionListener<Color> { 
    public void optionChanged(OptionEvent<Color> oce) {
      MATCH_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(oce.value);
      if (_matchHighlight != null) {
        int start = _matchHighlight.getStartOffset();
        int end = _matchHighlight.getEndOffset();
        _matchHighlight.remove();
        _addHighlight(start, end);
      }
    }
  }
  
  /**
   * The OptionListener for COMPILER_ERROR_COLOR
   */
  private class ErrorColorOptionListener implements OptionListener<Color> { 
    public void optionChanged(OptionEvent<Color> oce) {
      ERROR_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(oce.value);
      if (_errorHighlightTag != null) {
        int start = _errorHighlightTag.getStartOffset();
        int end = _errorHighlightTag.getEndOffset();
        _errorHighlightTag.remove();
        addCompilerErrorHighlight(start, end);
      }
      if (_testErrorHighlightTag != null) {
        int start = _testErrorHighlightTag.getStartOffset();
        int end = _testErrorHighlightTag.getEndOffset();
        _testErrorHighlightTag.remove();
        addTestErrorHighlight(start, end);
      }
    }
  }
  
  /**
   * The OptionListener for DEBUG_BREAKPOINT_COLOR 
   */
  private class BreakpointColorOptionListener implements OptionListener<Color> { 
    public void optionChanged(OptionEvent<Color> oce) {
      BREAKPOINT_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(oce.value);
    }
  }
  
  /**
   * The OptionListener for DEBUG_THREAD_COLOR 
   */
  private class ThreadColorOptionListener implements OptionListener<Color> { 
    public void optionChanged(OptionEvent<Color> oce) {
      THREAD_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(oce.value);
    }
  }
  
  /**
   * The menu item for the "Toggle Breakpoint" option. Stored in field so that it may be enabled and
   * disabled depending on Debug Mode
   */
  private JMenuItem _toggleBreakpointMenuItem;
  
  /**
   * The menu item for the "Add Watch" option. Stored in field so that it may be enabled and
   * disabled depending on Debug Mode
   */
  //private JMenuItem _addWatchMenuItem;

  /**
   * The contextual popup menu for the Definitions Pane.
   */
  private JPopupMenu _popMenu;
  
  /**
   * The mouse adapter for handling a popup menu
   */
  private PopupMenuMouseAdapter _popupMenuMA;
  
  /**
   * Listens to caret to highlight errors as appropriate.
   */
  private CompilerErrorCaretListener _errorListener;

  /**
   * Listens to caret to highlight JUnit errors as appropriate.
   */
  private JUnitErrorCaretListener _junitErrorListener;  
  
  private ActionListener _setSizeListener = null;
  
  /**
   * Looks for changes in the caret position to see if a paren/brace/bracket
   * highlight is needed.
  */
  private CaretListener _matchListener = new CaretListener() {
    /**
     * Checks caret position to see if it needs to set or remove a highlight
     * from the document.
     * When the cursor is immediately right of ')', '}', or ']', it highlights
     * up to the matching open paren/brace/bracket.
     * @param e the event fired by the caret position change
     */
    public void caretUpdate(CaretEvent e) {
      //_doc().setCurrentLocation(getCaretPosition());
      _doc.syncCurrentLocationWithDefinitions(getCaretPosition());
      _removePreviousHighlight();
      try {
        _updateMatchHighlight();
      } catch (BadLocationException ex) {}
    }
  };  
  
  /**
   * Updates the highlight if there is any.
   * @exception BadLocationException
   */
  private void _updateMatchHighlight() throws BadLocationException {
    int to = getCaretPosition();
    int from = _doc.balanceBackward(); //_doc()._reduced.balanceBackward();
    if (from > -1) {
      // Found a matching open brace to this close brace
      from = to - from;
      _addHighlight(from, to);
      Highlighter.Highlight[] _lites = getHighlighter().getHighlights();
    }
    /** Not ready yet...
    else {
      // Try to match a close brace to this open brace
      from = _doc.balanceForward();
      if (from > -1) {
        from = to + from;
        _addHighlight(to, from);
        Highlighter.Highlight[] _lites = getHighlighter().getHighlights();
      }
    }
    */
  }

  /**
   * Adds a highlight to the document.  Called by _updateMatchHighlight().
   * @param from start of highlight
   * @param to end of highlight
   * @exception BadLocationException
   */
  private void _addHighlight(int from, int to) {
    _matchHighlight = _highlightManager.addHighlight(from, to, MATCH_PAINTER);
  }
  
  /**
   * Removes the previous highlight so document is cleared when caret position changes.
   */
  private void _removePreviousHighlight() {
    if (_matchHighlight != null) {
      _matchHighlight.remove();
      //_highlightManager.removeHighlight((HighlightManager.HighlightInfo)_matchHighlight);
      _matchHighlight = null;
    }
  }

  private UndoableEditListener _undoListener = new UndoableEditListener() {

    /**
     * The function to handle what happens when an UndoableEditEvent occurs.
     * @param e
     */
    public void undoableEditHappened(UndoableEditEvent e) {
      //Remember the edit and update the menus
      UndoWithPosition undo = new UndoWithPosition(e.getEdit(), getCaretPosition());
      _undoManager.addEdit(undo);      
      _undoAction.updateUndoState();
      _redoAction.updateRedoState();
    }
  };

  /**
   * An action to handle indentation spawned by pressing the tab key.
   */
  private class IndentKeyActionTab extends AbstractAction {

    /** Handle the key typed event from the text field. */
    public void actionPerformed(ActionEvent e) {
      //int pos = getCaretPosition();
      //_doc().setCurrentLocation(pos);
      indent();
    }
  }


  /**
   * Used for indent action spawned by pressing the enter key, '{', or '}'.
   */
  private class IndentKeyAction extends AbstractAction {
    /**
     * The key string ("\n"|"{"|"}") for the key pressed that invokes this
     * instance. Not used currently, but there for readability and possible
     * future use, e.g., debugging add-ons or the rewrite of the indention code.
     */
    private final String _key;

    /**
     * The default action to take when the specified key is pressed.
     */
    private final Action _defaultAction;

    IndentKeyAction(String key, Action defaultAction) {
      _key = key;
      _defaultAction = defaultAction;
    }

    /**
     * Handle the "key typed" event from the text field.
     * Calls the default action to make sure the right things happen, then makes
     * a call to indentLine().
     */
    public void actionPerformed(ActionEvent e) {
      _defaultAction.actionPerformed(e);
      indent();
    }
  }

  /**
   * Special action to take care of case when tab key is pressed.
   */
  private Action _indentKeyActionTab = new IndentKeyActionTab();
  /**
   * Because the "default" action for the enter key is special, it must be
   * grabbed from the Keymap using getAction(KeyStroke), which returns the
   * "default" action for all keys which have behavior extending beyond
   * regular text keys.
   */
  private Action _indentKeyActionLine =
    new IndentKeyAction("\n",
                        (Action)this.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)));

  /**
   * Likewise, regular text keys like '{', '}', and ':' do not have special actions
   * that are returned by getAction(KeyStroke). To make sure these behave right,
   * we use getDefaultAction() instead.
   */
  private Action _indentKeyActionSquiggly =
    new IndentKeyAction("}", getKeymap().getDefaultAction());

  private Action _indentKeyActionOpenSquiggly =
    new IndentKeyAction("{", getKeymap().getDefaultAction());

  private Action _indentKeyActionColon =
    new IndentKeyAction(":", getKeymap().getDefaultAction());

  /**
   * Takes in any keyboard input, checks to see if it is in the keyToActionMap 
   * in KeybindingManager, if so executes the action, otherwise checks if it
   * contains the current platform's menu shortcut modifier and if so, ignores 
   * that command (this disallows the execution of the UI's default 
   * actions such as cut/copy/paste/select all), otherwise does whatever 
   * normally would be done
   */
  public void processKeyEvent(KeyEvent e) {
    KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
    Action a = KeyBindingManager.Singleton.get(ks);
    if (a != null) {
      SwingUtilities.notifyAction(a, ks, e, e.getSource(), e.getModifiers());

      // Make sure we don't consume it again
      e.consume();
    }
    else {    
      // backspace deletes twice without this check, overrides other keystrokes
      // that use the mask modifier
      if (((ks.getModifiers() & mask) == 0) && ks.getKeyChar() != '\010') {
        super.processKeyEvent(e);       
      }
    }
  }
  
  
  /**
   * Constructor.  Sets up all the defaults.
   * @param mf the parent window
   */
  public DefinitionsPane(MainFrame mf,
                         GlobalModel model,
                         OpenDefinitionsDocument doc)
  {
    _mainFrame = mf;
    _model = model;
    _doc = doc;    
    setDocument(_doc);
    setContentType("text/java");
    setBackground(DrJava.getConfig().getSetting(DEFINITIONS_BACKGROUND_COLOR));
    //setFont(new Font("Courier", 0, 12));
    Font mainFont = DrJava.getConfig().getSetting(FONT_MAIN);
    setFont(mainFont);
    
    //setSize(new Dimension(1024, 1000));
    setEditable(true);

    //add actions for indent key
    Keymap ourMap = addKeymap("INDENT_KEYMAP", getKeymap());
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                                 (Action)_indentKeyActionLine);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
                                 (Action)_indentKeyActionTab);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke('}'),
      (Action)_indentKeyActionSquiggly);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke('{'),
      (Action)_indentKeyActionOpenSquiggly);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(':'),
      (Action)_indentKeyActionColon);
    setKeymap(ourMap);
                      
    //this.setEditorKit(new StyledEditorKit());
    
    // Add listener that checks if position in the document has changed.
    // If it has changed, check and see if we should be highlighting matching braces.
    this.addCaretListener(_matchListener);
 
    DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_BACKGROUND_COLOR,
                                    new BackgroundColorOptionListener());
    DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_MATCH_COLOR,
                                    new MatchColorOptionListener());
    DrJava.getConfig().addOptionListener( OptionConstants.COMPILER_ERROR_COLOR,
                                    new ErrorColorOptionListener());
    DrJava.getConfig().addOptionListener( OptionConstants.DEBUG_BREAKPOINT_COLOR,
                                    new BreakpointColorOptionListener());
    DrJava.getConfig().addOptionListener( OptionConstants.DEBUG_THREAD_COLOR,
                                    new ThreadColorOptionListener());
        
    createPopupMenu();
      
    //Add listener to components that can bring up popup menus.
    _popupMenuMA = new PopupMenuMouseAdapter();
    this.addMouseListener( _popupMenuMA );
      
    _highlightManager = new HighlightManager(this);
     
  }

  /**
   * Be sure to update the document (and thus the reduced model) any time
   * the caret position changes.
   * @param pos
   */
  public void setCaretPosition(int pos) {
    super.setCaretPosition(pos);
    _doc.getDocument().setCurrentLocation(pos);
  }
  
  /**
   *  Creates the popup menu for the DefinitionsPane
   */
  private void createPopupMenu() {
    
    //Create the popup menu.
    _popMenu = new JPopupMenu();
    
    _popMenu.add(_mainFrame.cutAction);
    _popMenu.add(_mainFrame.copyAction);
    _popMenu.add(_mainFrame.pasteAction);
    _popMenu.addSeparator();
    
    JMenuItem indentItem = new JMenuItem("Indent Line(s)");
    indentItem.addActionListener ( new AbstractAction() {
      public void actionPerformed( ActionEvent ae) {
        _indentLines();
      }
    });
    _popMenu.add(indentItem);
    
    if (_mainFrame.getModel().getDebugger() != null) {
      _popMenu.addSeparator();
      
      // Breakpoint
      JMenuItem breakpointItem = new JMenuItem("Toggle Breakpoint");
      breakpointItem.addActionListener( new AbstractAction() {
        public void actionPerformed( ActionEvent ae ) {
          //Make sure that the breakpoint is set on the *clicked* line, if within a selection block.
          setCaretPosition(viewToModel(_popupMenuMA.getLastMouseClick().getPoint()));
          _mainFrame.debuggerToggleBreakpoint();
        }
      });
      _toggleBreakpointMenuItem = _popMenu.add(breakpointItem);
      _toggleBreakpointMenuItem.setEnabled(false);
    }
   
    
  }
  
  /*
   * The private MouseAdapter for responding to various clicks concerning the popup menu
   */
  private class PopupMenuMouseAdapter extends MouseAdapter {
    
    private MouseEvent _lastMouseClick = null;
    
    public void mousePressed(MouseEvent e) {
      
      _lastMouseClick = e;
      
      // if not in the selected area, 
      if ( (viewToModel(e.getPoint()) < getSelectionStart()) || (viewToModel(e.getPoint()) > getSelectionEnd()) ) {
        //move caret to clicked position, deselecting previous selection
        setCaretPosition(viewToModel(e.getPoint()));
      }
      
      maybeShowPopup(e);
      //Don't show the "Toggle Breakpoint" option in the contextual menu, if the JMenuItem is null.
      if (_toggleBreakpointMenuItem != null) {
        _toggleBreakpointMenuItem.setEnabled(_mainFrame.inDebugMode());
      }
      //Don't show the "Add Watch" option in the contextual menu, if the JMenuItem is null.
      //if (_addWatchMenuItem != null) {
      //  _addWatchMenuItem.setEnabled(_mainFrame.inDebugMode());
      //}
      
    }
    
    public void mouseReleased(MouseEvent e) {
      maybeShowPopup(e);
    }
    
    private void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        _popMenu.show(e.getComponent(),
                      e.getX(), e.getY());
      }
    }
    
    public MouseEvent getLastMouseClick() {
      return _lastMouseClick;
    }
  }
  
  /**
   *  Indents the lines contained within the given selection.
   */
  private void _indentLines() {
    _doc.indentLinesInDefinitions(getSelectionStart(), getSelectionEnd());
  }
  
  
  /**
   * @return the undo action
   */
  public Action getUndoAction() {
    return  _undoAction;
  }

  /**
   * @return the redo action
   */
  public Action getRedoAction() {
    return  _redoAction;
  }

  /**
   * Get the OpenDefinitionsDocument contained in this DefinitionsPane.
   */
  public OpenDefinitionsDocument getOpenDocument() {
    return _doc;
  }

  /**
   * Access to the pane's HighlightManager
   */
  public HighlightManager getHighlightManager() {
    return _highlightManager;
  }
    
  /**
   * Set the caret position and also scroll to make sure the location is
   * visible.
   * @param pos Location to scroll to.
   */
  public void setPositionAndScroll(int pos) {
    try {
      setCaretPosition(pos);
      scrollRectToVisible(modelToView(pos));
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
  }


  /**
   * Override JEditorPane's setDocument to make sure only
   * the Document in our final OpenDefinitionsDocument can
   * be used.
   */
  public void setDocument(Document doc) {
    if (_doc != null) {
      if ((doc == null) || (!doc.equals(_doc.getDocument()))) {
        throw new IllegalStateException("Cannot set the document of " +
                                        "a DefinitionsPane to a " +
                                        "different document.");
      }
    }
    super.setDocument(doc);
  }

  /**
   * Add a CompilerErrorCaretListener to this pane, keeping it
   * accessible so its error model can be updated later.
   */
  public void addErrorCaretListener(CompilerErrorCaretListener listener) {
    _errorListener = listener;
    addCaretListener(listener);
  }

  /**
   * Add a JUnitErrorCaretListener to this pane, keeping it
   * accessible so its error model can be updated later.
   */
  public void addJUnitErrorCaretListener(JUnitErrorCaretListener listener) {
    _junitErrorListener = listener;
    addCaretListener(listener);
  }

  /**
   * Gets the CompilerErrorCaretListener for this pane.
   */
  public CompilerErrorCaretListener getErrorCaretListener() {
    return _errorListener;
  }

  /**
   * Gets the JUnitErrorCaretListener for this pane.
   */
  public JUnitErrorCaretListener getJUnitErrorCaretListener() {
    return _junitErrorListener;
  }

  /**
   * Adds a compiler error highlight to the document.
   * @exception BadLocationException
   */
  public void addCompilerErrorHighlight(int from, int to)
  {
    removeCompilerErrorHighlight();
    _errorHighlightTag = _highlightManager.addHighlight(from, to, ERROR_PAINTER);
  }

  /**
   * Removes the previous compiler error highlight from the document after 
   * the cursor has moved.
   */
  public void removeCompilerErrorHighlight() {
    if (_errorHighlightTag != null) {
      _errorHighlightTag.remove();
      _errorHighlightTag = null;
    }
  }
  
  /**
   * Adds a JUnit test error highlight to the document.
   * @exception BadLocationException
   */
  public void addTestErrorHighlight(int from, int to)
  {
    removeTestErrorHighlight();
    _testErrorHighlightTag = _highlightManager.addHighlight(from, to, ERROR_PAINTER);
  }

  /**
   * Removes the previous test error highlight from the document after the cursor
   * has moved.
   */
  public void removeTestErrorHighlight() {
    if (_testErrorHighlightTag != null) {
      _testErrorHighlightTag.remove();
      _testErrorHighlightTag = null;
    }
  }

  public boolean hasWarnedAboutModified() {
    return _hasWarnedAboutModified;
  }
  
  public void hasWarnedAboutModified( boolean hasWarned) {
    _hasWarnedAboutModified = hasWarned;
  }
  
  public void addBreakpointHighlight( Breakpoint bp ) {
    /*
    int lineStart = getStartPosFromLineNumber(bp.getLineNumber());
    int lineEnd = _doc.getLineEndPos(lineStart);
        
    _highlightManager.addHighlight(lineStart, lineEnd, _breakpointHighlighter);
    */
  }

  public void removeBreakpointHighlight( Breakpoint bp) {
    
  }

  /**
   * Reset undo machinery on setDocument.
   */
  private void setDocument(OpenDefinitionsDocument doc) {
    super.setDocument(doc.getDocument());

    _resetUndo();
  }
  
  public int getCurrentLine() { 
    try {
      int pos = getCaretPosition();
      FontMetrics metrics = getFontMetrics(getFont());
      Rectangle startRect = modelToView(pos);
      if (startRect == null) { 
        return 1;
      }
      //top left position is (3,3), so font size<=6 will be off
      return (new Double (startRect.getY() / metrics.getHeight()).intValue() + 1);
    } catch (BadLocationException e) {
      // This shouldnt happen b/c we retrieve the caret pos before calling
      // modelToView
      throw new UnexpectedException(e);
    }
  }
  
  public int getCurrentCol() {
    return _doc.getDocument().getCurrentCol();
  }
  public void setSize(int width, int height) {
    super.setSize(width, height);
    if (_setSizeListener != null) {
      _setSizeListener.actionPerformed(null);
    }
  }
  
  public void addSetSizeListener(ActionListener listener) {
    _setSizeListener = listener;
  } 
  public void removeSetSizeListener() {
    _setSizeListener = null;
  }
  
  public void centerViewOnOffset(int offset) {
    try {
      FontMetrics metrics = getFontMetrics(getFont());
      int length = _doc.getDocument().getLength();
      double viewWidth = _mainFrame.getDefViewport().getWidth();
      double viewHeight = _mainFrame.getDefViewport().getHeight();
      // Scroll to make sure this item is visible
      // Centers the selection in the viewport
      Rectangle startRect;
      startRect = this.modelToView(offset);
      
      int startRectX = (int)startRect.getX();
      int startRectY = (int)startRect.getY();
      startRect.setLocation(startRectX-(int)(viewWidth/2), 
                            startRectY-(int)(viewHeight/2));
      Point endPoint = new Point(startRectX+(int)(viewWidth/2), 
                                 startRectY+(int)(viewHeight/2 + 
                                                  metrics.getHeight()/2));
      
      // Add the end rect onto the start rect to make a rectangle
      // that encompasses the entire selection
      startRect.add(endPoint);     
      
      this.scrollRectToVisible(startRect);
      removeSetSizeListener();
    }
    
    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
  }   
  
  public void centerViewOnLine(int lineNumber) {
    FontMetrics metrics = getFontMetrics(getFont());
    Point p = new Point(0, metrics.getHeight() * (lineNumber));
    int offset = this.viewToModel(p);
    this.centerViewOnOffset(offset);
  }

  /**
   * Reset the document Undo list.
   */
  public void resetUndo() {
    _undoManager.discardAllEdits();

    _undoAction.updateUndoState();
    _redoAction.updateRedoState();
  }

  /**
   * Reset the document Undo list.
   */
  private void _resetUndo() {
    if (_undoAction == null) {
      _undoAction = new UndoAction();
    }
    if (_redoAction == null) {
      _redoAction = new RedoAction();
    }
    _undoManager = new OurUndoManager();
    _undoManager.setLimit(UNDO_LIMIT);

    getDocument().addUndoableEditListener(_undoListener);
    _undoAction.updateUndoState();
    _redoAction.updateRedoState();
  }


  /**
   * Overriding this method ensures that all new documents created in this
   * editor pane use our editor kit (and thus our model).
   */
  protected EditorKit createDefaultEditorKit() {
    return EDITOR_KIT;
  }


  /**
   * Perform an indent either on the current line or on the given
   * selected box of text.  Calls are sent to GlobalModel which are then
   * forwarded on to DefinitionsDocument.  Hopefully the indent code
   * will be fixed and corrected so this doesn't look so ugly.
   * The purpose is to divorce the pane from the document so we can just
   * pass a document to DefinitionsPane and that's all it cares about.
   */
  public void indent() {
    // because indent() is a function called directly by the Keymap
    // it does not go through the regular insertString channels and thus
    // it may not be in sync with the document's position.  For that
    // reason, we must sync the document with the pane before we go
    // ahead and indent.
    // old: _doc().setCurrentLocation(getCaretPosition());
    // new:
    _doc.syncCurrentLocationWithDefinitions(getCaretPosition());
    int selStart = getSelectionStart();
    int selEnd = getSelectionEnd();

    // Show a wait cursor for reasonable sized blocks
    boolean showWaitCursor = selEnd > (selStart + 100);

    // Temporary hack because of slow indent...
    //  Prompt if more than 10000 characters to be indented
    boolean doIndent = true;
    if (selEnd > (selStart + 10000)) {
      Object[] options = {"Yes","No"};
      int n = JOptionPane.showOptionDialog
        (_mainFrame,
         "Re-indenting this block may take a very long time.  Are you sure?",
         "Confirm Re-indent",
         JOptionPane.YES_NO_OPTION,
         JOptionPane.QUESTION_MESSAGE,
         null,
         options,
         options[1]);
      if (n==JOptionPane.NO_OPTION) { doIndent = false; }
    }

    // Do the indent
    if (doIndent) {
      if (showWaitCursor) {
        _mainFrame.hourglassOn();
      }
      //_doc.indentLinesInDefinitions(selStart, selEnd);
      _indentLines();
      setCaretPosition(_doc.getCurrentDefinitionsLocation());
      if (showWaitCursor) {
        _mainFrame.hourglassOff();
      }
    }
  }

  /**
   * The undo action.
   */
  private class UndoAction extends AbstractAction {
    
    /**
     * Constructor.
    */
    private UndoAction() {
      super("Undo");
      setEnabled(false);
    }

    /**
     * What to do when user chooses to undo.
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
      try {
        UndoableEdit edit = _undoManager.getNextUndo();
        int pos = -1;
        if (edit != null && edit instanceof UndoWithPosition) {
          pos = ((UndoWithPosition)edit).getPosition();
        }
        
        if (pos > -1) {
          //centerViewOnOffset(pos);
          setCaretPosition(pos);
        }
        _undoManager.undo();
        _doc.getDocument().setModifiedSinceSave();
        _mainFrame.updateFileTitle();
      } catch (CannotUndoException ex) {
        System.out.println("Unable to undo: " + ex);
        ex.printStackTrace();
      }
      updateUndoState();
      _redoAction.updateRedoState();
    }


    /**
     * Updates the undo list, i.e., where we are as regards undo and redo.
     */
    protected void updateUndoState() {
      if (_undoManager.canUndo()) {
        setEnabled(true);
        putValue(Action.NAME, _undoManager.getUndoPresentationName());
      }
      else {
        setEnabled(false);
        putValue(Action.NAME, "Undo");
      }
    }
  }

  /**
   * Redo action.
   */
  private class RedoAction extends AbstractAction {

    /**
     * Constructor.
     */
    private RedoAction() {
      super("Redo");
      setEnabled(false);
    }

    /**
     * In the event that the user chooses to redo something, this is what's called.
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
      try {
        UndoableEdit edit = _undoManager.getNextRedo();
        int pos = -1;
        if (edit instanceof UndoWithPosition) {
          pos = ((UndoWithPosition)edit).getPosition();
        }
        _undoManager.redo();
        
        if (pos > -1) {
          //centerViewOnOffset(pos);
          setCaretPosition(pos);
        }
        _doc.getDocument().setModifiedSinceSave();
        _mainFrame.updateFileTitle();
      } catch (CannotRedoException ex) {
        System.out.println("Unable to redo: " + ex);
        ex.printStackTrace();
      }
      updateRedoState();
      _undoAction.updateUndoState();
    }

    /**
     * Updates the redo state, i.e., where we are as regards undo and redo.
     */
    protected void updateRedoState() {
      if (_undoManager.canRedo()) {
        setEnabled(true);
        putValue(Action.NAME, _undoManager.getRedoPresentationName());
      }
      else {
        setEnabled(false);
        putValue(Action.NAME, "Redo");
      }
    }
  }
  
  /**
   * Wrapper for UndoableEdit that pairs UndoableEdits with their
   * caret positions
   */
  private class UndoWithPosition implements UndoableEdit {
    private UndoableEdit _undo;
    private int _pos;
    
    public UndoWithPosition(UndoableEdit undo, int pos) {
      _undo = undo;
      _pos = pos;
    }
    
    public int getPosition() {
      return _pos;
    }
    
    public boolean addEdit(UndoableEdit ue) {
      return _undo.addEdit(ue);
    }
    
    public boolean canRedo() {
      return _undo.canRedo();
    }
    
    public boolean canUndo() {
      return _undo.canUndo();
    }
    
    public void die() {
      _undo.die();
    }
    
    public String getPresentationName() {
      return _undo.getPresentationName();
    }
    
    public String getUndoPresentationName() {
      return _undo.getUndoPresentationName();
    }
    
    public String getRedoPresentationName() {
      return _undo.getRedoPresentationName();
    }
    
    public boolean isSignificant() {
      return _undo.isSignificant();
    }
    
    public void redo() {
      _undo.redo();
    }
    
    public boolean replaceEdit(UndoableEdit ue) {
      return _undo.replaceEdit(ue);
    }
    
    public void undo() {
      _undo.undo();
    }
  }
  
  /**
   * Is used to be able to call editToBeUndone and editToBeRedone since they
   * are protected methods in UndoManager
   */
  private class OurUndoManager extends UndoManager {
    public UndoableEdit getNextUndo() {
      return editToBeUndone();
    }
    
    public UndoableEdit getNextRedo() {
      return editToBeRedone();
    }
  }
}
