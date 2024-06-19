/*
 * PaperInfoToken.java
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

import pcgen.core.Constants;
import pcgen.core.Globals;
import pcgen.core.PlayerCharacter;

//PAPERINFO
public class PaperInfoToken extends Token {
	public static final String TOKENNAME = "PAPERINFO";

	public String getTokenName() {
		return TOKENNAME;
	}

	public String getToken(String tokenSource, PlayerCharacter pc) {
		return getPaperInfoToken(tokenSource);
	}

	public static String getPaperInfoToken(String tokenSource) {
		String oString = tokenSource;
		tokenSource = tokenSource.substring(10);
		int infoType = -1;
		if (tokenSource.startsWith("NAME")) {
			infoType = Constants.PAPERINFO_NAME;
		}
		else if (tokenSource.startsWith("HEIGHT")) {
			infoType = Constants.PAPERINFO_HEIGHT;
		}
		else if (tokenSource.startsWith("WIDTH")) {
			infoType = Constants.PAPERINFO_WIDTH;
		}
		else if (tokenSource.startsWith("MARGIN")) {
			tokenSource = tokenSource.substring(6);
			if (tokenSource.startsWith("TOP")) {
				infoType = Constants.PAPERINFO_TOPMARGIN;
			}
			else if (tokenSource.startsWith("BOTTOM")) {
				infoType = Constants.PAPERINFO_BOTTOMMARGIN;
			}
			else if (tokenSource.startsWith("LEFT")) {
				infoType = Constants.PAPERINFO_LEFTMARGIN;
			}
			else if (tokenSource.startsWith("RIGHT")) {
				infoType = Constants.PAPERINFO_RIGHTMARGIN;
			}
		}
		if (infoType >= 0) {
			int offs = tokenSource.indexOf('=');
			String info = Globals.getPaperInfo(infoType);
			if (info == null) {
				if (offs >= 0) {
					oString = tokenSource.substring(offs + 1);
				}
			}
			else {
				oString = info;
			}
		}
		return oString;
	}
}

