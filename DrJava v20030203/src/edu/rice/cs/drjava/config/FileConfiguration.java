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
import java.io.*;

/**
 * A Configuration object that is backed by a file.
 * @version $Id: FileConfiguration.java,v 1.1 2005/04/27 20:30:32 elmoutam Exp $
 */
public class FileConfiguration extends SavableConfiguration {  
  
  public final File file;
  
  /**
   * Creates a new Configuration object using the values stored in file f.
   * Any values not specified by f will be set to defaults from OptionConstants.
   * @param f Properties file containing customized values
   */
  public FileConfiguration(File f) {
    super(new DefaultOptionMap());
    file = f.getAbsoluteFile();
  }
  
  /**
   * Calls SavableConfiguration.loadConfiguration, which loads all values
   * from the file, based on the defaults in OptionConstants.
   */
  public void loadConfiguration() throws IOException {
    loadConfiguration(new BufferedInputStream(new FileInputStream(file)));
  }
  
  /**
   * Saves the current settings to the stored properties file.
   */
  public void saveConfiguration() throws IOException {
    saveConfiguration("DrJava configuration file");
  }
  
  /**
   * Saves the current settings to the stored properties file.
   * @param header Description of the properties list
   */
  public void saveConfiguration(String header) throws IOException {
    OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
    super.saveConfiguration(os,header);
    os.close(); // in this implementation, close the file after saving.
  }
}
