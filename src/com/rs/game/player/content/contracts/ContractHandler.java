package com.rs.game.player.content.contracts;

import java.io.Serializable;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

public class ContractHandler implements Serializable {
	
	private static final long serialVersionUID = 1; //Don't change this, else RIP..
	
	private static final transient int[][] npcs = { 
		{ 50, 5, 12, 10, 30},//KBD
		{ 8133,	8, 15, 4, 15},//Corp
		{ 6260,	5, 12, 7, 35},//Bandos
		{ 6222,	5, 12, 7, 35},//Armadyl
		{ 6247,	8, 12, 7, 35},//Zamorak
		{ 6203,	5, 12, 7, 35},//Sara
		{ 7134, 5, 12, 1, 1},//bork
		{ 14301, 3, 7, 10, 25},//Glacor
		{ 8351, 5, 12, 5, 30},//Tormented D
		{ 2883, 6, 20, 5, 20},//Rex
		{ 2882, 6, 20, 5, 20},//Supreme
		{ 2881, 6, 20, 5, 20},//Prime
		{ 3200, 10, 20, 10, 40},//Choas ele
		{ 19463, 10, 30, 2, 4},//Araxxor
		{ 15454, 10, 20, 3, 5},//QBD
		{ 13450, 10, 25, 2, 4},//Nex
	};

	public static boolean getNewContract(Player player) {
		int index = Utils.random(npcs.length);
		int minimum = npcs[index][3];
		int maximum = npcs[index][4];
		int minpoints = npcs[index][1];
		int maxpoints = npcs[index][2];
		player.setContract(new Contract(npcs[index][0], 995, Utils.random(minpoints, maxpoints), Utils.random(minimum, maximum)));
		player.getContract().setCompleted(false);
		return true;
	}
	
	public static boolean checkContract(Player killer, int id, NPC npc) {
		if (killer == null)
			return false;
		if (killer.getContract() != null) {
			String npcName = NPCDefinitions.getNPCDefinitions(killer.getContract().getNpcId()).getName().toLowerCase();
			
			if (id == killer.getContract().getNpcId()) {
				killer.getContract().decreaseAmount();
				killer.setTotalKills(killer.getTotalKills() + 1);
				killer.getSkills().addXp(Skills.SLAYER, npc.getCombatLevel() / 2);
				if (killer.getContract().getKillAmount() >= 1)
					killer.sendMessage(Colors.gold+"<shad=292421>You have "+killer.getContract().getKillAmount()+"x "+ npcName + " left to kill.");
				if (killer.getContract().getKillAmount() <= 0) {
					killer.getContract().setCompleted(true);
					killer.setTotalContract(killer.getTotalContract() + 1);
					killer.setReaperPoints(killer.getReaperPoints() + Contract.givePoints(killer));
					killer.sendMessage(Colors.green+"You have completed your reaper task.");
					killer.setContract(null);
				}
				return true;
			}
			return false;
		}
		return false;
	}
}