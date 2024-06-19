/*
 * PersistenceObserver.java
 *
 * Copyright 2004 (C) Frugal <frugal@purplewombat.co.uk>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.       See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Created on 18-Dec-2003
 *
 * Current Ver: $Revision: 1.1 $
 *
 * Last Editor: $Author: vauchers $
 *
 * Last Edited: $Date: 2006/02/21 01:33:07 $
 *
 */
package pcgen.gui;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;

import pcgen.core.PObject;

public class PersistenceObserver implements Observer {
	int totalFileCount = 0;
	int currentFileCount = 0;
	SourceLoadProgressDialog dialog = null;
	
	public PersistenceObserver()
	{
		dialog = new SourceLoadProgressDialog(null, false);
		dialog.pack();
		dialog.show();
	}
	
	
  /* (non-Javadoc)
   * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
   */
  public void update(Observable o, Object arg) {
    if (arg instanceof URL)
    {
    	setCurrentFileCount(getCurrentFileCount()+1);
    	final URL url = (URL) arg;
    	Runnable doWork = new Runnable() { 
    		public void run() { 
    			dialog.setCurrentFile(url.toString());
    		} 
    	}; 
    	SwingUtilities.invokeLater(doWork);
    	
//    	float percentage = 100 * getCurrentFileCount() / getTotalFileCount();
//    	System.out.println("Persistence Observer: "+percentage+"% ["+getCurrentFileCount()+" of "+getTotalFileCount()+"] loading new file: " + url);    	
    }
    else if (arg instanceof PObject)
    {
    	//System.out.println("Persistence Observer: loaded PObject: " + ((PObject)arg).getName());    	
    }
    else if (arg instanceof Exception)
    {
    	final Exception e = (Exception)arg;
    	Runnable doWork = new Runnable() { 
    		public void run() { 
    			dialog.addMessage(e.getMessage());
    		} 
    	}; 
    	SwingUtilities.invokeLater(doWork);
    	//System.out.println("Persistence Observer: ERROR: " + e.getMessage());
    }
    else if (arg instanceof Integer)
    {
    	int count = ((Integer)arg).intValue();
    	setTotalFileCount(count);
    }
    else
    {
    	//System.out.println("Persistence Observer: UNKNOWN: " + arg);
    }
  }

	/**
	 * @return Returns the totalFileCount.
	 */
	protected int getTotalFileCount() {
		return totalFileCount;
	}

	/**
	 * @param totalFileCount The totalFileCount to set.
	 */
	protected void setTotalFileCount(int atotalFileCount) {
		this.totalFileCount = atotalFileCount;
		Runnable doWork = new Runnable() { 
			public void run() { 
				dialog.setTotalFileCount(totalFileCount);
			} 
		}; 
		SwingUtilities.invokeLater(doWork);
	}

	/**
	 * @return Returns the currentFileCount.
	 */
	protected int getCurrentFileCount() {
		return currentFileCount;
	}

	/**
	 * @param currentFileCount The currentFileCount to set.
	 */
	protected void setCurrentFileCount(int aCurrentFileCount) {
		this.currentFileCount = aCurrentFileCount;
		Runnable doWork = new Runnable() { 
			public void run() { 
				dialog.setCurrentFileCount(currentFileCount);
			} 
		}; 
		SwingUtilities.invokeLater(doWork);
	}


	/**
	 * 
	 */
	public void end() {
		dialog.setVisible(false);
	}

}
