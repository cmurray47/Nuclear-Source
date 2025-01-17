package com.rs.game.player.dialogues;

import com.rs.game.player.Skills;
import com.rs.game.player.actions.crafting.SirenicScaleCrafting;
import com.rs.game.player.actions.crafting.SirenicScaleCrafting.Sirenic;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;

/**
 * Handles the Sirenic Scale crafting skills dialogue.
 * @author Vichy
 */
public class SirenicScaleCraftingD extends Dialogue {
	
	private Sirenic[] scale;

	@Override
	public void start() {
		scale = (Sirenic[]) parameters[1];
		int count = 0;
		int[] ids = new int[scale.length];
		for (Sirenic scale : scale)
			ids[count++] = scale.getProduceEnergy().getId();
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Which armour piece would you like to create?", 
				1, ids, new ItemNameFilter() {
			int count = 0;

			@Override
			public String rename(String name) {
				Sirenic scale = Sirenic.values()[count++];
				if (player.getSkills().getLevel(Skills.CRAFTING) < scale.getLevelRequired())
					name = "<col=ff0000>" + name + "<br><col=ff0000>Level " + scale.getLevelRequired();
				return name;

			}
		});
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int idx = SkillsDialogue.getItemSlot(componentId);
		if (idx > scale.length) {
			end();
			return;
		}
		player.getActionManager().setAction(new SirenicScaleCrafting(scale[idx], SkillsDialogue.getQuantity(player)));
		end();
	}

	@Override
	public void finish() {  }
}