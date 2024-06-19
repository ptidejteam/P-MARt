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

package edu.rice.cs.drjava.model.definitions;

import javax.swing.text.*;
import java.awt.*;
import javax.swing.event.DocumentEvent;
// TODO: Check synchronization.
import java.util.Vector;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

/**
 * This view class renders text on the screen using the reduced model info.
 * By extending WrappedPlainView, we only have to override the parts we want to.
 * Here we only override drawUnselectedText. We may want to override 
 * drawSelectedText at some point. As of 2002/06/17, we now extend PlainView because
 * WrappedPlainView was causing bugs related to resizing the viewport of the 
 * definitions scroll pane.
 *
 * @version $Id: ColoringView.java,v 1.1 2005/08/05 12:45:13 guehene Exp $
 */
public class ColoringView extends PlainView implements OptionConstants {
  private DefinitionsDocument _doc;

  private static Color COMMENTED_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_COMMENT_COLOR);
  private static Color DOUBLE_QUOTED_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_DOUBLE_QUOTED_COLOR);
  private static Color SINGLE_QUOTED_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_SINGLE_QUOTED_COLOR);
  private static Color NORMAL_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_NORMAL_COLOR);
  private static Color KEYWORD_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_KEYWORD_COLOR);
  private static Color NUMBER_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_NUMBER_COLOR);
  private static Color TYPE_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_TYPE_COLOR);

  /**
   * Constructor.
   * @param   Element elem
   */
  ColoringView(Element elem) {
    super(elem);
    
    // Might be a PlainDocument (when DefPane is first constructed).
    //   See comments for DefinitionsEditorKit.createNewDocument() for details.
    Document doc = getDocument();
    if (doc instanceof DefinitionsDocument) {
      _doc = (DefinitionsDocument)doc;
    }
    
    // Listen for updates to configurable colors
    ColorOptionListener col = new ColorOptionListener();
    
    DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_COMMENT_COLOR, col);
    DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_DOUBLE_QUOTED_COLOR, col);
    DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_SINGLE_QUOTED_COLOR, col);
    DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_NORMAL_COLOR, col);
    DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_KEYWORD_COLOR, col);
    DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_NUMBER_COLOR, col);
    DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_TYPE_COLOR, col); 
 }

  /**
   * Renders the given range in the model as normal unselected
   * text.
   * Note that this is text that's all on one line. The superclass deals
   * with breaking lines and such. So all we have to do here is draw the
   * text on [p0,p1) in the model. We have to start drawing at (x,y), and
   * the function returns the x coordinate when we're done.
   *
   * @param g the graphics context
   * @param x the starting X coordinate
   * @param y the starting Y coordinate
   * @param p0 the beginning position in the model
   * @param p1 the ending position in the model
   * @returns the x coordinate at the end of the range
   * @exception BadLocationException if the range is invalid
   */
  protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) 
    throws BadLocationException 
  {
    /*
     DrJava.consoleErr().println("drawUnselected: " + p0 + "-" + p1 + 
     " doclen=" + _doc.getLength() +" x="+x+" y="+y);
     */
    // If there's nothing to show, don't do anything!
    // For some reason I don't understand we tend to get called sometimes
    // to render a zero-length area.
    if (p0 == p1) {
      return  x;
    }
    Vector<HighlightStatus> stats = _doc.getHighlightStatus(p0, p1);
    if (stats.size() < 1) {
      throw  new RuntimeException("GetHighlightStatus returned nothing!");
    }
    for (int i = 0; i < stats.size(); i++) {
      HighlightStatus stat = stats.get(i);
      setFormattingForState(g, stat.getState());
      // If this highlight status extends past p1, end at p1
      int length = stat.getLength();
      int location = stat.getLocation();
      if (location + length > p1) {
        length = p1 - stat.getLocation();
      }
      Segment text = getLineBuffer();
      /*
       DrJava.consoleErr().println("Highlight: loc=" + location + " len=" +
       length + " state=" + stat.getState() +
       " text=" + text);
       */
      _doc.getText(location, length, text);
      x = Utilities.drawTabbedText(text, x, y, g, this, location);
    }
    //DrJava.consoleErr().println("returning x: " + x);
    return  x;
  }

  /**
   * put your documentation comment here
   * @param g
   * @param x
   * @param y
   * @param p0
   * @param p1
   * @return 
   * @exception BadLocationException
   */
  protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) 
    throws BadLocationException 
  {
    /*
     DrJava.consoleErr().println("drawSelected: " + p0 + "-" + p1 + 
     " doclen=" + _doc.getLength() +" x="+x+" y="+y);
     */
    return  super.drawSelectedText(g, x, y, p0, p1);
  }

  /**
   * Given a particular state, assign it a color.
   * @param g Graphics object
   * @param state a given state
   */
  private void setFormattingForState(Graphics g, int state) {
    switch (state) {
      case HighlightStatus.NORMAL:
        g.setColor(NORMAL_COLOR);
        break;
      case HighlightStatus.COMMENTED:
        g.setColor(COMMENTED_COLOR);
        break;
      case HighlightStatus.SINGLE_QUOTED:
        g.setColor(SINGLE_QUOTED_COLOR);
        break;
      case HighlightStatus.DOUBLE_QUOTED:
        g.setColor(DOUBLE_QUOTED_COLOR);
        break;
      case HighlightStatus.KEYWORD:
        g.setColor(KEYWORD_COLOR);
        break;
      case HighlightStatus.NUMBER:
        g.setColor(NUMBER_COLOR);
        break;
      case HighlightStatus.TYPE:
        g.setColor(TYPE_COLOR);
        break;
      default:
        throw  new RuntimeException("Can't get color for invalid state: " + state);
    }
  }

  /**
   * Called when a change occurs.
   * @param changes document changes
   * @param a a Shape
   * @param f a ViewFactory
   */
  public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
    super.changedUpdate(changes, a, f);
    // Make sure we redraw since something changed in the formatting
    getContainer().repaint();
  }
  
  /**
   * Called when an OptionListener perceives a change in any of the colors
   */ 
  public void updateColors() {
    
    COMMENTED_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_COMMENT_COLOR);
    DOUBLE_QUOTED_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_DOUBLE_QUOTED_COLOR);
    SINGLE_QUOTED_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_SINGLE_QUOTED_COLOR);
    NORMAL_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_NORMAL_COLOR);
    KEYWORD_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_KEYWORD_COLOR);
    NUMBER_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_NUMBER_COLOR);
    TYPE_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_TYPE_COLOR);
    
    //Avoid the ColoringView that does not have a container.
    if ( getContainer() != null) {
      getContainer().repaint();
    }
      
  }
  
  /**
   * The OptionListeners for DEFINITIONS COLORs 
   */
  private class ColorOptionListener implements OptionListener<Color> {
    
    public void optionChanged(OptionEvent<Color> oce) {
      updateColors();
    }
  }
  
}