/*
 * LstSystemLoader.java
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
 * Current Ver: $Revision: 1.1 $
 * Last Editor: $Author: vauchers $
 * Last Edited: $Date: 2006/02/21 01:33:26 $
 *
 */

package pcgen.persistence.lst;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import pcgen.core.BioSet;
import pcgen.core.Campaign;
import pcgen.core.Constants;
import pcgen.core.CustomData;
import pcgen.core.Deity;
import pcgen.core.Domain;
import pcgen.core.Equipment;
import pcgen.core.EquipmentList;
import pcgen.core.EquipmentModifier;
import pcgen.core.GameMode;
import pcgen.core.Globals;
import pcgen.core.Kit;
import pcgen.core.LevelInfo;
import pcgen.core.PCClass;
import pcgen.core.PCTemplate;
import pcgen.core.PObject;
import pcgen.core.Race;
import pcgen.core.SettingsHandler;
import pcgen.core.Skill;
import pcgen.core.SystemCollections;
import pcgen.core.character.CompanionMod;
import pcgen.core.spell.Spell;
import pcgen.core.utils.Utility;
import pcgen.gui.pcGenGUI;
import pcgen.gui.utils.GuiFacade;
import pcgen.persistence.PersistenceLayerException;
import pcgen.persistence.SystemLoader;
import pcgen.util.Logging;

/**
 * ???
 *
 * @author David Rice <david-pcgen@jcuz.com>
 * @version $Revision: 1.1 $
 */
public final class LstSystemLoader implements SystemLoader
{

	// list of PObjects for character spells with subclasses
	private static final List pList = new ArrayList();

	private boolean showOGL = false;
	private boolean showD20 = false;
	private boolean showLicensed = true;
	private StringBuffer licensesToDisplayString = new StringBuffer();
	private List licenseFiles = new ArrayList();

	private CampaignSourceEntry globalCampaign = new CampaignSourceEntry(
		new Campaign(), "System Configuration Document" );

	private BioSetLoader bioLoader = new BioSetLoader();
	private BonusStackLoader bonusLoader = new BonusStackLoader();
	private CampaignLoader campaignLoader = new CampaignLoader();
	private EquipSlotLoader eqSlotLoader = new EquipSlotLoader();
	private LocationLoader locationLoader = new LocationLoader();
	private PaperInfoLoader paperLoader = new PaperInfoLoader();
	private PointBuyLoader pointBuyLoader = new PointBuyLoader();
	private SchoolListLoader schoolLoader = new SchoolListLoader();
	private SizeAdjustmentLoader sizeLoader = new SizeAdjustmentLoader();
	private SpecialAbilityLoader abilityLoader = new SpecialAbilityLoader();
	private StatsAndChecksLoader statCheckLoader = new StatsAndChecksLoader();
	private TraitLoader traitLoader = new TraitLoader();
	private WeaponProfLoader wProfLoader = new WeaponProfLoader();
	private UnitSetLoader unitLoader = new UnitSetLoader();
	private SkillLoader skillLoader = new SkillLoader();
	private LanguageLoader languageLoader = new LanguageLoader();
	private FeatLoader featLoader = new FeatLoader();
	private RaceLoader raceLoader = new RaceLoader();
	private PCClassLoader classLoader = new PCClassLoader();
	private DomainLoader domainLoader = new DomainLoader();
	private DeityLoader deityLoader = new DeityLoader();
	private SpellLoader spellLoader = new SpellLoader();
	private CurrencyParser currencyParser = new CurrencyParser();

	/**
	 * TODO: Convert remaining items to new persistence framework!
	 * Define the order in which the file types are ordered
	 * so we don't have to keep renumbering them
	 */
	private static final int[] loadOrder =
		{
			LstConstants.EQMODIFIER_TYPE		// loaded before the equipment so any modifiers will be found
			, LstConstants.EQUIPMENT_TYPE
			, LstConstants.LOAD_TYPE
			, LstConstants.CLASSSKILL_TYPE
			, LstConstants.CLASSSPELL_TYPE
			, LstConstants.REQSKILL_TYPE
			, LstConstants.TEMPLATE_TYPE
			, LstConstants.COMPANIONMOD_TYPE
			, LstConstants.KIT_TYPE
		};

	private static final int MODE_EXCLUDE = -1;
	private static final int MODE_DEFAULT = 0;
	private static final int MODE_INCLUDE = +1;

	private final FilenameFilter pccFileFilter = new FilenameFilter()
	{
		public boolean accept(File aFile, String aString)
		{
			try
			{
				if (aString.endsWith(Constants.s_PCGEN_CAMPAIGN_EXTENSION))
				{
					final String fName = aFile.getPath() + File.separator + aString;
					//Test to avoid reloading existing campaigns, so we can safely
					// call loadPCCFilesInDirectory repeatedly. -rlk 2002-03-30
					if (Globals.getCampaignByFilename(fName, false) == null)
					{
						campaignLoader.loadLstFile(fName);
					}
				}
				else if (aFile.isDirectory())
				{
					loadPCCFilesInDirectory(aFile.getPath() + File.separator + aString);
				}
			}
			catch (PersistenceLayerException e)
			{
				// LATER: This is not an appropriate way to deal with this exception.
				// Deal with it this way because of the way the loading takes place.  XXX
				Logging.errorPrint("PersistanceLayer", e);
			}
			return false;
		}
	};

	private static final FilenameFilter gameModeFileFilter = new FilenameFilter()
	{
		public boolean accept(File aFile, String aString)
		{
			try
			{
				final File d = new File(aFile, aString);
				if (d.isDirectory())
				{
					// the directory must contain
					// a "miscinfo.lst" file and a
					// "statsandchecks.lst" file to be
					// a complete gameMode
					File f = new File(d, "statsandchecks.lst");
					if (f.exists())
					{
						f = new File(d, "miscinfo.lst");
						return f.exists();
					}
					return false;
				}
			}
			catch (SecurityException e)
			{
				Logging.errorPrint("GameModes.listGameFiles", e);
			}
			return false;
		}
	};

	/////////////////////////////////////////////////////////////////
	// Property(s)
	/////////////////////////////////////////////////////////////////

	private String currentSource = "";

	private final List chosenCampaignSourcefiles = new ArrayList();
	private static int lineNum = 0;

	private final Set sourcesSet = new TreeSet();
	private final Map loadedFiles = new HashMap();

	private final List lstExcludeFiles = new ArrayList();
	private final List pccFileList = new ArrayList();
	private final List raceFileList = new ArrayList();
	private final List classFileList = new ArrayList();
	private final List companionmodFileList = new ArrayList();
	private final List skillFileList = new ArrayList();
	private final List featFileList = new ArrayList();
	private final List deityFileList = new ArrayList();
	private final List domainFileList = new ArrayList();
	private final List weaponProfFileList = new ArrayList();
	private final List equipmentFileList = new ArrayList();
	private final List classSkillFileList = new ArrayList();
	private final List classSpellFileList = new ArrayList();
	private final List spellFileList = new ArrayList();
	private final List languageFileList = new ArrayList();
	private final List reqSkillFileList = new ArrayList();
	private final List templateFileList = new ArrayList();
	private final List equipmodFileList = new ArrayList();
	private final List coinFileList = new ArrayList();
	private final List kitFileList = new ArrayList();
	private final List bioSetFileList = new ArrayList();

	// Used to store MODs to later processing
	// I had to store both the line and the filetype where this line was in
	private final List modLines = new ArrayList();
	private final List modFileType = new ArrayList();

	// Used to store FORGETs for later processing; works much like MODs.
	// Patch [651150] and Feature Request [650672].  sk4p 10 Dec 2002
	private final List forgetLines = new ArrayList();
	private final List forgetFileType = new ArrayList();

	private String skillReq = "";

	private boolean customItemsLoaded = false;
	private BioSet bioSet = new BioSet();

	////////////////////////////////////////////////////////////
	// Constructor(s)
	////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of LstSystemLoader
	 */
	public LstSystemLoader()
	{
	}

	////////////////////////////////////////////////////////////
	// Accessor(s) and Mutator(s)
	////////////////////////////////////////////////////////////
	public void setCurrentSource(String aString)
	{
		currentSource = aString;
	}

	public List getChosenCampaignSourcefiles()
	{
		return chosenCampaignSourcefiles;
	}

