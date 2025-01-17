package com.rs.cache.loaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.alex.io.InputStream;
import com.alex.utils.Constants;
import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Skills;
import com.rs.tools.MapInformationParser;
import com.rs.utils.FileUtilities;
import com.rs.utils.ItemExamines;

@SuppressWarnings("unused")
public final class ItemDefinitions {

	private static final ConcurrentHashMap<Integer, ItemDefinitions> itemsDefinitions = new ConcurrentHashMap<Integer, ItemDefinitions>();

	public int id;
	public boolean loaded;

	public int baseModel;
	public String name;

	// model size information
	public int modelScale;
	public int modelPitch;
	public int modelRoll;
	public int modelTranslateX;
	public int modelTranslateY;

	// extra information
	public int stackable;
	public int value;
	public boolean membersOnly;

	// wearing model information
	public int maleEquip1;
	public int femaleEquip1;
	public int maleEquip2;
	public int femaleEquip2;

	// options
	public String[] groundOptions;
	public String[] inventoryOptions;

	// model information
	public int[] originalModelColors;
	public int[] modifiedModelColors;
	public short[] originalTextureColors;
	public short[] modifiedTextureColors;
	public byte[] unknownArray1;
	public byte[] unknownArray3;
	public int[] unknownArray2;
	// extra information, not used for newer items
	public boolean unnoted;

	public int maleEquipModelId3;
	public int femaleEquipModelId3;
	public int unknownInt1;
	public int unknownInt2;
	public int unknownInt3;
	public int unknownInt4;
	public int unknownInt5;
	public int unknownInt6;
	public int certId;
	public int certTemplateId;
	public int[] stackIds;
	public int[] stackAmounts;
	public int unknownInt7;
	public int unknownInt8;
	public int unknownInt9;
	public int unknownInt10;
	public int unknownInt11;
	public int teamId;
	public int lendId;
	public int lendTemplateId;
	public int unknownInt12;
	public int unknownInt13;
	public int unknownInt14;
	public int unknownInt15;
	public int unknownInt16;
	public int unknownInt17;
	public int unknownInt18;
	public int unknownInt19;
	public int unknownInt20;
	public int unknownInt21;
	public int unknownInt22;
	public int unknownInt23;
	public int equipSlot;
	public int equipType;

	// extra added
	public boolean noted;
	public boolean lended;

	public HashMap<Integer, Object> clientScriptData;
	public HashMap<Integer, Integer> itemRequiriments;
	public int[] unknownArray5;
	public int[] unknownArray4;
	public byte[] unknownArray6;

	public byte[] data;

	public static final ItemDefinitions getItemDefinitions(int itemId) {
		ItemDefinitions def = itemsDefinitions.get(itemId);
		if (def == null)
			itemsDefinitions.put(itemId, def = new ItemDefinitions(itemId));
		return def;
	}

	private void setStackable(boolean stackable) {
		this.stackable = stackable ? 1 : 0;
	}

	private void setName(String name) {
		this.name = name;
	}

	public static final void clearItemsDefinitions() {
		itemsDefinitions.clear();
	}

	public ItemDefinitions(int id) {
		this.id = id;
		setDefaultsVariableValues();
		setDefaultOptions();
		loadItemDefinitions();
	}

	public boolean isLoaded() {
		return loaded;
	}

	public final void loadItemDefinitions() {
		data = Cache.STORE.getIndexes()[Constants.ITEM_DEFINITIONS_INDEX].getFile(getArchiveId(), getFileId());
		if (data == null)
			return;
		readOpcodeValues(new InputStream(data));
		if (certTemplateId != -1)
			toNote();
		if (lendTemplateId != -1)
			toLend();
		if (bindTemplateId != -1)
			toBind();
		loaded = true;
	}

	public byte[] getData() {
		return data;
	}

	public void toNote() {
		// ItemDefinitions noteItem; //certTemplateId
		ItemDefinitions realItem = getItemDefinitions(certId);
		membersOnly = realItem.membersOnly;
		value = realItem.value;
		name = realItem.name;
		stackable = 1;
		noted = true;
		clientScriptData = realItem.clientScriptData;
	}

