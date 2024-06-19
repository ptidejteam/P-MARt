/*
 * ProhibitedListToken.java
 * Copyright 2003 (C) Devon Jones <soulcatcher@evilsoft.org>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Created on December 15, 2003, 12:21 PM
 *
 * Current Ver: $Revision: 1.1 $
 * Last Editor: $Author: vauchers $
 * Last Edited: $Date: 2006/02/21 01:33:05 $
 *
 */

package pcgen.io.exporttoken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import pcgen.core.Constants;
import pcgen.core.PlayerCharacter;
import pcgen.core.PCClass;

//PROHIBITEDLIST
public class ProhibitedListToken extends Token {
	public static final String TOKENNAME = "PROHIBITEDLIST";

	public String getTokenName() {
		return TOKENNAME;
	}

	public String getToken(String tokenSource, PlayerCharacter pc) {
		return getProhibitedListToken(tokenSource, pc);
	}

	public static String getProhibitedListToken(String tokenSource, PlayerCharacter pc) {
		String retString = "";
		int i;
		int k = tokenSource.lastIndexOf(',');
		if (k >= 0) {
			tokenSource = tokenSource.substring(k + 1);
		}
		else {
			tokenSource = ", ";
		}

		List stringList = new ArrayList();
		for (Iterator iter = pc.getClassList().iterator(); iter.hasNext();) {
			PCClass pcClass = (PCClass) iter.next();
			if (pcClass.getLevel() > 0) {
				if (!pcClass.getProhibitedString().equals(Constants.s_NONE)) {
					stringList.add(pcClass.getProhibitedString());
				}
			}
		}
		for (i = 0; i < stringList.size(); ++i) {
			retString += stringList.get(i);
			if (i < stringList.size() - 1) {
				retString += tokenSource;
			}
		}
		return retString;
	}
}

