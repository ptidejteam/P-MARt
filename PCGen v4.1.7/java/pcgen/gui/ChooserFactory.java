/*
 * Chooser.java
 * Copyright 2002 (C) Jonas Karlsson
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package pcgen.gui;

/**
 * This factory class returns a Chooser of the appropriate type. This is intended
 * to reduce the core/gui interdependence. Much more work is needed on this...
 * Currently only a SwingChooser has been implemented.
 *
 * @author    Jonas Karlsson
 * @version $Revision: 1.1 $
 */

public final class ChooserFactory
{

	/**
	 * Deliberately private so it can't be instantiated.
	 */
	private ChooserFactory()
	{
	}

	/**
	 * The default implementation returns a SwingChooser
	 * @return
	 */
	public static ChooserInterface getChooserInstance()
	{
		return new SwingChooser();
	}
}
