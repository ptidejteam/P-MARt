/*
 * Filterable.java
 * Copyright 2002 (C) Thomas Behr <ravenlock@gmx.de>
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
 * Created on February 9, 2002, 2:30 PM
 */
package pcgen.gui.filter;

import java.util.List;

/**
 * <code>Filterable</code>
 *
 * @author Thomas Behr
 * @version $Revision: 1.1 $
 */

public interface Filterable
{

	/**
	 * Selector
	 * (we need this one for saving filter settings)
	 */
	public String getName();

	/**
	 * Selector
	 */
	public List getAvailableFilters();

	/**
	 * Selector
	 */
	public List getRemovedFilters();

	/**
	 * Selector
	 */
	public List getSelectedFilters();

	/**
	 * specifies wheter the "match any" option should be available
	 */
	public boolean isMatchAnyEnabled();

	/**
	 * specifies wheter the "negate/reverse" option should be available
	 */
	public boolean isNegateEnabled();

	/**
	 * returns the filter seletion mode<br>
         * possible values are
         * <ul>
         * <li>	DISABLED_MODE
         * <li> DEMO_MODE
         * <li> SINGLE_SINGLE_MODE
         * <li> SINGLE_MULTI_MODE
         * <li> MULTI_MULTI_MODE
         * </ul>
         * as defined in the FilterConstants interface
         *
         * <br>author: Thomas Behr
         *
         * @return the selection mode for this Filterable
	 */
	public int getSelectionMode();

	/**
         * returns the filter mode<br>
         * possible values are
         * <ul>
         * <li>	MATCH_ALL
         * <li> MATCH_ALL_NEGATE
         * <li> MATCH_ANY
         * <li> MATCH_ANY_NEGATE
         * </ul>
         * as defined in the FilterConstants interface
         *
         * <br>author: Thomas Behr
         *
         * @return the filter mode for this Filterable
	 */
	public int getFilterMode();

	/**
         * sets the filter mode<br>
         * recognized values are
         * <ul>
         * <li>	MATCH_ALL
         * <li> MATCH_ALL_NEGATE
         * <li> MATCH_ANY
         * <li> MATCH_ANY_NEGATE
         * </ul>
         * as defined in the FilterConstants interface
         *
         * <br>author: Thomas Behr
         *
	 * @param mode   the mode to be set
	 *
	 */
	public void setFilterMode(int mode);

	/**
         * initializes all available filters for this Filterable
         *
         * <br>author: Thomas Behr
	 */
	public void initializeFilters();

	/**
         * re-applies the selected filters;
         * has to be called after changes to the filter selection
         *
         * <br>author: Thomas Behr
	 */
	public void refreshFiltering();
}



