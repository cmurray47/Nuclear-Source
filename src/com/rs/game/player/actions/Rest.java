package com.rs.game.player.actions;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class Rest extends Action {

	private static int[][] REST_DEFS = { { 5713, 1549, 5748 },
			{ 11786, 1550, 11788 }, { 5713, 1551, 2921 } // TODO First emote

	};
    private static int[][] Z_REST = { { 17301, /* i know this but do you? */17302, 17303 } // TODO First emote
	};
	
	private static int[][] ARCANE_EMOTES = { { 20286, 20287, 20288 } };
	private static int[] ARCANE_GFX = { 4003, 4004, 4005 };
	private int index;

	@Override
	public boolean start(Player player) {
		if (!process(player))
			return false;
		if (player.ZREST == false && player.AREST == false) {
		index = Utils.random(REST_DEFS.length);
		} else if(player.ZREST == true && player.AREST == false){
		index = Utils.random(Z_REST.length);
		}if (player.ZREST == false && player.AREST == true) {
		index = Utils.random(ARCANE_EMOTES.length);	
		}
		player.setResting(true);
		if (player.ZREST == false && player.AREST == false) {
		player.setNextAnimation(new Animation(REST_DEFS[index][0]));
		player.getAppearence().setRenderEmote(REST_DEFS[index][1]);
		} else if(player.ZREST == true && player.AREST == false){
		player.setNextAnimation(new Animation(Z_REST[index][0]));
		player.getAppearence().setRenderEmote(Z_REST[index][1]);
		} else if (player.ZREST == false && player.AREST == true) {
		player.setNextGraphics(new Graphics(ARCANE_GFX[0]));
		player.setNextAnimation(new Animation(ARCANE_EMOTES[index][0]));
		player.setNextGraphics(new Graphics(ARCANE_GFX[1]));
		player.getAppearence().setRenderEmote(ARCANE_EMOTES[index][1]);
		}
		return true;
	}

	@Override
	public boolean process(Player player) {
		if (player.getPoison().isPoisoned()) {
			player.getPackets().sendGameMessage(
					"You can't rest while you're poisoned.");
			return false;
		}
		if (player.getAttackedByDelay() + 10000 > Utils.currentTimeMillis()) {
			player.getPackets().sendGameMessage(
					"You can't rest until 10 seconds after the end of combat.");
			return false;
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		return 0;
	}

	@Override
	public void stop(Player player) {
		player.setResting(false);
		if (player.ZREST == false && player.AREST == false) {
		player.setNextAnimation(new Animation(REST_DEFS[index][2]));
		}  else if (player.ZREST == true && player.AREST == false){
		player.setNextAnimation(new Animation(Z_REST[index][2]));	
		} else if (player.ZREST == false && player.AREST == true) {
		player.setNextGraphics(new Graphics(ARCANE_GFX[2]));	
		player.setNextAnimation(new Animation(ARCANE_EMOTES[index][2]));	
		}
		player.getEmotesManager().setNextEmoteEnd();
		player.getAppearence().setRenderEmote(-1);
	}

}
