/*
 *  Initiative - A role playing utility to track turns
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
 *  InitHolderVector.java
 *
 *  Created on January 16, 2002, 1:08 PM
 */
package gmgen.plugin;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

/**
 *@author     devon
 *@created    March 20, 2003
 *@version
 */

public class InitHolderList extends LinkedList {
	/*
	 *  History:
	 *  March 20, 2003: Cleanup for Version 1.0
	 */
	/**
	 *  Method for adding a combatant to the list
	 *
	 *@param  user  The Combatant to be added
	 *@return       if the add is successful.
	 */
	public boolean add(InitHolder user) {
		boolean result = super.add(user);
		if (result) {
			this.sort();
		}
		return result;
	}


	/**  sorts the list based on initiative */
	public void sort() {
		Collections.sort(this, new InitHolderComperator());
	}


	/**  Rolls an initiative check for the whole list */
	public void check() {
		Dice d20 = new Dice(1, 20);

		for(int i = 0; i < this.size(); i++) {
			InitHolder c = (InitHolder) this.get(i);
			int roll = d20.roll();
			c.getInitiative().checkExtRoll(roll);
		}
		this.sort();

		calculateNumberField();
	}

	public void calculateNumberField() {
		int j = 1;
		for(int i = 0; i < this.size(); i++) {
			InitHolder c = (InitHolder) this.get(i);
			if (c instanceof Combatant) {
				Combatant cbt = (Combatant) c;
				cbt.setNumber(j);
				j++;
			}
		}
	}

	/**
	 *  Gets the Max Init of the InitHolderList object, minimum 20
	 *
	 *@return    the highest initiative in the list (minimum 20)
	 */
	public int getMaxInit() {
		int maxInit = 20;
		for (int i = 0; i < this.size(); i++) {
			InitHolder c = (InitHolder) this.get(i);
			int cInit = c.getInitiative().getCurrentInitiative();
			if (cInit > maxInit) {
				maxInit = cInit;
			}
		}
		return maxInit;
	}


	/**
	 *  Checks to see if a particular initiative is Active (a combatant has that
	 *  initiative)
	 *
	 *@param  init  Initiative value to check
	 *@return       if it is active
	 */
	public boolean initValid(int init) {
		for (int i = 0; i < this.size(); i++) {
			InitHolder c = (InitHolder) this.get(i);
			if (!c.getStatus().equals("Dead")) {
				int cInit = c.getInitiative().getCurrentInitiative();

				if (cInit == init) {
					return true;
				}
			}
		}
		return false;
	}


	/**
	 *  Returns a vector intended for use as a row for a table.
	 *
	 *@param  i            the index of the InitHolder to return the vector for
	 *@param  columnOrder  a LinkedList containing the order of columns, retrieved
	 *                     from the table object.
	 *@return              The Vector that contains a table row.
	 */
	public Vector getRowVector(int i, LinkedList columnOrder) {
		InitHolder iH = (InitHolder) this.get(i);
		return iH.getRowVector(columnOrder);
	}

	/**
	 *  Determines if a string passed in exists within the InitHolderList
	 *  And if it does, it appends a space and a number that is unique in the list
	 *
	 *@param  name  String to compare
	 *@return       Unique Name
	 */
	public String getUniqueName(String name) {
		/* TODO: uncomment when we can use 1.4
		int i = 1;
		while (!isUniqueName(name)) {
			name = name.replaceAll(" \\(\\d.*\\)", "") + " (" + i + ")";
			i++;
		}
		*/
		return name;
	}

	/**
	 *  Determines if a string passed in exists within the InitHolderList
	 *
	 *@param  name  String to compare
	 *@return       if the string is unique or not
	 */
	public boolean isUniqueName(String name) {
		for (int i = 0; i < this.size(); i++) {
			InitHolder c = (InitHolder) this.get(i);
			if (c.getName().equals(name)) {
				return false;
			}
		}
		return true;
	}
}