	public void setChosenCampaignSourcefiles(List l)
	{
		chosenCampaignSourcefiles.clear();
		chosenCampaignSourcefiles.addAll(l);
		SettingsHandler.getOptions().setProperty("pcgen.files.chosenCampaignSourcefiles", Utility.join(chosenCampaignSourcefiles, ','));
	}

	public Set getSources()
	{
		return sourcesSet;
	}

	public boolean isCustomItemsLoaded()
	{
		return customItemsLoaded;
	}

	/**
	 *
	 */
	public void setCustomItemsLoaded(boolean argLoaded)
	{
		customItemsLoaded = argLoaded;
	}

	public void initialize() throws PersistenceLayerException
	{
		loadGameModes();

		// Load the initial campaigns
		loadPCCFilesInDirectory(SettingsHandler.getPccFilesLocation().getAbsolutePath());

		// Now that those are loaded, make sure to initialize the recursive campaigns
		campaignLoader.initRecursivePccFiles();

		final String systemPrefix = SettingsHandler.getPcgenSystemDir() + File.separator;

		schoolLoader.loadLstFile(systemPrefix + "schools.lst");
		abilityLoader.loadLstFile(systemPrefix + "specials" + File.separator + "specials.lst");
		bonusLoader.loadLstFile(systemPrefix + "specials" + File.separator + "bonusstacks.lst");
		eqSlotLoader.loadLstFile(systemPrefix + "specials" + File.separator + "equipmentslots.lst");
		sizeLoader.loadLstFile(systemPrefix + "sizeAdjustment.lst");
		paperLoader.loadLstFile(systemPrefix + "paperInfo.lst");
		traitLoader.loadLstFile(systemPrefix + "bio" + File.separator + "traits.lst");
		locationLoader.loadLstFile(systemPrefix + "bio" + File.separator + "locations.lst");
		bioLoader.loadLstFile(systemPrefix + "bio" + File.separator + "biosettings.lst");
		pointBuyLoader.loadLstFile(systemPrefix + "pointbuymethods.lst");
		unitLoader.loadLstFile(systemPrefix + "unitSet.lst");

		Globals.sortPObjectList(Globals.getCampaignList());
	}

	/**
	 * This just calls loadPCCFilesInDirectory.
	 * Note:  This only handles added campaigns right now, not removed ones
	 * <p/>
	 * author Ryan Koppenhaver <rlkoppenhaver@yahoo.com>
	 *
	 * @see pcgen.persistence.PersistenceManager#refreshCampaigns
	 */
	public void refreshCampaigns()
	{
		Globals.clearCampaignsForRefresh();
		loadPCCFilesInDirectory(SettingsHandler.getPccFilesLocation().getAbsolutePath());
	}

	////////////////////////////////////////////////////////////
	// Private Method(s)
	////////////////////////////////////////////////////////////

	public void emptyLists()
	{
		loadedFiles.clear();
		chosenCampaignSourcefiles.clear();
		licensesToDisplayString = new StringBuffer();
		Globals.getBioSet().clearUserMap();

		releaseFileData();
		bioSet.clearUserMap();

		skillReq = "";
		customItemsLoaded = false;
	}

	/**
	 * This method releases the memory used by reading and storing
	 * the file data.
	 */
	private void releaseFileData()
	{
		lstExcludeFiles.clear();
		pccFileList.clear();
		raceFileList.clear();
		classFileList.clear();
		companionmodFileList.clear();
		skillFileList.clear();
		featFileList.clear();
		deityFileList.clear();
		domainFileList.clear();
		templateFileList.clear();
		weaponProfFileList.clear();
		equipmentFileList.clear();
		classSkillFileList.clear();
		classSpellFileList.clear();
		spellFileList.clear();
		reqSkillFileList.clear();
		languageFileList.clear();
		equipmodFileList.clear();
		coinFileList.clear();
		kitFileList.clear();
		bioSetFileList.clear();
	}

	private void loadPCCFilesInDirectory(String aDirectory)
	{
		new File(aDirectory).list(pccFileFilter);
	}

	/**
	 * This method gets the set of files to parse for a given object type.
	 *
	 * @param lineType int indicating the type of objects to retrieve the
	 *                 LST source lines for
	 * @return List containing the LST source lines for the requested
	 *         object type
	 */
	private List getFilesForType(final int lineType)
	{
		List lineList = null;

		switch (lineType)
		{
			case LstConstants.COMPANIONMOD_TYPE:
				lineList = companionmodFileList;
				break;

			case LstConstants.EQUIPMENT_TYPE:
				lineList = equipmentFileList;
				break;

			case LstConstants.LOAD_TYPE:
				break;

			case LstConstants.CLASSSKILL_TYPE:
				lineList = classSkillFileList;
				break;

			case LstConstants.CLASSSPELL_TYPE:
				lineList = classSpellFileList;
				break;

			case LstConstants.REQSKILL_TYPE:
				lineList = reqSkillFileList;
				break;

			case LstConstants.TEMPLATE_TYPE:
				lineList = templateFileList;
				break;

			case LstConstants.EQMODIFIER_TYPE:
				lineList = equipmodFileList;
				break;

			case LstConstants.KIT_TYPE:
				lineList = kitFileList;
				break;

			default:
				Logging.errorPrint("Campaign list corrupt; no such lineType (" + lineType + ") exists. Stopped parsing campaigns, but not aborting program.");
		}
		return lineList;
	}

	/**
	 * @see pcgen.persistence.SystemLoader#loadCampaigns(List)
	 */
	public void loadCampaigns(final List aSelectedCampaignsList) throws PersistenceLayerException
	{
		Globals.setSorted(false);
		sourcesSet.clear();
		licenseFiles.clear();
		if (aSelectedCampaignsList.size() == 0)
		{
			throw new PersistenceLayerException("You must select at least one campaign to load.");
		}

		// 21 Nov 2002: Put load inside a try/finally block to make sure
		// that file lines were cleared even if an exception occurred.
		// -- sage_sam
		try
		{
			// The first thing we need to do is load the
			// correct statsandchecks.lst file for this gameMode
			if (SettingsHandler.getGame() != null)
			{
				final String modeFilePrefix = SettingsHandler.getPcgenSystemDir() + File.separator + "gameModes" + File.separator + SettingsHandler.getGame().getName() + File.separator;
				statCheckLoader.loadLstFile(modeFilePrefix + "statsandchecks.lst");
			}
			else
			{
				// Autoload campaigns is set but there
				// is no current gameMode, so just return
				return;
			}

			// Sort the campaigns
			sortCampaignsByRank(aSelectedCampaignsList);

			// Read the campaigns
			readPccFiles(aSelectedCampaignsList);

			// Add custom campaign files at the start of the lists
			addCustomFilesToStartOfList();

			/* Load using the new LstFileLoaders */

			wProfLoader.loadLstFiles(weaponProfFileList);
			// load skills before classes to handle class skills
			skillLoader.loadLstFiles(skillFileList);
			// load before races to handle auto known languages
			languageLoader.loadLstFiles(languageFileList);
			// load before race or class to handle feats
			featLoader.loadLstFiles(featFileList);
			
			raceLoader.loadLstFiles(raceFileList);
			classLoader.loadLstFiles(classFileList);
			domainLoader.loadLstFiles(domainFileList);
			deityLoader.loadLstFiles(deityFileList);
			spellLoader.loadLstFiles(spellFileList);
			
			// loaded before equipment to cover costs
			currencyParser.loadLstFiles(coinFileList);

			// TODO: Convert remaining items to new persistence framework!
			
			// Process file content by load order [file type]
			for (int loadIdx = 0; loadIdx < loadOrder.length; loadIdx++)
			{
				final int lineType = loadOrder[loadIdx];
				List fileList = getFilesForType(lineType);
				if ((fileList != null) && (!fileList.isEmpty()))
				{
					List bArrayList = new ArrayList();

					// This relies on new items being added to the *end* of an ArrayList.
					processFileList(lineType, fileList, bArrayList);
				}

			} // end of load order loop

			// Load the bio settings files
			bioLoader.loadLstFiles(bioSetFileList);

			// Check for the required skills
			checkRequiredSkills();

			// Check for the default deities
			checkRequiredDeities();

			// Add default EQ mods
			addDefaultEquipmentMods();

			// Load custom items
			loadCustomItems();

			// Check for valid race types
			checkRaceTypes();

			// Verify weapons are melee or ranged
			verifyWeaponsMeleeOrRanged();

			//  Auto-gen additional equipment
			if (!SettingsHandler.wantToLoadMasterworkAndMagic())
			{
				EquipmentList.autoGenerateEquipment();
			}

			//  Show the licenses
			showLicensesIfNeeded();

		}
		finally
		{
			releaseFileData();
		}
	}

