package com.rs.game.player.achievements.impl;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.achievements.Achievement;
import com.rs.game.player.achievements.Types;

public class TrapSpecialistAchievement extends Achievement {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7468356336022909571L;

	public TrapSpecialistAchievement() {
		super(Types.HARD, "Trap Specialist","Catch a total of @TOTAL@ creatures with box traps.");
	}

	@Override
	public String getRewardInfo() {
		return "1x Achievement Points & 750K";
	}
	
	@Override
	public Item[] getRewards() {
		Item rewards[] = new Item[] { new Item(995,750000), new Item(19808,1)};
		return rewards;
	}
	
	

	@Override
	public void giveReward(Player player) {
		this.addAchievementPoints(player, 3);
		this.addItem(player, new Item(995, 750000),new Item(19808,1));
		
	}

	@Override
	public int getTotalAmount() {
		return 800;
	}

	@Override
	public String getKey() {
		return "box_traps";
	}


}