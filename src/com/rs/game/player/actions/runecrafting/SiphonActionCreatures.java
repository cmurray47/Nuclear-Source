package com.rs.game.player.actions.runecrafting;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.Runecrafting;
import com.rs.game.player.controlers.RunespanControler;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class SiphonActionCreatures extends Action {

	private static enum Creature {

		AIR_ESSLING(15403, 9.5, 24215, 16634, 1, 16571,1),

		MIND_ESSLING(15404, 10, 24217, 16634, 1, 16571,2),

		WATER_ESSLING(15405, 12.6, 24214, 16634, 5, 16571,3),

		EARTH_ESSLING(15406, 14.3, 24216, 16634, 9, 16571,4),

		FIRE_ESSLING(15407, 17.4, 24213, 16634, 14, 16571,5),

		BODY_ESSHOUND(15408, 23.1, 24218, 16650, 20, 16661,7),

		COSMIC_ESSHOUND(15409, 26.6, 24223, 16650, 27, 16661,9),

		CHOAS_ESSHOUND(15410, 30.8, 24221, 16650, 35, 16661,11),

		ASTRAL_ESSHOUND(15411, 35.7, 24224, 16650, 40, 16661,13),

		NATURE_ESSHOUND(15412, 43.4, 24220, 16650, 44, 16661,15),

		LAW_ESSHOUND(15413, 53.9, 24222, 16650, 54, 16661,17),

		DEATH_ESSWRAITH(15414, 60, 24219, 16644, 65, 16641,25),

		BLOOD_ESSWRAITH(15415, 73.1, 24225, 16644, 77, 16641,30),

		SOUL_ESSWRAITH(15416, 106.5, 24226, 16644, 90, 16641,35);

		private int npcId, runeId, npcEmoteId, levelRequired, deathEmote;
		private double xp;
		private int points;

		private Creature(int npcId, double xp, int runeId, int npcEmoteId, int levelRequired, int deathEmote, int points) {
			this.npcId = npcId;
			this.xp = xp;
			this.runeId = runeId;
			this.npcEmoteId = npcEmoteId;
			this.levelRequired = levelRequired;
			this.deathEmote = deathEmote;
			this.points = points;
		}

		public int getDeathEmote() {
			return deathEmote;
		}

		public int getNpcEmoteId() {
			return npcEmoteId;
		}

		public int getRuneId() {
			return runeId;
		}

		public int getLevelRequired() {
			return levelRequired;
		}
		public int getPoints() {
			return points;
		}
	}

	private Creature creatures;
	private NPC creature;
	private boolean started;
	private int npcLife;
	private Animation EMOTE = new Animation(16596);

	public SiphonActionCreatures(Creature creatures, NPC creature) {
		this.creatures = creatures;
		this.creature = creature;
	}

	public static boolean siphon(Player player, NPC npc) {
		Creature creature = getCreature(npc.getId());
		if (creature == null)
			return false;
		player.getActionManager().setAction(new SiphonActionCreatures(creature, npc));
		return true;
	}

	private static Creature getCreature(int id) {
		for (Creature creature : Creature.values())
			if (creature.npcId == id)
				return creature;
		return null;
	}

	@Override
	public boolean start(Player player) {
		if (checkAll(player)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean process(Player player) {
		return checkAll(player);
	}

	public boolean checkAll(final Player player) {
		if (player.isLocked()) {
			return false;
		}
		if (creature.hasFinished())
			return false;
		if (!player.withinDistance(creature, 6) || !player.clipedProjectile(creature, true, creature.getDefinitions().size)) {
			//player.calcFollow(creature, true);
			started = false;
			return true;
		}
		if (player.getSkills().getLevel(Skills.RUNECRAFTING) < creatures.getLevelRequired()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "This creature requires level " + creatures.getLevelRequired() + " to siphon.");
			return false;
		}
		if (!player.getInventory().hasFreeSlots() && !player.getInventory().containsItem(creatures.getRuneId(), 1)) {
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			return false;
		}
		if (!player.getInventory().containsItem(24227, 1)) {
			player.getPackets().sendGameMessage("You don't have any rune essence to siphon from that creature.", true);
			return false;
		}
		if (!started) {
			player.resetWalkSteps();
			player.setNextAnimation(EMOTE);
			started = true;
		}
		creature.resetWalkSteps();
		return true;
	}

	@Override
	public int processWithDelay(final Player player) {
		if (started) {
			int level = player.getSkills().getLevel(Skills.RUNECRAFTING);
			if (level <= 50 ? Utils.getRandom(2) == 1 : Utils.getRandom(1) == 1) {
				player.getPointsManager().runespanStore +=Utils.random(1,10);
				((RunespanControler) player.getControlerManager().getControler()).refreshInventoryPoints();
				player.getInventory().addItem(creatures.getRuneId(), 1);
				player.getInventory().deleteItem(24227, 1);
				double totalXp = creatures.xp;
				if (Runecrafting.hasRcingSuit(player))
					totalXp *= 1.025;
				player.getSkills().addXp(Skills.RUNECRAFTING, totalXp);
				player.setNextGraphics(new Graphics(3071));
				if (npcLife++ == 10)
					return processEsslingDeath(player);
			}
			player.setNextAnimation(EMOTE);
			creature.setNextAnimation(new Animation(creatures.getNpcEmoteId()));
			creature.setNextFaceWorldTile(player);
			creature.resetWalkSteps();
			player.setNextFaceWorldTile(creature);
			World.sendProjectile(creature, player, 3060, 31, 35, 35, 0, 2, 0);
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.setNextGraphics(new Graphics(3062));
				}
			}, 1);
		}
		return 1;
	}

	public int processEsslingDeath(final Player player) {
		creature.setNextAnimation(new Animation(creatures.getDeathEmote()));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getPackets().sendGameMessage("The creature has been broken down.");
				player.getPackets().sendGameMessage("You pick up the essence left by the creature.", true);
				player.setNextAnimation(new Animation(16599));
				creature.setRespawnTask();
				player.getInventory().addItem(24227, 50);
				stop();
			}
		}, 2);
		return -1;
	}

	@Override
	public void stop(Player player) {
		player.setNextAnimation(new Animation(16599));
		setActionDelay(player, 3);
	}

	public static boolean chipCreature(Player player, NPC npc) {
		Creature creature = getCreature(npc.getId());
		if (creature == null)
			return false;
		if (!player.getInventory().containsItem(creature.getRuneId(), 10)) {
			player.getPackets().sendGameMessage("You dont have enough " + ItemDefinitions.getItemDefinitions(creature.getRuneId()).getName() + "s to chip away at that creature.");
			return true;
		} else {
			player.getPackets().sendGameMessage("You use some runes to fire a blast of runic energy at the creature, chipping of some rune essense from its body.");
			player.getInventory().deleteItem(creature.getRuneId(), 10);
			player.getInventory().addItem(24227, 10);
			World.sendProjectile(npc, player, 3060, 31, 35, 35, 0, 2, 0);
		}
		return true;
	}
}
