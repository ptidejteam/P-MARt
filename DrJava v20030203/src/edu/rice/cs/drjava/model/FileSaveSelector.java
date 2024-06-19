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

package edu.rice.cs.drjava.model;

import java.io.File;
/**
 * An interface to give GlobalModel a file to save a
 * document to.
 *
 * @version $Id: FileSaveSelector.java,v 1.1 2005/04/27 20:30:32 elmoutam Exp $
 */
public interface FileSaveSelector {

  /**
   * Returns the file to save.
   * @throws OperationCanceledException if the save request is canceled
   */
  public File getFile() throws OperationCanceledException;
  
  /**
   * Informs the user that the chosen file is already open and cannot be saved.
   * The save is not performed.
   */
  public void warnFileOpen();
  
  /**
   * Confirms whether the existing chosen file should be overwritten.
   */
  public boolean verifyOverwrite();
  
  /**
   * Confirms whether a new file should be selected after the existing chosen
   * file is detected to have been deleted or moved.
   * @param oldFile The file that was moved or deleted.
   */
  public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, 
                                          File oldFile);

}