	/**
	 * This method processes a List of files of a given type, parsing
	 * the content of each file into PCGen core objects.
	 *
	 * @param lineType   int representing the type of files
	 * @param lineList   List containing the LST source lines
	 * @param bArrayList List that will contain pcgen.core objects
	 * @throws PersistenceLayerException if an error is found in the LST source
	 */
	private void processFileList(final int lineType, List lineList, List bArrayList)
		throws PersistenceLayerException
	{
		//  Campaigns aren't processed here any more.
		if (lineType == LstConstants.CAMPAIGN_TYPE)
		{
			Logging.errorPrint("No longer processing campaigns in processFileList.");
			return;
		}

		String fileName;
		for (int j = 0; j < lineList.size(); ++j)
		{
			final Object o = lineList.get(j);
			final String aLine;

			if( o instanceof String )
			{
				aLine = (String) o;
			}
			else
			{
				aLine = ((CampaignSourceEntry) o).getFile();
			}

			final StringTokenizer lineTokenizer = new StringTokenizer(aLine, "|");
			String extraInfo = null;

			// 1. The first token is the file name to process
			if (lineTokenizer.hasMoreTokens())
			{
				fileName = lineTokenizer.nextToken();
			}
			else
			{
				// Hey! No tokens!
				continue;
			}

			// The rest of the line is extra info
			if (fileName.length() < aLine.length())
			{
				extraInfo = aLine.substring(fileName.length());
			}

			// 2. Check whether the file was already [completely] loaded
			if (loadedFiles.containsKey(fileName))
			{
				// if so, continue processing lines
				continue;
			}

			// 3. Parse the file into a list of PObjects/Strings
			loadFileIntoList(fileName, lineType, bArrayList);

			// 4. Check for restrictions on loading the file.
			if (extraInfo != null)
			{
				// There are INCLUDE and EXCLUDE tags.  Process them.
				processExtraInfo(lineType, bArrayList, extraInfo);
			}
			else
			{
				// Using all data from the file.  Add it to the loaded list.
				loadedFiles.put(fileName, fileName);
			}

			// 5. Add the resulting information to Globals.
			if (!bArrayList.isEmpty())
			{
				addToGlobals(lineType, bArrayList);
			}

		} // end lineList loop
	}

	/**
	 * This method processes extra info from a line in a PCC/LST file,
	 * typically of the form INCLUDE or EXCLUDE.
	 *
	 * @param lineType    int indicating the type of line data
	 * @param pObjectList List of PObjects created from the data file
	 * @param extraInfo   String containing the extra info
	 */
	private void processExtraInfo(
		int lineType,
		List pObjectList,
		String extraInfo)
	{
		final StringTokenizer infoTokenizer = new StringTokenizer(extraInfo, "|");
		int inMode = MODE_DEFAULT;
		ArrayList includeExcludeNames = new ArrayList();

		while (infoTokenizer.hasMoreTokens())
		{
			// Get the next token (duh)
			String currentToken = infoTokenizer.nextToken();

			// Count parens in the token for use in identifying the start/end of
			// an include/exclude group
			final int openParens = countOpenParens(currentToken);
			final int closeParens = countCloseParens(currentToken);

			boolean handled = false;

			// Handle the start of an INCLUDE or EXCLUDE group
			if (currentToken.startsWith("(EXCLUDE"))
			{
				String name = currentToken.substring(9);
				if (name.endsWith(")"))
					name = name.substring(0, name.length() - 1);
				includeExcludeNames.add(name);
				inMode = MODE_EXCLUDE;
				handled = true;
			}
			else if (currentToken.startsWith("(INCLUDE"))
			{
				String name = currentToken.substring(9);
				if (name.endsWith(")"))
					name = name.substring(0, name.length() - 1);
				includeExcludeNames.add(name);
				inMode = MODE_INCLUDE;
				handled = true;
			}

			// Handle the end of an INCLUDE or EXCLUDE group
			if (currentToken.endsWith(")") && (closeParens > openParens))
			{
				if (!handled)
				{
					includeExcludeNames.add(currentToken.substring(0, currentToken.length() - 1));
				}

				if (inMode == MODE_EXCLUDE)
				{
					// exclude
					PObject anObject;
					for (int k = pObjectList.size() - 1; k >= 0; --k)
					{
						anObject = (PObject) pObjectList.get(k);
						if (includeExcludeNames.contains(anObject.getKeyName()))
						{
							pObjectList.remove(k);
						}
					}
				}
				else if (inMode == MODE_INCLUDE)
				{
					// include
					PObject anObject;
					for (int k = pObjectList.size() - 1; k >= 0; --k)
					{
						anObject = (PObject) pObjectList.get(k);
						if (!includeExcludeNames.contains(anObject.getKeyName()))
						{
							pObjectList.remove(k);
						}
					}
				}

				handled = true;
				inMode = MODE_DEFAULT;
			}

			// If we get here without handling the token, we need to do something with it.
			if (!handled)
			{
				// Assume it is part of a larger INCLUDE or EXCLUDE unless
				// it is a REQSKILL or SPECIAL line.
				if (lineType != LstConstants.SPECIAL_TYPE && lineType != LstConstants.REQSKILL_TYPE)
				{
					includeExcludeNames.add(currentToken);
				}
				else
				{
					// It is a REQSKILL or SPECIAL line; add it to the original list of info
					// This will probably blow something up later via a ClassCastException.
					pObjectList.add(currentToken);
				}
			}
		} // end while (infoTokenizer.hasMoreTokens())
	}

	/**
	 * This method counts the closing parens ')' in a given token.
	 *
	 * @param token String to count parens in.
	 * @return int number of closing parens in the token
	 */
	private int countCloseParens(final String token)
	{
		String dString = token;
		int parenCount = 0;

		while (dString.lastIndexOf(')') >= 0)
		{
			++parenCount;
			dString = dString.substring(0, dString.lastIndexOf(')'));
		}
		return parenCount;
	}

	/**
	 * This method counts the opening parens '(' in a given token.
	 *
	 * @param token String to count parens in.
	 * @return int number of opening parens in the token
	 */
	private int countOpenParens(final String token)
	{
		String dString = token;
		int parenCount = 0;

		while (dString.lastIndexOf('(') >= 0)
		{
			++parenCount;
			dString = dString.substring(0, dString.lastIndexOf('('));
		}
		return parenCount;
	}

	/**
	 * This method is called to verify that all weapons loaded from the equipment files
	 * are classified as either Melee or Ranged.  This is required so that to-hit values
	 * can be calculated for that weapon.
	 *
	 * @throws PersistenceLayerException if a weapon is neither melee or ranged, indicating
	 *                                   the name of the weapon that caused the error
	 */
	private void verifyWeaponsMeleeOrRanged() throws PersistenceLayerException
	{
		//
		// Check all the weapons to see if they are either Melee or Ranged, to avoid
		// problems when we go to export/preview the character
		//
		for (Iterator e2 = EquipmentList.getEquipmentList().iterator(); e2.hasNext();)
		{
			final Equipment aEq = (Equipment) e2.next();
			if (aEq.isWeapon() && !aEq.isMelee() && !aEq.isRanged())
			{
				throw new PersistenceLayerException("Weapon: " + aEq.getName()
					+ " is neither Melee nor Ranged." + Constants.s_LINE_SEP + Constants.s_APPNAME
					+ " cannot calculate \"to hit\" unless one of these is selected."
					+ Constants.s_LINE_SEP + "Source: " + aEq.getSourceFile());
			}
		}
	}

	/**
	 * This method checks all the loaded races to make sure that a type (such as HUMANOID) has
	 * been set.  If one was not set in the LST files, it is assumed to be HUMANOID and the
	 * Globals are updated accordingly.
	 */
	private void checkRaceTypes()
	{
		for (Iterator e2 = Globals.getRaceMap().values().iterator(); e2.hasNext();)
		{
			final Race aRace = (Race) e2.next();
			if (aRace.getMyTypeCount() == 0)
			{
				Logging.errorPrint("Race " + aRace.getName() + " has no type. Assuming Humanoid.");
				aRace.setTypeInfo("HUMANOID");
			}
		}
	}

