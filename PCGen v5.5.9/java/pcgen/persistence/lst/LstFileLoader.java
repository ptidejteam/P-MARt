/*
 * LstFileLoader.java
 * Copyright 2003 (C) David Hibbs <sage_sam@users.sourceforge.net>
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
 * Created on September 22, 2003, 11:29 AM
 *
 * Current Ver: $Revision: 1.1 $ <br>
 * Last Editor: $Author: vauchers $ <br>
 * Last Edited: $Date: 2006/02/21 01:28:23 $
 */

package pcgen.persistence.lst;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import pcgen.core.Constants;
import pcgen.core.SettingsHandler;
import pcgen.core.utils.Utility;
import pcgen.gui.utils.GuiFacade;
import pcgen.persistence.PersistenceLayerException;
import pcgen.util.Logging;

/**
 * This class is a base class for LST file loaders.
 * <p/>
 * This class lays out a skeleton for LST file loading, setting
 * up shared features and functions for loading and parsing of files
 * such as identification of COPY, MOD, and FORGET operations as
 * well as skipping of comment and blank lines.  Each implementation
 * must either implement performCopy, peformMod, performForget,
 * and or such operations are not supprted.  Instances must also either
 * implement addToGlobals or handle additions themselves (such as for
 * system files, etc).
 * <p/>
 * Instances of LstFileLoader or its subclasses are not thread-safe,
 * so any thread should only acccess a single loader (or group of loaders)
 * at a time.
 */
public abstract class LstFileLoader
{
	/**
	 * This method loads the given list of LST files.
	 *
	 * @param fileList containing the list of files to read
	 * @throws PersistenceLayerException if there is a problem with the
	 *                                   LST syntax
	 */
	public abstract void loadLstFiles(List fileList)
		throws PersistenceLayerException;

	/**
	 * This method is used to determine if a line in an LST file is
	 * considered a comment or something to be parsed.
	 *
	 * @param line String to determine whether is a comment or not
	 * @return boolean true if the line is a comment (or blank)
	 */
	protected final boolean isComment(String line)
	{
		return (line.length() == 0) || (line.startsWith("#"));
	}

	/**
	 * This method reads the given file or URL and stores its contents in the
	 * provided data buffer, returning a URL to the specified file for use in
	 * log/error messages by its caller.
	 *
	 * @param argFileName String path of the file or URL to read
	 * @param dataBuffer  StringBuffer to buffer the file content into
	 * @return URL pointing to the actual file read, for use in debug/log
	 *         messages
	 * @throws PersistenceLayerException if an error occurs in reading the file
	 */
	public static URL readFileGetURL(
		final String argFileName,
		final StringBuffer dataBuffer)
		throws PersistenceLayerException
	{
		URL aURL;

		if (argFileName.length() <= 0)
		{
			// We have a problem!
			throw new PersistenceLayerException("LstFileLoader.readFileGetURL() has a blank argFileName!");
		}


		// Don't changes the slashes if this is a url.
		String fileName = argFileName;

		if (!Utility.isURL(fileName))
		{
			fileName = Utility.fixFilenamePath(fileName);
		}

		// Common case first - URL
		// because this includes file:/ - which most stuff gets translated to
		if (Utility.isURL(fileName))
		{
			aURL = readFromURL(fileName, dataBuffer);
		}
		//Uncommon case: Plain Old File Name
		else
		{
			aURL = readFromFile(fileName, dataBuffer);
		}

		return aURL;
	}

