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

import java.security.*;

/**
 * A security manager to prevent exiting the VM indiscriminately.
 * This manager disallows System.exit (unless you call {@link #exitVM}).
 * It also disallows setting a security manager, since this would override
 * the exit prevention!
 *
 * If this security manager is enabled and an exit is attempted,
 * either via System.exit or via {@link #exitVM} when exiting is blocked,
 * a {@link ExitingNotAllowedException} will be thrown.
 *
 * @version $Id: PreventExitSecurityManager.java,v 1.1 2005/08/05 12:45:58 guehene Exp $
 */
public class PreventExitSecurityManager extends SecurityManager {
  private static final Permission SET_MANAGER_PERM
    = new RuntimePermission("setSecurityManager");

  private final SecurityManager _parent;

  /** Has an unauthorized exit been attempted? */
  private boolean _exitAttempted = false;

  /** Is it time to exit, for real? */
  private boolean _timeToExit = false;

  /** Are we in exit blocking mode? */
  private boolean _blockExit = false;

  /** Is it time to unset this security manager? */
  private boolean _timeToDeactivate = false;

  /**
   * Creates a PreventExitSecurityManager, delegating all permission checks
   * except for exiting to the given parent manager.
   *
   * @param parent SecurityManager to delegate permission to
   *               This may be null, signifying to allow all.
   */
  private PreventExitSecurityManager(final SecurityManager parent) {
    _parent = parent;
  }

  /**
   * Creates a new exit-preventing security manager, using the previous
   * security manager to delegate to.
   */
  public static PreventExitSecurityManager activate() {
    PreventExitSecurityManager mgr
      = new PreventExitSecurityManager(System.getSecurityManager());
    System.setSecurityManager(mgr);

    return mgr;
  }

  /** Removes this security manager. */
  public void deactivate() {
    _timeToDeactivate = true;
    System.setSecurityManager(_parent);
  }

  /**
   * Exits the VM unless exiting is presently blocked.
   * Blocking exit is used in test cases that want to see if we try to exit.
   */
  public void exitVM(int status) {
    if (! _blockExit) {
      _timeToExit = true;
    }

    System.exit(status);
  }

  /**
   * Sets whether exiting the VM is unconditionally blocked or not.
   * It's useful to block exiting to allow test cases to pretend to exit, just
   * to make sure it would have exited under certain conditions.
   *
   * @param b If true, exiting will never be allowed until set false.
   */
  public void setBlockExit(boolean b) {
    _blockExit = b;
  }

  /**
   * Returns whether a System.exit was attempted since the last time this
   * method was called.
   */
  public boolean exitAttempted() {
    boolean old = _exitAttempted;
    _exitAttempted = false;
    return old;
  }

  /**
   * Disallow setting security manager, but otherwise delegate to parent.
   */
  public void checkPermission(Permission perm) {
    if (perm.equals(SET_MANAGER_PERM)) {
      if (! _timeToDeactivate) {
        throw new SecurityException("Can not reset security manager!");
      }
    }
    else {
      if (_parent != null) {
        _parent.checkPermission(perm);
      }
    }
  }

  public void checkExit(int status) {
    if (! _timeToExit) {
      _exitAttempted = true;
      throw new ExitingNotAllowedException();
    }
  }
}


