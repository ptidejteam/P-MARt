/*
 * DeityLoader.java
 * Copyright 2001 (C) Bryan McRoberts <mocha@mcs.net>
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
 * Created on February 22, 2002, 10:29 PM
 *
 * $Id: DeityLoader.java,v 1.1 2006/02/21 01:33:26 vauchers Exp $
 */

package pcgen.persistence.lst;

import java.util.StringTokenizer;

import pcgen.core.Deity;
import pcgen.core.Globals;
import pcgen.core.PObject;
import pcgen.persistence.PersistenceLayerException;
import pcgen.persistence.SystemLoader;
import pcgen.util.Logging;

/**
 * This class is an LstObjectLoader that loads deity information.
 *
 * @author David Rice <david-pcgen@jcuz.com>
 * @version $Revision: 1.1 $
 */
final class DeityLoader extends LstObjectFileLoader
{
	/**
	 * Creates a new instance of DeityLoader
	 */
	public DeityLoader()
	{
		super();
	}

	/**
	 * @see pcgen.persistence.lst.LstObjectFileLoader#parseLine(pcgen.core.PObject, java.lang.String, pcgen.persistence.lst.CampaignSourceEntry)
	 */
	public PObject parseLine(
		PObject target,
		String lstLine,
		CampaignSourceEntry source)
		throws PersistenceLayerException
	{
		Deity deity = (Deity) target;
		if (deity == null)
		{
			deity = new Deity();
		}

		final StringTokenizer colToken = new StringTokenizer(lstLine, SystemLoader.TAB_DELIM);
		int col = 0;

		while (colToken.hasMoreTokens())
		{
			final String colString = colToken.nextToken().trim();
			if (PObjectLoader.parseTag(deity, colString))
			{
				continue;
			}

			final int colLen = colString.length();
			if ((colLen > 6) && colString.startsWith("ALIGN:"))
			{
				deity.setAlignment(colString.substring(6));
			}
			else if (colString.startsWith("DEITYWEAP:"))
			{
				deity.setFavoredWeapon(colString.substring(10));
			}
			else if (colString.startsWith("DOMAINS:"))
			{
				deity.setDomainListString(colString.substring(8));
			}
			else if (colString.startsWith("FOLLOWERALIGN:"))
			{
				deity.setFollowerAlignments(colString.substring(14));
			}
			else if ((colLen > 9) && colString.startsWith("PANTHEON:"))
			{
				deity.setPantheonList(colString.substring(9));
			}
			else if (colString.startsWith("QUALIFY:"))
			{
				deity.setQualifyString(colString.substring(8));
			}
			else if ((colLen > 5) && colString.startsWith("RACE:"))
			{
				deity.setRaceList(colString.substring(5));
			}
			else if (colString.startsWith("SYMBOL:"))
			{
				deity.setHolyItem(colString.substring(7));
			}
			else if (col >= 0 && col < 6)
			{
				switch (col)
				{
					case 0:
						if( (!colString.equals(deity.getName())) 
							&& (colString.indexOf(".MOD")<0))
						{
							finishObject(deity);
							deity = new Deity();
							deity.setName(colString);
							deity.setSourceCampaign(source.getCampaign());
							deity.setSourceFile(source.getFile());
						}
						break;
					default:
						Logging.errorPrint("In DeityLoader.parseLine the column " + col + " is not possible.");
						break;
				}
				col++;
			}
			else
			{
				Logging.errorPrint("Illegal deity info '" + colString + "' in " + source.getFile() );
			}
		}
		
		return deity;
	}
	/**
	 * @see pcgen.persistence.lst.LstObjectFileLoader#finishObject(pcgen.core.PObject)
	 */
	protected void finishObject(PObject target)
	{
		if( includeObject(target) )
		{
			if( Globals.getDeityNamed(target.getName())==null )
			{
				Globals.getDeityList().add(target);
			}
		}
	}

	/**
	 * @see pcgen.persistence.lst.LstObjectFileLoader#getObjectNamed(java.lang.String)
	 */
	protected PObject getObjectNamed(String baseName)
	{
		return Globals.getDeityNamed(baseName);
	}

	/**
	 * @see pcgen.persistence.lst.LstObjectFileLoader#performForget(pcgen.core.PObject)
	 */
	protected void performForget(PObject objToForget)
		throws PersistenceLayerException
	{
		Globals.getDeityList().remove(objToForget);
	}

}
