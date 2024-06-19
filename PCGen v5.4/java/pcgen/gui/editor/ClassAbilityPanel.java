/*
 * ClassAbilityPanel
 * Copyright 2003 (C) Bryan McRoberts <merton.monk@codemonkeypublishing.com >
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
 * Created on January 8, 2003, 8:15 PM
 *
 * @(#) $Id: ClassAbilityPanel.java,v 1.1 2006/02/21 01:18:42 vauchers Exp $
 */

package pcgen.gui.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import pcgen.core.Constants;
import pcgen.core.Globals;
import pcgen.core.PCClass;
import pcgen.core.PCStat;
import pcgen.core.PObject;
import pcgen.core.SystemCollections;
import pcgen.gui.utils.JComboBoxEx;

/**
 * <code>ClassAbilityPanel</code>
 *
 * @author  Bryan McRoberts <merton.monk@codemonkeypublishing.com>
 */

public class ClassAbilityPanel extends JPanel implements PObjectUpdater
{

	private JTextField attackCycle = new JTextField();
	private JTextField hitDice = new JTextField();
	private JTextField maxLevel = new JTextField();
	private JTextField deity = new JTextField();
	private JTextField itemCreate = new JTextField();
	private JTextField extraFeats = new JTextField();
	private JTextField levelsPerFeat = new JTextField();
	private JTextField castAs = new JTextField();
	private JTextField knownSpells = new JTextField();
	private JCheckBox memorize = new JCheckBox();
	private JTextField prohibited = new JTextField();
	private JTextField specialtyKnown = new JTextField();
	private JCheckBox spellBook = new JCheckBox();
	private JTextField spellList = new JTextField();
	//private JTextField spellStat = new JTextField();
	//private JTextField spellType = new JTextField();
	private JComboBoxEx spellStat = new JComboBoxEx();
	private JComboBoxEx spellType = new JComboBoxEx();

	/** Creates new form ClassAbilityPanel */
	public ClassAbilityPanel()
	{
		initComponents();
		initComponentContents();
	}

	private void initComponentContents()
	{
		Iterator e;
		String aString;
		List aList = new ArrayList();
		//
		// Make list of stats
		//
		for (e = SystemCollections.getUnmodifiableStatList().iterator(); e.hasNext();)
		{
			aList.add(((PCStat) e.next()).getAbb());
		}
		aList.remove(Constants.s_NONE);
		Collections.sort(aList);
		aList.add(0, Constants.s_NONE);
		spellStat.setModel(new DefaultComboBoxModel(aList.toArray()));

		//
		// Make list of used spell types
		//
		aList.clear();
		for (e = Globals.getClassList().iterator(); e.hasNext();)
		{
			aString = ((PCClass) e.next()).getSpellType();
			if (!aList.contains(aString))
			{
				aList.add(aString);
			}
		}

		aList.remove(Constants.s_NONE);
		Collections.sort(aList);
		aList.add(0, Constants.s_NONE);
		spellType.setModel(new DefaultComboBoxModel(aList.toArray()));
	}

