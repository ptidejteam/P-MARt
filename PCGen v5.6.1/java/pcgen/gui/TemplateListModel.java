/*
 * MainPrint.java
 * Copyright 2003 (C) Bryan McRoberts <merton_monk@yahoo.com>
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
 * Created on April 21, 2001, 2:15 PM
 */

package pcgen.gui;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractListModel;
import pcgen.core.Globals;
import pcgen.core.SettingsHandler;
import pcgen.gui.utils.GuiFacade;

/**
 * When first created, this class will cache the contents of the "Templates" directory.
 *
 * @author Jonas Karlsson
 * @version $Revision: 1.1 $
 */

class TemplateListModel extends AbstractListModel
{
	private String[] cSheets;
	private CsheetFilter csheetFilter = null;
	private String fileType = null;
	private String[] pSheets;
	private boolean partyMode = false;
	private CsheetFilter psheetFilter = null;
	private int attempts = 0;

	public TemplateListModel(CsheetFilter argCsheetFilter, CsheetFilter argPsheetFilter, boolean argPartyMode, String argFileType)
	{
		super();
		csheetFilter = argCsheetFilter;
		psheetFilter = argPsheetFilter;
		partyMode = argPartyMode;
		fileType = argFileType;

		updateTemplateList();
	}

	public Object getElementAt(int index)
	{
		if (partyMode)
		{
			if (index >= pSheets.length)
			{
				return null;
			}
			else
			{
				return pSheets[index];
			}
		}
		else
		{
			if (index >= cSheets.length)
			{
				return null;
			}
			else
			{
				return cSheets[index];
			}
		}
	}

	public int getSize()
	{
		if (partyMode)
		{
			return pSheets.length;
		}
		else
		{
			return cSheets.length;
		}
	}

	public int indexOf(Object o)
	{
		if (partyMode)
		{
			return Arrays.binarySearch(pSheets, o);
		}
		else
		{
			return Arrays.binarySearch(cSheets, o);
		}
	}

	public void updateTemplateList()
	{
		csheetFilter.setDirFilter(fileType);
		csheetFilter.setIgnoreExtension(".");
		List aList = csheetFilter.getAccepted();

		if (aList.size() == 0 && attempts == 0)
		{
			Object[] options = {"OK", "CANCEL"};
			if (GuiFacade.showOptionDialog(null, "No templates found. Attempt to change to " + Globals.getDefaultPath() + File.separator + "outputsheets ?", "Warning", GuiFacade.DEFAULT_OPTION, GuiFacade.WARNING_MESSAGE, null, options, options[0]) == GuiFacade.YES_OPTION)
			{
				SettingsHandler.setPcgenOutputSheetDir(new File(Globals.getDefaultPath() + File.separator + "outputsheets"));
				attempts = 1;
				aList = csheetFilter.getAccepted();
			}
		}

		cSheets = new String[aList.size()];
		for (int i = 0; i < aList.size(); i++)
		{
			cSheets[i] = aList.get(i).toString();
		}
		psheetFilter.setDirFilter(fileType);
		psheetFilter.setIgnoreExtension(".");
		aList = psheetFilter.getAccepted();
		pSheets = new String[aList.size()];
		for (int i = 0; i < aList.size(); i++)
		{
			pSheets[i] = aList.get(i).toString();
		}

		Arrays.sort(pSheets);
		Arrays.sort(cSheets);

	}
	
	/**
	 * Are we currently in party mode (exporting a full party)?
	 * @return true if in party mode.
	 */
	public boolean isPartyMode()
	{
		return partyMode;
	}

	/**
	 * Set the party mode flag. true means we will be exporting the full party.
	 * @param b
	 */
	public void setPartyMode(boolean b)
	{
		partyMode = b;
	}
}
