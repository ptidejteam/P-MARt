/*
 * RaceToken.java
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

import java.util.List;
import pcgen.core.Constants;
import pcgen.core.PCClass;
import pcgen.core.PlayerCharacter;
import pcgen.core.SpecialAbility;
import pcgen.core.SettingsHandler;

//RACE
//RACE.ABILITYLIST
public class RaceToken extends Token {
	public static final String TOKENNAME = "RACE";

	public String getTokenName() {
		return TOKENNAME;
	}

	public String getToken(String tokenSource, PlayerCharacter pc) {
		String retString = "";
		if ("RACE".equals(tokenSource)) {
			retString = getRaceToken(pc);
		}
		else if ("RACE.ABILITYLIST".equals(tokenSource)) {
			retString = getAbilityListToken(pc);
		}
		return retString;
	}

	public static String getRaceToken(PlayerCharacter pc) {
		String retString = "";

		String tempRaceName = pc.getRace().getDisplayName();
		if (tempRaceName.equals(Constants.s_NONE)) {
			tempRaceName = pc.getRace().getOutputName();
		}

		StringBuffer extraRaceInfo = new StringBuffer(40);

		if (!pc.getSubRace().equals(Constants.s_NONE)) {
			extraRaceInfo.append(pc.getSubRace());
		}
		if (SettingsHandler.hideMonsterClasses()) {
			final String monsterClass = pc.getRace().getMonsterClass(false);
			if (monsterClass != null) {
				final PCClass aClass = pc.getClassNamed(monsterClass);
				if (aClass != null) {
					int minHD = pc.getRace().hitDice() + pc.getRace().getMonsterClassLevels();
					int monsterHD = pc.getRace().hitDice() + aClass.getLevel();
					if (monsterHD != minHD) {
						if (extraRaceInfo.length() != 0) {
							extraRaceInfo.append(' ');
						}
						extraRaceInfo.append(monsterHD).append("HD");
					}
				}
			}
		}
		retString = tempRaceName;
		if (extraRaceInfo.length() != 0) {
			retString = " (" + extraRaceInfo.toString() + ')';
		}

		return retString;
	}

	public static String getAbilityListToken(PlayerCharacter pc) {
		String retString = "";
		List saList = pc.getRace().getSpecialAbilityList();
		if (saList == null || saList.isEmpty()) {
			return "";
		}

		boolean firstLine = true;
		for (int i = 0; i < saList.size(); i++) {
			if(!firstLine) {
				retString += ", ";
			}
			firstLine = false;

			retString += ((SpecialAbility) saList.get(i)).getName();
		}
		return retString;
	}
}




