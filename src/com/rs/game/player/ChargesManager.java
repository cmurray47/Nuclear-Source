package com.rs.game.player;


import java.io.Serializable;
import java.util.HashMap;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.perks.Perks.Perk;
import com.rs.utils.Utils;

public class ChargesManager implements Serializable {

	private static final long serialVersionUID = -5978513415281726450L;

	private transient Player player;

	private HashMap<Integer, Integer> charges;

	public ChargesManager() {
		charges = new HashMap<Integer, Integer>();
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public int getCurrentCharges(int itemId) {
		if(charges.get(itemId) == null) // when an item is not used,so 100%
			return 60000;
    return charges.get(itemId);
}

	public void process() {
		Item[] items = player.getEquipment().getItems().getItems();
		for (int slot = 0; slot < items.length; slot++) {
			Item item = items[slot];
			if (item == null)
				continue;
			if (player.getAttackedByDelay() > Utils.currentTimeMillis()) {
				int newId = ItemConstants.getDegradeItemWhenCombating(item
						.getId());
				if (newId != -1) {
					item.setId(newId);
					player.getEquipment().refresh(slot);
					player.getAppearence().generateAppearenceData();
					player.getPackets().sendGameMessage(
							"Your " + item.getDefinitions().getName()
									+ " degraded.");
				}
			}
			int defaultCharges = ItemConstants.getItemDefaultCharges(item
					.getId());
			if (defaultCharges == -1)
				continue;
			if (ItemConstants.itemDegradesWhileWearing(item.getId()))
				degrade(item.getId(), defaultCharges, slot);
			else if (player.getAttackedByDelay() > Utils.currentTimeMillis())
				degrade(item.getId(), defaultCharges, slot);
		}
	}

	public void die() {
		Item[] equipItems = player.getEquipment().getItems().getItems();
		for (int slot = 0; slot < equipItems.length; slot++) {
			if (equipItems[slot] != null && degradeCompletly(equipItems[slot]))
				player.getEquipment().getItems().set(slot, null);
		}
		Item[] invItems = player.getInventory().getItems().getItems();
		for (int slot = 0; slot < invItems.length; slot++) {
			if (invItems[slot] != null && degradeCompletly(invItems[slot]))
				player.getInventory().getItems().set(slot, null);
		}
	}
	
    public void die(Integer[] slots, Integer[] slots2) {
	Item[] equipItems = player.getEquipment().getItems().getItems();
	Item[] invItems = player.getInventory().getItems().getItems();

	if (slots == null) {
	    for (int slot = 0; slot < equipItems.length; slot++) {
		if (equipItems[slot] != null && degradeCompletly(equipItems[slot]))
		    player.getEquipment().getItems().set(slot, null);
	    }
	    for (int slot = 0; slot < invItems.length; slot++) {
		if (invItems[slot] != null && degradeCompletly(invItems[slot]))
		    player.getInventory().getItems().set(slot, null);
	    }
	} else {
	    for (int slot : slots) {
		if (slot >= 16) {
		    if (invItems[slot - 16] != null && degradeCompletly(invItems[slot - 16]))
			player.getInventory().getItems().set(slot - 16, null);
		} else {
		    if (equipItems[slot - 1] != null && degradeCompletly(equipItems[slot - 1]))
			player.getEquipment().getItems().set(slot - 1, null);
		}
	    }
	    for (int slot : slots2) {
		if (slot >= 16) {
		    if (invItems[slot - 16] != null && degradeCompletly(invItems[slot - 16]))
			player.getInventory().getItems().set(slot - 16, null);
		} else {
		    if (equipItems[slot - 1] != null && degradeCompletly(equipItems[slot - 1]))
			player.getEquipment().getItems().set(slot - 1, null);
		}
	    }
	}
    }

	/*
	 * return disapear;
	 */
	public boolean degradeCompletly(Item item) {
		int defaultCharges = ItemConstants.getItemDefaultCharges(item.getId());
		if (defaultCharges == -1)
			return false;
		while (true) {
			if (ItemConstants.itemDegradesWhileWearing(item.getId())
					|| ItemConstants.itemDegradesWhileCombating(item.getId())) {
				charges.remove(item.getId());
				int newId = ItemConstants.getItemDegrade(item.getId());
				if (newId == -1)
					return ItemConstants.getItemDefaultCharges(item.getId()) == -1 ? false
							: true;
				item.setId(newId);
			} else {
				int newId = ItemConstants.getItemDegrade(item.getId());
				if (newId != -1) {
					charges.remove(item.getId());
					item.setId(newId);
				}
				break;
			}
		}
		return false;
	}

	public void wear(int slot) {
		Item item = player.getEquipment().getItems().get(slot);
		if (item == null)
			return;
		if(player.getPerkHandler().perks.contains(Perk.DEGRADE_PERK))
			return;
		int newId = ItemConstants.getDegradeItemWhenWear(item.getId());
		if (newId == -1)
			return;
		player.getEquipment().getItems().set(slot, new Item(newId, 1));
		player.getEquipment().refresh(slot);
		player.getAppearence().generateAppearenceData();
		player.getPackets().sendGameMessage(
				"Your " + item.getDefinitions().getName() + " degraded.");
			if (slot == 0)
						player.setNextGraphics(new Graphics(1859));
					if (slot == 4)
						player.setNextGraphics(new Graphics(1861));
					if (slot == 7)
					 player.setNextGraphics(new Graphics(1860));
	}

	private void degrade(int itemId, int defaultCharges, int slot) {
		Integer c = charges.remove(itemId);
		if (c == null)
			c = defaultCharges;
		else {
			c--;
			if (c == 0) {
				int newId = ItemConstants.getItemDegrade(itemId);
				player.getEquipment().getItems()
						.set(slot, newId != -1 ? new Item(newId, 1) : null);
				if (newId == -1)
					player.getPackets().sendGameMessage(
							"Your "
									+ ItemDefinitions
											.getItemDefinitions(itemId)
											.getName() + " became into dust.");
				else
					player.getPackets().sendGameMessage(
							"Your "
									+ ItemDefinitions
											.getItemDefinitions(itemId)
											.getName() + " degraded.");
				player.getEquipment().refresh(slot);
				player.getAppearence().generateAppearenceData();
				return;
			}
		}
		charges.put(itemId, c);
	}

}
