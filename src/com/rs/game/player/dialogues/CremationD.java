package com.rs.game.player.dialogues;

import com.rs.game.WorldObject;
import com.rs.game.player.content.BonesOnFire;
import com.rs.game.player.content.BonesOnFire.Bones;
import com.rs.game.player.content.SkillsDialogue;

public class CremationD extends Dialogue {

		private Bones bones;
		private WorldObject object;

		@Override
		public void start() {
			this.bones = (Bones) parameters[0];
			this.object = (WorldObject) parameters[1];

			SkillsDialogue
					.sendSkillsDialogue(
							player,
							SkillsDialogue.OFFER,
							"How many would you like to offer?",
							player.getInventory().getItems()
									.getNumberOf(bones.getBone()),
							new int[] { bones.getBone().getId() }, null);
		}

		@Override
		public void run(int interfaceId, int componentId) {
			player.getActionManager().setAction(
					new BonesOnFire(object, bones.getBone(), SkillsDialogue
							.getQuantity(player)));
		end();
	}

	@Override
	public void finish() {

	}

}