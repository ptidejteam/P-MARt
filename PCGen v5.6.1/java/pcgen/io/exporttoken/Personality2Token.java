/*
 * Personality2Token.java
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

import pcgen.core.PlayerCharacter;

//PERSONALITY2
public class Personality2Token extends Token {
	public static final String TOKENNAME = "PERSONALITY2";

	public String getTokenName() {
		return TOKENNAME;
	}

	//TODO: Move this to a token that has all of teh descriptive stuff about a cahracter
	public String getToken(String tokenSource, PlayerCharacter pc) {
		return getPersonality2Token(pc);
	}

	public static String getPersonality2Token(PlayerCharacter pc) {
		return pc.getTrait2();
	}
}