	/*
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents()
	{
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		JLabel tempLabel;

		setLayout(new GridBagLayout());

		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.WEST;

		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.weightx = 0.16;

		tempLabel = new JLabel("Attack Cycle:");
		gridBagConstraints = buildConstraints(gridBagConstraints, 0, 0, true);
		add(tempLabel, gridBagConstraints);

		gridBagConstraints = buildConstraints(gridBagConstraints, 1, 0, true);
		add(attackCycle, gridBagConstraints);

		tempLabel = new JLabel("Hit Die:");
		gridBagConstraints = buildConstraints(gridBagConstraints, 2, 0, true);
		add(tempLabel, gridBagConstraints);

		gridBagConstraints = buildConstraints(gridBagConstraints, 3, 0, true);
		add(hitDice, gridBagConstraints);

		tempLabel = new JLabel("Max Level:");
		gridBagConstraints = buildConstraints(gridBagConstraints, 4, 0, true);
		add(tempLabel, gridBagConstraints);

		gridBagConstraints = buildConstraints(gridBagConstraints, 5, 0, true);
		gridBagConstraints.weightx = 0.2;
		add(maxLevel, gridBagConstraints);

		gridBagConstraints.weightx = 0.0;

		tempLabel = new JLabel("Bonus Feats:");
		gridBagConstraints = buildConstraints(gridBagConstraints, 0, 1, true);
		add(tempLabel, gridBagConstraints);

		gridBagConstraints = buildConstraints(gridBagConstraints, 1, 1, true);
		add(extraFeats, gridBagConstraints);

		tempLabel = new JLabel("Levels Per Feat:");
		gridBagConstraints = buildConstraints(gridBagConstraints, 2, 1, true);
		add(tempLabel, gridBagConstraints);

		gridBagConstraints = buildConstraints(gridBagConstraints, 3, 1, true);
		add(levelsPerFeat, gridBagConstraints);

		tempLabel = new JLabel("Item Create Mult:");
		gridBagConstraints = buildConstraints(gridBagConstraints, 4, 1, true);
		add(tempLabel, gridBagConstraints);

		gridBagConstraints = buildConstraints(gridBagConstraints, 5, 1, true);
		add(itemCreate, gridBagConstraints);

		tempLabel = new JLabel("Spell Type:");
		gridBagConstraints = buildConstraints(gridBagConstraints, 0, 2, true);
		add(tempLabel, gridBagConstraints);

		spellType.setEditable(true);
		gridBagConstraints = buildConstraints(gridBagConstraints, 1, 2, true);
		add(spellType, gridBagConstraints);

		tempLabel = new JLabel("Spell Stat:");
		gridBagConstraints = buildConstraints(gridBagConstraints, 2, 2, true);
		add(tempLabel, gridBagConstraints);

		spellStat.setEditable(true);
		gridBagConstraints = buildConstraints(gridBagConstraints, 3, 2, true);
		add(spellStat, gridBagConstraints);

		tempLabel = new JLabel("Cast As:");
		gridBagConstraints = buildConstraints(gridBagConstraints, 4, 2, true);
		add(tempLabel, gridBagConstraints);

		gridBagConstraints = buildConstraints(gridBagConstraints, 5, 2, true);
		add(castAs, gridBagConstraints);

		tempLabel = new JLabel("Uses Spell Book:");
		gridBagConstraints = buildConstraints(gridBagConstraints, 0, 3, true);
		add(tempLabel, gridBagConstraints);

		gridBagConstraints = buildConstraints(gridBagConstraints, 1, 3, true);
		add(spellBook, gridBagConstraints);

		tempLabel = new JLabel("Memorizes Spells:");
		gridBagConstraints = buildConstraints(gridBagConstraints, 2, 3, true);
		add(tempLabel, gridBagConstraints);

		gridBagConstraints = buildConstraints(gridBagConstraints, 3, 3, true);
		add(memorize, gridBagConstraints);

		tempLabel = new JLabel("Prohibited:");
		gridBagConstraints = buildConstraints(gridBagConstraints, 4, 3, true);
		add(tempLabel, gridBagConstraints);

		gridBagConstraints = buildConstraints(gridBagConstraints, 5, 3, true);
		add(prohibited, gridBagConstraints);

		tempLabel = new JLabel("Specialty Known:");
		gridBagConstraints = buildConstraints(gridBagConstraints, 0, 4, true);
		add(tempLabel, gridBagConstraints);

		gridBagConstraints = buildConstraints(gridBagConstraints, 1, 4, true);
		gridBagConstraints.gridwidth = 2;
		add(specialtyKnown, gridBagConstraints);

		tempLabel = new JLabel("Spell List:");
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints = buildConstraints(gridBagConstraints, 3, 4, true);
		add(tempLabel, gridBagConstraints);

		gridBagConstraints = buildConstraints(gridBagConstraints, 4, 4, true);
		gridBagConstraints.gridwidth = 2;
		add(spellList, gridBagConstraints);

		tempLabel = new JLabel("Known Spells:");
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints = buildConstraints(gridBagConstraints, 0, 5, true);
		add(tempLabel, gridBagConstraints);

		gridBagConstraints = buildConstraints(gridBagConstraints, 1, 5, true);
		gridBagConstraints.gridwidth = 5;
		add(knownSpells, gridBagConstraints);

	}

	private static GridBagConstraints buildConstraints(GridBagConstraints gridBagConstraints, int gridx, int gridy, boolean useInsets)
	{
		gridBagConstraints.gridx = gridx;
		gridBagConstraints.gridy = gridy;
		if (useInsets)
		{
			gridBagConstraints.insets = new Insets(2, 5, 2, 5);
		}
		return gridBagConstraints;
	}

	public void updateData(PObject po)
	{
		if (!(po instanceof PCClass))
		{
			return;
		}
		PCClass obj = (PCClass) po;
		String a = attackCycle.getText().trim();
		if (a.length() > 0)
		{
			obj.setAttackCycle(a);
		}
		a = hitDice.getText().trim();
		if (a.length() > 0)
		{
			obj.setHitDie(Integer.parseInt(a));
		}
		a = deity.getText().trim();
		if (a.length() > 0)
		{
			obj.setDeityString(a);
		}

		a = itemCreate.getText().trim();
		obj.setItemCreationMultiplier(a);

		a = extraFeats.getText().trim();
		if (a.length() > 0)
		{
			obj.setInitialFeats(Integer.parseInt(a));
		}
		a = levelsPerFeat.getText().trim();
		if (a.length() > 0)
		{
			obj.setLevelsPerFeat(Integer.parseInt(a));
		}
		a = castAs.getText().trim();
		if (a.length() > 0)
		{
			obj.setCastAs(a);
		}
		a = knownSpells.getText().trim();
		if (a.length() > 0)
		{
			obj.addKnownSpellsList(a);
		}
		obj.setMemorizeSpells(memorize.getSelectedObjects() != null);
		a = prohibited.getText().trim();
		if (a.length() > 0)
		{
			obj.setProhibitedString(a);
		}
		a = specialtyKnown.getText().trim();
		if (a.length() > 0)
		{
			obj.getSpecialtyKnownList().add(a);
		}
		obj.setSpellBookUsed(spellBook.getSelectedObjects() != null);
		a = spellList.getText().trim();
		if (a.length() > 0)
		{
			obj.setSpellLevelString(a);
		}

		//a = spellStat.getText().trim();
		//if (a.length() > 0)
		//{
		//	obj.setSpellBaseStat(a);
		//}
		//a = spellType.getText().trim();
		//if (a.length() > 0)
		//{
		//	obj.setSpellType(a);
		//}
		obj.setSpellBaseStat(Constants.s_NONE);
		a = (String) spellStat.getSelectedItem();
		if ((a != null) && (a.length() > 0))
		{
			obj.setSpellBaseStat(a);
		}

		obj.setSpellType(Constants.s_NONE);
		a = (String) spellType.getSelectedItem();
		if ((a != null) && (a.length() > 0))
		{
			obj.setSpellType(a);
		}

		a = maxLevel.getText().trim();
		if (a.length() > 0)
		{
			obj.setMaxLevel(Integer.valueOf(a).intValue());
		}
	}

	public void updateView(PObject po)
	{
		if (!(po instanceof PCClass))
		{
			return;
		}
		PCClass obj = (PCClass) po;
		attackCycle.setText(obj.getAttackCycle());
		hitDice.setText(String.valueOf(obj.getHitDieUnadjusted()));
		deity.setText(obj.getDeityString());
		itemCreate.setText(obj.getItemCreationMultiplier());
		extraFeats.setText(String.valueOf(obj.getInitialFeats()));
		levelsPerFeat.setText(String.valueOf(obj.getLevelsPerFeat()));
		castAs.setText(obj.getCastAs());
//		knownSpells.setText( requires a list!
		memorize.setSelected(obj.getMemorizeSpells());
		prohibited.setText(obj.getProhibitedString());
//		specialtyKnown.setText( requires a list!
		spellBook.setSelected(obj.getSpellBookUsed());
		spellList.setText(obj.getSpellLevelString());
		//spellStat.setText(obj.getSpellBaseStat());
		//spellType.setText(obj.getSpellType());
		spellStat.setSelectedItem(obj.getSpellBaseStat());
		spellType.setSelectedItem(obj.getSpellType());
		maxLevel.setText(String.valueOf(obj.getMaxLevel()));
	}
}

