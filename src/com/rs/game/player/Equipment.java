package com.rs.game.player;

import java.io.Serializable;
import java.util.ArrayList;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.minigames.duel.DuelControler;
import com.rs.game.player.actions.Bonfire;
import com.rs.game.player.controlers.DungeonControler;
import com.rs.utils.ItemExamines;
import com.rs.utils.Utils;

public final class Equipment implements Serializable {

	private static final long serialVersionUID = -4147163237095647617L;

	public static final byte SLOT_HAT = 0, SLOT_CAPE = 1, SLOT_AMULET = 2,
			SLOT_WEAPON = 3, SLOT_CHEST = 4, SLOT_SHIELD = 5, SLOT_LEGS = 7,
			SLOT_HANDS = 9, SLOT_FEET = 10, SLOT_RING = 12, SLOT_ARROWS = 13,
			SLOT_AURA = 14;


	private ItemsContainer<Item> items;
	//cosmetic part
	private ItemsContainer<Item> cosmeticItems;
	private ArrayList<Item> keepSakeItems;
	private ArrayList<Cosmetic> savedCosmetics;
	private boolean[] hiddenSlots;
	private int costumeColor;
	private boolean hideAll;

	private transient Player player;
	private transient int equipmentHpIncrease;
	
	public ItemsContainer<Item> getItemsContainer() {
		return items;
	}
	
    public boolean containsOneItem(int... itemIds) {
	for (int itemId : itemIds) {
	    if (items.containsOne(new Item(itemId, 1)))
		return true;
	}
	return false;
    }

