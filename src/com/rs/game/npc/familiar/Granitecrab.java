package com.rs.game.npc.familiar;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.summoning.Summoning.Pouches;

public class Granitecrab extends Familiar {

    /**
     *
     */
    private static final long serialVersionUID = 649164679697311630L;

    public Granitecrab(Player owner, Pouches pouch, WorldTile tile,
                       int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
        super(owner, pouch, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
    }

    @Override
    public int getBOBSize() {
        return 0;
    }

    @Override
    public int getSpecialAmount() {
        return 12;
    }

    @Override
    public SpecialAttack getSpecialAttack() {
        return SpecialAttack.CLICK;
    }

    @Override
    public String getSpecialDescription() {
        return "Increases your restance to all attacks by four.";
    }

    @Override
    public String getSpecialName() {
        return "Stony Shell";
    }

    @Override
    public boolean submitSpecial(Object object) {
        Player player = (Player) object;
        int newLevel = player.getSkills().getLevel(Skills.DEFENCE) + 4;
        if (newLevel > player.getSkills().getLevelForXp(Skills.DEFENCE) + 4)
            newLevel = player.getSkills().getLevelForXp(Skills.DEFENCE) + 4;
        player.setNextGraphics(new Graphics(1300));
        player.setNextAnimation(new Animation(7660));
        setNextGraphics(new Graphics(8108));
        setNextAnimation(new Animation(1326));
        player.getSkills().set(Skills.DEFENCE, newLevel);
        return true;
    }

}
