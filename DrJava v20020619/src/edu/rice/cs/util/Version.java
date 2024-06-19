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

package edu.rice.cs.util;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * This interface hold the information about this build of util.
 * This file is copied to Version.java by the build process, which also
 * fills in the right values of the date and time.
 *
 * This javadoc corresponds to build util-20020612-1824;
 *
 * @version $Id: Version.java,v 1.1 2005/08/05 12:45:58 guehene Exp $
 */
public abstract class Version {
  /**
   * This string will be automatically expanded upon "ant commit".
   * Do not edit it by hand!
   */
  private static final String BUILD_TIME_STRING = "20020612-1824";

  /** A {@link Date} version of the build time. */
  private static final Date BUILD_TIME = _getBuildDate();

  public static String getBuildTimeString() {
    return BUILD_TIME_STRING;
  }

  public static Date getBuildTime() {
    return BUILD_TIME;
  }

  private static Date _getBuildDate() {
    try {
      return new SimpleDateFormat("yyyyMMdd-HHmm z").parse(BUILD_TIME_STRING + " GMT");
    }
    catch (Exception e) { // parse format or whatever problem
      return null;
    }
  }

  public static void main(String[] args) {
    System.out.println("Version for edu.rice.cs.util: " + BUILD_TIME_STRING);
  }
} 
