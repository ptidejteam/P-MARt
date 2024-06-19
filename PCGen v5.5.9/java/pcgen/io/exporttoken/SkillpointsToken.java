/*
 * SkillpointsToken.java
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

import java.util.Iterator;
import java.util.StringTokenizer;
import pcgen.core.PlayerCharacter;
import pcgen.core.PCClass;
import pcgen.core.Skill;

//SKILLPOINTS
//SKILLPOINTS.TOTAL
//SKILLPOINTS.UNUSED
//SKILLPOINTS.USED
public class SkillpointsToken extends Token {
	public static final String TOKENNAME = "SKILLPOINTS";

	public String getTokenName() {
		return TOKENNAME;
	}

	public String getToken(String tokenSource, PlayerCharacter pc) {
		final StringTokenizer aTok = new StringTokenizer(tokenSource, ".");
		String bString;

		bString = aTok.nextToken();
		if (aTok.hasMoreTokens()) {
			bString = aTok.nextToken();
		}
		if (bString.startsWith("SKILLPOINTS")) {
			bString = "TOTAL";
		}

		float aTotalSkillPoints = 0;

		if ("TOTAL".equals(bString) || "UNUSED".equals(bString)) {
			aTotalSkillPoints += getUnusedSkillPoints(pc);
		}

		if ("TOTAL".equals(bString) || "USED".equals(bString)) {
			aTotalSkillPoints += getUsedSkillPoints(pc);
		}
		return aTotalSkillPoints + "";
	}

	public static int getTotalSkillPoints(PlayerCharacter pc) {
		return getUnusedSkillPoints(pc) + getUsedSkillPoints(pc);
	}

	public static int getUnusedSkillPoints(PlayerCharacter pc) {
		float usedPoints = 0;
		for (Iterator it = pc.getClassList().iterator(); it.hasNext();) {
			PCClass pcClass = (PCClass) it.next();

			if (pcClass.getSkillPool().intValue() > 0) {
				usedPoints += pcClass.getSkillPool().intValue();
			}
		}
		return (int) usedPoints;
	}

	public static int getUsedSkillPoints(PlayerCharacter pc) {
		float usedPoints = 0;
		Skill aSkill;
		for (Iterator it = pc.getSkillList().iterator(); it.hasNext();) {
			aSkill = (Skill) it.next();

			if ((aSkill.getRank().doubleValue() > 0) || (aSkill.getOutputIndex() != 0)) {
				float ranks;
				String className;
				String classRanks;
				for (Iterator it2 = aSkill.getRankList().iterator(); it2.hasNext();) {
					classRanks = (String) it2.next();

					int index = classRanks.indexOf(':');
					className = classRanks.substring(0, index);
					ranks = Float.valueOf(classRanks.substring(index + 1)).floatValue();

					PCClass pcClass = pc.getClassKeyed(className);

					usedPoints += ranks * aSkill.costForPCClass(pcClass).intValue();
				}
			}
		}
		return (int) usedPoints;
	}
}

