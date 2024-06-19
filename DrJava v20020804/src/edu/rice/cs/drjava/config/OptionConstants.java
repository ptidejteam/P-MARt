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

package edu.rice.cs.drjava.config;
import java.io.File;
import gj.util.Vector;
import java.awt.Color;
import java.awt.Font;
import edu.rice.cs.drjava.DrJava;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.Toolkit;
import edu.rice.cs.drjava.CodeStatus;

public interface OptionConstants extends ConfigurationTool {
  
  // STATIC VARIABLES  
  
  /* ---------- Resource Location and Classpath Options ---------- */
  
  public static final FileOption JAVAC_LOCATION = 
    new FileOption("javac.location", FileOption.NULL_FILE);
  
  public static final FileOption JSR14_LOCATION =
    new FileOption("jsr14.location", FileOption.NULL_FILE);
  
  public static final FileOption JSR14_COLLECTIONSPATH = 
    new FileOption("jsr14.collectionspath", FileOption.NULL_FILE);
  
  public static final VectorOption<File> EXTRA_CLASSPATH = 
    new ClasspathOption().evaluate("extra.classpath");
  
  
  /* ---------- Color Options ---------- */
  
  public static final ColorOption DEFINITIONS_NORMAL_COLOR = 
    new ColorOption("definitions.normal.color", Color.black);
  public static final ColorOption DEFINITIONS_KEYWORD_COLOR = 
    new ColorOption("definitions.keyword.color", Color.blue);
  public static final ColorOption DEFINITIONS_TYPE_COLOR = 
    new ColorOption("definitions.type.color", Color.blue.darker().darker());
  public static final ColorOption DEFINITIONS_COMMENT_COLOR = 
    new ColorOption("definitions.comment.color", Color.green.darker().darker());
  public static final ColorOption DEFINITIONS_DOUBLE_QUOTED_COLOR = 
    new ColorOption("definitions.double.quoted.color", Color.red.darker());
  public static final ColorOption DEFINITIONS_SINGLE_QUOTED_COLOR = 
    new ColorOption("definitions.single.quoted.color", Color.magenta);
  public static final ColorOption DEFINITIONS_NUMBER_COLOR = 
    new ColorOption("definitions.number.color", Color.cyan.darker().darker());
  
  /**
   * Color for background of definitions pane.
   */
  public static final ColorOption DEFINITIONS_BACKGROUND_COLOR = 
    new ColorOption("definitions.background.color", Color.white);
  
  /**
   * Color for highlighting brace-matching.
   */
  public static final ColorOption DEFINITIONS_MATCH_COLOR = 
    new ColorOption("definitions.match.color", new Color(190, 255, 230));
  
  /**
   * Color for highlighting errors and test failures.
   */
  public static final ColorOption COMPILER_ERROR_COLOR = 
    new ColorOption("compiler.error.color", Color.yellow);
  
  /**
   * Color for highlighting breakpoints.
   */
  public static final ColorOption DEBUG_BREAKPOINT_COLOR = 
    new ColorOption("debug.breakpoint.color", Color.red);
  
  /**
   * Color for highlighting thread locations.
   */
  public static final ColorOption DEBUG_THREAD_COLOR = 
    new ColorOption("debug.thread.color", new Color(100,255,255));
  
  
  /* ---------- Font Options ---------- */
  
  /** Main (definitions document, tab contents) */
  
  public static final FontOption FONT_MAIN = 
    new FontOption("font.main", Font.decode("Monospaced-PLAIN-12"));
        
  /** List of open documents */
  public static final FontOption FONT_DOCLIST = 
    new FontOption("font.doclist", Font.decode("Monospaced-PLAIN-10"));
        
 /** Toolbar buttons */
  public static final FontOption FONT_TOOLBAR = 
    new FontOption("font.toolbar", Font.decode("dialog-PLAIN-10"));
       
  
  /* ---------- Other Display Options ---------- */
  
  /**
   * Whether icons should be displayed on the toolbar buttons.
   */    
  public static final BooleanOption TOOLBAR_ICONS_ENABLED =
    new BooleanOption("toolbar.icons.enabled", new Boolean(true));
  
