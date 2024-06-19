/*
 *  DiceFudge.java
 *  Copyright (C) 2002 Devon D Jones
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *  The author of this program grants you the ability to use this code
 *  in conjunction with code that is covered under the Open Gaming License
 *
 */
package gmgen.plugin;

/** Fudge Die
 * @author Soulcatcher
 * @created May 24, 2003
 */
public class DiceFudge extends Die {
	/**  Number of sides */
	private static final int sides = 6;
	/** Die modifier */
	private int modifier = 0;
	/**  Die to do the rolling behind the scenes */
	protected Dice die;


	/** Constructor for the DiceFudge object
         * @param num Number of fudge dice
         * @param modifier Modifier to the rolls
         */
	public DiceFudge(int num, int modifier) {
		this.num = num;
		this.modifier = modifier;
		rolls = new Integer[num];
		die = new Dice(1, sides);
		roll();
	}


	/** Constructor for the DiceFudge object
         * @param num Number of Fudge Dice
         */
	public DiceFudge(int num) {
		this(num, 0);
	}


	/** Roll the dice
         * @return Result of the roll
         */
	public int roll() {
		int i;
		total = 0;
		for (i = 0; i < num; i++) {
			rolls[i] = new Integer(die.roll());
			if (rolls[i].intValue() == 1 || rolls[i].intValue() == 2) {
				total--;
			} else if (rolls[i].intValue() == 5 || rolls[i].intValue() == 6) {
				total++;
			}
		}

		total += modifier;
		return total;
	}


	/**  The name of the die in the ndF format
         * @return ndF
         */
	public String toString() {
		return num + "dF";
	}
}



