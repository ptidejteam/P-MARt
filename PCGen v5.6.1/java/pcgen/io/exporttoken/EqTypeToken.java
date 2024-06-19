/*
 * EqTypeToken.java
 * Copyright 2003 (C) Devon Jones <soulcatcher@evilsoft.org>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.     See the GNU
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
import java.util.StringTokenizer;
import pcgen.core.Constants;
import pcgen.core.Equipment;
import pcgen.core.PlayerCharacter;

//EQTYPE
public class EqTypeToken extends EqToken
{
	public static final String TOKENNAME = "EQTYPE";

	public String getTokenName()
	{
		return TOKENNAME;
	}

	public String getToken(String tokenSource, PlayerCharacter pc)
	{
		StringTokenizer aTok = new StringTokenizer(tokenSource, ".", false);
		aTok.nextToken();

		//Merge
		String token = aTok.nextToken();
		int merge = Constants.MERGE_ALL;
		if (token.indexOf("MERGE") >= 0)
		{
			merge = returnMergeType(token);
			token = aTok.nextToken();
		}

		//Get List
		List eqList = new ArrayList();
		if ("Container".equals(token))
		{
			for (Iterator e = pc.getEquipmentListInOutputOrder(merge).iterator(); e.hasNext();)
			{
				Equipment eq = (Equipment) e.next();

				if (eq.acceptsChildren())
				{
					eqList.add(eq);
				}
			}
		}
		else
		{
			eqList = pc.getEquipmentOfTypeInOutputOrder(token, 3, merge);
		}

		int temp = -1;
		//Begin Not code...
		while (aTok.hasMoreTokens())
		{
			if ("NOT".equalsIgnoreCase(token))
			{
				eqList = listNotType(pc, eqList, aTok.nextToken());
			}
			else if ("ADD".equalsIgnoreCase(token))
			{
				eqList = listAddType(pc, eqList, aTok.nextToken());
			}
			else if ("IS".equalsIgnoreCase(token))
			{
				eqList = listIsType(pc, eqList, aTok.nextToken());
			}
			else
			{
				// In the end of the above, bString would
				// be valid token, that should go into temp.
				try
				{
					temp = Integer.parseInt(token);
				}
				catch (NumberFormatException exc)
				{
					// not an error!
				}
			}

			if (temp >= 0)
			{
				break;
			}
			else
			{
				token = aTok.nextToken();
			}
		}

		String tempString = aTok.nextToken();

		if ((temp >= 0) && (temp < eqList.size()))
		{
			Equipment eq = (Equipment)eqList.get(temp);
			return getEqToken(pc, eq, tempString, aTok);
		}
		return "";
	}
}

