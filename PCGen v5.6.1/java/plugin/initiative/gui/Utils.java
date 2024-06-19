/*
 *  pcgen - DESCRIPTION OF PACKAGE
 *  Copyright (C) 2004 Ross M. Lodge
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
 *  Utils.java
 *
 *  Created on Feb 13, 2004, 3:54:51 PM
 */
package plugin.initiative.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFormattedTextField;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;

/**
 * <p>
 * A utility class for building some GUI elements
 * </p>
 * 
 * @author Ross M. Lodge
 *
 */
public class Utils
{

	/**
	 * 
	 * <p>Builds a formatted text field with specified min and max</p>
	 * @param min
	 * @param max
	 * @return
	 */
	public static JFormattedTextField buildIntegerField(int min, int max)
	{
		java.text.NumberFormat numberFormat =
			java.text.NumberFormat.getIntegerInstance();
		NumberFormatter formatter = new NumberFormatter(numberFormat);
		formatter.setMinimum(new Integer(min));
		formatter.setMaximum(new Integer(max));
		final JFormattedTextField returnValue = new JFormattedTextField(formatter);
		returnValue.setColumns(3);
		return returnValue;
	}

	/**
	 * 
	 * <p>
	 * Builds a formatted text field with specified min and max, and
	 * attaches the slider to it via listeners.  The text field gets it's
	 * min and max from the slider.
	 * </p>
	 * @param matchingSlider
	 * @return
	 */
	public static JFormattedTextField buildIntegerFieldWithSlider(final JSlider matchingSlider)
	{
		final JFormattedTextField returnValue = buildIntegerField(matchingSlider.getMinimum(), matchingSlider.getMaximum());
		returnValue.addPropertyChangeListener(new PropertyChangeListener()
		{
			
			public void propertyChange(PropertyChangeEvent evt)
			{
				if ("value".equals(evt.getPropertyName())) {
					Number value = (Number)evt.getNewValue();
					if (matchingSlider != null && value != null) {
						matchingSlider.setValue(value.intValue());
					}
				}
			}
		});
		matchingSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				JSlider source = (JSlider) e.getSource();
				int value = (int) source.getValue();
				if (!source.getValueIsAdjusting())
				{ //done adjusting
					returnValue.setValue(new Integer(value)); //update ftf value
				}
				else
				{ //value is adjusting; just set the text
					returnValue.setText(String.valueOf(value));
				}
			}
		});
		return returnValue;
	}

	/**
	 * 
	 * <p>Builds the specified slider.</p>
	 * @param min
	 * @param max
	 * @return
	 */
	public static JSlider buildSlider(int min, int max)
	{
		JSlider slider = new JSlider();
		slider.setMinimum(min);
		slider.setMaximum(max);
		slider.setMajorTickSpacing(5);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		return slider;
	}

}
