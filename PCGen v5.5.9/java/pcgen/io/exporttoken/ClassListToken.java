/*
 * ClassListToken.java
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
 * Last Edited: $Date: 2006/02/21 01:28:06 $
 *
 */

package pcgen.io.exporttoken;

import pcgen.core.Constants;
import pcgen.core.PCClass;
import pcgen.core.PlayerCharacter;

//CLASSLIST
public class ClassListToken extends Token  {
	public static final String TOKENNAME = "CLASSLIST";

	public String getTokenName() {
		return TOKENNAME;
	}

	public String getToken(String tokenSource, PlayerCharacter pc) {
		return getClassListToken(pc);
	}

	public static String getClassListToken(PlayerCharacter pc) {
		String retString = "";
		boolean firstLine = true;
		for (int i = 0; i < pc.getClassList().size(); i++) {
			PCClass pcClass = (PCClass)  pc.getClassList().get(i);
			if(!firstLine) {
				retString += " ";
			}
			firstLine = false;

			if (Constants.s_NONE.equals(pcClass.getSubClassName()) || "".equals(pcClass.getSubClassName())) {
				retString += pcClass.getOutputName();
			}
			else {
				retString += pcClass.getSubClassName();
			}
			retString += pcClass.getLevel();
		}
		return retString;
	}
}

