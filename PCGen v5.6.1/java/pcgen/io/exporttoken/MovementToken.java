/*
 * MovementToken.java
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

import java.util.StringTokenizer;
import pcgen.core.Globals;
import pcgen.core.PlayerCharacter;

//MOVEMENT
//MOVEMENT.movetype
public class MovementToken extends Token {
	public static final String TOKENNAME = "MOVEMENT";

	public String getTokenName()
	{
		return TOKENNAME;
	}

	//TODO: Move the |MOVEMENT| results into MoveToken, and then Eliminate MovementToken
	//      Also add .moveType to movement as a switchout replacement for .x
	public String getToken(String tokenSource, PlayerCharacter pc)
	{
		String retString = "";
		StringTokenizer aTok = new StringTokenizer(tokenSource, ".");
		aTok.nextToken();
		if(aTok.hasMoreTokens())
		{
			String moveType = aTok.nextToken();
			retString = getMoveTypeToken(pc, moveType);
		}
		else
		{
			retString = getMovementToken(pc);
		}
		return retString;
	}

	public static String getMovementToken(PlayerCharacter pc)
	{
		String retString = "";
		boolean firstLine = true;
		for (int i = 0; i < pc.getNumberOfMovements(); i++)
		{
			if(!firstLine)
			{
				retString += ", ";
			}
			firstLine = false;

			retString += pc.getMovementType(i) + " ";
			retString += getRateToken(pc, i);
		}
		return retString;
	}

	public static String getMoveTypeToken(PlayerCharacter pc, String moveType)
	{
		String retString = "";
		for (int i = 0; i < pc.getNumberOfMovements(); i++)
		{
			if (pc.getMovementType(i).toUpperCase().equals(moveType.toUpperCase()))
			{
				retString = getRateToken(pc, i);
			}
		}
		return retString;
	}

	public static String getRateToken(PlayerCharacter pc, int moveNumber)
	{
		return Globals.displayDistanceInUnitSet(pc.movement(moveNumber)) + Globals.getDistanceUnit();
	}
}

