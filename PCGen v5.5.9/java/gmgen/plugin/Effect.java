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
 *  Spell.java
 *
 *  Created on January 16, 2002, 12:27 PM
 */
package gmgen.plugin;

import java.util.LinkedList;
import java.util.Vector;

/**
 *@author     devon
 *@created    March 20, 2003
 *@version
 */
public class Effect extends Event {

	/**  Description of the Field */
	public SystemInitiative init;
	protected String status = "";


	/**
	 *  Creates new Spell
	 *
	 *@param  name      Description of the Parameter
	 *@param  player    Description of the Parameter
	 *@param  duration  Description of the Parameter
	 *@param  init      Description of the Parameter
	 */
	public Effect(String player, String effect, String description, int duration, boolean alert) {
		super("", player, description, duration, 0, alert);
		this.status = effect;
	}

	public String getEndText() {
		return "Effect " + getName() + " Completed or Occured";
	}

	/**
	 *  builds a vector that is intended to be turned into a table row that
	 *  contains all of this object's informaiton
	 *
	 *@param  columnOrder  The current table's column order
	 *@return              The Row Vector
	 */
	public Vector getRowVector(LinkedList columnOrder) {
		Vector rowVector = new Vector();
		int columns = columnOrder.size();
		for (int j = 0; j < columns; j++) {
			String columnName = (String) columnOrder.get(j);
			if (columnName.equals("Name")) { // Event's name
				rowVector.add("");
			}
			else if (columnName.equals("Player")) { // Player's Name who cast the spell
				rowVector.add("Owner: " + getPlayer());
			}
			else if (columnName.equals("Status")) { // Event's Status
				rowVector.add(getStatus());
			}
			else if (columnName.equals("+")) { // Ignored
				rowVector.add("");
			}
			else if (columnName.equals("Init")) { // Event's Initiative
				rowVector.add("");
			}
			else if (columnName.equals("Dur")) { // Event's Duration
				rowVector.add("" + getDuration());
			}
			else if (columnName.equals("#")) { // Ignored
				rowVector.add("");
			}
			else if (columnName.equals("HP")) { // Ignored
				rowVector.add("");
			}
			else if (columnName.equals("HP Max")) { // Ignored
				rowVector.add("");
			}
			else if (columnName.equals("Type")) { //PC, Enemy, Ally, -
				rowVector.add("");
			}
		}
		return rowVector;
	}


	/**
	 *  changes the value of a table field in the backend data set
	 *
	 *@param  columnOrder  A list of columns in order for the table
	 *@param  colNumber    What column number has been edited
	 *@param  data         The new value for the field
	 */
	public void editRow(LinkedList columnOrder, int colNumber, Object data) {
		Vector rowVector = new Vector();
		String columnName = (String) columnOrder.get(colNumber);
		String strData = String.valueOf(data);
		if (columnName.equals("Name")) { // Spell's Name
			setName(strData);
		}
		else if (columnName.equals("Player")) { // Name of the player who cast the spell
			setPlayer(strData);
		}
		else if (columnName.equals("Status")) { // SPell's status
			setStatus(strData);
		}
		else if (columnName.equals("Dur")) { // Spell's duration
			Integer intData = new Integer(strData);
			setDuration(intData.intValue());
		}
	}
}