	public void toBind() {
		// ItemDefinitions lendItem; //lendTemplateId
		ItemDefinitions realItem = getItemDefinitions(bindId);
		originalModelColors = realItem.originalModelColors;
		maleEquipModelId3 = realItem.maleEquipModelId3;
		femaleEquipModelId3 = realItem.femaleEquipModelId3;
		teamId = realItem.teamId;
		value = 0;
		membersOnly = realItem.membersOnly;
		name = realItem.name;
		inventoryOptions = new String[5];
		groundOptions = realItem.groundOptions;
		if (realItem.inventoryOptions != null)
			for (int optionIndex = 0; optionIndex < 4; optionIndex++)
				inventoryOptions[optionIndex] = realItem.inventoryOptions[optionIndex];
		inventoryOptions[4] = "Destroy";
		maleEquip1 = realItem.maleEquip1;
		maleEquip2 = realItem.maleEquip2;
		femaleEquip1 = realItem.femaleEquip1;
		femaleEquip2 = realItem.femaleEquip2;
		clientScriptData = realItem.clientScriptData;
		equipSlot = realItem.equipSlot;
		equipType = realItem.equipType;
		this.equipLookHideSlot2 = realItem.equipLookHideSlot2;
	}

	public void toLend() {
		// ItemDefinitions lendItem; //lendTemplateId
		ItemDefinitions realItem = getItemDefinitions(lendId);
		originalModelColors = realItem.originalModelColors;
		maleEquipModelId3 = realItem.maleEquipModelId3;
		femaleEquipModelId3 = realItem.femaleEquipModelId3;
		teamId = realItem.teamId;
		value = 0;
		membersOnly = realItem.membersOnly;
		name = realItem.name;
		inventoryOptions = new String[5];
		groundOptions = realItem.groundOptions;
		if (realItem.inventoryOptions != null)
			for (int optionIndex = 0; optionIndex < 4; optionIndex++)
				inventoryOptions[optionIndex] = realItem.inventoryOptions[optionIndex];
		inventoryOptions[4] = "Discard";
		maleEquip1 = realItem.maleEquip1;
		maleEquip2 = realItem.maleEquip2;
		femaleEquip1 = realItem.femaleEquip1;
		femaleEquip2 = realItem.femaleEquip2;
		clientScriptData = realItem.clientScriptData;
		equipSlot = realItem.equipSlot;
		equipType = realItem.equipType;
		lended = true;
	}

	public int getArchiveId() {
		return getId() >>> 8;
	}

	public int getFileId() {
		return 0xff & getId();
	}

	public boolean isDestroyItem() {
		if (inventoryOptions == null)
			return false;
		for (String option : inventoryOptions) {
			if (option == null)
				continue;
			if (option.equalsIgnoreCase("destroy"))
				return true;
		}
		return false;
	}

	public boolean containsOption(int i, String option) {
		if (inventoryOptions == null || inventoryOptions[i] == null || inventoryOptions.length <= i)
			return false;
		return inventoryOptions[i].equals(option);
	}

	public boolean containsOption(String option) {
		if (inventoryOptions == null)
			return false;
		for (String o : inventoryOptions) {
			if (o == null || !o.equals(option))
				continue;
			return true;
		}
		return false;
	}

	public boolean isWearItem() {
		return equipSlot != -1;
	}

	public boolean isWearItem(boolean male) {
		if (equipSlot < Equipment.SLOT_RING && (male ? getMaleWornModelId1() == -1 : getFemaleWornModelId1() == -1))
			return false;
		return equipSlot != -1;
	}

	public boolean containsInventoryOption(int i, String option) {
		if (inventoryOptions == null || inventoryOptions[i] == null || inventoryOptions.length <= i)
			return false;
		return inventoryOptions[i].equals(option);
	}

	public int getStageOnDeath() {
		if (clientScriptData == null)
			return 0;
		Object protectedOnDeath = clientScriptData.get(1397);
		if (protectedOnDeath != null && protectedOnDeath instanceof Integer)
			return (Integer) protectedOnDeath;
		return 0;
	}

	public boolean hasSpecialBar() {
		if (clientScriptData == null)
			return false;
		Object specialBar = clientScriptData.get(686);
		if (specialBar != null && specialBar instanceof Integer)
			return (Integer) specialBar == 1;
		return false;
	}

	public int getAttackSpeed() {
		if (clientScriptData == null)
			return 4;
		Object attackSpeed = clientScriptData.get(14);
		if (attackSpeed != null && attackSpeed instanceof Integer)
			return (int) attackSpeed;
		return 4;
	}

