package com.rs.game.player.actions.divination;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
/**
 * 
 * @author paolo
 *
 */
public class DivinePlacing {

	public static void check(final Player owner) {
		WorldTasksManager.schedule(new WorldTask() {
		    @Override
		    public void run() {
		if(owner.divine > 1)
		return;
		if(owner.divine == 0){
				owner.sendMessage("Your divine has vanished.");
				stop();
					}
		    return;
		    }  
			}, 0, 0);
		}



	/**
	 * places the divine object
	 * @param owner {the player}
	 * @param item item you use
	 * @param fobject 
	 * @param lobject 
	 * @param lvl  level needed 
	 * @param skillId	skilltype
	 */
public static void placeDivine(final Player owner, final int item, final int fobject, final int lobject, final int lvl, final int skillId) {
	if(!World.isTileFree(0,owner.getX() + 1, owner.getY(), 1)){
		owner.sm("You can't place your divine location here.");
		return;
	}
	WorldTasksManager.schedule(new WorldTask() {
	    int ticks;
	    @Override
	    public void run() {
		ticks++;
	if(ticks == 1){
		owner.getInventory().deleteItem(item, 1);
		owner.setNextAnimation(new Animation(21217));
		owner.addFreezeDelay(2700);
		final WorldObject divinefirststage = new WorldObject(fobject,10, 0, owner.getX() + 1, owner.getY(), owner.getPlane()); // object spawning
		owner.faceObject(divinefirststage); // forces player to face where he is putting the object
		World.spawnTemporaryObject(divinefirststage, 2700); // dont touch
	}
	if(ticks == 5){
		check(owner); // lets the owner know his divine disapeared
		final WorldObject divinefinalstage = new WorldObject(lobject,10, 0, owner.getX() + 1, owner.getY(), owner.getPlane(), owner); // object with animation that player will mine
		World.spawnTemporaryDivineObject(divinefinalstage, 40000, owner);	// time object will stay in miliseconds
		owner.divine = lobject;
		owner.divines = owner;
		stop();
				}
	    return;
	    }  
		}, 0, 0);
	}
}
