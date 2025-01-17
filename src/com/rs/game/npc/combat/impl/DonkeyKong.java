package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class DonkeyKong extends CombatScript {

	@Override
	public int attack(final NPC npc, final Entity target) {
		if (npc.getHitpoints() < npc.getMaxHitpoints() / 2
				&& Utils.random(5) == 0) { // if lower than 50% hp, 1/5 prob of
			// healing 10%
			npc.heal(30);
		}
		npc.setCombatLevel(512);
		npc.setCapDamage(700);

		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (Utils.getRandom(4) == 0) {
			switch (Utils.getRandom(2)) {
			case 0:
				npc.setNextForceTalk(new ForceTalk("You will DIE!"));
				break;
			case 1:
				npc.setNextForceTalk(new ForceTalk("You will not survive!"));
				break;
			case 2:
				npc.setNextForceTalk(new ForceTalk("You cannot take me!"));
				break;
			}
		}
		if (Utils.getRandom(2) == 0) { // magical attack
			npc.setNextAnimation(new Animation(15042));
			for (final Entity t : npc.getPossibleTargets()) {
				delayHit(
						npc,
						1,
						t,
						getMagicHit(
								npc,
								getRandomMaxHit(npc, 540,
										NPCCombatDefinitions.MAGE, t)));
				World.sendProjectile(npc, t, 1002, 41, 16, 41, 35, 16, 0);
				target.setNextGraphics(new Graphics(3000));
			}
		} else if (Utils.getRandom(2) == 1) {
			npc.setNextAnimation(new Animation(15046));
			if (target instanceof Player) {
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						int skill = Utils.getRandom(2);
						skill = skill == 0 ? Skills.PRAYER
								: (skill == 1 ? Skills.SUMMONING
										: Skills.PRAYER);
						final Player player = (Player) target;
						if (skill == Skills.PRAYER)
							player.getPrayer().drainPrayer(990);
						else {
							int lvl = player.getSkills().getLevel(skill);
							lvl -= 1 + Utils.getRandom(4);
							player.getSkills().set(skill, lvl < 0 ? 0 : lvl);
						}
						player.sm("Your " + Skills.SKILL_NAME[skill]
								+ " has been dropped!");
					}

				}, 1);
			}
		} else { // melee attack
			npc.setNextAnimation(new Animation(15046));
			delayHit(
					npc,
					0,
					target,
					getMeleeHit(
							npc,
							getRandomMaxHit(npc, 440,
									NPCCombatDefinitions.MELEE, target)));
		}
		return defs.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 13216 };
	}

}