	/**
	 * This method adds the default available equipment modififiers to the Globals.
	 *
	 * @throws PersistenceLayerException if some bizarre error occurs, likely due
	 *                                   to a change in EquipmentModifierLoader
	 */
	private void addDefaultEquipmentMods() throws PersistenceLayerException
	{
		String aLine;
		EquipmentModifier anObj = new EquipmentModifier();
		aLine = "Add Type\tKEY:ADDTYPE\tTYPE:ALL\tCOST:0\tNAMEOPT:NONAME\tSOURCE:PCGen Internal\tCHOOSE:COUNT=ALL|desired TYPE(s)|TYPE=EQTYPES";
		EquipmentModifierLoader.parseLine(anObj, aLine, null, -1);
		EquipmentList.getModifierList().add(anObj);

		//
		// Add internal equipment modifier for adding weapon/armor types to equipment
		//
		anObj = new EquipmentModifier();
		aLine = Constants.s_INTERNAL_EQMOD_WEAPON + "\tTYPE:Weapon\tVISIBLE:No\tCHOOSE:DUMMY\tNAMEOPT:NONAME";
		EquipmentModifierLoader.parseLine(anObj, aLine, null, -1);
		EquipmentList.getModifierList().add(anObj);

		anObj = new EquipmentModifier();
		aLine = Constants.s_INTERNAL_EQMOD_ARMOR + "\tTYPE:Armor\tVISIBLE:No\tCHOOSE:DUMMY\tNAMEOPT:NONAME";
		EquipmentModifierLoader.parseLine(anObj, aLine, null, -1);
		EquipmentList.getModifierList().add(anObj);
	}

	/**
	 * This method checks to make sure that the deities required for the current mode
	 * have been loaded into the Globals as Deities.  Prior to calling this method,
	 * deities are stored as simple String objects.
	 *
	 * @throws PersistenceLayerException if something bizarre occurs, such as this
	 *                                   method being invoked more than once, a change to DeityLoader, or
	 *                                   an invalid LST file containing the default deities.
	 */
	private void checkRequiredDeities() throws PersistenceLayerException
	{
		//
		// Add in the default deities (unless they're already there)
		//
		final List gDeities = Globals.getGlobalDeityList();
		if ((gDeities != null) && (gDeities.size() != 0))
		{
			deityLoader.loadLstFile(globalCampaign);
			int i = 0;
			for (Iterator e = gDeities.iterator(); e.hasNext();)
			{
				final String aLine = (String) e.next();
				final Deity aDeity = (Deity) deityLoader.parseLine(null, aLine, globalCampaign);
				deityLoader.finishObject(aDeity);
			}
		}
	}

	/**
	 * This method ensures that the global required skills are loaded and marked as required.
	 */
	private void checkRequiredSkills()
	{
		if (skillReq.length() > 0)
		{
			for (Iterator e1 = Globals.getSkillList().iterator(); e1.hasNext();)
			{
				final Skill aSkill = (Skill) e1.next();
				if (("UNTRAINED".equals(skillReq) && aSkill.getUntrained().length() > 0 && aSkill.getUntrained().charAt(0) == 'Y') || skillReq.equals("ALL"))
				{
					aSkill.setRequired(true);
				}
			}
		}
	}

	/**
	 * This method shows the OGL and D20 Licenses as needed/set in the SettingsHandler.
	 */
	private void showLicensesIfNeeded()
	{
		// Only worry about it if we're using the GUI
		if (Globals.getUseGUI())
		{
			if (showOGL && SettingsHandler.showLicense())
			{
				pcGenGUI.showLicense();
			}
			if (showLicensed  && SettingsHandler.showLicense())
			{
				pcGenGUI.showLicense("Special Licenses", licensesToDisplayString.toString());
				pcGenGUI.showLicense("Special Licenses", licenseFiles);
			}
			if (showD20 && SettingsHandler.showD20Info())
			{
				// D20 compliant removed as of 10/14/2003
				// some products may still be D20 compliant, so we need to display this
				// even though PCGen itself is not D20 compliant
				pcGenGUI.showMandatoryD20Info();
			}
		}

		// Prevent redisplay (i.e. sources unloaded, then re-loaded
		Globals.getSection15().setLength(0);
		showOGL = false;
		showD20 = false;
		showLicensed = false;
	}

	/**
	 * This method reads the PCC (Campaign) files and, if options are allowed to be set
	 * in the sources, sets the SettingsHandler settings to reflect the changes from the
	 * campaign files.
	 *
	 * @param aSelectedCampaignsList List of Campaigns to load
	 * @throws PersistenceLayerException if there is an error loading a given Campaign
	 */
	private void readPccFiles(final List aSelectedCampaignsList)
		throws PersistenceLayerException
	{
		if (SettingsHandler.isOptionAllowedInSources())
		{
			SettingsHandler.setOptionsProperties(); // Prime options based on currently selected preferences
		}

		for (int i = 0; i < aSelectedCampaignsList.size(); ++i)
		{
			Campaign aCamp = (Campaign) aSelectedCampaignsList.get(i);
			loadCampaignFile(aCamp);
			if (SettingsHandler.isOptionAllowedInSources())
			{
				setCampaignOptions(aCamp);
			}
		}
		//
		// This was added in v1.64. Why? This will read from options.ini, replacing anything that's been changed, not
		// just campaign-specific items. Commenting out as it breaks loading after selecting game mode on the Campaign menu.
		// Game mode reverts back to game mode saved in options.ini
		// - Byngl Sept 15, 2002
		// This allows options to be set by campaign files. It doesn't read directly from options.ini,
		// only from the properties. The setOptionsProperties call added above should prime these with
		// the current values before we load the campaigns.
		// - James Dempsey 09 Oct 2002
		if (SettingsHandler.isOptionAllowedInSources())
		{
			SettingsHandler.getOptionsFromProperties();
		}

	}

	/**
	 * This method sorts the provided listof Campaign objects by rank.
	 *
	 * @param aSelectedCampaignsList List of Campaign objects to sort
	 */
	private void sortCampaignsByRank(final List aSelectedCampaignsList)
	{
		// Local temporaries
		int i;
		Campaign aCamp;
		Campaign bCamp;

		// Loop through, performing a swap sort
		for (i = 0; i < aSelectedCampaignsList.size() - 1; ++i)
		{
			aCamp = (Campaign) aSelectedCampaignsList.get(i);
			sourcesSet.add(aCamp.getSourceInForm(Constants.SOURCELONG));
			int aCampRank = aCamp.getRank().intValue();
			for (int j = i + 1; j < aSelectedCampaignsList.size(); ++j)
			{
				bCamp = (Campaign) aSelectedCampaignsList.get(j);
				if (bCamp.getRank().intValue() < aCampRank)
				{
					aSelectedCampaignsList.set(i, bCamp);
					aSelectedCampaignsList.set(j, aCamp);
					aCamp = bCamp;
					aCampRank = aCamp.getRank().intValue();
				}
			}
			if (aSelectedCampaignsList.size() > 0)
			{
				aCamp =
					(Campaign) aSelectedCampaignsList.get(
						aSelectedCampaignsList.size() - 1);
				sourcesSet.add(aCamp.getSourceInForm(Constants.SOURCELONG));
			}
		} // end of campaign sort
	}

