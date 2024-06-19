/*
 * Checks.java
 * Copyright 2002 (C) Greg Bingleman <byngl@hotmail.com>
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
 * Created on December 13, 2002, 9:19 AM
 *
 * Current Ver: $Revision: 1.1 $
 * Last Editor: $Author: vauchers $
 * Last Edited: $Date: 2006/02/21 01:27:55 $
 *
 */

package pcgen.core.bonus;

import java.util.Iterator;
import pcgen.core.PObject;
import pcgen.core.SystemCollections;

/**
 * <code>Checks</code>
 *
 * @author  Greg Bingleman <byngl@hotmail.com>
 */
final class Checks extends BonusObj
{
	Checks()
	{
		super();
	}

	private static class CheckInfo
	{
		PObject pobj = null;
		boolean isBase;

		CheckInfo(PObject argPobj, boolean argIsBase)
		{
			super();
			pobj = argPobj;
			isBase = argIsBase;
		}
	}

	boolean parseToken(String argToken)
	{
		boolean isBase = false;
		final String token;
		if (argToken.startsWith("BASE."))
		{
			token = argToken.substring(5);
			isBase = true;
		}
		else
		{
			token = argToken;
		}
		PObject aCheck = SystemCollections.getCheckNamed(token);
		if (aCheck != null)
		{
			addBonusInfo(new CheckInfo(aCheck, isBase));
			return true;
		}
		else if (token.equals("ALL"))
		{
			// Special case of:  BONUS:CHECKS|ALL|x
			for (Iterator ac = SystemCollections.getUnmodifiableCheckList().iterator(); ac.hasNext();)
			{
				aCheck = (PObject) ac.next();
				addBonusInfo(new CheckInfo(aCheck, isBase));
			}
			return true;
		}
		return false;
	}

	String unparseToken(Object obj)
	{
		String token = "";
		if (((CheckInfo) obj).isBase)
		{
			token = "BASE.";
		}
		return token + ((CheckInfo) obj).pobj.getName();
	}
}
