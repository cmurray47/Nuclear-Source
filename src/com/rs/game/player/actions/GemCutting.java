package com.rs.game.player.actions;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.achievements.impl.DiamondCrafterAchievement;
import com.rs.net.decoders.handlers.InventoryOptionsHandler;

public class GemCutting extends Action {

	/**
	 * Enum for gems
	 * 
	 * @author Raghav
	 * 
	 */
	public enum Gem {
		OPAL(1625, 1609, 15.0, 1, 886),

		JADE(1627, 1611, 20, 13, 886),

		RED_TOPAZ(1629, 1613, 25, 16, 887),

		SAPPHIRE(1623, 1607, 50, 20, 888),

		EMERALD(1621, 1605, 67, 27, 889),

		RUBY(1619, 1603, 85, 34, 887),

		DIAMOND(1617, 1601, 107.5, 43, 890),

		DRAGONSTONE(1631, 1615, 137.5, 55, 885),

		ONYX(6571, 6573, 167.5, 67, 2717),
		
		HYDRIX(31853, 31855, 197.5, 79, 24309),

        SLAYER_TABLET(4155, 13723, 10, 12, 2717),
		
		LIMESTONE(3211, 3420, 10, 1, 2717);

		private double experience;
		private int levelRequired;
		private int uncut, cut;

		private int emote;

		private Gem(int uncut, int cut, double experience, int levelRequired,
				int emote) {
			this.uncut = uncut;
			this.cut = cut;
			this.experience = experience;
			this.levelRequired = levelRequired;
			this.emote = emote;
		}

		public int getLevelRequired() {
			return levelRequired;
		}

		public double getExperience() {
			return experience;
		}

		public int getUncut() {
			return uncut;
		}

		public int getCut() {
			return cut;
		}

		public int getEmote() {
			return emote;
		}

	}

	public static void cut(Player player, Gem gem) {
		if (player.getInventory().getItems()
				.getNumberOf(new Item(gem.getUncut(), 1)) <= 1) 
			player.getActionManager().setAction(new GemCutting(gem, 1));
		else
			player.getDialogueManager().startDialogue("GemCuttingD", gem);
	}

	private Gem gem;
	private int quantity;

	public GemCutting(Gem gem, int quantity) {
		this.gem = gem;
		this.quantity = quantity;
	}

	public boolean checkAll(Player player) {
		 if (!player.getInventory().containsItemToolBelt(1755, 1)){
			 player.getDialogueManager().startDialogue("ItemMessage", "You need a chisle to complete the action.", 1755);
			 return false;
		 }
		if (player.getSkills().getLevel(Skills.CRAFTING) < gem
				.getLevelRequired()) {
			player.getDialogueManager().startDialogue(
					"SimpleMessage",
					"You need a crafting level of " + gem.getLevelRequired()
					+ " to cut that gem.");
			return false;
		}
		if (player.hasSlayerTabs == false && gem.getUncut() == 4155 ){
		player.sm("You need to unlock this feature first.");
		return false;
		}
		if (!player.getInventory().containsOneItem(gem.getUncut())) {
			player.getDialogueManager().startDialogue(
					"SimpleMessage",
					"You don't have any "
							+ ItemDefinitions
							.getItemDefinitions(gem.getUncut())
							.getName().toLowerCase() + " to cut.");
			return false;
		}
		return true;
	}

	@Override
	public boolean start(Player player) {
		if (checkAll(player)) {
			setActionDelay(player, 1);
			player.setNextAnimation(new Animation(gem.getEmote()));
			return true;
		}
		return false;
	}

	@Override
	public boolean process(Player player) {
		return checkAll(player);
	}

	@Override
	public int processWithDelay(Player player) {
		player.getInventory().deleteItem(gem.getUncut(), 1);
		player.getInventory().addItem(gem.getCut(), 1);
		if (gem.getUncut() == 1617){
			player.cutDiamonds++;
			player.getAchievementManager().notifyUpdate(DiamondCrafterAchievement.class);
		}else if (gem.getUncut() == 6571)
			player.cutOnyxs++;
		if (player.getDailyTask() != null)
			player.getDailyTask().incrementTask(player, 3, gem.getUncut(), Skills.CRAFTING);
		player.randomevent(player);
		player.getSkills().addXp(Skills.CRAFTING, gem.getExperience());
		player.getPackets().sendGameMessage(
				"You cut the "
						+ ItemDefinitions.getItemDefinitions(gem.getUncut())
						.getName().toLowerCase() + ".", true);
		quantity--;
		if (quantity <= 0)
			return -1;
		player.setNextAnimation(new Animation(gem.getEmote())); // start the
		// emote and add
		// 2 delay
		return 0;
	}

	@Override
	public void stop(final Player player) {
		setActionDelay(player, 3);
	}

	 public static boolean isCutting(Player player, Item item1, Item item2) {
			Item gem = InventoryOptionsHandler.contains(1755, item1, item2);
			if (gem == null)
			    return false;
			return isCutting(player, gem.getId());
		    }

		    public static boolean isCutting(Player player, int gemId) {
			for (Gem gem : Gem.values()) {
			    if (gem.uncut == gemId) {
				cut(player, gem);
				return true;
			    }
			}
			return false;
		    }
}