	/**
	 * Reads the source file for the campaign aCamp and adds the names
	 * of files to be loaded to raceFileList, classFileList etc.
	 */
	private void loadCampaignFile(Campaign aCamp) throws PersistenceLayerException
	{
		aCamp.setIsLoaded(true);
		final String sourceFile = aCamp.getSourceFile();

		// Update the list of chosen campaign source files
		if (!chosenCampaignSourcefiles.contains(sourceFile))
		{
			chosenCampaignSourcefiles.add(sourceFile);
			SettingsHandler.getOptions().setProperty("pcgen.files.chosenCampaignSourcefiles", Utility.join(chosenCampaignSourcefiles, ','));
		}

		// Update whether licenses need shown
		showOGL |= aCamp.isOGL();
		showD20 |= aCamp.isD20();
		showLicensed |= aCamp.isLicensed();
		if (aCamp.isLicensed())
		{
			licensesToDisplayString = licensesToDisplayString.append(aCamp.getLicense());
			licenseFiles.addAll(aCamp.getLicenseFiles());
		}

		// Load the LST files to be loaded for the campaign
		lstExcludeFiles.addAll(aCamp.getLstExcludeFiles());
		raceFileList.addAll(aCamp.getRaceFileList());
		classFileList.addAll(aCamp.getClassFileList());
		companionmodFileList.addAll(aCamp.getCompanionmodFileList());
		skillFileList.addAll(aCamp.getSkillFileList());
		featFileList.addAll(aCamp.getFeatFileList());
		deityFileList.addAll(aCamp.getDeityFileList());
		domainFileList.addAll(aCamp.getDomainFileList());
		weaponProfFileList.addAll(aCamp.getWeaponProfFileList());
		equipmentFileList.addAll(aCamp.getEquipmentFileList());
		classSkillFileList.addAll(aCamp.getClassSkillFileList());
		classSpellFileList.addAll(aCamp.getClassSpellFileList());
		spellFileList.addAll(aCamp.getSpellFileList());
		languageFileList.addAll(aCamp.getLanguageFileList());
		reqSkillFileList.addAll(aCamp.getReqSkillFileList());
		templateFileList.addAll(aCamp.getTemplateFileList());
		equipmodFileList.addAll(aCamp.getEquipmodFileList());
		coinFileList.addAll(aCamp.getCoinFileList());
		kitFileList.addAll(aCamp.getKitFileList());
		bioSetFileList.addAll(aCamp.getBioSetFileList());
	}

	private void addCustomFilesToStartOfList()
	{
		String tempGame;
		CampaignSourceEntry tempSource = null;

		// The dummy campaign for custom data.
		Campaign customCampaign = new Campaign();
		customCampaign.setName("Custom");
		customCampaign.setDescription("Custom data");
		
		//
		// Add the custom bioset file to the start of the list if it exists
		//
		if (new File(CustomData.customBioSetFilePath(true)).exists())
		{
			tempGame = CustomData.customBioSetFilePath(true);
			bioSetFileList.remove(tempGame);
			bioSetFileList.add(0, tempGame);
		}

		//
		// Add the custom class file to the start of the list if it exists
		//
		if (new File(CustomData.customClassFilePath(true)).exists())
		{
			tempSource = new CampaignSourceEntry(
				customCampaign,
				CustomData.customClassFilePath(true));
			classFileList.remove(tempSource);
			classFileList.add(0, tempSource);
		}

		//
		// Add the custom deity file to the start of the list if it exists
		//
		if (new File(CustomData.customDeityFilePath(true)).exists())
		{
			tempSource = new CampaignSourceEntry(
				customCampaign,
				CustomData.customDeityFilePath(true));
			deityFileList.remove(tempSource);
			deityFileList.add(0, tempSource);
		}

		//
		// Add the custom domain file to the start of the list if it exists
		//
		if (new File(CustomData.customDomainFilePath(true)).exists())
		{
			tempSource = new CampaignSourceEntry(
				customCampaign,
				CustomData.customDomainFilePath(true));
			domainFileList.remove(tempSource);
			domainFileList.add(0, tempSource);
		}

		//
		// Add the custom feat file to the start of the list if it exists
		//
		if (new File(CustomData.customFeatFilePath(true)).exists())
		{
			tempSource = new CampaignSourceEntry(
				customCampaign,
				CustomData.customFeatFilePath(true));
			featFileList.remove(tempSource);
			featFileList.add(0, tempSource);
		}

		//
		// Add the custom language file to the start of the list if it exists
		//
		if (new File(CustomData.customLanguageFilePath(true)).exists())
		{
			tempSource = new CampaignSourceEntry(
				customCampaign,
				CustomData.customLanguageFilePath(true));
			languageFileList.remove(tempSource);
			languageFileList.add(0, tempSource);
		}

		//
		// Add the custom race file to the start of the list if it exists
		//
		if (new File(CustomData.customRaceFilePath(true)).exists())
		{
			tempSource = new CampaignSourceEntry(
				customCampaign,
				CustomData.customRaceFilePath(true));
			raceFileList.remove(tempSource);
			raceFileList.add(0, tempSource);
		}

		//
		// Add the custom skill file to the start of the list if it exists
		//
		if (new File(CustomData.customSkillFilePath(true)).exists())
		{
			tempSource = new CampaignSourceEntry(
				customCampaign,
				CustomData.customSkillFilePath(true));
			skillFileList.remove(tempSource);
			skillFileList.add(0, tempSource);
		}

		//
		// Add the custom spell file to the start of the list if it exists
		//
		if (new File(CustomData.customSpellFilePath(true)).exists())
		{
			tempSource = new CampaignSourceEntry(
				customCampaign,
				CustomData.customSpellFilePath(true));
			spellFileList.remove(tempSource);
			spellFileList.add(0, tempSource);
		}

		//
		// Add the custom template file to the start of the list if it exists
		//
		if (new File(CustomData.customTemplateFilePath(true)).exists())
		{
			tempGame = CustomData.customTemplateFilePath(true);
			templateFileList.remove(tempGame);
			templateFileList.add(0, tempGame);
		}
	}

	/**
	 * Sets the options specified in the campaign aCamp.
	 */
	private static void setCampaignOptions(Campaign aCamp)
	{
		final Properties options = aCamp.getOptions();
		if (options != null)
		{
			for (Enumeration e = options.propertyNames(); e.hasMoreElements();)
			{
				final String key = (String) e.nextElement();
				final String value = options.getProperty(key);
				SettingsHandler.setPCGenOption(key, value);
			}
		}
	}

	/**
	 * @see pcgen.persistence.SystemLoader#loadFileIntoList(String, int, List)
	 */
	public void loadFileIntoList(String fileName, int fileType, List aList) throws PersistenceLayerException
	{
		initFile(fileName, fileType, aList, true);
	}

