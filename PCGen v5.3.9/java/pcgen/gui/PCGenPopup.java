/*
 * PCGenPopup.java
 * Copyright 2002, 2003 (C) B. K. Oxley (binkley) <binkley@alumni.rice.edu>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 *
 * Created on February 14th, 2002.
 */

package pcgen.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import pcgen.gui.utils.IconUtilitities;
import pcgen.gui.utils.Utility;

/**
 * Generic popup dialog.  The real work goes on in the panel.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @version $Revision: 1.1 $
 */
class PCGenPopup extends JFrame
{
	/**
	 * Forbid the default constructor.
	 */
	private PCGenPopup()
	{
		//Yes, it should be empty.
	}

	/**
	 * Construct a generic PCGen popup dialog.  The real work goes
	 * on in the panel.
	 * The caller is responsible for making the dialog visible once
	 * a content panel has been added.
	 *
	 * @param title String the frame title
	 */
	public PCGenPopup(String title)
	{
		super(title);

		IconUtilitities.maybeSetIcon(this, "PcgenIcon.gif");
		Utility.centerFrame(this, true);
	}

	/**
	 * Construct a generic PCGen popup dialog.  The real work goes
	 * on in the panel.
	 *
	 * @param title String the frame title
	 * @param panel JPanel the frame contents
	 */
	public PCGenPopup(String title, JPanel panel)
	{
		super(title);

		IconUtilitities.maybeSetIcon(this, "PcgenIcon.gif");
		Utility.centerFrame(this, true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setPanel(panel);

		setVisible(true);
	}

	/**
	 * Use the panel as the frame content.
	 *
	 * @param panel JPanel the frame contents
	 */
	public void setPanel(JPanel panel)
	{
		getContentPane().add(panel);
	}
}