	public int getStabAttack() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(0);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getSlashAttack() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(1);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getCrushAttack() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(2);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getMagicAttack() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(3);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getRangeAttack() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(4);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getStabDef() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(5);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getSlashDef() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(6);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getCrushDef() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(7);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getMagicDef() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(8);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getRangeDef() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(9);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getSummoningDef() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(417);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getAbsorveMeleeBonus() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(967);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getAbsorveMageBonus() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(969);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getAbsorveRangeBonus() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(968);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getStrengthBonus() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(641);
		if (value != null && value instanceof Integer)
			return (int) value / 10;
		return 0;
	}

	public int getRangedStrBonus() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(643);
		if (value != null && value instanceof Integer)
			return (int) value / 10;
		return 0;
	}

	public int getMagicDamage() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(685);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getPrayerBonus() {
		if (id > 25439 || clientScriptData == null)
			return 0;
		Object value = clientScriptData.get(11);
		if (value != null && value instanceof Integer)
			return (int) value;
		return 0;
	}

	public int getRenderAnimId() {
		if(id == 23680 )//&& id <= 23682)
			return 2574;
		if (clientScriptData == null)
			return 1284;
		Object animId = clientScriptData.get(644);
		if (animId != null && animId instanceof Integer)
			return (Integer) animId;
		return 1284;
	}

	public int getModelZoom() {
		return modelScale;
	}

	public int getModelOffset1() {
		return modelTranslateX;
	}

	public int getModelOffset2() {
		return modelTranslateY;
	}

	public HashMap<Integer, Integer> getWearingSkillRequiriments() {
		if (clientScriptData == null)
			return null;
		if (itemRequiriments == null) {
			HashMap<Integer, Integer> skills = new HashMap<Integer, Integer>();
			for (int i = 0; i < 10; i++) {
				Integer skill = (Integer) clientScriptData.get(749 + (i * 2));
				if (skill != null) {
					Integer level = (Integer) clientScriptData.get(750 + (i * 2));
					if (level != null)
						skills.put(skill, level);
				}
			}
			Integer maxedSkill = (Integer) clientScriptData.get(277);
			if (maxedSkill != null)
				skills.put(maxedSkill, getId() == 19709 ? 120 : 99);
			itemRequiriments = skills;

			if (name.toLowerCase().contains("berserker helm"))
				itemRequiriments.put(Skills.DEFENCE, 45);
			else if (name.toLowerCase().contains("helm of neitiznot"))
				itemRequiriments.put(Skills.DEFENCE, 55);
			else if (name.toLowerCase().startsWith("body "))
				itemRequiriments.put(Skills.DEFENCE, 33);
			else if (name.toLowerCase().startsWith("cosmic "))
				itemRequiriments.put(Skills.DEFENCE, 40);
			else if (name.toLowerCase().startsWith("chaos "))
				itemRequiriments.put(Skills.DEFENCE, 50);
			switch (getId()) {
			case 21371:
			case 21372:
			case 21373:
			case 21374:
			case 21375:
				itemRequiriments.put(Skills.ATTACK, 85);
				break;
			case 10887:
				itemRequiriments.put(Skills.PRAYER, 51);
				break;
			case 7460:
				itemRequiriments.put(Skills.DEFENCE, 13);
				break;
			case 7461:
				itemRequiriments.put(Skills.DEFENCE, 35);
				break;
			case 7462:
				itemRequiriments.put(Skills.DEFENCE, 35);
				break;
			case 12674:
			case 12675:
				itemRequiriments.put(Skills.DEFENCE, 45);
				break;
			case 12680:
			case 12681:
				itemRequiriments.put(Skills.DEFENCE, 55);
				break;
			case 10828:
				itemRequiriments.put(Skills.CONSTRUCTION, 20);
				itemRequiriments.put(Skills.WOODCUTTING, 54);
				itemRequiriments.put(Skills.CRAFTING, 46);
				itemRequiriments.put(Skills.AGILITY, 40);
				break;
			case 2412:
			case 2413:
			case 2414:
				itemRequiriments.put(Skills.MAGIC, 60);
				break;
			case 19784:
			case 22401:
			case 19780: // Korasi
				itemRequiriments.put(Skills.ATTACK, 78);
				itemRequiriments.put(Skills.STRENGTH, 78);
				itemRequiriments.put(Skills.MAGIC, 80);
				itemRequiriments.put(Skills.DEFENCE, 10);
				itemRequiriments.put(Skills.SUMMONING, 55);
				break;
			case 20822:
			case 20823:
			case 20824:
			case 20825:
			case 20826:
				itemRequiriments.put(Skills.DEFENCE, 99);
				break;
			case 1377:
			case 1434:
				itemRequiriments.put(Skills.DEFENCE, 28);
				break;
			case 8846:
				itemRequiriments.put(0, 5);
				itemRequiriments.put(1, 5);
				break;
			case 8847:
				itemRequiriments.put(Skills.ATTACK, 10);
				itemRequiriments.put(Skills.DEFENCE, 10);
				break;
			case 8848:
				itemRequiriments.put(Skills.ATTACK, 20);
				itemRequiriments.put(Skills.DEFENCE, 20);
				break;
			case 8849:
				itemRequiriments.put(Skills.ATTACK, 30);
				itemRequiriments.put(Skills.DEFENCE, 30);
				break;
			case 8850:
				itemRequiriments.put(Skills.ATTACK, 40);
				itemRequiriments.put(Skills.DEFENCE, 40);
				break;
			case 20072:
				itemRequiriments.put(Skills.ATTACK, 60);
				itemRequiriments.put(Skills.DEFENCE, 60);
				break;
			case 8839:
			case 8840:
			case 8841:
			case 8842:
			case 11663:
			case 11664:
			case 11665:
			case 11674:
			case 11675:
			case 11676:
				itemRequiriments.put(Skills.DEFENCE, 42);
				itemRequiriments.put(Skills.HITPOINTS, 42);
				itemRequiriments.put(Skills.RANGE, 42);
				itemRequiriments.put(Skills.ATTACK, 42);
				itemRequiriments.put(Skills.MAGIC, 42);
				itemRequiriments.put(Skills.STRENGTH, 42);
				break;
			case 19785:
			case 19786:
			case 19787:
			case 19788:
			case 19789:
			case 19790:
				itemRequiriments.put(Skills.ATTACK, 78);
				itemRequiriments.put(Skills.STRENGTH, 78);
				itemRequiriments.put(Skills.MAGIC, 80);
				itemRequiriments.put(Skills.HITPOINTS, 42);
				itemRequiriments.put(Skills.RANGE, 42);
				itemRequiriments.put(Skills.PRAYER, 22);
				break;
			}
		}

		return itemRequiriments;
	}

	public void setDefaultOptions() {
		groundOptions = new String[] { null, null, "take", null, null };
		inventoryOptions = new String[] { null, null, null, null, "drop" };
	}

	public void setDefaultsVariableValues() {
		name = "null";
		maleEquip1 = -1;
		maleEquip2 = -1;
		femaleEquip1 = -1;
		femaleEquip2 = -1;
		modelScale = 2000;
		lendId = -1;
		lendTemplateId = -1;
		certId = -1;
		certTemplateId = -1;
		unknownInt9 = 128;
		value = 1;
		maleEquipModelId3 = -1;
		femaleEquipModelId3 = -1;
		bindTemplateId = -1;
		bindId = -1;
		teamId = -1;
		equipSlot = -1;
		equipType = -1;
		equipLookHideSlot2 = -1;
	}

	public final void readValues(InputStream stream, int opcode) {
		if (opcode == 1)
			baseModel = stream.readBigSmart();
		else if (opcode == 2)
			name = stream.readString();
		else if (opcode == 4)
			modelScale = stream.readUnsignedShort();
		else if (opcode == 5)
			modelPitch = stream.readUnsignedShort();
		else if (opcode == 6)
			modelRoll = stream.readUnsignedShort();
		else if (opcode == 7) {
			modelTranslateX = stream.readUnsignedShort();
			if (modelTranslateX > 32767)
				modelTranslateX -= 65536;
			modelTranslateX <<= 0;
		} else if (opcode == 8) {
			modelTranslateY = stream.readUnsignedShort();
			if (modelTranslateY > 32767)
				modelTranslateY -= 65536;
			modelTranslateY <<= 0;
		} else if (opcode == 11)
			stackable = 1;
		else if (opcode == 12)
			value = stream.readInt();
		else if (opcode == 13) {
			equipSlot = stream.readUnsignedByte();
		} else if (opcode == 14) {
			equipType = stream.readUnsignedByte();
		} else if (opcode == 16)
			membersOnly = true;
		else if (opcode == 18) { // added
			stream.readUnsignedShort();
		} else if (opcode == 23)
			maleEquip1 = stream.readBigSmart();
		else if (opcode == 24)
			maleEquip2 = stream.readBigSmart();
		else if (opcode == 25)
			femaleEquip1 = stream.readBigSmart();
		else if (opcode == 26)
			femaleEquip2 = stream.readBigSmart();
		else if (opcode == 27)
			equipLookHideSlot2 = stream.readUnsignedByte();
		else if (opcode >= 30 && opcode < 35)
			groundOptions[opcode - 30] = stream.readString();
		else if (opcode >= 35 && opcode < 40)
			inventoryOptions[opcode - 35] = stream.readString();
		else if (opcode == 40) {
			int length = stream.readUnsignedByte();
			originalModelColors = new int[length];
			modifiedModelColors = new int[length];
			for (int index = 0; index < length; index++) {
				originalModelColors[index] = stream.readUnsignedShort();
				modifiedModelColors[index] = stream.readUnsignedShort();
			}
		} else if (opcode == 41) {
			int length = stream.readUnsignedByte();
			originalTextureColors = new short[length];
			modifiedTextureColors = new short[length];
			for (int index = 0; index < length; index++) {
				originalTextureColors[index] = (short) stream.readUnsignedShort();
				modifiedTextureColors[index] = (short) stream.readUnsignedShort();
			}
		} else if (opcode == 42) {
			int length = stream.readUnsignedByte();
			unknownArray1 = new byte[length];
			for (int index = 0; index < length; index++)
				unknownArray1[index] = (byte) stream.readByte();
		} else if (opcode == 44) {
			int length = stream.readUnsignedShort();
			int arraySize = 0;
			for (int modifier = 0; modifier > 0; modifier++) {
				arraySize++;
				unknownArray3 = new byte[arraySize];
				byte offset = 0;
				for (int index = 0; index < arraySize; index++) {
					if ((length & 1 << index) > 0) {
						unknownArray3[index] = offset;
					} else {
						unknownArray3[index] = -1;
					}
				}
			}
		} else if (45 == opcode) {
			int i_97_ = (short) stream.readUnsignedShort();
			int i_98_ = 0;
			for (int i_99_ = i_97_; i_99_ > 0; i_99_ >>= 1)
				i_98_++;
			unknownArray6 = new byte[i_98_];
			byte i_100_ = 0;
			for (int i_101_ = 0; i_101_ < i_98_; i_101_++) {
				if ((i_97_ & 1 << i_101_) > 0) {
					unknownArray6[i_101_] = i_100_;
					i_100_++;
				} else
					unknownArray6[i_101_] = (byte) -1;
			}
		} else if (opcode == 65)
			unnoted = true;
		else if (opcode == 78)
			maleEquipModelId3 = stream.readBigSmart();
		else if (opcode == 79)
			femaleEquipModelId3 = stream.readBigSmart();
		else if (opcode == 90)
			unknownInt1 = stream.readBigSmart();
		else if (opcode == 91)
			unknownInt2 = stream.readBigSmart();
		else if (opcode == 92)
			unknownInt3 = stream.readBigSmart();
		else if (opcode == 93)
			unknownInt4 = stream.readBigSmart();
		else if (opcode == 94) {// new
			int anInt7887 = stream.readUnsignedShort();
		} else if (opcode == 95)
			unknownInt5 = stream.readUnsignedShort();
		else if (opcode == 96)
			unknownInt6 = stream.readUnsignedByte();
		else if (opcode == 97)
			certId = stream.readUnsignedShort();
		else if (opcode == 98)
			certTemplateId = stream.readUnsignedShort();
		else if (opcode >= 100 && opcode < 110) {
			if (stackIds == null) {
				stackIds = new int[10];
				stackAmounts = new int[10];
			}
			stackIds[opcode - 100] = stream.readUnsignedShort();
			stackAmounts[opcode - 100] = stream.readUnsignedShort();
		} else if (opcode == 110)
			unknownInt7 = stream.readUnsignedShort();
		else if (opcode == 111)
			unknownInt8 = stream.readUnsignedShort();
		else if (opcode == 112)
			unknownInt9 = stream.readUnsignedShort();
		else if (opcode == 113)
			unknownInt10 = stream.readByte();
		else if (opcode == 114)
			unknownInt11 = stream.readByte() * 5;
		else if (opcode == 115)
			teamId = stream.readUnsignedByte();
		else if (opcode == 121)
			lendId = stream.readUnsignedShort();
		else if (opcode == 122)
			lendTemplateId = stream.readUnsignedShort();
		else if (opcode == 125) {
			unknownInt12 = stream.readByte() << 2;
			unknownInt13 = stream.readByte() << 2;
			unknownInt14 = stream.readByte() << 2;
		} else if (opcode == 126) {
			unknownInt15 = stream.readByte() << 2;
			unknownInt16 = stream.readByte() << 2;
			unknownInt17 = stream.readByte() << 2;
		} else if (opcode == 127) {
			unknownInt18 = stream.readUnsignedByte();
			unknownInt19 = stream.readUnsignedShort();
		} else if (opcode == 128) {
			unknownInt20 = stream.readUnsignedByte();
			unknownInt21 = stream.readUnsignedShort();
		} else if (opcode == 129) {
			unknownInt20 = stream.readUnsignedByte();
			unknownInt21 = stream.readUnsignedShort();
		} else if (opcode == 130) {
			unknownInt22 = stream.readUnsignedByte();
			unknownInt23 = stream.readUnsignedShort();
		} else if (opcode == 132) {
			int length = stream.readUnsignedByte();
			unknownArray2 = new int[length];
			for (int index = 0; index < length; index++)
				unknownArray2[index] = stream.readUnsignedShort();
		} else if (opcode == 134) {
			int unknownValue = stream.readUnsignedByte();
		} else if (opcode == 139) {
			bindId = stream.readUnsignedShort();
		} else if (opcode == 140) {
			bindTemplateId = stream.readUnsignedShort();
		} else if (opcode >= 142 && opcode < 147) {
			if (unknownArray4 == null) {
				unknownArray4 = new int[6];
				Arrays.fill(unknownArray4, -1);
			}
			unknownArray4[opcode - 142] = stream.readUnsignedShort();
		} else if (opcode >= 150 && opcode < 155) {
			if (null == unknownArray5) {
				unknownArray5 = new int[5];
				Arrays.fill(unknownArray5, -1);
			}
			unknownArray5[opcode - 150] = stream.readUnsignedShort();
		} else if (opcode == 156) { // new

		} else if (157 == opcode) {// new
			boolean aBool7955 = true;
		} else if (161 == opcode) {// new
			int anInt7904 = stream.readUnsignedShort();
		} else if (162 == opcode) {// new
			int anInt7923 = stream.readUnsignedShort();
		} else if (163 == opcode) {// new
			int anInt7939 = stream.readUnsignedShort();
		} else if (164 == opcode) {// new coinshare shard
			String aString7902 = stream.readString();
		} else if (opcode == 165) {// new
			stackable = 2;
		} else if (opcode == 242) {
			int oldInvModel = stream.readBigSmart();
		} else if (opcode == 243) {
			int oldMaleEquipModelId3 = stream.readBigSmart();
		} else if (opcode == 244) {
			int oldFemaleEquipModelId3 = stream.readBigSmart();
		} else if (opcode == 245) {
			int oldMaleEquipModelId2 = stream.readBigSmart();
		} else if (opcode == 246) {
			int oldFemaleEquipModelId2 = stream.readBigSmart();
		} else if (opcode == 247) {
			int oldMaleEquipModelId1 = stream.readBigSmart();
		} else if (opcode == 248) {
			int oldFemaleEquipModelId1 = stream.readBigSmart();
		} else if (opcode == 251) {
			int length = stream.readUnsignedByte();
			int[] oldoriginalModelColors = new int[length];
			int[] oldmodifiedModelColors = new int[length];
			for (int index = 0; index < length; index++) {
				oldoriginalModelColors[index] = stream.readUnsignedShort();
				oldmodifiedModelColors[index] = stream.readUnsignedShort();
			}
		} else if (opcode == 252) {
			int length = stream.readUnsignedByte();
			short[] oldoriginalTextureColors = new short[length];
			short[] oldmodifiedTextureColors = new short[length];
			for (int index = 0; index < length; index++) {
				oldoriginalTextureColors[index] = (short) stream.readUnsignedShort();
				oldmodifiedTextureColors[index] = (short) stream.readUnsignedShort();
			}
		} else if (opcode == 249) {
			int length = stream.readUnsignedByte();
			if (clientScriptData == null)
				clientScriptData = new HashMap<Integer, Object>(length);
			for (int index = 0; index < length; index++) {
				boolean stringInstance = stream.readUnsignedByte() == 1;
				int key = stream.read24BitInt();
				Object value = stringInstance ? stream.readString() : stream.readInt();
				clientScriptData.put(key, value);
			}
		} else {
			if (Settings.DEBUG)
				throw new RuntimeException("MISSING OPCODE " + opcode + " FOR ITEM " + getId());
		}
	}

	public int bindTemplateId;
	public int bindId;

	public final void readOpcodeValues(InputStream stream) {
		while (true) {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0)
				break;
			readValues(stream, opcode);
		}
	}

	public String getName() {
		return name;
	}

	public int getFemaleWornModelId1() {
		return femaleEquip1;
	}

	public int getFemaleWornModelId2() {
		return femaleEquip2;
	}

	public int getFemaleWornModelId3() {
		return femaleEquipModelId3;
	}

	public int getMaleWornModelId1() {
		return maleEquip1;
	}

	public int getMaleWornModelId2() {
		return maleEquip2;
	}

	public int getMaleWornModelId3() {
		return maleEquipModelId3;
	}

	public boolean isOverSized() {
		return modelScale > 5000;
	}

	public boolean isLended() {
		return lended;
	}

	public boolean isMembersOnly() {
		return membersOnly;
	}

	public boolean isStackable() {
		return stackable == 1 || id == 0;
	}

	public boolean isNoted() {
		return noted;
	}

	public int getLendId() {
		return lendId;
	}

	public int getCertId() {
		return certId;
	}

	public int getValue() {
		return value;
	}

	public int getId() {
		return id;
	}

	public int getEquipSlot() {
		return equipSlot;
	}

	public int getEquipType() {
		return equipType;
	}

	public String getExamine() {
		return ItemExamines.getExamine(new Item(id));
	}

	public void setValue(int value) {
		this.value = value;
	}

	public static int getEquipType(String name) {
		if (name.contains("sword") || name.contains("dagger") || name.contains("scimitar") || name.contains("whip")
				|| name.contains("spear") || name.contains("mace") || name.contains("battleaxe")
				|| name.contains("staff") || name.contains("Staff") || name.contains("battleaxe")
				|| name.contains("hatchet") || name.contains("pickaxe") || name.contains("axe") || name.contains("wand")
				|| name.contains("katana") || name.contains("Katana") || name.contains("scythe")
				|| name.contains("maul")) {
			return 11;
		}
		if (name.contains("plate") || name.contains("body") || name.contains("apron") || name.contains("chest")
				|| name.contains("top")) {
			return 3;
		}
		if (name.contains("gloves")) {
			return 5;
		}
		if (name.contains("boots")) {
			return 6;
		}
		if (name.contains("skirt") || name.contains("legs") || name.contains("bottom")) {
			return 4;
		}
		if (name.contains("helm") || name.contains("hat") || name.contains("hood")) {
			return 0;
		}
		if (name.contains("shield") || name.contains("ket-xil") || name.equalsIgnoreCase("book")
				|| name.contains("defender") || name.contains("teddy") || name.contains("vyre'lector")) {
			return 9;
		}
		if (name.contains("cape") || name.contains("Cape")) {
			return 1;
		}
		if (name.contains("neck")) {
			return 2;
		}
		if (name.contains("arrow") || name.contains("bolt")) {
			return 7;
		}
		if (name.contains("bow")) {
			return 12;
		}
		return -1;
	}

	public int getSheatheModelId() {
		if (clientScriptData == null)
			return -1;
		Object modelId = clientScriptData.get(2820);
		if (modelId != null && modelId instanceof Integer)
			return (Integer) modelId;
		return -1;
	}

	public GeneralRequirementMap getCombatMap() {
		int csMapOpcode = getCSOpcode(686);
		if (csMapOpcode != 0)
			return GeneralRequirementMap.getMap(csMapOpcode);
		return null;
	}

	public int getCSOpcode(int opcode) {
		if (clientScriptData != null) {
			Object value = clientScriptData.get(opcode);
			if (value != null && value instanceof Integer)
				return (int) value;
		}
		return 0;
	}

	public int getCombatOpcode(int opcode) {
		Integer value = (Integer) clientScriptData.get(opcode);
		if (value != null)
			return value;
		GeneralRequirementMap map = getCombatMap();
		return map == null ? 0 : map.getIntValue(opcode);
	}

	public int getCombatStyle() {
		return getCombatOpcode(2853);
	}

	public boolean isMeleeTypeGear() {
		return getCSOpcode(2821) == 1;
	}

	public boolean isRangeTypeGear() {
		return getCSOpcode(2822) == 1;
	}

	public boolean isMagicTypeGear() {
		return getCSOpcode(2823) == 1;
	}

	public boolean isAllTypeGear() {
		return getCSOpcode(2824) == 1;
	}

	public boolean isMeleeTypeWeapon() {
		return getCSOpcode(2825) == 1;
	}

	public boolean isRangeTypeWeapon() {
		return getCSOpcode(2826) == 1;
	}

	public boolean isMagicTypeWeapon() {
		return getCSOpcode(2827) == 1;
	}
	
	public int getTipitPrice() {
		try {
			for (String lines : FileUtilities.readFile("./data/items/prices.txt")) {
				String[] data = lines.split(" - ");
				if (Integer.parseInt(data[0]) == id)
					return Integer.parseInt(data[1]);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 1;
	}

	public List<Item> getCreateItemRequirements(boolean infusingScroll) {
		if (clientScriptData == null)
			return null;
		List<Item> items = new ArrayList<Item>();
		int requiredId = -1;
		int requiredAmount = -1;
		for (int key : clientScriptData.keySet()) {
			Object value = clientScriptData.get(key);
			if (value instanceof String)
				continue;
			if (key >= 2656 && key <= 2665) {
				if(key == 2656 || key == 2657 || key == 2658 ){
					requiredId = (Integer) value;
					requiredAmount = 1;
				} else if (key == 2665)
				{
					requiredAmount = (Integer) value;
					requiredId = 12183;
				}
				if (requiredId != -1 && requiredAmount != -1) {
					if (infusingScroll) {
						requiredId = getId();
						requiredAmount = 1;
					}
				/*	if (items.size() == 0 && !infusingScroll)
						items.add(new Item(requiredAmount, 1));
					else*/
						items.add(new Item(requiredId, requiredAmount));
					requiredId = -1;
					requiredAmount = -1;
					if (infusingScroll) {
						break;
					}
				}
			}
		}
		return items;
	}

	public int getSpecialAttackAmount() {
		if (clientScriptData == null)
			return 0;
		final Object data = clientScriptData.get(4332);
		if (data != null && data instanceof Integer)
			return (Integer) data;
		return 0;
	}
    /**
     * checks if the 3th option is bind and returns true
     * @return
     */
	public boolean isBindItem() {
		if (inventoryOptions == null)
			return false;
		for (String option : inventoryOptions) {
			if (option == null)
				continue;
			if (option.equalsIgnoreCase("bind"))
				return true;
		}
		return false;
	}

	public double getDungShopValueMultiplier() {
		if (clientScriptData == null)
			return 1;
		Object value = clientScriptData.get(1046);
		if (value != null && value instanceof Integer)
			return ((Integer) value).doubleValue() / 100;
		return 1;
	}

	public int equipLookHideSlot2;

	public int getEquipType2() {
		return equipLookHideSlot2;
	}
	public int getMainhandEmote(final boolean legacy) {
        if (MapInformationParser.getMap() == null || clientScriptData == null) {
            return -1;
        }
        final Integer index = (Integer) clientScriptData.get(686);
        if (index == null) {
            return -1;
        }
        final HashMap<Object, Object> map = MapInformationParser.getMap().get(
                index);
        final Object key = legacy ? 4385 : 2914;// RS3 and legacy == 4385
        if (map != null) {
            if (map.get(key.toString()) != null) {
                Object value = map.get(key.toString());
                return Integer.valueOf(value.toString());
            }
        }
        return -1;
    }

    public int getOffhandEmote(final boolean legacy) {
        if (MapInformationParser.getMap() == null || clientScriptData == null) {
            return -1;
        }
        final Integer index = (Integer) clientScriptData.get(686);
        if (index == null) {
            return -1;
        }
        final HashMap<Object, Object> map = MapInformationParser.getMap().get(
                index);
        final Object key = legacy ? 4389 : 2831;// RS3 is 2831, legacy is 4389
        if (map != null) {
            if (map.get(key.toString()) != null) {
                Object value = map.get(key.toString());
                return Integer.valueOf(value.toString());
            } else {
                return getMainhandEmote(legacy);
            }
        }
        return -1;
    } 


public boolean isShield() {
        return getCSOpcode(2832) == 1;
    }
}