	/**
	 * This method loads the contents of a file into the appropriate
	 * line lists.
	 *
	 * @param argFileName    String file to load
	 * @param fileType       int indicating the type of file
	 * @param aList          List to load the lines into
	 * @param throwException boolean true to force exceptions to be thrown
	 *                       if encountered while reading the file
	 * @throws PersistenceLayerException
	 */
	private void initFile(String argFileName, int fileType, List aList, boolean throwException) throws PersistenceLayerException
	{
		final StringBuffer dataBuffer = new StringBuffer();
		if (lstExcludeFiles.contains(argFileName))
		{
			return;
		}
		final URL aURL = LstFileLoader.readFileGetURL(argFileName, dataBuffer);
		if (aURL == null)
		{
			return;
		}

		/*
		 * Need to keep the Windows line separator as newline
		 * delimiter to ensure cross-platform portability.
		 *
		 * author: Thomas Behr 2002-11-13
		 */
		final Map sourceMap = new HashMap();
		final String newlinedelim = "\r\n";
		final String aString = dataBuffer.toString();
		final StringTokenizer newlineStr = new StringTokenizer(aString, newlinedelim);

		setCurrentSource("");
		String nameString = "";
		String aLine = "";
		String prevLine = "";
		Campaign sourceCampaign = null;
		PObject anObj = null;

		while (newlineStr.hasMoreTokens())
		{
			boolean isModItem;
			final boolean isForgetItem;
			aLine = newlineStr.nextToken();
			++lineNum;

			if (aLine.startsWith("CAMPAIGN:") &&
				(fileType != LstConstants.CAMPAIGN_TYPE)) // && fileType != -1 sage_sam 10 Sept 2003
			{
				sourceCampaign = Globals.getCampaignNamed(aLine.substring(9));
				continue;
			}
			//
			// Ignore commented-out lines
			// and empty lines
			if (aLine.length() == 0)
			{
				continue;
			}
			if (fileType != LstConstants.CAMPAIGN_TYPE && aLine.length() > 0 && aLine.charAt(0) == '#')
			{
				continue;
			}

			if (aLine.startsWith("SOURCE") && fileType != LstConstants.CAMPAIGN_TYPE)
			{
				final StringTokenizer sTok = new StringTokenizer(aLine, "|");
				while (sTok.hasMoreTokens())
				{
					final String arg = sTok.nextToken();
					final String key = arg.substring(6, arg.indexOf(":"));
					final String val = arg.substring(arg.indexOf(":") + 1);
					sourceMap.put(key, val);
				}
				continue;
			}
			// used for .COPY= cases
			String copyName = null;
			// check for special case of CLASS:name.MOD
			isModItem = aLine.endsWith(".MOD");
			if (isModItem && aLine.startsWith("CLASS:"))
			{
				nameString = aLine.substring(0, aLine.length() - 4);
			}
			isForgetItem = aLine.endsWith(".FORGET");
			if (isForgetItem)
			{
				nameString = aLine.substring(0, aLine.length() - 7);
			}
			// first field is usually name
			// (only exception is class-level lines)
			// see if name ends with .MOD, if so, use
			// existing item instead of creating a new one
			if (aLine.indexOf('\t') > 2)
			{
				final StringTokenizer t = new StringTokenizer(aLine, "\t");
				nameString = t.nextToken();
				isModItem = nameString.endsWith(".MOD");
				if (isModItem)
				{
					nameString = nameString.substring(0, nameString.length() - 4);
				}
				else if (nameString.indexOf(".COPY=") > 0)
				{
					copyName = nameString.substring(nameString.indexOf(".COPY=") + 6);
					nameString = nameString.substring(0, nameString.indexOf(".COPY="));
				}
			}
			switch (fileType)
			{
				case LstConstants.COMPANIONMOD_TYPE:
					anObj = initFileTypeCompanionMod(isForgetItem, nameString, fileType, isModItem, anObj, sourceCampaign, sourceMap, aList, aLine, aURL);
					break;

				case LstConstants.EQUIPMENT_TYPE:
					anObj = initFileTypeEquipment(isForgetItem, nameString, fileType, isModItem, copyName, anObj, sourceCampaign, sourceMap, aList, aLine, aURL);
					break;

				case LstConstants.LOAD_TYPE:
					Globals.getLoadStrings().add(aLine);
					break;

				case -1: // if we're in the process of loading campaigns/sources when
					// another source is loaded via PCC:, then it's fileType=-1

				case LstConstants.CLASSSKILL_TYPE:
					//Deliberate fall-through
				case LstConstants.CLASSSPELL_TYPE:
					//Deliberate fall-through
				case LstConstants.REQSKILL_TYPE:
					aList.add(aLine);
					break;

				case LstConstants.TEMPLATE_TYPE:
					anObj = initFileTypeTemplate(isForgetItem, nameString, fileType, isModItem, anObj, sourceCampaign, sourceMap, aList, aLine, aURL);
					break;

				case LstConstants.EQMODIFIER_TYPE:
					anObj = initFileTypeEqModifier(sourceCampaign, sourceMap, aLine, aURL, aList);
					break;

				case LstConstants.KIT_TYPE:
					if (aLine.startsWith("REGION:"))
					{
						prevLine = aLine.substring(7);
						continue;
					}
					if (prevLine.length() == 0)
					{
						throw new PersistenceLayerException("Illegal kit info " + aURL.toString() + ":" + Integer.toString(lineNum) + " \"" + aLine + "\"");
					}

					if (aLine.startsWith("STARTPACK:"))
					{
						anObj = new Kit(prevLine);
						anObj.setSourceCampaign(sourceCampaign);
						anObj.setSourceMap(sourceMap);
						Globals.getKitInfo().add(anObj);
					}
					if (anObj != null)
					{
						KitLoader.parseLine((Kit) anObj, aLine, aURL, lineNum);
					}
					break;

				default:
					Logging.errorPrint("In LstSystemLoader.initValue the fileType " + fileType + " is not handled.");
					break;
			}

			//
			// Save the source file in object
			//
			switch (fileType)
			{
				case LstConstants.EQUIPMENT_TYPE:
				case LstConstants.EQMODIFIER_TYPE:
				case LstConstants.KIT_TYPE:
					if (anObj != null)
					{
						anObj.setSourceFile(aURL.toString());
					}
					break;

				case LstConstants.LOAD_TYPE:
				case LstConstants.CLASSSKILL_TYPE:
				case LstConstants.CLASSSPELL_TYPE:
				case LstConstants.REQSKILL_TYPE:
				case LstConstants.TEMPLATE_TYPE:
				case LstConstants.EQUIPSLOT_TYPE:
				case LstConstants.COMPANIONMOD_TYPE:
				case -1:
					break;
				default:
					Logging.errorPrint("In LstSystemLoader.initValue the fileType " + fileType + " is not handled.");
					break;
			}
		}
		setCurrentSource("");
	}

	private static PObject initFileTypeEqModifier(Campaign sourceCampaign, Map sourceMap, String aLine, final URL aURL, List aList) throws PersistenceLayerException
	{
		final PObject anObj;
		anObj = new EquipmentModifier();
		anObj.setSourceCampaign(sourceCampaign);
		anObj.setSourceMap(sourceMap);
		EquipmentModifierLoader.parseLine((EquipmentModifier) anObj, aLine, aURL, lineNum);
		aList.add(anObj);
		return anObj;
	}

	private PObject initFileTypeTemplate(boolean forgetItem, String nameString, int fileType, boolean modItem, PObject anObj, Campaign sourceCampaign, Map sourceMap, List aList, String aLine, final URL aURL) throws PersistenceLayerException
	{
		if (forgetItem)
		{
			forgetItem(Globals.getTemplateNamed(nameString), nameString, fileType);
			return anObj;
		}

		if (!modItem)
		{
			anObj = new PCTemplate();
			anObj.setSourceCampaign(sourceCampaign);
			anObj.setSourceMap(sourceMap);
			aList.add(anObj);
		}
		else
		{
			anObj = Globals.getTemplateNamed(nameString);
		}
		if (anObj == null)
		{
			modLines.add(aLine);
			modFileType.add(new Integer(fileType));
		}
		if (anObj != null)
		{
			PCTemplateLoader.parseLine((PCTemplate) anObj, aLine, aURL, lineNum);
		}
		return anObj;
	}

	private PObject initFileTypeEquipment(boolean forgetItem, String nameString, int fileType, boolean modItem, String copyName, PObject anObj, Campaign sourceCampaign, Map sourceMap, List aList, String aLine, final URL aURL) throws PersistenceLayerException
	{
		if (forgetItem)
		{
			forgetItem(EquipmentList.getEquipmentNamed(nameString), nameString, fileType);
			return anObj;
		}

		if (!modItem)
		{
			if (copyName == null)
			{
				anObj = new Equipment();
				anObj.setSourceCampaign(sourceCampaign);
				anObj.setSourceMap(sourceMap);
			}
			else
			{
				anObj = EquipmentList.getEquipmentNamed(nameString);
				if (anObj != null)
				{
					try
					{
						anObj = (PObject) anObj.clone();
					}
					catch (CloneNotSupportedException exc)
					{
						GuiFacade.showMessageDialog(null, exc.getMessage(), Constants.s_APPNAME, GuiFacade.ERROR_MESSAGE);
					}
					anObj.setName(copyName);
				}
				else
				{
					Logging.errorPrint("Could not copy " + nameString + " to create " + copyName);
				}
			}
			if (anObj != null)
			{
				aList.add(anObj);
			}
		}
		else
		{
			anObj = EquipmentList.getEquipmentNamed(nameString);
			if (anObj == null)
			{
				anObj = EquipmentList.getEquipmentNamed(nameString, aList);
			}
		}
		if (modItem && (anObj == null))
		{
			modLines.add(aLine);
			modFileType.add(new Integer(fileType));
		}
		if (anObj != null)
		{
			EquipmentLoader.parseLine((Equipment) anObj, aLine, aURL, lineNum);
		}
		return anObj;
	}

	private PObject initFileTypeCompanionMod(boolean forgetItem, String nameString, int fileType, boolean modItem, PObject anObj, Campaign sourceCampaign, Map sourceMap, List aList, String aLine, final URL aURL)
	{
		if (forgetItem)
		{
			forgetItem(Globals.getCompanionMod(nameString), nameString, fileType);
			return anObj;
		}

		if (!modItem)
		{
			anObj = new CompanionMod();
			anObj.setSourceCampaign(sourceCampaign);
			anObj.setSourceMap(sourceMap);
			aList.add(anObj);
		}
		else
		{
			anObj = Globals.getCompanionMod(nameString);
		}
		if (anObj == null)
		{
			modLines.add(aLine);
			modFileType.add(new Integer(fileType));
		}
		if (anObj != null)
		{
			CompanionModLoader.parseLine((CompanionMod) anObj, aLine, aURL, lineNum);
		}
		return anObj;
	}

