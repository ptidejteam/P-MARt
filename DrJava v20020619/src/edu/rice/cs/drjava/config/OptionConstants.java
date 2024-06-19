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
import edu.rice.cs.drjava.DrJava;
public interface OptionConstants extends ConfigurationTool {
  
  // STATIC VARIABLES  
  
  
  /* ---------- Resource Location and Classpath Options ---------- */
  
  public static final StringOption JAVAC_LOCATION = 
    new StringOption("javac.location","");
  
  public static final StringOption JSR14_LOCATION =
    new StringOption("jsr14.location","");
  
  public static final StringOption JSR14_COLLECTIONSPATH = 
    new StringOption("jsr14.collectionspath","");
  
  public static final VectorOption<String> EXTRA_CLASSPATH = 
    (new Begin<VectorOption<String>>() {
      private String warning =
        "WARNING: Configurability interface only supports path separators"+
        " of maximum length 1 character as of this moment.";
      public VectorOption<String> evaluate() {
        // system path separator
        String ps = System.getProperty("path.separator");
        if(ps.length() > 1) { 
          // spit out warning if it's more than one character.
          System.err.println(warning);
          System.err.println("using '"+ps.charAt(0)+
                             "' for delimiter.");
        }
        StringOption sop = new StringOption("","");
        String name = "extra.classpath";
        char delim = ps.charAt(0);
        return new VectorOption<String>(name,sop,"",delim,"",new Vector<String>());
      }
    }).evaluate();
  
  
  /* ---------- Color Options ---------- */
  
  public static final ColorOption DEFINITIONS_COMMENT_COLOR = 
    new ColorOption("definitions.comment.color", Color.green.darker().darker());
  public static final ColorOption DEFINITIONS_DOUBLE_QUOTED_COLOR = 
    new ColorOption("definitions.double.quoted.color", Color.red.darker());
  public static final ColorOption DEFINITIONS_SINGLE_QUOTED_COLOR = 
    new ColorOption("definitions.single.quoted.color", Color.magenta);
  public static final ColorOption DEFINITIONS_NORMAL_COLOR = 
    new ColorOption("definitions.normal.color", Color.black);
  public static final ColorOption DEFINITIONS_KEYWORD_COLOR = 
    new ColorOption("definitions.keyword.color", Color.blue);
  public static final ColorOption DEFINITIONS_NUMBER_COLOR = 
    new ColorOption("definitions.number.color", Color.black);
  public static final ColorOption DEFINITIONS_TYPE_COLOR = 
    new ColorOption("definitions.type.color", Color.blue.darker().darker());
  
  
  /**
   * Color for highlighting brace-matching.
   */
  public static final ColorOption DEFINITIONS_MATCH_COLOR = 
    new ColorOption("definitions.match.color", new Color(190, 255, 230));
  
  
  /* ---------- Font Options ---------- */
  
  /* Main (definitions document, tab contents) */
  public static final StringOption FONT_MAIN_NAME = 
    new StringOption("font.main.name", "Monospaced");
  public static final IntegerOption FONT_MAIN_STYLE = 
    new IntegerOption("font.main.style", new Integer(0));
  public static final IntegerOption FONT_MAIN_SIZE = 
    new IntegerOption("font.main.size", new Integer(12));

  /* List of open documents */
  public static final StringOption FONT_DOCLIST_NAME = 
    new StringOption("font.doclist.name", "Monospaced");
  public static final IntegerOption FONT_DOCLIST_STYLE = 
    new IntegerOption("font.doclist.style", new Integer(0));
  public static final IntegerOption FONT_DOCLIST_SIZE = 
    new IntegerOption("font.doclist.size", new Integer(10));
  
  /* Toolbar buttons */
  public static final StringOption FONT_TOOLBAR_NAME = 
    new StringOption("font.toolbar.name", "dialog");
  public static final IntegerOption FONT_TOOLBAR_STYLE = 
    new IntegerOption("font.toolbar.style", new Integer(0));
  public static final IntegerOption FONT_TOOLBAR_SIZE = 
    new IntegerOption("font.toolbar.size", new Integer(10));
  
  
  /* ---------- Other Display Options ---------- */
  
  /**
   * Whether icons should be displayed on the toolbar buttons
   */    
  public static final BooleanOption TOOLBAR_ICONS_ENABLED =
    new BooleanOption("toolbar.icons.enabled", new Boolean(true));
  
  /**
   * Whether text should be displayed on the toolbar buttons.
   * Note: this is only relevant if toolbar icons are enabled
   */    
  public static final BooleanOption TOOLBAR_TEXT_ENABLED =
    new BooleanOption("toolbar.text.enabled", new Boolean(true));
  
  
  /* ---------- Misc Options ---------- */
  
  /**
   * Directory to start looking for files in when DrJava starts up.
   */
  public static final StringOption WORKING_DIRECTORY = 
    new StringOption("working.directory", System.getProperty("user.dir"));
  
  /**
   * How many spaces to use for indenting.
   */
  public static final IntegerOption INDENT_LEVEL =
    new IntegerOption("indent.level",new Integer(2));
  
  /**
   * Number of lines to remember in the Interactions History
   */
  public static final IntegerOption HISTORY_MAX_SIZE =
    new IntegerOption("history.max.size", new Integer(500));
  
  /**
   * Whether the integrated debugger should be displayed as available.
   */
  public static final BooleanOption DEBUGGER_ENABLED =
    new BooleanOption("debugger.enabled", new Boolean(false));

  /**
   * Whether the integrated debugger should display the advanced mode JSwat console
   */    
  public static final BooleanOption DEBUGGER_ADVANCED =
    new BooleanOption("debugger.advanced", new Boolean(false));
  

}



