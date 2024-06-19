package gmgen.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import pcgen.core.Constants;
import pcgen.core.Deity;
import pcgen.core.Domain;
import pcgen.core.Equipment;
import pcgen.core.Feat;
import pcgen.core.Globals;
import pcgen.core.PCClass;
import pcgen.core.PlayerCharacter;
import pcgen.core.SettingsHandler;
import pcgen.core.Skill;
import pcgen.core.StatList;
import pcgen.core.SystemCollections;
import pcgen.io.ExportHandler;

public class PlayerCharacterOutput {
	private PlayerCharacter pc;

	public PlayerCharacterOutput(PlayerCharacter pc) {
		this.pc = pc;
		Globals.setCurrentPC(pc);
	}

	public String getAC() {
		return pc.getACTotal() + "";
	}

	public String getACFlatFooted() {
		return pc.flatfootedAC() + "";
	}

	public String getACTouch() {
		return pc.touchAC() + "";
	}

	public String getAlignmentLong() {
		return SystemCollections.getLongAlignmentAtIndex(pc.getAlignment());
	}

	public String getAlignmentShort() {
		return SystemCollections.getShortAlignmentAtIndex(pc.getAlignment());
	}

	public String getBAB() {
		return pc.baseAttackBonus() + "";
	}

	public String getMeleeTotal() {
		int tohitBonus = (int) pc.getTotalBonusTo("TOHIT", "TOHIT") +
			(int) pc.getTotalBonusTo("TOHIT", "TYPE.MELEE") +
			(int) pc.getTotalBonusTo("COMBAT", "TOHIT") +
			(int) pc.getTotalBonusTo("COMBAT", "TOHIT.MELEE");
		return pc.getAttackString(Constants.ATTACKSTRING_MELEE, tohitBonus);
	}

	public String getRangedTotal() {
		int tohitBonus = (int) pc.getTotalBonusTo("TOHIT", "TOHIT") +
			(int) pc.getTotalBonusTo("TOHIT", "TYPE.RANGED") +
			(int) pc.getTotalBonusTo("COMBAT", "TOHIT") +
			(int) pc.getTotalBonusTo("COMBAT", "TOHIT.RANGED");
		return pc.getAttackString(Constants.ATTACKSTRING_MELEE, tohitBonus);
	}


	public String getClasses() {
		StringBuffer sb = new StringBuffer();
		ArrayList classList = pc.getClassList();
		for(int i = 0; i < classList.size(); i++) {
			PCClass mClass = (PCClass) classList.get(i);
			sb.append(mClass.getName() + mClass.getLevel() + " ");
		}
		return sb.toString();
	}

	public String getCR() {
		return pc.calcCR() + "";
	}

	public String getCritterType() {
		return pc.getCritterType();
	}

	public String getDeity() {
		Deity deity = pc.getDeity();
		if(deity != null) {
			return deity.getOutputName();
		}
		return null;
	}

	public String getDomainName(Domain domain) {
		return domain.getName();
	}

	public String getDomainPower(Domain domain) {
		return domain.piDescString();
	}

	public String getEquipmentList() {
		StringBuffer sb = new StringBuffer();
		List eqList = pc.getEquipmentListInOutputOrder();
		boolean firstLine = true;
		for(int i = 0; i < eqList.size(); i++) {
			Equipment eq = (Equipment) eqList.get(i);
			if(!firstLine) {
				sb.append(", ");
			}
			firstLine = false;
			DecimalFormat formater = new DecimalFormat();
			formater.setMaximumFractionDigits(1);
			formater.setMinimumFractionDigits(0);
			sb.append(formater.format(eq.getQty()) + " " + eq.getName());
		}
		return sb.toString();
	}

	public String getExportToken(String token) {
		StringWriter retWriter = new StringWriter();
		BufferedWriter bufWriter = new BufferedWriter(retWriter);
		ExportHandler export = new ExportHandler(new File(""));
		export.replaceTokenSkipMath(pc, token, bufWriter);
		retWriter.flush();
		try {
			bufWriter.flush();
		}
		catch (IOException e) {
		}
		return retWriter.toString();
	}

	public String getFeatList() {
		StringBuffer sb = new StringBuffer();
		ArrayList featList = pc.getFeatList();
		boolean firstLine = true;
		for(int i = 0; i < featList.size(); i++) {
			Feat feat = (Feat) featList.get(i);
			if(!firstLine) {
				sb.append(", ");
			}
			firstLine = false;
			sb.append(feat.qualifiedName());
		}
		return sb.toString();
	}

	public String getGender() {
		return pc.getGender();
	}

	public String getHitDice() {
		return getExportToken("HITDICE");
	}

	public String getHitPoints() {
		return pc.hitPoints() + "";
	}

	public String getInitMiscMod() {
		StatList sl = pc.getStatList();
		int statMod = sl.getStatModFor("DEX");
		int miscMod = pc.initiativeMod() - statMod;
		return "+" + miscMod;
	}