	/**
	 * This method adds a given list of objects to the appropriate Globals
	 * storage.
	 *
	 * @param lineType   int indicating the type of objects in the list
	 * @param aArrayList List containing the objects to add to Globals
	 */
	private void addToGlobals(final int lineType, final List aArrayList)
	{
		String aClassName = "";
		for (int i = 0; i < aArrayList.size(); ++i)
		{
			switch (lineType)
			{
				case LstConstants.COMPANIONMOD_TYPE:
					final CompanionMod cMod = Globals.getCompanionMod(((CompanionMod) aArrayList.get(i)).getKeyName());
					if (cMod == null)
					{
						Globals.getCompanionModList().add(aArrayList.get(i));
					}
					break;
				case LstConstants.EQUIPMENT_TYPE:
					final Equipment eq = EquipmentList.getEquipmentKeyed(((Equipment) aArrayList.get(i)).getKeyName());
					if (eq == null)
					{
						EquipmentList.getEquipmentList().add(aArrayList.get(i));
					}
					break;
				case LstConstants.CLASSSKILL_TYPE:
					parseClassSkillFrom((String) aArrayList.get(i));
					break;
				case LstConstants.CLASSSPELL_TYPE:
					aClassName = parseClassSpellFrom((String) aArrayList.get(i), aClassName);
					break;

				case LstConstants.REQSKILL_TYPE:
					final String aString = (String) aArrayList.get(i);
					if ("ALL".equals(aString) || "UNTRAINED".equals(aString))
					{
						skillReq = aString;
					}
					else
					{
						final Skill skillKeyed = Globals.getSkillKeyed(aString);
						if (skillKeyed != null)
						{
							skillKeyed.setRequired(true);
						}
					}
					break;

				case LstConstants.TEMPLATE_TYPE:
					final PCTemplate aTemplate = Globals.getTemplateKeyed(((PCTemplate) aArrayList.get(i)).getKeyName());
					if (aTemplate == null)
					{
						Globals.getTemplateList().add(aArrayList.get(i));
					}
					break;

				case LstConstants.EQMODIFIER_TYPE:
					final EquipmentModifier aModifier = EquipmentList.getModifierKeyed(((EquipmentModifier) aArrayList.get(i)).getKeyName());
					if (aModifier == null)
					{
						EquipmentList.getModifierList().add(aArrayList.get(i));
					}
					break;

				case LstConstants.KIT_TYPE:
					break;

				case -1:
					break;

				default:
					Logging.errorPrint("In LstSystemLoader.initValue the lineType " + lineType + " is not handled.");
					break;
			}
		}
		aArrayList.clear();
	}

	private void loadCustomItems()
	{
		customItemsLoaded = true;
		if (!SettingsHandler.getSaveCustomEquipment())
		{
			return;
		}

		final BufferedReader br = CustomData.getCustomEquipmentReader();

		// Why is this here?  This implies it is somehow
		// order-independent and should precede the opening of
		// the file.  This is almost assuredly a bug of some
		// kind waiting to happen.  Aha!  Just look at what is
		// in the "finally" clause below.  --bko XXX
		EquipmentList.setAutoGeneration(true);

/*		if (br == null)
		{
			return;
		}
*/
		try
		{
			while (br != null)
			{
				String aLine = br.readLine();
				if (aLine == null)
				{
					break;
				}
				if (aLine.startsWith("BASEITEM:"))
				{
					final int idx = aLine.indexOf('\t', 9);
					if (idx < 10)
					{
						continue;
					}
					final String baseItemKey = aLine.substring(9, idx);
					aLine = aLine.substring(idx + 1);
					Equipment aEq = EquipmentList.getEquipmentKeyed(baseItemKey);
					if (aEq != null)
					{
						aEq = (Equipment) aEq.clone();
						aEq.load(aLine);
						EquipmentList.addEquipment(aEq);
					}
				}
			}
		}
		catch (IOException e)
		{
			Logging.errorPrint("Error when loading custom items", e);
		}
		finally
		{
			EquipmentList.setAutoGeneration(false);
			Globals.sortPObjectList(EquipmentList.getEquipmentList());
			try
			{
				if (br != null)
				{
					br.close();
				}
			}
			catch (IOException ex)
			{
				Logging.errorPrint("Error when closing infile after loading custom items", ex);
			}
		}
	}

	private static void parseClassSkillFrom(String aLine)
	{
		StringTokenizer aTok = new StringTokenizer(aLine, "\t");
		String className = aTok.nextToken();
		final PCClass aClass = Globals.getClassKeyed(className);
		String aName = className;
		if (aClass != null)
		{
			aName = aClass.getKeyName();
		}
		if (aTok.hasMoreTokens())
		{
			className = aTok.nextToken();
			aTok = new StringTokenizer(className, "|");
			String aString;
			Skill aSkill;
			while (aTok.hasMoreTokens())
			{
				aString = aTok.nextToken();
				final String aStringParen = aString + "(";
				aSkill = Globals.getSkillKeyed(aString);
				if (aSkill != null)
				{
					aSkill.getClassList().add(aName);
				}
				else
				{
					Skill bSkill;
					for (Iterator e = Globals.getSkillList().iterator(); e.hasNext();)
					{
						bSkill = (Skill) e.next();
						if (bSkill.getKeyName().startsWith(aStringParen))
						{
							bSkill.getClassList().add(aName);
						}
					}
				}
			}
		}
	}

	private static String parseClassSpellFrom(String aLine, String aName)
	{
		StringTokenizer aTok = new StringTokenizer(aLine, "\t");
		final String aString = aTok.nextToken();
		if (aString.startsWith("DOMAIN:"))
		{
			aName = aString.substring(7);
			final Domain aDom = Globals.getDomainKeyed(aName);
			if (aDom != null)
			{
				aName = "DOMAIN|" + aName;
			}
			else
			{
				aName = "";
			}
		}
		if (aString.startsWith("CLASS:"))
		{
			boolean isClass = true;
			aName = "";
			if (aString.length() > 6)
			{
				aName = aString.substring(6);
			}

			// first look for an actual class

			PObject aClass = Globals.getClassKeyed(aName);
			//
			// If the class does not have any spell-casting, then it must either
			// be a domain or a subclass
			//
			if (aClass != null)
			{
				if (((PCClass) aClass).getSpellType().equalsIgnoreCase(Constants.s_NONE))
				{
					aClass = null;
				}
			}

			// then look for a domain

			if (aClass == null)
			{
				aClass = Globals.getDomainKeyed(aName);
				if (aClass != null)
				{
					isClass = false;
				}
			}

			// if it's not one of those, leave it since it might be a subclass

			if (aClass != null)
			{
				aName = aClass.getKeyName();
			}
			if (isClass)
			{
				aName = "CLASS|" + aName;
			}
			else
			{
				aName = "DOMAIN|" + aName;
			}
		}
		else if (aTok.hasMoreTokens())
		{
			PObject aClass;
			final String name = aName.substring(aName.indexOf('|') + 1);
			if (aName.startsWith("DOMAIN|"))
			{
				aClass = Globals.getDomainNamed(name);
			}
			else if (aName.startsWith("CLASS|"))
			{
				aClass = Globals.getClassNamed(name);
			}
			else
			{
				return aName;
			}

			if (aClass == null) // then it must be a subclass
			{
				for (Iterator i = pList.iterator(); i.hasNext();)
				{
					aClass = (PObject) i.next();
					if (aClass.getName().equals(name))
					{
						break;
					}
					else
					{
						aClass = null;
					}
				}
				if (aClass == null)
				{
					aClass = new PObject();
					aClass.setName(name);
					pList.add(aClass);
				}
			}
			final int level = Integer.parseInt(aString);
			final String bString = aTok.nextToken();
			aTok = new StringTokenizer(bString, "|");
			while (aTok.hasMoreTokens())
			{
				final Spell aSpell = Globals.getSpellKeyed(aTok.nextToken().trim());
				if (aSpell != null)
				{
					aSpell.setLevelInfo(aName, level);
				}
			}
		}
		return aName;
	}

