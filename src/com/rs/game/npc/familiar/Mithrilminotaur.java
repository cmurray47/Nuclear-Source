package com.rs.game.npc.familiar;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.actions.summoning.Summoning.Pouches;

public class Mithrilminotaur extends Familiar {

    private static final long serialVersionUID = -4657392160246588028L;

    public Mithrilminotaur(Player owner, Pouches pouch, WorldTile tile,
                           int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
        super(owner, pouch, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
    }

    @Override
    public String getSpecialName() {
        return "Bull Rush";
    }

    @Override
    public String getSpecialDescription() {
        return "A magical attack doing up to 40 life points of damage while stunning an opponent.";
    }

    @Override
    public int getBOBSize() {
        return 0;
    }

    @Override
    public int getSpecialAmount() {
        return 9;
    }

    @Override
    public SpecialAttack getSpecialAttack() {
        return SpecialAttack.ENTITY;
    }

    @Override
    public boolean submitSpecial(Object object) {
        getOwner().setNextGraphics(new Graphics(1316));
        getOwner().setNextAnimation(new Animation(7660));
        return true;
    }
}
