/*
 * BigDecimalHelper.java
 * Copyright 2003 (C) Jonas Karlsson <jujutsunerd@sf.net>
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
 * Created on April 12, 2003, 3:20 AM
 */
package pcgen.util;

import java.math.BigDecimal;

/**
 * This contains helper functions for BigDecimal.
 * @author     Jonas Karlsson <jujutsunerd@sf.net>
 * @version    $Revision: 1.1 $
 */

public class BigDecimalHelper
{

	/**
	 * As BigDecimal is immutable, try to use this rather than creating new zeros all the time.
	 */
	public static final BigDecimal ZERO = new BigDecimal(0);

	/**
	 * Returns a string with the trimmed number.
	 * E.g. numberToTrim=3.1000 > 3.1
	 * If numberToTrim is non-numeric, 0 is returned (should be changed.)
	 * @param numberToTrim The value to trim.
	 * @return
	 */
	public static String trimZeros(String numberToTrim)
	{
		BigDecimal aBigD = ZERO;
		try
		{
			aBigD = new BigDecimal(numberToTrim);
		}
		catch (NumberFormatException exc)
		{
			Logging.errorPrint("Cannot trim zeroes from " + numberToTrim + " as is not a number. Using 0 instead.");
		}
		return trimBigDecimal(aBigD).toString();
	}

	public static String trimZeros(BigDecimal n)
	{
		return trimBigDecimal(n).toString();
	}

	/**
	 * trimBigDecimal ( (BigDecimal) a) to cut off all trailing zeros.
	 * It's a terrible hack.
	 * @param n the BigDecimal to trim all trailing zeros from
	 * @return the trimmed BigDecimal
	 */
	public static BigDecimal trimBigDecimal(BigDecimal n)
	{
		try
		{
			// loop until we catch an exception
			while (true)
			{
				n = n.setScale(n.scale() - 1);
			}
		}
		catch (ArithmeticException ignore)
		{
			// Not "real" error
			// setScale() tried to eliminate a non-zero digit
		}
		return n;
	}

	/**
	 * Sets [n] to [dp] decimal places.
	 * @param n the BigDecimal to format
	 * @param dp the wanted number of decimal places
	 * @return the formated BigDecimal
	 */
	public static BigDecimal formatBigDecimal(BigDecimal n, int dp)
	{
		return n.setScale(dp, BigDecimal.ROUND_HALF_UP);	// Sets scale and rounds up if most significant (cut off) number >= 5
	}
}