	static final int[] DISABLED_SLOTS = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 1, 1, 0 };

	public Equipment() {
		items = new ItemsContainer<Item>(15, false);
		cosmeticItems = new ItemsContainer<>(15, false);
		keepSakeItems = new ArrayList<>(50);
		savedCosmetics = new ArrayList<>();
		hiddenSlots = new boolean[15];
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void init() {
		if (cosmeticItems == null)
			cosmeticItems = new ItemsContainer<>(15, false);
		if (hiddenSlots == null)
			hiddenSlots = new boolean[15];
		if (keepSakeItems == null)
			keepSakeItems = new ArrayList<>(50);
		if (savedCosmetics == null)
			savedCosmetics = new ArrayList<>();
		player.getPackets().sendItems(94, items);
		refresh(null);
	}

	public void refresh(int... slots) {
		if (slots != null) {
			if (player.getTemporaryAttributtes().get("Cosmetics") != null) {
				Item[] cosmetics = items.getItemsCopy();
				for (int i = 0; i < cosmetics.length; i++) {
					Item item = cosmetics[i];
					if (item == null)
						cosmetics[i] = new Item(0);
				}
				player.getPackets().sendUpdateItems(94, cosmetics, slots);
			} else
				player.getPackets().sendUpdateItems(94, items, slots);
			player.getCombatDefinitions().checkAttackStyle();
		}
		player.getCombatDefinitions().refreshBonuses();
		refreshConfigs(slots == null);
	}

	public void reset() {
		items.reset();
		init();
	}

	public Item getItem(int slot) {
		return items.get(slot);
	}

	public void sendExamine(int slotId) {
		Item item = items.get(slotId);
		if (item == null)
			return;
		player.getPackets().sendGameMessage(ItemExamines.getExamine(item));
	}

	public void refreshConfigs(boolean init) {
		double hpIncrease = 0;
		for (int index = 0; index < items.getSize(); index++) {
			Item item = items.get(index);
			if (item == null)
				continue;
			int id = item.getId();
			if (index == Equipment.SLOT_HAT) {
				if (id == 20135 || id == 20137 // torva
						|| id == 20147 || id == 20149 // pernix
						|| id == 20159 || id == 20161 // virtus
						)
					hpIncrease += 66;
			
			} else if (index == Equipment.SLOT_CHEST) {
				if (id == 20139 || id == 20141 // torva
						|| id == 20151 || id == 20153 // pernix
						|| id == 20163 || id == 20165 // virtus
						)
					hpIncrease += 200;

			}
			 else if (index == Equipment.SLOT_LEGS) {
				if (id == 20143 || id == 20145 // torva
						|| id == 20155 || id == 20157 // pernix
						|| id == 20167 || id == 20169 // virtus
						)
					hpIncrease += 134;
				
			}
		}
		
		if(player.getLastBonfire() > 0) {
			int maxhp = player.getSkills().getLevel(Skills.HITPOINTS) * 10;
			hpIncrease += (maxhp * Bonfire.getBonfireBoostMultiplier(player)) - maxhp;
		}
		if(player.getHpBoostMultiplier() != 0) {
			int maxhp = player.getSkills().getLevel(Skills.HITPOINTS) * 10;
			hpIncrease += maxhp*player.getHpBoostMultiplier();
		}
		if (hpIncrease != equipmentHpIncrease) {
			equipmentHpIncrease = (int) hpIncrease;
			if (!init)
				player.refreshHitPoints();
		} 
	}

	public static boolean hideArms(Item item) {
		String name = item.getName().toLowerCase();
		if
		//temp old graphics fix, but bugs alil new ones
		(name.contains("d'hide body")
		|| name.contains("dragonhide body")
		|| name.equals("stripy pirate shirt")
		|| (name.contains("chainbody") 
				&& (name.contains("iron") || name.contains("bronze")
						|| name.contains("steel")
						|| name.contains("black")
						|| name.contains("mithril")
						|| name.contains("adamant")
						|| name.contains("rune")
						|| name.contains("white")))
		|| name.equals("leather body")
		|| name.equals("hardleather body")
		|| name.contains("studded body"))
			return false;
		return item.getDefinitions().getEquipType() == 6;
	}
	
	public static boolean hideHair(Item item) {
		return item.getDefinitions().getEquipType() == 8;
	}
	
	public static boolean showBear(Item item) { 
		String name = item.getName().toLowerCase();
		return !hideHair(item)
				|| name.contains("horns")
				|| name.contains("hat")
				|| name.contains("afro")
				|| name.contains("cowl")
				|| name.contains("tattoo")
				|| name.contains("headdress")
				|| name.contains("hood")
				|| (name.contains("mask") && !name.contains("h'ween"))
				|| (name.contains("helm") && !name.contains("full"));
	}

	public static int getItemSlot(int itemId) {
		return ItemDefinitions.getItemDefinitions(itemId).getEquipSlot();
	}

	public static boolean isTwoHandedWeapon(Item item) {
		return item.getDefinitions().getEquipType() == 5;
	}

	public int getWeaponRenderEmote() {
		Item weapon = items.get(3);
		if (weapon == null)
			return 1426;
		return weapon.getDefinitions().getRenderAnimId();
	}

	public boolean hasShield() {
		return items.get(5) != null;
	}

	public int getWeaponId() {
		Item item = items.get(SLOT_WEAPON);
		if (item == null)
			return -1;
		return item.getId();
	}
	
	public Item getWeapon() {
		Item item = items.get(SLOT_WEAPON);
		if (item == null)
			return null;
		return item;
	}

	public int getChestId() {
		Item item = items.get(SLOT_CHEST);
		if (item == null)
			return -1;
		return item.getId();
	}

	public int getHatId() {
		Item item = items.get(SLOT_HAT);
		if (item == null)
			return -1;
		return item.getId();
	}

	public int getShieldId() {
		Item item = items.get(SLOT_SHIELD);
		if (item == null)
			return -1;
		return item.getId();
	}

	public int getLegsId() {
		Item item = items.get(SLOT_LEGS);
		if (item == null)
			return -1;
		return item.getId();
	}

	public void removeAmmo(int ammoId, int ammount) {
		if (ammount == -1) {
			items.remove(SLOT_WEAPON, new Item(ammoId, 1));
			refresh(SLOT_WEAPON);
			player.getAppearence().generateAppearenceData();
		} else {
			items.remove(SLOT_ARROWS, new Item(ammoId, ammount));
			refresh(SLOT_ARROWS);
		}
	}

	public int getAuraId() {
		Item item = items.get(SLOT_AURA);
		if (item == null)
			return -1;
		return item.getId();
	}

	public int getCapeId() {
		Item item = items.get(SLOT_CAPE);
		if (item == null)
			return -1;
		return item.getId();
	}

	public int getRingId() {
		Item item = items.get(SLOT_RING);
		if (item == null)
			return -1;
		return item.getId();
	}

	public int getAmmoId() {
		Item item = items.get(SLOT_ARROWS);
		if (item == null)
			return -1;
		return item.getId();
	}
	
	public int getAmmoAmount() {
		Item item = items.get(SLOT_ARROWS);
		if (item == null)
			return -1;
		return item.getAmount();
	}

	public void deleteItem(int itemId, int amount) {
		Item[] itemsBefore = items.getItemsCopy();
		items.remove(new Item(itemId, amount));
		refreshItems(itemsBefore);
	}

	public void refreshItems(Item[] itemsBefore) {
		int[] changedSlots = new int[itemsBefore.length];
		int count = 0;
		for (int index = 0; index < itemsBefore.length; index++) {
			if (itemsBefore[index] != items.getItems()[index])
				changedSlots[count++] = index;
		}
		int[] finalChangedSlots = new int[count];
		System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
		refresh(finalChangedSlots);
	}

	public int getBootsId() {
		Item item = items.get(SLOT_FEET);
		if (item == null)
			return -1;
		return item.getId();
	}

	public int getGlovesId() {
		Item item = items.get(SLOT_HANDS);
		if (item == null)
			return -1;
		return item.getId();
	}
	
	public boolean wearingGloves() {
		return items.get(SLOT_HANDS) != null;
	}

	public ItemsContainer<Item> getItems() {
		return items;
	}

	public int getEquipmentHpIncrease() {
		return equipmentHpIncrease;
	}

	public void setEquipmentHpIncrease(int hp) {
		this.equipmentHpIncrease = hp;
	}

	public boolean wearingArmour() {
		return getItem(SLOT_HAT) != null || getItem(SLOT_CAPE) != null
				|| getItem(SLOT_AMULET) != null || getItem(SLOT_WEAPON) != null
				|| getItem(SLOT_CHEST) != null || getItem(SLOT_SHIELD) != null
				|| getItem(SLOT_LEGS) != null || getItem(SLOT_HANDS) != null
				|| getItem(SLOT_FEET) != null;
	}

	public int getAmuletId() {
		Item item = items.get(SLOT_AMULET);
		if (item == null)
			return -1;
		return item.getId();
	}

	public boolean hasTwoHandedWeapon() {
		Item weapon = items.get(SLOT_WEAPON);
		return weapon != null && isTwoHandedWeapon(weapon);
	}
		public boolean hasForinthyBrace() {
		Item item = items.get(SLOT_HANDS);
		if (item == null)
			return false;
		if (!item.getName().toLowerCase().contains("forinthry"))
			return false;
		return true;
	}

	public long getEquipmentValue() {
		long value = 0;
		for (Item equipment : player.getEquipment().getItems().toArray()) {
			if (equipment == null)
				continue;
			long amount = equipment.getAmount();
			value += equipment.getDefinitions().getTipitPrice() * amount;
		}
		return value;
	}

	public void deleteItem(Item item) {
		if (!player.getControlerManager().canDeleteInventoryItem(item.getId(),
				item.getAmount()))
			return;
		Item[] itemsBefore = items.getItemsCopy();
		items.remove(item);
		refreshItems(itemsBefore);
	}

	public void processForinthyBracelet(Player player) {
		Item item = items.get(SLOT_HANDS);
		if (item == null)
			return;
		if (!item.getName().toLowerCase().contains("forinthry"))
			return;
		if (player.getForinthyRepel() > Utils.currentTimeMillis()) {
			player.sm("Revenants are still passive towards you for the next "+Utils.getMinsLeft(player.getForinthyRepel())+ "minutes.");
			return;
		}
		if (item.getId() == 11103)
			player.getEquipment().getItems().set(SLOT_HANDS, null);
		else
			player.getEquipment().getItems().set(SLOT_HANDS, new Item(item.getId() + 2, 1));
		player.getEquipment().refresh(SLOT_HANDS);
		player.getAppearence().generateAppearenceData();
		player.processForinthryBrace();
		return;
	}

	public static boolean isTwoHandedWeapon(ItemDefinitions defs) {
		return defs.getEquipType() == 5;
	}

	public void removeAmmo(int ammoId, int ammount, boolean primary){
		if (ammount == -1) {
			items.remove((primary ? SLOT_WEAPON : SLOT_SHIELD), new Item(ammoId, ammount));
			refresh((primary ? SLOT_WEAPON : SLOT_SHIELD));
			player.getAppearence().generateAppearenceData();
		} else {
			items.remove(SLOT_ARROWS, new Item(ammoId, ammount));
			refresh(SLOT_ARROWS);
		}
	}

	public int getWeaponEndCombatEmote() {
		Item weapon = items.get(3);
		if (weapon == null) {
		    Item offhand = items.get(SLOT_SHIELD);
		    if (offhand == null)
		    	return -1;
		    int emote = offhand.getDefinitions().getCombatOpcode(2918);
		    return emote == 0 ? 18025 : emote;
		}
		int emote = weapon.getDefinitions().getCombatOpcode(2918);
		return emote == 0 ? 18025 : emote;
    }

	 public int getWeaponStance() {
	    	boolean combatStance = player.getCombatDefinitions().isCombatStance();
			Item weapon = items.get(3);
			if (weapon == null) {
				Item offhand = items.get(SLOT_SHIELD);
			    if (offhand == null)
			    	return combatStance ? 2688 : 2699;
			    int emote = offhand.getDefinitions().getCombatOpcode(combatStance ? 2955 : 2954);
				    return emote == 0 ? combatStance ? 2688 : 2699 : emote;
			}
			int emote = weapon.getDefinitions().getCombatOpcode(combatStance ? 2955 : 2954);
			if (weapon.getId() == 4084) //sled exception
				return 1119;
			return emote == 0 ? combatStance ? 2688 : 2699 : emote;
	    }

	 public ItemsContainer<Item> getCosmeticItems() {
			return cosmeticItems;
		}

		public ArrayList<Item> getKeepSakeItems() {
			return keepSakeItems;
		}

		public boolean containsKeepSakeItem(int itemId) {
			for (Item item : keepSakeItems) {
				if (item == null)
					continue;
				if (item.getId() == itemId)
					return true;
			}
			return false;
		}

		public ArrayList<Cosmetic> getSavedCosmetics() {
			return savedCosmetics;
		}

		public boolean[] getHiddenSlots() {
			return hiddenSlots;
		}

		public boolean isCanDisplayCosmetic() {
			if (player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof DuelControler)
				return false;
			return !(player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof DungeonControler) && !player.isCanPvp();
		}

		public int getCostumeColor() {
			return costumeColor;
		}

		public void setCostumeColor(int costumeColor) {
			this.costumeColor = costumeColor;
			player.getAppearence().generateAppearenceData();
		}
		
		public boolean isHideAll() {
			return hideAll;
		}

		public void setHideAll(boolean hideAll) {
			this.hideAll = hideAll;
		}

		public void resetCosmetics() {
			cosmeticItems.reset();
			for (int i = 0; i < hiddenSlots.length; i++)
				hiddenSlots[i] = false;
			hideAll = false;
		}

		public static final class Cosmetic implements Serializable {

			/**
			 * 
			 */
			private static final long serialVersionUID = 3243926269985535095L;
			private ItemsContainer<Item> cosmeticItems;
			private boolean[] hiddenSlots;
			private String cosmeticName;

			public Cosmetic(String cosmeticName, ItemsContainer<Item> cosmeticItems, boolean[] hiddenSlots) {
				this.cosmeticName = cosmeticName;
				this.cosmeticItems = cosmeticItems;
				this.hiddenSlots = hiddenSlots;
			}

			public ItemsContainer<Item> getCosmeticItems() {
				return cosmeticItems;
			}

			public boolean[] getHiddenSlots() {
				return hiddenSlots;
			}

			public String getCosmeticName() {
				return cosmeticName;
			}

		}

		public void wieldOneItem(int slot, Item toWield) {
			items.set(slot, toWield);
			refresh(slot, slot == 3 ? 5 : slot == 3 ? 0 : 3);
			player.getAppearence().generateAppearenceData();
			if (slot == 3)
				player.getCombatDefinitions().resetSpecialAttack();
			player.getCharges().wear(slot);
		}

	
}
