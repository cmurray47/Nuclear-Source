package com.rs.game.player.dialogues.impl.dungeoneering;

import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.content.dungeoneering.skills.DungeoneeringRunecrafting;
import com.rs.game.player.content.dungeoneering.skills.DungeoneeringStaves;
import com.rs.game.player.dialogues.Dialogue;

public class DungRunecraftingD extends Dialogue {

	public static final int[][] RUNES =
	{
	{ 17780, 17781, 17782, 17783 },
	{ 17784, 17785, 17786, 17787 },
	{ 17788, 17789, 17790, 17791, 17792 },
	{ 16997, 17001, 17005, 17009, 17013, 16999, 17003, 17007, 17011, 17015 } };

	@Override
	public void start() {
		int type = (int) this.parameters[0];
		sendRCOptions(type);
	}

	//TODO remake this. 4 cjay
	
	private void sendRCOptions(int type) {
		if (type == 0) {
			sendOptionsDialogue("What would you like to make?", "Runes", "Staves");
			stage = 0;
		} else {
			sendOptionsDialogue("What would you like to make?", "Elemental", "Catalytic", "Misc");
			stage = (byte) (type + 1);
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 0) {
			if (componentId == OPTION_1) {
				sendRCOptions((componentId == 11 ? 0 : componentId - 12) + 1);
			} else {
				SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Choose how many you wish to make,<br>then click on the item to begin.", 28, RUNES[3], new ItemNameFilter() {
					@Override
					public String rename(String name) {
						return name;
					}
				});
				stage = 6;
			}
		} else if (stage == 1) {
			sendRCOptions((componentId == 11 ? 0 : componentId - 12) + 1);
		} else if (stage == 2) {
			SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Choose how many you wish to make,<br>then click on the item to begin.", 28, RUNES[(componentId == 11 ? 0 : componentId - 12)], new ItemNameFilter() {
				@Override
				public String rename(String name) {
					return name;
				}
			});
			stage = (byte) ((componentId == 11 ? 0 : componentId - 12) + 3);
		} else if (stage >= 3 && stage <= 6) {
			int option = SkillsDialogue.getItemSlot(componentId);
			int quantity = SkillsDialogue.getQuantity(player);
			if (stage == 3) {
				if (option == 0)
					player.getActionManager().setAction(new DungeoneeringRunecrafting(quantity, RUNES[0][option], 1, .10, 11, 2, 22, 3, 34, 4, 44, 5, 55, 6, 66, 7, 77, 88, 9, 99, 10));
				else if (option == 1)
					player.getActionManager().setAction(new DungeoneeringRunecrafting(quantity, RUNES[0][option], 5, .12, 19, 2, 38, 3, 57, 4, 76, 5, 95, 6));
				else if (option == 2)
					player.getActionManager().setAction(new DungeoneeringRunecrafting(quantity, RUNES[0][option], 9, .13, 26, 2, 52, 3, 78, 4));
				else if (option == 3)
					player.getActionManager().setAction(new DungeoneeringRunecrafting(quantity, RUNES[0][option], 14, .14, 35, 2, 70, 3));
			} else if (stage == 4) {
				if (option == 0)
					player.getActionManager().setAction(new DungeoneeringRunecrafting(quantity, RUNES[1][option], 2, .11, 14, 2, 28, 3, 42, 4, 56, 5, 70, 6, 84, 7, 98, 8));
				else if (option == 1)
					player.getActionManager().setAction(new DungeoneeringRunecrafting(quantity, RUNES[1][option], 35, .17, 74, 2));
				else if (option == 2)
					player.getActionManager().setAction(new DungeoneeringRunecrafting(quantity, RUNES[1][option], 35, .20, 74, 2));
				else if (option == 3)
					player.getActionManager().setAction(new DungeoneeringRunecrafting(quantity, RUNES[1][option], 77, .21));
			} else if (stage == 5) {
				if (option == 0)
					player.getActionManager().setAction(new DungeoneeringRunecrafting(quantity, RUNES[2][option], 20, .15, 46, 2, 92, 3));
				else if (option == 1)
					player.getActionManager().setAction(new DungeoneeringRunecrafting(quantity, RUNES[2][option], 27, .16, 59, 2));
				else if (option == 2)
					player.getActionManager().setAction(new DungeoneeringRunecrafting(quantity, RUNES[2][option], 40, .174, 82, 2));
				else if (option == 3)
					player.getActionManager().setAction(new DungeoneeringRunecrafting(quantity, RUNES[2][option], 45, .18, 91, 2));
				else
					player.getActionManager().setAction(new DungeoneeringRunecrafting(quantity, RUNES[2][option], 50, .19));
			} else if (stage == 6) {
				player.getActionManager().setAction(new DungeoneeringStaves(option, quantity));
			}
			end();
		}
	}

	@Override
	public void finish() {

	}
}
