package com.rs.game.player.dialogues;

import com.rs.game.Animation;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;

public class PotteryWheelFishing extends Dialogue {

    private static final int[] PRODUCTS = { 20319,20325,20331,20337,20343};
    private static final int[] LEVELS = { 2 ,15,41,53,76};
    private static final double[] EXPERIENCE = { 12,20,31.2,36,72};
    private static final int SOFT_CLAY = 1761;

    @Override
    public void start() {
	SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "How many hides would you like to make?<br>Choose a number, then click the pot to begin.", 28, PRODUCTS, new ItemNameFilter() {
	    int count = 0;

	    @Override
	    public String rename(String name) {
		int levelRequired = LEVELS[count++];
		if (player.getSkills().getLevel(Skills.CRAFTING) < levelRequired)
		    name = "<col=ff0000>" + name + "<br><col=ff0000>Level " + levelRequired;
		return name;
	    }
	});
    }

    @Override
    public void run(int interfaceId, int componentId) {
	final int componentIndex = SkillsDialogue.getItemSlot(componentId);
	if (componentIndex > PRODUCTS.length) {
	    end();
	    return;
	}
	player.getActionManager().setAction(new Action() {
	    int ticks;

	    @Override
	    public boolean start(Player player) {

		int clayAmount = player.getInventory().getAmountOf(SOFT_CLAY);
		if (clayAmount <= 1) {
		    end();
		    player.getPackets().sendGameMessage("You need 2 soft clays in order to make an urn.");
		    return false;
		}
		final int levelReq = LEVELS[componentIndex];
		if (player.getSkills().getLevel(Skills.CRAFTING) < levelReq) {
		    end();
		    player.getPackets().sendGameMessage("You need a crafting level of " + levelReq + " in order to spin this.");
		    return false;
		}
		int requestedAmount = SkillsDialogue.getQuantity(player);
		if (requestedAmount > clayAmount)
		    requestedAmount = clayAmount/2;
		ticks = requestedAmount;
		return true;
	    }

	    @Override
	    public boolean process(Player player) {
		return ticks > 0;
	    }

	    @Override
	    public int processWithDelay(Player player) {
		ticks--;
		player.setNextAnimation(new Animation(896));
		player.getSkills().addXp(Skills.CRAFTING, EXPERIENCE[componentIndex]);
		player.getInventory().deleteItem(SOFT_CLAY, 2);
		player.getInventory().addItem(PRODUCTS[componentIndex], 1);
		return 4;
	    }

	    @Override
	    public void stop(Player player) {
		setActionDelay(player, 3);
	    }
	});
    }

    @Override
    public void finish() {

    }
}
