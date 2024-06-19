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

/**
 * An exception which DrJava throws on an unexpected error.
 * Many times, we have to catch BadLocationExceptions in
 * code that accesses DefinitionDocument, even if we know for a
 * fact that a BadLocationException cannot occur.  In that case,
 * and in other similar cases where we know that an exception should not
 * occur, we throw this on the off chance that something does go wrong.
 * This aids us in debugging the code.
 * @version $Id: UnexpectedException.java,v 1.1 2005/04/27 20:30:33 elmoutam Exp $
 */
public class UnexpectedException extends RuntimeException {

  private Throwable _value;

   /**
   * Constructs an unexpected exception with
   * <code>value.toString()</code> as it's message.
   */
  public UnexpectedException(Throwable value) {
    super(value.toString());
    _value = value;
  }

   /**
   * Constructs an unexpected exception with a custom message string in
   * addition to <code>value.toString()</code>.
   */
  public UnexpectedException(Throwable value, String msg) {
    super(msg + ": " + value.toString());
    _value = value;
  }

  /**
   * Returns the contained exception.
   */
  public Throwable getContainedThrowable() {
    return _value;
  }
}