	public String getInitStatMod() {
		StatList sl = pc.getStatList();
		int statMod = sl.getStatModFor("DEX");
		return "+" + statMod;
	}

	public String getInitTotal() {
		return "+" + pc.initiativeMod();
	}

	public String getName() {
		return pc.getName();
	}

	public String getRace() {
		return pc.getRace().getName();
	}

	public String getRegion() {
		return pc.getRegion();
	}

	public String getSaveFort() {
		return "+" + new Double(pc.getBonus(1, true)).intValue();
	}

	public String getSaveRef() {
		return "+" + new Double(pc.getBonus(2, true)).intValue();
	}

	public String getSaveWill() {
		return "+" + new Double(pc.getBonus(3, true)).intValue();
	}

	public String getSize() {
		return pc.getSize();
	}

	public String getSkillList() {
		StringBuffer sb = new StringBuffer();
		ArrayList skillList = pc.getSkillListInOutputOrder();
		boolean firstLine = true;
		for(int i = 0; i < skillList.size(); i++) {
			Skill skill = (Skill) skillList.get(i);
			int modSkill = -1;
			if (skill.getKeyStat().compareToIgnoreCase(Constants.s_NONE) != 0) {
				modSkill = skill.modifier().intValue() - pc.getStatList().getStatModFor(skill.getKeyStat());
			}
			if (skill.getTotalRank().intValue() > 0 || modSkill > 0) {
				int temp = skill.modifier().intValue() + skill.getTotalRank().intValue();
				if(!firstLine) {
					sb.append(", ");
				}
				firstLine = false;
				sb.append(skill.getOutputName() + " +" + temp);
			}
		}
		return sb.toString();
	}

	public String getSpecialAbilities() {
		StringBuffer sb = new StringBuffer();
		boolean firstLine = true;
		ArrayList saList = pc.getSpecialAbilityTimesList();
		for(int i = 0; i < saList.size(); i++) {
			if(!firstLine) {
				sb.append(", ");
			}
			firstLine = false;
			sb.append((String) saList.get(i));
		}
		return sb.toString();
	}

	public String getSpeed() {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < pc.getNumberOfMovements(); i++) {
			sb.append(pc.getMovementType(i) + " " + pc.movement(i) + Globals.getDistanceUnit());
		}
		return sb.toString();
	}

	public String getStat(String statAbbrev) {
		StatList sl = pc.getStatList();
		return sl.getTotalStatFor(statAbbrev) + "";
	}

	public StatList getStatList() {
		return pc.getStatList();
	}

	public String getStatMod(String statAbbrev) {
		StatList sl = pc.getStatList();
		return "+" + sl.getStatModFor(statAbbrev);
	}

	public String getVision() {
		return pc.getVision();
	}

	public String getWeaponName(Equipment eq) {
		return eq.getOutputName() + eq.getAppliedName();
	}

	public String getWeaponRange(Equipment eq) {
		return eq.getRange().toString() + Globals.getDistanceUnit();
	}

	public String getWeaponCritRange(int weaponNo) {
		return getExportToken("WEAPON." + weaponNo + ".CRIT");
	}

	public String getWeaponCritMult(int weaponNo) {
		return getExportToken("WEAPON." + weaponNo + ".MULT");
	}

	public String getWeaponDamage(int weaponNo) {
		return getExportToken("WEAPON." + weaponNo + ".DAMAGE");
	}

	public String getWeaponHand(Equipment eq) {
		String location = Equipment.getLocationName(eq.getLocation());
		final int start = location.indexOf('(') + 1; // move past the paren
		if (start > 0) {
			int end = location.indexOf(')', start);
			if (end > 0) {
				location = location.substring(start, end);
			}
		}
		return location;
	}

	public String getWeaponSize(Equipment eq) {
		return eq.getSize();
	}

	public String getWeaponSpecialProperties(Equipment eq) {
		return eq.getSpecialProperties();
	}

	public String getWeaponToHit(int weaponNo) {
		return getExportToken("WEAPON." + weaponNo + ".TOTALHIT");
	}

	public String getWeaponType(Equipment eq) {
		String types = getWeaponType(eq, true);
		if (eq.isDouble()) {
			types += '/' + getWeaponType(eq, false);
		}
		return types;
	}

	public String getWeaponType(Equipment eq, boolean primary) {
		StringBuffer sb = new StringBuffer();
		StringTokenizer aTok = new StringTokenizer(SettingsHandler.getGame().getWeaponTypes(), "|", false);
		while (aTok.countTokens() >= 2) {
			final String aType = aTok.nextToken();
			final String abbrev = aTok.nextToken();
			if (eq.isType(aType, true)) {
				sb.append(abbrev);
			}
		}
		return sb.toString();
	}
}

