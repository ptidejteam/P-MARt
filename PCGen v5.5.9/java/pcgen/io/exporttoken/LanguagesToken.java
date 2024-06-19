/*
 * LanguagesToken.java
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

import java.util.SortedSet;
import java.util.StringTokenizer;
import pcgen.core.PlayerCharacter;

//LANGUAGES.x
public class LanguagesToken extends Token {
	public static final String TOKENNAME = "LANGUAGES";

	public String getTokenName() {
		return TOKENNAME;
	}

	public String getToken(String tokenSource, PlayerCharacter pc) {
		StringTokenizer aTok = new StringTokenizer(tokenSource, ".");
		aTok.nextToken();
		int languageIndex = 0;
		int startIndex = 0;
		if(aTok.hasMoreTokens()) {
			try {
				languageIndex = Integer.parseInt(aTok.nextToken());
				startIndex = languageIndex;
			}
			catch(Exception e) {}
		}
		else
		{
			languageIndex = pc.getLanguagesList().size();
		}
		StringBuffer langBuf = new StringBuffer();
		for (int i = startIndex; i <= languageIndex; i++)
		{
			if (i>startIndex && i < languageIndex)
				langBuf.append(", ");
			langBuf.append(getLanguagesToken(pc, i));
		}
		return langBuf.toString();
	}

	public static String getLanguagesToken(PlayerCharacter pc, int languageIndex) {
		SortedSet languageSet = pc.getLanguagesList();
		if (languageIndex >= 0 && languageIndex < languageSet.size()) {
			return languageSet.toArray()[languageIndex].toString();
		}
		return "";
	}
}

