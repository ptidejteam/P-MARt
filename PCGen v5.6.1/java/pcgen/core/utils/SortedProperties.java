/*
 *  SortedProperties.java
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Created on November 07, 2003, 2:15 AM
 *
 * Current Ver: $Revision: 1.1 $
 * Last Editor: $Author: vauchers $
 * Last Edited: $Date: 2006/02/21 01:33:38 $
 *
 */

package pcgen.core.utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import pcgen.util.Logging;

/**
 * An subclass of Properties whose output is sorted
 *
 * @author Jayme Cox <jaymecox@users.sourceforge.net>
 * @version $Revision: 1.1 $
 */
public class SortedProperties extends Properties
{
	public SortedProperties()
	{
		super();
	}

	public void mystore(FileOutputStream out, String header)
	{
		BufferedWriter bw = null;
		SortedMap aMap = new TreeMap(this);
		Iterator entries = aMap.entrySet().iterator();
		Map.Entry entry;
		try
		{
			bw = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
			bw.write(header);
			bw.newLine();

			while (entries.hasNext())
			{
				entry = (Map.Entry) entries.next();
				// The following characters must be escaped:
				// #, !, = and :
				String aString = fixUp((String) entry.getValue());
				bw.write(entry.getKey() + "=" + aString);
				bw.newLine();
			}
			bw.flush();
		}
		catch (UnsupportedEncodingException ex)
		{
			Logging.errorPrint("Error writing to the options.ini file: ", ex);
		}
		catch (IOException ex)
		{
			Logging.errorPrint("Error writing to the options.ini file: ", ex);
		}
		finally
		{
			try
			{
				if (bw != null)
				{
					bw.flush();
					bw.close();
				}
			}
			catch (IOException iox)
			{
				Logging.debugPrint("Caught exception trying to close writer in SortedProperties.mystore", iox);
				// ignore
			}
		}
	}

	private static String fixUp(String aString)
	{
		StringBuffer ab = new StringBuffer(aString.length());
		for (int i = 0; i < aString.length(); i++)
		{
			// #, !, = and :
			if ((aString.charAt(i) == '#')
				|| (aString.charAt(i) == '\\')
				|| (aString.charAt(i) == '!')
				|| (aString.charAt(i) == '=')
				|| (aString.charAt(i) == ':'))
			{
				ab.append("\\").append(aString.charAt(i));
			}
			else
			{
				ab.append(aString.charAt(i));
			}
		}
		return ab.toString();
	}
}