  /**
   * Whether text should be displayed on the toolbar buttons.
   * Note: this is only relevant if toolbar icons are enabled
   */    
  public static final BooleanOption TOOLBAR_TEXT_ENABLED =
    new BooleanOption("toolbar.text.enabled", new Boolean(true));
  
  /**
   * Whether the line-numbers should be displayed in a row header.
   */
  public static final BooleanOption LINEENUM_ENABLED = 
    new BooleanOption("lineenum.enabled", new Boolean(false));
  
  /* ---------- Key Binding Options ----------- */
  static int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
  /**
   * The key binding for creating a new file
   */
  public static final KeyStrokeOption KEY_NEW_FILE =
    new KeyStrokeOption("key.new.file", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_N, mask));
  /**
   * The key binding for opening a file
   */
  public static final KeyStrokeOption KEY_OPEN_FILE =
    new KeyStrokeOption("key.open.file", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_O, mask));
  /**
   * The key binding for saving a file
   */
  public static final KeyStrokeOption KEY_SAVE_FILE =
    new KeyStrokeOption("key.save.file", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_S, mask));
  /**
   * The key binding for saving a file as
   */
  public static final KeyStrokeOption KEY_SAVE_FILE_AS =
    new KeyStrokeOption("key.save.file.as", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_S, mask |
                                               InputEvent.SHIFT_MASK));
  /**
   * The key binding for closing a file
   */
  public static final KeyStrokeOption KEY_CLOSE_FILE =
    new KeyStrokeOption("key.close.file", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_W, mask));
  /**
   * The key binding for showing the print preview
   */
  public static final KeyStrokeOption KEY_PRINT_PREVIEW =
    new KeyStrokeOption("key.print.preview", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_P, mask |
                                               InputEvent.SHIFT_MASK));
  /**
   * The key binding for printing a file
   */
  public static final KeyStrokeOption KEY_PRINT =
    new KeyStrokeOption("key.print", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_P, mask));
  /**
   * The key binding for quitting
   */
  public static final KeyStrokeOption KEY_QUIT =
    new KeyStrokeOption("key.quit", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_Q, mask));
  /**
   * The key binding for undo-ing
   */
  public static final KeyStrokeOption KEY_UNDO =
    new KeyStrokeOption("key.undo", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_Z, mask));
  /**
   * The key binding for redo-ing
   */
  public static final KeyStrokeOption KEY_REDO =
    new KeyStrokeOption("key.redo", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_R, mask));
  /**
   * The key binding for cutting
   */
  public static final KeyStrokeOption KEY_CUT =
    new KeyStrokeOption("key.cut", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_X, mask));
  /**
   * The key binding for copying
   */
  public static final KeyStrokeOption KEY_COPY =
    new KeyStrokeOption("key.copy", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_C, mask));
  /**
   * The key binding for pasting
   */
  public static final KeyStrokeOption KEY_PASTE =
    new KeyStrokeOption("key.paste", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_V, mask));
  /**
   * The key binding for selecting all text
   */
  public static final KeyStrokeOption KEY_SELECT_ALL =
    new KeyStrokeOption("key.select.all", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_A, mask));
  /**
   * The key binding for find and replace
   */
  public static final KeyStrokeOption KEY_FIND_NEXT =
    new KeyStrokeOption("key.find.next", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
  /**
   * The key binding for find and replace
   */
  public static final KeyStrokeOption KEY_FIND_REPLACE =
    new KeyStrokeOption("key.find.replace", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_F, mask));
  /**
   * The key binding for goto line
   */
  public static final KeyStrokeOption KEY_GOTO_LINE =
    new KeyStrokeOption("key.goto.line", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_G, mask));
  /**
   * The key binding for selecting previous document
   */
  public static final KeyStrokeOption KEY_PREVIOUS_DOCUMENT =
    new KeyStrokeOption("key.previous.document", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, mask));
  /**
   * The key binding for selecting next document
   */
  public static final KeyStrokeOption KEY_NEXT_DOCUMENT =
    new KeyStrokeOption("key.next.document", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, mask));
  /**
   * The key binding for compiling current document
   */
  public static final KeyStrokeOption KEY_COMPILE =
    new KeyStrokeOption("key.compile", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_F5, 
                                               InputEvent.SHIFT_MASK));
  
  /**
   * The key binding for compiling all
   */
  public static final KeyStrokeOption KEY_COMPILE_ALL =
    new KeyStrokeOption("key.compile.all", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
  
  /**
   * The key binding for aborting the current interaction.
   * (replaced by Reset Interactions, with no shortcut.)
   *
  public static final KeyStrokeOption KEY_ABORT_INTERACTION =
    new KeyStrokeOption("key.abort.interaction", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
                        */
  /**
   * The key binding for moving the cursor backwards
   */
  public static final KeyStrokeOption KEY_BACKWARD =
    new KeyStrokeOption("key.backward", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
  /**
   * The key binding for moving the cursor to the beginning of the document
   */
  public static final KeyStrokeOption KEY_BEGIN_DOCUMENT =
    new KeyStrokeOption("key.begin.document", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_HOME, mask));
  /**
   * The key binding for moving the cursor to the beginning of the current line
   */
  public static final KeyStrokeOption KEY_BEGIN_LINE =
    new KeyStrokeOption("key.begin.line", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0));
  /**
   * The key binding for moving the cursor to the beginning of the current paragraph.
   * (Doesn't seem to do anything useful...)
   *
  public static final KeyStrokeOption KEY_BEGIN_PARAGRAPH =
    new KeyStrokeOption("key.begin.paragraph", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_UP, mask));
   */
  
  /**
   * The key binding for moving the cursor to the beginning of the previous word
   */
  public static final KeyStrokeOption KEY_PREVIOUS_WORD =
    new KeyStrokeOption("key.previous.word", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, mask));
  /**
   * The key binding for deleting the next character
   */
  public static final KeyStrokeOption KEY_DELETE_NEXT =
    new KeyStrokeOption("key.delete.next", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
  /**
   * The key binding for deleting the previous character
   */
  public static final KeyStrokeOption KEY_DELETE_PREVIOUS =
    new KeyStrokeOption("key.delete.previous", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
  /**
   * The key binding for moving the cursor down
   */
  public static final KeyStrokeOption KEY_DOWN =
    new KeyStrokeOption("key.down", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
  /**
   * The key binding for moving the cursor up
   */
  public static final KeyStrokeOption KEY_UP =
    new KeyStrokeOption("key.up", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
  /**
   * The key binding for moving the cursor to the end of the document
   */
  public static final KeyStrokeOption KEY_END_DOCUMENT =
    new KeyStrokeOption("key.end.document", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_END, mask));
  /**
   * The key binding for moving the cursor to the end of the current line
   */
  public static final KeyStrokeOption KEY_END_LINE =
    new KeyStrokeOption("key.end.line", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_END, 0));
  /**
   * The key binding for moving the cursor to the end of the current paragraph.
   * (Doesn't seem to do anything useful...)
   *
  public static final KeyStrokeOption KEY_END_PARAGRAPH =
    new KeyStrokeOption("key.end.paragraph", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, mask));
   */
  
  /**
   * The key binding for moving the cursor to the beginning of the next word
   */
  public static final KeyStrokeOption KEY_NEXT_WORD =
    new KeyStrokeOption("key.next.word", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, mask));
  /**
   * The key binding for moving the cursor forwards
   */
  public static final KeyStrokeOption KEY_FORWARD =
    new KeyStrokeOption("key.forward", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
  /**
   * The key binding for page down
   */
  public static final KeyStrokeOption KEY_PAGE_DOWN =
    new KeyStrokeOption("key.page.down", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
  /**
   * The key binding for page up
   */
  public static final KeyStrokeOption KEY_PAGE_UP =
    new KeyStrokeOption("key.page.up", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
  /**
   * The key binding for cutting a line
   */
  public static final KeyStrokeOption KEY_CUT_LINE =
    new KeyStrokeOption("key.cut.line", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_K, mask));
  
  /**
   * The key binding for toggling debug mode
   */
  public static final KeyStrokeOption KEY_DEBUG_MODE_TOGGLE =
    new KeyStrokeOption("key.debug.mode.toggle", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_D, mask));
  
  /**
   * The key binding for suspending the debugger
   *
  public static final KeyStrokeOption KEY_DEBUG_SUSPEND =
    new KeyStrokeOption("key.debug.suspend", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
                        */
  
  /**
   * The key binding for resuming the debugger
   */
  public static final KeyStrokeOption KEY_DEBUG_RESUME =
    new KeyStrokeOption("key.debug.resume", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
  /**
   * The key binding for stepping into in the debugger
   */
  public static final KeyStrokeOption KEY_DEBUG_STEP_INTO =
    new KeyStrokeOption("key.debug.step.into", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
  /**
   * The key binding for stepping over in the debugger
   */
  public static final KeyStrokeOption KEY_DEBUG_STEP_OVER =
    new KeyStrokeOption("key.debug.step.over", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
  /**
   * The key binding for stepping out in the debugger
   */
  public static final KeyStrokeOption KEY_DEBUG_STEP_OUT =
    new KeyStrokeOption("key.debug.step.out", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_F12, 
                                               InputEvent.SHIFT_MASK));
  /**
   * The key binding for toggling a breakpoint
   */
  public static final KeyStrokeOption KEY_DEBUG_BREAKPOINT_TOGGLE =
    new KeyStrokeOption("key.debug.breakpoint.toggle", 
                        KeyStroke.getKeyStroke(KeyEvent.VK_B, mask));
    
    
  /* ---------- Debugger Options ---------- */
    
  /**
   * A classpath-structured vector of all paths to look for source files on
   * while stepping in the debugger.
   */
  public static final VectorOption<File> DEBUG_SOURCEPATH = 
    new ClasspathOption().evaluate("debug.sourcepath");
  
  /**
   * Whether all current threads should be displayed when a thread suspends
   */    
  public static final BooleanOption DEBUG_SHOW_THREADS =
    new BooleanOption("debug.show.threads", new Boolean(false));
  
  /**
   * Whether stepping should step through Java's source files
   */    
  public static final BooleanOption DEBUG_STEP_JAVA =
    new BooleanOption("debug.step.java", new Boolean(false));
  
  /**
   * Whether stepping should step through Dynamic Java's source files
   */    
  public static final BooleanOption DEBUG_STEP_INTERPRETER =
    new BooleanOption("debug.step.interpreter", new Boolean(false));
  
  /**
   * Whether stepping should step through DrJava's source files
   */    
  public static final BooleanOption DEBUG_STEP_DRJAVA =
    new BooleanOption("debug.step.drjava", new Boolean(false));
    
  
  /* ---------- Misc Options ---------- */
  
  /**
   * Directory to start looking for files in when DrJava starts up.
   */
  public static final FileOption WORKING_DIRECTORY = 
    new FileOption("working.directory", FileOption.NULL_FILE);
  
  /**
   * How many spaces to use for indenting.
   */
  public static final NonNegativeIntegerOption INDENT_LEVEL =
    new NonNegativeIntegerOption("indent.level",new Integer(2));
  
  /**
   * Number of lines to remember in the Interactions History
   */
  public static final NonNegativeIntegerOption HISTORY_MAX_SIZE =
    new NonNegativeIntegerOption("history.max.size", new Integer(500));
  
  /**
   * Number of files to list in the recent file list
   */
  public static final NonNegativeIntegerOption RECENT_FILES_MAX_SIZE =
    new NonNegativeIntegerOption("recent.files.max.size", new Integer(5));
  
  /**
   * A vector containing the most recently used files
   */
  public static final VectorOption<File> RECENT_FILES = 
    new VectorOption("recent.files",new FileOption("",null),new Vector<File>());
}


/**
 * Generate vector options separately to appease javadoc.
 * (It didn't like anonymous inner classes with generics in interfaces in Java 1.3.)
 */
class ClasspathOption {
  private String warning =
    "WARNING: Configurability interface only supports path separators"+
    " of maximum length 1 character as of this moment.";
  
  public VectorOption<File> evaluate(String optionName) {
    // system path separator
    String ps = System.getProperty("path.separator");
    if(ps.length() > 1) { 
      // spit out warning if it's more than one character.
      System.err.println(warning);
      System.err.println("using '"+ps.charAt(0)+
                         "' for delimiter.");
    }
    FileOption fop = new FileOption("",FileOption.NULL_FILE);
    //String name = "extra.classpath";
    char delim = ps.charAt(0);
    return new VectorOption<File>(optionName,fop,"",delim,"",new Vector<File>());
  }
}