	private void loadGameModes()
	{
		final String[] gameFiles = getGameFilesList();
		if ((gameFiles == null) || (gameFiles.length == 0))
		{
			return;
		}

		SystemCollections.clearGameModeList();
		final String aDirectory = SettingsHandler.getPcgenSystemDir() + File.separator + "gameModes" + File.separator;

		for (int i = 0; i < gameFiles.length; ++i)
		{
			final GameMode gm = loadGameModeMiscInfo(gameFiles[i], new File(aDirectory + gameFiles[i] + File.separator + "miscinfo.lst"));
			if (gm != null)
			{
				loadGameModeInfoFile(gm, new File(aDirectory + gameFiles[i] + File.separator + "load.lst"), "load");
				loadGameModeInfoFile(gm, new File(aDirectory + gameFiles[i] + File.separator + "level.lst"), "level");
				loadGameModeInfoFile(gm, new File(aDirectory + gameFiles[i] + File.separator + "rules.lst"), "rules");
				// TODO: fix this somehow....
				// The following is wrong, as there should be
				// a seperate statsandchecks for each gameMode
				// but the file sets things in Globals.java
				// which are common between all the gameModes
				// In addition they must be set otherwise
				// we get NullPointerExceptions when trying
				// to load from the command line.
				try
				{
					statCheckLoader.loadLstFile(aDirectory + gameFiles[i] + File.separator + "statsandchecks.lst");
				}
				catch (PersistenceLayerException ex)
				{
					// ignored
				}
			}
		}
		SystemCollections.sortGameModeList();
	}

	private static void loadGameModeInfoFile(GameMode gameMode, File aFile, String aType)
	{
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(aFile), "UTF-8"));
			String aLine;
			int lineNum = 0;
			while ((aLine = br.readLine()) != null)
			{
				lineNum++;
				// Ignore commented-out and empty lines
				if (aLine.length() > 0 && aLine.charAt(0) == '#' || (aLine.length() == 0))
				{
					continue;
				}
				if (aType.equals("load"))
				{
					gameMode.addLoadString(aLine);
				}
				else if (aType.equals("level"))
				{
					final LevelInfo level = new LevelInfo();
					LevelLoader.parseLine(level, aLine, lineNum);
					gameMode.addLevelInfo(level);
				}
				else if (aType.equals("rules"))
				{
					RuleCheckLoader.parseLine(gameMode, aLine);
				}
			}
		}
		catch (IOException ex)
		{
			Logging.errorPrint("Error when loading game mode " + aType + " info", ex);
		}
		finally
		{
			try
			{
				if (br != null)
				{
					br.close();
				}
			}
			catch (IOException ex)
			{
				Logging.errorPrint("Error when trying to close after loading game mode " + aType + " info", ex);
			}
		}
	}

	private static GameMode loadGameModeMiscInfo(String aName, File aFile)
	{
		GameMode gameMode = null;
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(aFile), "UTF-8"));
			String aLine;
			while ((aLine = br.readLine()) != null)
			{
				// Ignore commented-out and empty lines
				if (aLine.length() > 0 && aLine.charAt(0) == '#' || (aLine.length() == 0))
				{
					continue;
				}

				if (gameMode == null)
				{
					gameMode = new GameMode(aName);
					SystemCollections.addToGameModeList(gameMode);
				}

				GameModeLoader.parseMiscGameInfoLine(gameMode, aLine, aFile, lineNum);
			}
		}
		catch (IOException ex)
		{
			Logging.errorPrint("Error when loading game mode misc info", ex);
		}
		finally
		{
			try
			{
				if (br != null)
				{
					br.close();
				}
			}
			catch (IOException ex)
			{
				Logging.errorPrint("Error when trying to clase file after loading game mode misc info", ex);
			}
		}
		return gameMode;
	}

	/**
	 * Get a list of all the directories in system/gameModes/
	 * that contain a file named statsandchecks.lst and miscinfo.lst
	 */
	private static String[] getGameFilesList()
	{
		final String aDirectory = SettingsHandler.getPcgenSystemDir() + File.separator + "gameModes" + File.separator;
		return new File(aDirectory).list(gameModeFileFilter);
	}

	/**
	 * @see pcgen.persistence.SystemLoader#loadModItems(boolean)
	 */
	public void loadModItems(boolean flagDisplayError)
	{
		PObject anObj;
		String aString;

		if (modLines.size() > 0)
		{
			for (int i = 0; i < modLines.size();)
			{
				anObj = null;
				StringTokenizer aTok = new StringTokenizer(modLines.get(i).toString(), TAB_DELIM);
				aString = aTok.nextToken();
				aTok = new StringTokenizer(aString, ":");
				aTok.nextToken();
				if (aTok.countTokens() > 0)
				{
					aString = aTok.nextToken();
				}
				aString = aString.substring(0, aString.indexOf(".MOD"));
				try
				{
					switch (Integer.valueOf(modFileType.get(i).toString()).intValue())
					{
						case -1:
							i++;
							continue;
						case LstConstants.COMPANIONMOD_TYPE:
							anObj = Globals.getCompanionMod(aString);
							CompanionModLoader.parseLine((CompanionMod) anObj, modLines.get(i).toString(), null, 0);
							modLines.remove(i);
							modFileType.remove(i);
							break;
						case LstConstants.EQUIPMENT_TYPE:
							anObj = EquipmentList.getEquipmentNamed(aString);
							EquipmentLoader.parseLine((Equipment) anObj, modLines.get(i).toString(), null, 0);
							modLines.remove(i);
							modFileType.remove(i);
							break;
						case LstConstants.TEMPLATE_TYPE:
							anObj = Globals.getTemplateNamed(aString);
							PCTemplateLoader.parseLine((PCTemplate) anObj, modLines.get(i).toString(), null, 0);
							modLines.remove(i);
							modFileType.remove(i);
							break;
						default:
							Logging.errorPrint("In LstSystemLoader.loadMod the fileType " + modFileType.get(i).toString() + " is not handled.");
							break;
					}
				}
				catch (PersistenceLayerException ple)
				{
					Logging.errorPrint("PersistenceLayerException in LstSystemLoader.loadMod. ", ple);
				}
				catch (NullPointerException npe)
				{
					Logging.errorPrint("Null pointer exception in LstSystemLoader.loadMod. ", npe);
				}
				if (anObj == null)
				{
					if (flagDisplayError) // && (!empty))
					{
						Logging.errorPrint("Cannot apply .MOD: " + aString + " not found");
					}
					i++;
				}
			}
		}

		// Now process all the FORGETs
		// sk4p 10 Dec 2002
		if (forgetLines.size() > 0)
		{
			for (int i = 0; i < forgetLines.size(); i++)
			{
				aString = forgetLines.get(i).toString();
				try
				{
					switch (Integer.valueOf(forgetFileType.get(i).toString()).intValue())
					{
						case LstConstants.COMPANIONMOD_TYPE:
							anObj = Globals.getCompanionMod(aString);
							Globals.getCompanionModList().remove(anObj);
							break;
						case LstConstants.EQUIPMENT_TYPE:
							anObj = EquipmentList.getEquipmentNamed(aString);
							EquipmentList.getEquipmentList().remove(anObj);
							break;
						case LstConstants.TEMPLATE_TYPE:
							anObj = Globals.getTemplateNamed(aString);
							Globals.getTemplateList().remove(anObj);
							break;
						default:
							Logging.errorPrint("In LstSystemLoader.loadMod the fileType " + modFileType.get(i).toString() + " cannot be forgotten.");
							break;
					}
				}
				catch (NullPointerException e)
				{
					Logging.errorPrint("Null pointer exception in LstSystemLoader.loadMod. ", e);
				}
			}
			forgetLines.clear();
			forgetFileType.clear();
		}
	}

	/**
	 * Called repeatedly to forget items when .FORGET has been applied.
	 *
	 * @param itemToForget
	 * @param nameOfItemToForget
	 * @param fileType
	 */
	private void forgetItem(PObject itemToForget, String nameOfItemToForget, int fileType)
	{
		if (itemToForget == null)
		{
			Logging.errorPrint("Forgetting " + nameOfItemToForget + ": Not defined yet");
		}
		forgetLines.add(nameOfItemToForget);
		forgetFileType.add(new Integer(fileType));
	}

}