	/**
	 * This method reads the given file and stores its contents in the provided
	 * data buffer, returning a URL to the specified file for use in log/error
	 * messages by its caller.
	 *
	 * @param fileName   String path of the file to read -- MUST be a file
	 *                   path, not a URL!
	 * @param dataBuffer StringBuffer to buffer the file content into
	 * @return URL pointing to the actual file read, for use in debug/log
	 *         messages
	 * @throws PersistenceLayerException if an error occurs in reading the file
	 */
	private static URL readFromFile(
		String fileName,
		final StringBuffer dataBuffer)
		throws PersistenceLayerException
	{
		URL aURL = null;
		InputStream inputStream = null;

		final File aFile = new File(fileName);
		if (!aFile.exists())
		{
			// They will know if something important
			// is missing, so just debug this error
			Logging.debugPrint(fileName + " doesn't seem to exist!");
			return null;
		}

		try
		{
			aURL = aFile.toURL();
			final int length = (int) aFile.length();
			inputStream = new FileInputStream(aFile);
			final byte[] inputLine = new byte[length];
			int bytesRead = inputStream.read(inputLine, 0, length);
			if (bytesRead != length)
			{
				Logging.errorPrint(
					"Only read "
					+ bytesRead
					+ " bytes from "
					+ fileName
					+ " but expected "
					+ length
					+ " in LstSystemLoader.initFile. Continuing anyway");
			}
			dataBuffer.append(new String(inputLine));
		}
		catch (IOException ioe)
		{
			aURL = null;

			// Don't throw an exception here because a simple
			// file not found will prevent ANY other files from
			// being loaded/processed -- NOT what we want
			Logging.errorPrint(
				"ERROR:"
				+ fileName
				+ "\n"
				+ "Exception type:"
				+ ioe.getClass().getName()
				+ "\n"
				+ "Message:"
				+ ioe.getMessage());
		}
		finally
		{
			if (inputStream != null)
			{
				try
				{
					inputStream.close();
				}
				catch (IOException e2)
				{
					Logging.errorPrint(
						"Can't close inputStream in LstSystemLoader.initFile",
						e2);
				}
			}
		}

		return aURL;
	}

	/**
	 * This method reads the given URL and stores its contents in the provided
	 * data buffer, returning a URL to the specified file for use in log/error
	 * messages by its caller.
	 *
	 * @param url        String path of the URL to read -- MUST be a URL path,
	 *                   not a file!
	 * @param dataBuffer StringBuffer to buffer the file content into
	 * @return URL pointing to the actual file read, for use in debug/log
	 *         messages
	 * @throws PersistenceLayerException if an error occurs in reading the file
	 */
	private static URL readFromURL(String url, final StringBuffer dataBuffer)
		throws PersistenceLayerException
	{
		URL aURL = null;
		InputStream inputStream = null;

		try
		{
			//only load local urls, unless loading of URLs is allowed
			if (!Utility.isNetURL(url) || SettingsHandler.isLoadURLs())
			{
				// Get the URL and open the stream
				aURL = new URL(url);
				inputStream = aURL.openStream();

				// Read from the stream
				final InputStreamReader ir = new InputStreamReader(inputStream);

				// Buffer the stream content
				final char[] b = new char[512];
				int n;
				while ((n = ir.read(b)) > 0)
				{
					dataBuffer.append(b, 0, n);
				}
			}
			else
			{
				// Just to protect people from using web
				// sources without their knowledge,
				// we added a preference.
				GuiFacade.showMessageDialog(
					null,
					"Preferences are currently set to NOT allow\nloading of "
					+ "sources from web links. \n"
					+ url
					+ " is a web link",
					Constants.s_APPNAME,
					GuiFacade.ERROR_MESSAGE);
				aURL = null;
			}
		}
		catch (IOException ioe)
		{
			aURL = null;

			// Don't throw an exception here because a simple
			// file not found will prevent ANY other files from
			// being loaded/processed -- NOT what we want
			Logging.errorPrint(
				"ERROR:"
				+ url
				+ "\n"
				+ "Exception type:"
				+ ioe.getClass().getName()
				+ "\n"
				+ "Message:"
				+ ioe.getMessage());
		}
		finally
		{
			if (inputStream != null)
			{
				try
				{
					inputStream.close();
				}
				catch (IOException e2)
				{
					Logging.errorPrint(
						"Can't close inputStream in LstSystemLoader.initFile",
						e2);
				}
			}
		}

		return aURL;
	}
}
