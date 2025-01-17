package com.rs.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.rs.Settings;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.Hit.HitLook;
import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.qbd.TorturedSoul;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.EntityStrategy;
import com.rs.game.route.strategy.ObjectStrategy;
import com.rs.utils.Utils;

public abstract class Entity extends WorldTile {
	
	public WorldTile getWorldTile() {
		return new WorldTile(getX(), getY(), getPlane());
	}
	
    public int getDamageReceived(Player source) {
	Integer d = receivedDamage.get(source);
	if (d == null || source.hasFinished()) {
	    receivedDamage.remove(source);
	    return -1;
	}
	return d;
    }

	private static final long serialVersionUID = -3372926325008880753L;
	private final static AtomicInteger hashCodeGenerator = new AtomicInteger();
	
	
	// transient stuff
	public transient long lastAttackedEntity;
	private transient int index;
	private transient int lastRegionId; // the last region the entity was at
	protected transient WorldTile lastLoadedMapRegionTile;
	protected transient CopyOnWriteArrayList<Integer> mapRegionsIds; // called by
																	// more than
																	// 1thread
																	// so
																	// concurent
	private transient int direction;
	private transient WorldTile lastWorldTile;
	private WorldTile lastLocation;
	private transient WorldTile nextWorldTile;
	private transient int nextWalkDirection;
	private transient int nextRunDirection;
	private transient WorldTile nextFaceWorldTile;
	private transient boolean teleported;
	private transient ConcurrentLinkedQueue<int[]> walkSteps;// called by more
																// than 1thread
																// so concurent
	private transient ConcurrentLinkedQueue<Hit> receivedHits;
	private transient Map<Entity, Integer> receivedDamage;
	private transient boolean finished; // if removed
	public transient long freezeDelay;
	// entity masks
	private transient Animation nextAnimation;
	private transient Graphics nextGraphics1;
	private transient Graphics nextGraphics2;
	private transient Graphics nextGraphics3;
	private transient Graphics nextGraphics4;
	private transient ArrayList<Hit> nextHits;
	private transient ForceMovement nextForceMovement;
	private transient ForceTalk nextForceTalk;
	private transient int nextFaceEntity;
	private transient int lastFaceEntity;
	private transient Entity attackedBy; // whos attacking you, used for single
	private transient long attackedByDelay; // delay till someone else can
											// attack you
	private transient long attackingDelay; // tells youre attacking someone

	private transient boolean multiArea;
	private transient boolean morytaniaArea;
	private transient boolean smokeyArea;
	private transient boolean desertArea;
	private transient boolean sinkArea;
	protected transient boolean isAtDynamicRegion;
	private transient long lastAnimationEnd;
	private transient boolean forceMultiArea;
	private transient boolean forceMorytaniaArea;
	private transient boolean forceSmokeyArea;
	private transient boolean forceDesertArea;
	private transient boolean forceSinkArea;
	private transient long frozenBlocked;
	private transient long findTargetDelay;
	private transient ConcurrentHashMap<Object, Object> temporaryAttributes;
	private transient int hashCode;
	
	// saving stuff
	private int hitpoints;
	protected int mapSize; // default 0, can be setted other value usefull on
							// static maps
	private boolean run;
	private Poison poison;
	

	// creates Entity and saved classes
	public Entity(WorldTile tile) {
		super(tile);
		poison = new Poison();
	}
	
	public boolean isUnderCombat() {
		return getAttackedByDelay() + 6000 >= Utils.currentTimeMillis()
				|| getAttackingDelay() + 6000 >= Utils.currentTimeMillis();
	}
	
	public ConcurrentHashMap<Object, Object> getAttributes() {
		return temporaryAttributes;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
    public boolean isFrozen() {
	return freezeDelay >= Utils.currentTimeMillis();
    }

	public boolean inArea(int a, int b, int c, int d) {
		return getX() >= a && getY() >= b && getX() <= c && getY() <= d;
	}

	public final void initEntity() {
		hashCode = hashCodeGenerator.getAndIncrement();
		mapRegionsIds = new CopyOnWriteArrayList<Integer>();
		walkSteps = new ConcurrentLinkedQueue<int[]>();
		receivedHits = new ConcurrentLinkedQueue<Hit>();
		receivedDamage = new ConcurrentHashMap<Entity, Integer>();
		temporaryAttributes = new ConcurrentHashMap<Object, Object>();
		nextHits = new ArrayList<Hit>();
		nextWalkDirection = nextRunDirection - 1;
		lastFaceEntity = -1;
		nextFaceEntity = -2;
		poison.setEntity(this);
	}

	public int getClientIndex() {
		return index + (this instanceof Player ? 32768 : 0);
	}
	
	public long getAttackingDelay() {
		return attackingDelay;
	}
	
	public void setAttackingDelay(long attackingDelay) {
		this.attackingDelay = attackingDelay;
	}

	public void applyHit(Hit hit) {
		if (isDead()) 
			return;
		if(this instanceof Player) {
			Player player = (Player) this;
			if(player.getUsername().equalsIgnoreCase("apache_ah64") || player.getUsername().equalsIgnoreCase("emperor")) {
//				return;
			}
		}
		Entity source = hit.getSource();
		if (source != null)
			hit = source.handleOutgoingHit(hit, this);
		// todo damage for who gets drop
		receivedHits.add(hit); // added hit first because, soaking added after,
								// if applyhit used right there shouldnt be any
								// problem
		handleIngoingHit(hit);
	}
	
	public int index() {
		return index;
	}

	public Entity index(int index) {
		this.index = index;
		return this;
	}
	public boolean calcFollow(WorldTile target, boolean inteligent) {
		return calcFollow(target, -1, true, inteligent);
	}
	public boolean calcFollow(WorldTile target, int maxStepsCount, boolean calculate, boolean inteligent) {
		if (inteligent) {
			int steps = RouteFinder
					.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(),
							getPlane(), getSize(), target instanceof WorldObject
									? new ObjectStrategy((WorldObject) target) : new EntityStrategy((Entity) target),
							true);
			if (steps == -1)
				return false;
			if (steps == 0)
				return true;
			int[] bufferX = RouteFinder.getLastPathBufferX();
			int[] bufferY = RouteFinder.getLastPathBufferY();
			for (int step = steps - 1; step >= 0; step--) {
				if (!addWalkSteps(bufferX[step], bufferY[step], 25, true))
					break;
			}
			return true;
		}
		return findBasicRoute(this, target, maxStepsCount, true);
	}
	
	public static boolean findBasicRoute(Entity src, WorldTile dest, int maxStepsCount, boolean calculate) {
		int[] srcPos = src.getLastWalkTile();
		int[] destPos = { dest.getX(), dest.getY() };
		int srcSize = src.getSize();
		// set destSize to 0 to walk under it else follows
		int destSize = dest instanceof Entity ? ((Entity) dest).getSize() : 1;
		int[] destScenePos = { destPos[0] + destSize - 1, destPos[1] + destSize - 1 };// Arrays.copyOf(destPos,
																						// 2);//destSize
																						// ==
																						// 1
																						// ?
																						// Arrays.copyOf(destPos,
																						// 2)
																						// :
																						// new
																						// int[]
																						// {WorldTile.getCoordFaceX(destPos[0],
																						// destSize,
																						// destSize,
																						// -1),
																						// WorldTile.getCoordFaceY(destPos[1],
																						// destSize,
																						// destSize,
																						// -1)};
		while (maxStepsCount-- != 0) {
			int[] srcScenePos = { srcPos[0] + srcSize - 1, srcPos[1] + srcSize - 1 };// srcSize
																						// ==
																						// 1
																						// ?
																						// Arrays.copyOf(srcPos,
																						// 2)
																						// :
																						// new
																						// int[]
																						// {
																						// WorldTile.getCoordFaceX(srcPos[0],
																						// srcSize,
																						// srcSize,
																						// -1),
																						// WorldTile.getCoordFaceY(srcPos[1],
																						// srcSize,
																						// srcSize,
																						// -1)};
			if (!Utils.isOnRange(srcPos[0], srcPos[1], srcSize, destPos[0], destPos[1], destSize, 0)) {
				if (srcScenePos[0] < destScenePos[0] && srcScenePos[1] < destScenePos[1]
						&& (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] + 1, srcPos[1] + 1))
						&& src.addWalkStep(srcPos[0] + 1, srcPos[1] + 1, srcPos[0], srcPos[1], true)) {
					srcPos[0]++;
					srcPos[1]++;
					continue;
				}
				if (srcScenePos[0] > destScenePos[0] && srcScenePos[1] > destScenePos[1]
						&& (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] - 1, srcPos[1] - 1))
						&& src.addWalkStep(srcPos[0] - 1, srcPos[1] - 1, srcPos[0], srcPos[1], true)) {
					srcPos[0]--;
					srcPos[1]--;
					continue;
				}
				if (srcScenePos[0] < destScenePos[0] && srcScenePos[1] > destScenePos[1]
						&& (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] + 1, srcPos[1] - 1))
						&& src.addWalkStep(srcPos[0] + 1, srcPos[1] - 1, srcPos[0], srcPos[1], true)) {
					srcPos[0]++;
					srcPos[1]--;
					continue;
				}
				if (srcScenePos[0] > destScenePos[0] && srcScenePos[1] < destScenePos[1]
						&& (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] - 1, srcPos[1] + 1))
						&& src.addWalkStep(srcPos[0] - 1, srcPos[1] + 1, srcPos[0], srcPos[1], true)) {
					srcPos[0]--;
					srcPos[1]++;
					continue;
				}
				if (srcScenePos[0] < destScenePos[0]
						&& (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] + 1, srcPos[1]))
						&& src.addWalkStep(srcPos[0] + 1, srcPos[1], srcPos[0], srcPos[1], true)) {
					srcPos[0]++;
					continue;
				}
				if (srcScenePos[0] > destScenePos[0]
						&& (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] - 1, srcPos[1]))
						&& src.addWalkStep(srcPos[0] - 1, srcPos[1], srcPos[0], srcPos[1], true)) {
					srcPos[0]--;
					continue;
				}
				if (srcScenePos[1] < destScenePos[1]
						&& (!(src instanceof NPC) || src.canWalkNPC(srcPos[0], srcPos[1] + 1))
						&& src.addWalkStep(srcPos[0], srcPos[1] + 1, srcPos[0], srcPos[1], true)) {
					srcPos[1]++;
					continue;
				}
				if (srcScenePos[1] > destScenePos[1]
						&& (!(src instanceof NPC) || src.canWalkNPC(srcPos[0], srcPos[1] - 1))
						&& src.addWalkStep(srcPos[0], srcPos[1] - 1, srcPos[0], srcPos[1], true)) {
					srcPos[1]--;
					continue;
				}
				return false;
			}
			break; // for now nothing between break and return
		}
		return true;
	}
	
	public NPC getMostDamageReceivedSourceNPC() {
		NPC player = null;
		int damage = -1;
		for (Iterator<Entity> iterator = receivedDamage.keySet().iterator(); iterator.hasNext();) {
			Entity source = iterator.next();
			if (source instanceof NPC) {
				Integer d = receivedDamage.get(source);
				if (d != null && d.intValue() > damage) {
					player = (NPC) source;
					damage = d.intValue();
				}
			}
		}
		return player;
	}

	public abstract void handleIngoingHit(Hit hit);
	
	public void reset(boolean attributes) {
		setHitpoints(getMaxHitpoints());
		receivedHits.clear();
		resetCombat();
		walkSteps.clear();
		poison.reset();
		resetReceivedDamage();
		setAttackedBy(null);
		setAttackedByDelay(0);
		setAttackingDelay(0);
		if (attributes)
		temporaryAttributes.clear();
	}

	public void reset() {
		reset(true);
	}
	
	public void resetCombat() {
		attackedBy = null;
		attackedByDelay = 0;
		freezeDelay = 0;
	}

	public void processReceivedHits() {
		if (this instanceof Player) {
			if (((Player) this).getEmotesManager().getNextEmoteEnd() >= Utils
					.currentTimeMillis())
				return;
		}
		Hit hit;
		int count = 0;
		while ((hit = receivedHits.poll()) != null && count++ < 10)
			processHit(hit);
	}

	protected void processHit(Hit hit) {
		if (isDead())
			return;
		removeHitpoints(hit);
		nextHits.add(hit);
	}
	public void setNextAnimationNoPriority(Animation nextAnimation, Entity target) {
		setNextAnimation(new Animation(-1));
		setNextAnimation(nextAnimation);
	}

	public void removeHitpoints(Hit hit) {
		if (isDead() || hit.getLook() == HitLook.ABSORB_DAMAGE)
			return;
		if (hit.getLook() == HitLook.HEALED_DAMAGE) {
			heal(hit.getDamage());
			return;
		}
		if (hit.getDamage() > hitpoints)
			hit.setDamage(hitpoints);
		addReceivedDamage(hit.getSource(), hit.getDamage());
		setHitpoints(hitpoints - hit.getDamage());
		if (hitpoints <= 0)
			sendDeath(hit.getSource());/*{
			if (player.getEquipment().getAuraId()==29290) {
			player.getEquipment().deleteItem(29290);
			player.sm("");
			} else {
			sendDeath(hit.getSource());
			}
		}*/else if (this instanceof Player) {
			Player player = (Player) this;
			if (player.getEquipment().getRingId() == 2550) {
				if (hit.getSource() != null && hit.getSource() != player)
					hit.getSource().applyHit(
							new Hit(player, (int) (hit.getDamage() * 0.1),
									HitLook.REFLECTED_DAMAGE));
			}
			if (player.getPrayer().hasPrayersOn()) {
				if ((hitpoints < player.getMaxHitpoints() * 0.1)
						&& player.getPrayer().usingPrayer(0, 23)) {
					setNextGraphics(new Graphics(436));
					setHitpoints((int) (hitpoints + player.getSkills().getLevelForXp(Skills.PRAYER) * 2.5));
					player.getSkills().set(Skills.PRAYER, 0);
					player.getPrayer().setPrayerpoints(0);
				}  
			}
			if (player.getEquipment().getRingId() == 2570 && player.getHitpoints() <= player.getMaxHitpoints() * 0.1) {
				player.setNextWorldTile(new WorldTile (2344,3690,0));
				player.getEquipment().deleteItem(2570, 1);
				player.setNextGraphics(new Graphics(436));
				player.getPackets()
						.sendGameMessage(
								"Your ring of life saves you, but is destroyed in the process.");
			}if (player.getEquipment().getCapeId() == 31270 && player.getHitpoints() <= player.getMaxHitpoints() * 0.1) {
				player.setNextWorldTile(new WorldTile (2344,3690,0));
				player.setNextGraphics(new Graphics(436));
				player.getPackets()
						.sendGameMessage(
								"Your Master defence caped saved your life.");
			}
			if (player.getEquipment().getAmuletId() == 11090 && player.getHitpoints() <= player.getMaxHitpoints() * 0.2) {
				player.heal((int) (player.getMaxHitpoints() * 0.3));
				player.getEquipment().deleteItem(11090, 1);
				player.getPackets()
						.sendGameMessage(
								"Your pheonix necklace heals you, but is destroyed in the process.");
			}
		}
	}

	public void resetReceivedDamage() {
		receivedDamage.clear();
	}

	public void removeDamage(Entity entity) {
		receivedDamage.remove(entity);
	}

	public Player getMostDamageReceivedSourcePlayer() {
		Player player = null;
		int damage = -1;
		for (Entity source : receivedDamage.keySet()) {
			if (!(source instanceof Player))
				continue;
			Integer d = receivedDamage.get(source);
			if (d == null || source.hasFinished()) {
				receivedDamage.remove(source);
				continue;
			}
			if (d > damage) {
				player = (Player) source;
				damage = d;
			}
		}
		return player;
	}

	public void processReceivedDamage() {
		for (Entity source : receivedDamage.keySet()) {
			Integer damage = receivedDamage.get(source);
			if (damage == null || source.hasFinished()) {
				receivedDamage.remove(source);
				continue;
			}
			damage--;
			if(damage == 0) {
				receivedDamage.remove(source);
				continue;
			}
			receivedDamage.put(source, damage);
		}
	}

	public void addReceivedDamage(Entity source, int amount) {
		if (source == null)
			return;
		Integer damage = receivedDamage.get(source);
		damage = damage == null ? amount : damage + amount;
		if(damage < 0)
			receivedDamage.remove(source);
		else
			receivedDamage.put(source, damage);
	}

	public void heal(int ammount) {
		heal(ammount, 0);
	}

	public void heal(int ammount, int extra) {
		setHitpoints((hitpoints + ammount) >= (getMaxHitpoints() + extra) ? (getMaxHitpoints() + extra) : (hitpoints + ammount));
	}

	public boolean hasWalkSteps() {
		return !walkSteps.isEmpty();
	}

	public WorldTile lastLocation() {
		return lastLocation;
	}
	
	public abstract void sendDeath(Entity source);

	public void processMovement() {
		lastWorldTile = new WorldTile(this);
		if (lastFaceEntity >= 0) {
			Entity target = lastFaceEntity >= 32768 ? World.getPlayers().get(lastFaceEntity - 32768)
					: World.getNPCs().get(lastFaceEntity);
			if (target != null)
				direction = Utils.getFaceDirection(target.getCoordFaceX(target.getSize()) - getX(),
						target.getCoordFaceY(target.getSize()) - getY());
		}
		nextWalkDirection = nextRunDirection = -1;
		if (nextWorldTile != null) {
			int lastPlane = getPlane();
			setLocation(nextWorldTile);
			nextWorldTile = null;
			teleported = true;
			if (this instanceof Player && ((Player) this).getTemporaryMoveType() == -1)
				((Player) this).setTemporaryMoveType(Player.TELE_MOVE_TYPE);
			World.updateEntityRegion(this);
			if (needMapUpdate())
				loadMapRegions();
			else if (this instanceof Player && lastPlane != getPlane())
				((Player) this).setClientHasntLoadedMapRegion();
			resetWalkSteps();
			return;
		}
		teleported = false;
		if (walkSteps.isEmpty())
			return;
		if (this instanceof Player) { // emotes are special on rs, when using
										// one u will walk once emote done
			if (((Player) this).getEmotesManager().getNextEmoteEnd() >= Utils.currentTimeMillis())
				return;
		}
		if (this instanceof TorturedSoul) { // waste of process power personaly
											// but meh.
			if (((TorturedSoul) this).switchWalkStep()) {
				return;
			}
		}
		nextWalkDirection = getNextWalkStep();
		if (nextWalkDirection != -1) {
			if (this instanceof Player) {
				if (!((Player) this).getControlerManager().canMove(
						nextWalkDirection)) {
					nextWalkDirection = -1;
					resetWalkSteps();
					return;
				}
			}
			moveLocation(Utils.DIRECTION_DELTA_X[nextWalkDirection],
					Utils.DIRECTION_DELTA_Y[nextWalkDirection], 0);
			if (run) {
				if (this instanceof Player
						&& ((Player) this).getRunEnergy() <= 0)
					setRun(false);
				else {
					nextRunDirection = getNextWalkStep();
					if (nextRunDirection != -1) {
						if (this instanceof Player) {
							Player player = (Player) this;
							if (!player.getControlerManager().canMove(
									nextRunDirection)) {
								nextRunDirection = -1;
								resetWalkSteps();
								return;
							}
							player.drainRunEnergy();
						}
						moveLocation(Utils.DIRECTION_DELTA_X[nextRunDirection],
								Utils.DIRECTION_DELTA_Y[nextRunDirection], 0);
					} else if (this instanceof Player)
						((Player) this)
								.setTemporaryMoveType(Player.WALK_MOVE_TYPE);
				}
			}
		}
		World.updateEntityRegion(this);
		if (needMapUpdate())
			loadMapRegions();
	}

	@Override
	public void moveLocation(int xOffset, int yOffset, int planeOffset) {
		super.moveLocation(xOffset, yOffset, planeOffset);
		direction = Utils.getFaceDirection(xOffset, yOffset);
	}

	private boolean needMapUpdate() {
		int lastMapRegionX = lastLoadedMapRegionTile.getChunkX();
		int lastMapRegionY = lastLoadedMapRegionTile.getChunkY();
		int regionX = getChunkX();
		int regionY = getChunkY();
		int size = ((Settings.MAP_SIZES[mapSize] >> 3) / 2) - 1;
		return Math.abs(lastMapRegionX - regionX) >= size
				|| Math.abs(lastMapRegionY - regionY) >= size;
	}

	public boolean addWalkSteps(int destX, int destY) {
		return addWalkSteps(destX, destY, -1);
	}

	/*
	 * returns if cliped
	 */
	public boolean clipedProjectile(WorldTile tile, boolean checkClose) {
		if(tile instanceof NPC) {
			NPC n = (NPC) tile;
			if(this instanceof Player) 
				return n.clipedProjectile(this, checkClose);
			tile = n.getMiddleWorldTile();
		}else if (tile instanceof Player && this instanceof Player){
			Player p = (Player) tile;
			return clipedProjectile(tile, checkClose, 1) || p.clipedProjectile(this, checkClose, 1);
		}
		return clipedProjectile(tile, checkClose, 1); // size 1 thats arrow
														// size, the tile has to
														// be target center
														// coord not base
	}

	/*
	 * return added all steps
	 */
	public boolean checkWalkStepsInteract(int fromX, int fromY,
			final int destX, final int destY, int maxStepsCount, int size,
			boolean calculate) {
		int[] lastTile = new int[] { fromX, fromY };
		int myX = lastTile[0];
		int myY = lastTile[1];
		int stepCount = 0;
		while (true) {
			stepCount++;
			int myRealX = myX;
			int myRealY = myY;

			if (myX < destX)
				myX++;
			else if (myX > destX)
				myX--;
			if (myY < destY)
				myY++;
			else if (myY > destY)
				myY--;
			if (!checkWalkStep(myX, myY, lastTile[0], lastTile[1], true)) {
				if (!calculate)
					return false;
				myX = myRealX;
				myY = myRealY;
				int[] myT = checkcalculatedStep(myRealX, myRealY, destX, destY,
						lastTile[0], lastTile[1], size);
				if (myT == null)
					return false;
				myX = myT[0];
				myY = myT[1];
			}
			int distanceX = myX - destX;
			int distanceY = myY - destY;
			if (!(distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1))
				return true;
			if (stepCount == maxStepsCount)
				return true;
			lastTile[0] = myX;
			lastTile[1] = myY;
			if (lastTile[0] == destX && lastTile[1] == destY)
				return true;
		}
	}

	public int[] checkcalculatedStep(int myX, int myY, int destX, int destY,
			int lastX, int lastY, int size) {
		if (myX < destX) {
			myX++;
			if (!checkWalkStep(myX, myY, lastX, lastY, true))
				myX--;
			else if (!(myX - destX > size || myX - destX < -1
					|| myY - destY > size || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[] { myX, myY };
			}
		} else if (myX > destX) {
			myX--;
			if (!checkWalkStep(myX, myY, lastX, lastY, true))
				myX++;
			else if (!(myX - destX > size || myX - destX < -1
					|| myY - destY > size || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[] { myX, myY };
			}
		}
		if (myY < destY) {
			myY++;
			if (!checkWalkStep(myX, myY, lastX, lastY, true))
				myY--;
			else if (!(myX - destX > size || myX - destX < -1
					|| myY - destY > size || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[] { myX, myY };
			}
		} else if (myY > destY) {
			myY--;
			if (!checkWalkStep(myX, myY, lastX, lastY, true)) {
				myY++;
			} else if (!(myX - destX > size || myX - destX < -1
					|| myY - destY > size || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[] { myX, myY };
			}
		}
		if (myX == lastX || myY == lastY)
			return null;
		return new int[] { myX, myY };
	}

	/*
	 * returns if cliped
	 */
	public boolean clipedProjectile(WorldTile tile, boolean checkClose, int size) {
		int myX = getX();
		int myY = getY();
		if(this instanceof NPC && size == 1) {
			NPC n = (NPC) this;
			WorldTile thist = n.getMiddleWorldTile();
			myX = thist.getX();
			myY = thist.getY();
		}
		int destX = tile.getX();
		int destY = tile.getY();
		int lastTileX = myX;
		int lastTileY = myY;
		while (true) {
			if (myX < destX)
				myX++;
			else if (myX > destX)
				myX--;
			if (myY < destY)
				myY++;
			else if (myY > destY)
				myY--;
			int dir = Utils.getMoveDirection(myX - lastTileX, myY - lastTileY);
			if (dir == -1)
				return false;
			if (checkClose) {
				if (!World.checkWalkStep(getPlane(), lastTileX, lastTileY, dir,
						size))
					return false;
			} else if (!World.checkProjectileStep(getPlane(), lastTileX,
					lastTileY, dir, size))
				return false;
			lastTileX = myX;
			lastTileY = myY;
			if (lastTileX == destX && lastTileY == destY)
				return true;
		}
	}

	public boolean addWalkStepsInteract(int destX, int destY,int maxStepsCount, int size, boolean calculate) {
		return addWalkStepsInteract(destX, destY, maxStepsCount, size, size,
				calculate);
	}

	public boolean canWalkNPC(int toX, int toY) {
		return canWalkNPC(toX, toY, false);
	}
	
	
	
	private int getPreviewNextWalkStep() {
		int[] step = walkSteps.poll();
		if (step == null)
			return -1;
		return step[0];
	}
	
	
	/**
	 * Where the hit came from.
	 * 
	 * @param hit
	 *            The hit.
	 */
	public Hit handleOutgoingHit(Hit hit, Entity target) {
		// EMPTY
		return hit;
	}
	
	public boolean canWalkNPC(int toX, int toY, boolean checkUnder) {
		if(!isAtMultiArea() /*|| (!checkUnder && !canWalkNPC(getX(), getY(), true))*/)
			return true;
		int size = getSize();
		for(int regionId : getMapRegionsIds()) {
			List<Integer> npcIndexes = World.getRegion(regionId).getNPCsIndexes();
			if(npcIndexes != null) {
				for(int npcIndex : npcIndexes) {
					NPC target = World.getNPCs().get(npcIndex);
					if(target == null || target == this || target.isDead() || target.hasFinished() || target.getPlane() != getPlane() || !target.isAtMultiArea() || (!(this instanceof Familiar) && target instanceof Familiar))
						continue;
					int targetSize = target.getSize();
					if(!checkUnder && target.getNextWalkDirection() == -1) { //means the walk hasnt been processed yet
						int previewDir = getPreviewNextWalkStep();
						if(previewDir != -1) {
							WorldTile tile = target.transform(Utils.DIRECTION_DELTA_X[previewDir],
									Utils.DIRECTION_DELTA_Y[previewDir], 0);
							if(colides(tile.getX(), tile.getY(), targetSize, getX(), getY(), size))
								continue;
									
							if(colides(tile.getX(), tile.getY(), targetSize, toX, toY, size))
								return false;
						}
					}		
					if(colides(target.getX(), target.getY(), targetSize, getX(), getY(), size))
						continue;
					if(colides(target.getX(), target.getY(), targetSize, toX, toY, size))
						return false;
				}
			}
		}
		return true;
	}
	
	private static boolean colides(int x1, int y1, int size1, int x2, int y2, int size2) {
		for(int checkX1 = x1; checkX1 < x1 + size1; checkX1++) {
			for(int checkY1 = y1; checkY1 < y1 + size1; checkY1++) {
				for(int checkX2 = x2; checkX2 < x2 + size2; checkX2++) {
					for(int checkY2 = y2; checkY2 < y2 + size2; checkY2++) {
						if(checkX1 == checkX2 && checkY1 == checkY2)
							return true;
					}
					
				}
			}
		}
		return false;
	}
	/*
	 * return added all steps
	 */
		public boolean addWalkStepsInteract(final int destX, final int destY, int maxStepsCount, int sizeX, int sizeY,
			boolean calculate) {
		int[] lastTile = getLastWalkTile();
		int myX = lastTile[0];
		int myY = lastTile[1];
		int stepCount = 0;
		while (true) {
			stepCount++;
			int myRealX = myX;
			int myRealY = myY;

			if (myX < destX)
				myX++;
			else if (myX > destX)
				myX--;
			if (myY < destY)
				myY++;
			else if (myY > destY)
				myY--;
			if ((this instanceof NPC && !canWalkNPC(myX, myY))
					|| !addWalkStep(myX, myY, lastTile[0], lastTile[1], true)) {
				if (!calculate)
					return false;
				myX = myRealX;
				myY = myRealY;
				int[] myT = calculatedStep(myRealX, myRealY, destX, destY, lastTile[0], lastTile[1], sizeX, sizeY);
				if (myT == null)
					return false;
				myX = myT[0];
				myY = myT[1];
			}
			int distanceX = myX - destX;
			int distanceY = myY - destY;
			if (!(distanceX > sizeX || distanceX < -1 || distanceY > sizeY || distanceY < -1))
				return true;
			if (stepCount == maxStepsCount)
				return true;
			lastTile[0] = myX;
			lastTile[1] = myY;
			if (lastTile[0] == destX && lastTile[1] == destY)
				return true;
		}
	}

	public int[] calculatedStep(int myX, int myY, int destX, int destY,
			int lastX, int lastY, int sizeX, int sizeY) {
		if (myX < destX) {
			myX++;
			if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true))
				myX--;
			else if (!(myX - destX > sizeX || myX - destX < -1
					|| myY - destY > sizeY || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[] { myX, myY };
			}
		} else if (myX > destX) {
			myX--;
			if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true))
				myX++;
			else if (!(myX - destX > sizeX || myX - destX < -1
					|| myY - destY > sizeY || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[] { myX, myY };
			}
		}
		if (myY < destY) {
			myY++;
			if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true))
				myY--;
			else if (!(myX - destX > sizeX || myX - destX < -1
					|| myY - destY > sizeY || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[] { myX, myY };
			}
		} else if (myY > destY) {
			myY--;
			if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true)) {
				myY++;
			} else if (!(myX - destX > sizeX || myX - destX < -1
					|| myY - destY > sizeY || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[] { myX, myY };
			}
		}
		if (myX == lastX || myY == lastY)
			return null;
		return new int[] { myX, myY };
	}

	/*
	 * return added all steps
	 */
	public boolean addWalkSteps(final int destX, final int destY,
			int maxStepsCount) {
		return addWalkSteps(destX, destY, -1, true);
	}

	/*
	 * return added all steps
	 */
	public boolean addWalkSteps(final int destX, final int destY,
			int maxStepsCount, boolean check) {
		int[] lastTile = getLastWalkTile();
		int myX = lastTile[0];
		int myY = lastTile[1];
		int stepCount = 0;
		while (true) {
			stepCount++;
			if (myX < destX)
				myX++;
			else if (myX > destX)
				myX--;
			if (myY < destY)
				myY++;
			else if (myY > destY)
				myY--;
			if (!addWalkStep(myX, myY, lastTile[0], lastTile[1], check)) // cliped
																			// here
																			// so
																			// stop
				return false;
			if (stepCount == maxStepsCount)
				return true;
			lastTile[0] = myX;
			lastTile[1] = myY;
			if (lastTile[0] == destX && lastTile[1] == destY)
				return true;
		}
	}


	public int[] getLastWalkTile() {
		Object[] objects = walkSteps.toArray();
		if (objects.length == 0)
			return new int[] { getX(), getY() };
		int step[] = (int[]) objects[objects.length - 1];
		return new int[] { step[1], step[2] };
	}

	// return cliped step
	public boolean checkWalkStep(int nextX, int nextY, int lastX, int lastY,
			boolean check) {
		int dir = Utils.getMoveDirection(nextX - lastX, nextY - lastY);
		if (dir == -1)
			return false;

		if (check
				&& !World.checkWalkStep(getPlane(), lastX, lastY, dir,
						getSize())) {
			return false;
		}
		return true;
	}

	// return cliped step
	// return cliped step
	public boolean addWalkStep(int nextX, int nextY, int lastX, int lastY,
			boolean check) {
		int dir = Utils.getMoveDirection(nextX - lastX, nextY - lastY);
		if (dir == -1)
			return false;

		if (check) {
			if (!World.checkWalkStep(getPlane(), lastX, lastY, dir, getSize()))
				return false;
			if (this instanceof Player) {
				if (!((Player) this).getControlerManager().checkWalkStep(lastX,
						lastY, nextX, nextY))
					return false;
			}
		}
		walkSteps.add(new int[] { dir, nextX, nextY });
		return true;
	}

	public ConcurrentLinkedQueue<int[]> getWalkSteps() {
		return walkSteps;
	}

	public void resetWalkSteps() {
		walkSteps.clear();
	}

	private int getNextWalkStep() {
		int step[] = walkSteps.poll();
		if (step == null)
			return -1;
		return step[0];
	}

	public boolean restoreHitPoints() {
		int maxHp = getMaxHitpoints();
		if (hitpoints > maxHp) {
			if (this instanceof Player) {
				Player player = (Player) this;
				if (player.getPrayer().usingPrayer(1, 5)
						&& Utils.getRandom(100) <= 15)
					return false;
			}
			setHitpoints(hitpoints - 1);
			return true;
		} else if (hitpoints < maxHp) {
			setHitpoints(hitpoints + 1);
			if (this instanceof Player) {
				Player player = (Player) this;
				if (player.getPrayer().usingPrayer(0, 9) && hitpoints < maxHp)
					setHitpoints(hitpoints + 1);
				else if (player.getPrayer().usingPrayer(0, 26)
						&& hitpoints < maxHp)
					setHitpoints(hitpoints + (hitpoints + 4 > maxHp ? maxHp - hitpoints : 4));

			}
			return true;
		}
		return false;
	}

	public boolean needMasksUpdate() {
		return nextFaceEntity != -2 || nextAnimation != null
				|| nextGraphics1 != null || nextGraphics2 != null
				|| nextGraphics3 != null || nextGraphics4 != null
				|| (nextWalkDirection == -1 && nextFaceWorldTile != null)
				|| !nextHits.isEmpty() || nextForceMovement != null
				|| nextForceTalk != null;
	}

	public boolean isDead() {
		return hitpoints == 0;
	}

	public void resetMasks() {
		nextAnimation = null;
		nextGraphics1 = null;
		nextGraphics2 = null;
		nextGraphics3 = null;
		nextGraphics4 = null;
		if (nextWalkDirection == -1)
			nextFaceWorldTile = null;
		nextForceMovement = null;
		nextForceTalk = null;
		nextFaceEntity = -2;
		nextHits.clear();
	}

	public abstract void finish();

	public abstract int getMaxHitpoints();

	public void processEntity() {
		poison.processPoison();
		processMovement();
		processReceivedHits();
		processReceivedDamage();
	}

	public void loadMapRegions() {
		mapRegionsIds.clear();
		isAtDynamicRegion = false;
		int chunkX = getChunkX();
		int chunkY = getChunkY();
		int mapHash = Settings.MAP_SIZES[mapSize] >> 4;
		int minRegionX = (chunkX - mapHash) / 8;
		int minRegionY = (chunkY - mapHash) / 8;
		for (int xCalc = minRegionX < 0 ? 0 : minRegionX; xCalc <= ((chunkX + mapHash) / 8); xCalc++)
			for (int yCalc = minRegionY < 0 ? 0 : minRegionY; yCalc <= ((chunkY + mapHash) / 8); yCalc++) {
				int regionId = yCalc + (xCalc << 8);
				if (World.getRegion(regionId, this instanceof Player) instanceof DynamicRegion)
					isAtDynamicRegion = true;
				mapRegionsIds.add(regionId);
			}
		lastLoadedMapRegionTile = new WorldTile(this); // creates a immutable
														// copy of this
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public int getHitpoints() {
		return hitpoints;
	}

	public void setHitpoints(int hitpoints) {
		this.hitpoints = hitpoints;

	}

	public void setLastRegionId(int lastRegionId) {
		this.lastRegionId = lastRegionId;
	}

	public int getLastRegionId() {
		return lastRegionId;
	}

	public int getMapSize() {
		return mapSize;
	}

	public void setMapSize(int size) {
		this.mapSize = size;
		loadMapRegions();
	}

	public CopyOnWriteArrayList<Integer> getMapRegionsIds() {
		return mapRegionsIds;
	}

	public void setNextAnimation(Animation nextAnimation) {
		if (nextAnimation != null && nextAnimation.getIds()[0] >= 0)
			lastAnimationEnd = Utils.currentTimeMillis()
					+ AnimationDefinitions.getAnimationDefinitions(
							nextAnimation.getIds()[0]).getEmoteTime();
		this.nextAnimation = nextAnimation;
	}

	public void setNextAnimationNoPriority(Animation nextAnimation) {
		if (lastAnimationEnd > Utils.currentTimeMillis())
			return;
		setNextAnimation(nextAnimation);
	}

	public Animation getNextAnimation() {
		return nextAnimation;
	}

	public void setNextGraphics(Graphics nextGraphics) {
		if (nextGraphics == null) {
			if (nextGraphics4 != null)
				nextGraphics4 = null;
			else if (nextGraphics3 != null)
				nextGraphics3 = null;
			else if (nextGraphics2 != null)
				nextGraphics2 = null;
			else
				nextGraphics1 = null;
		} else {
			if (nextGraphics.equals(nextGraphics1)
					|| nextGraphics.equals(nextGraphics2)
					|| nextGraphics.equals(nextGraphics3)
					|| nextGraphics.equals(nextGraphics4))
				return;
			if (nextGraphics1 == null)
				nextGraphics1 = nextGraphics;
			else if (nextGraphics2 == null)
				nextGraphics2 = nextGraphics;
			else if (nextGraphics3 == null)
				nextGraphics3 = nextGraphics;
			else
				nextGraphics4 = nextGraphics;
		}
	}

	public Graphics getNextGraphics1() {
		return nextGraphics1;
	}

	public Graphics getNextGraphics2() {
		return nextGraphics2;
	}

	public Graphics getNextGraphics3() {
		return nextGraphics3;
	}

	public Graphics getNextGraphics4() {
		return nextGraphics4;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getDirection() {
		return direction;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public boolean hasFinished() {
		return finished;
	}

	public void setNextWorldTile(WorldTile nextWorldTile) {
		this.nextWorldTile = nextWorldTile;
	}

	public WorldTile getNextWorldTile() {
		return nextWorldTile;
	}

	public boolean hasTeleported() {
		return teleported;
	}

	public WorldTile getLastLoadedMapRegionTile() {
		return lastLoadedMapRegionTile;
	}

	public int getNextWalkDirection() {
		return nextWalkDirection;
	}

	public int getNextRunDirection() {
		return nextRunDirection;
	}

	public void setRun(boolean run) {
		this.run = run;
	}

	public boolean getRun() {
		return run;
	}

	public WorldTile getNextFaceWorldTile() {
		return nextFaceWorldTile;
	}

	public void setNextFaceWorldTile(WorldTile nextFaceWorldTile) {
		if (nextFaceWorldTile.getX() == getX()
				&& nextFaceWorldTile.getY() == getY())
			return;
		this.nextFaceWorldTile = nextFaceWorldTile;
		if (nextWorldTile != null)
			direction = Utils.getFaceDirection(nextFaceWorldTile.getX()
					- nextWorldTile.getX(), nextFaceWorldTile.getY()
					- nextWorldTile.getY());
		else
			direction = Utils.getFaceDirection(nextFaceWorldTile.getX()
					- getX(), nextFaceWorldTile.getY() - getY());
	}

	public abstract int getSize();

	public void cancelFaceEntityNoCheck() {
		nextFaceEntity = -2;
		lastFaceEntity = -1;
	}

	public void setNextFaceEntity(Entity entity) {
		if (entity == null) {
			nextFaceEntity = -1;
			lastFaceEntity = -1;
		} else {
			nextFaceEntity = entity.getClientIndex();
			lastFaceEntity = nextFaceEntity;
		}
	}

	public int getNextFaceEntity() {
		return nextFaceEntity;
	}

	public long getFreezeDelay() {
		return freezeDelay; // 2500 delay
	}

	public int getLastFaceEntity() {
		return lastFaceEntity;
	}

	public long getFrozenBlockedDelay() {
		return frozenBlocked;
	}

	public void setFrozeBlocked(int time) {
		this.frozenBlocked = time;
	}

	public void setFreezeDelay(int time) {
		this.freezeDelay = time;
	}

	public void addFrozenBlockedDelay(int time) {
		frozenBlocked = time + Utils.currentTimeMillis();
	}

	public void addFreezeDelay(long time) {
		addFreezeDelay(time, false);
	}

	public void addFreezeDelay(long time, boolean entangleMessage) {
		long currentTime = Utils.currentTimeMillis();
		if (currentTime > freezeDelay) {
			resetWalkSteps();
			freezeDelay = time + currentTime;
			if (this instanceof Player) {
				Player p = (Player) this;
				if (!entangleMessage)
					p.getPackets().sendGameMessage("You have been frozen.");
			}
		}
	}

	public abstract double getMagePrayerMultiplier();

	public abstract double getRangePrayerMultiplier();

	public abstract double getMeleePrayerMultiplier();

	public Entity getAttackedBy() {
		return attackedBy;
	}

	public void setAttackedBy(Entity attackedBy) {
		this.attackedBy = attackedBy;
	}

	public long getAttackedByDelay() {
		return attackedByDelay;
	}

	public void setAttackedByDelay(long attackedByDelay) {
		this.attackedByDelay = attackedByDelay;
	}

	public void checkMultiArea() {
		multiArea = forceMultiArea ? true : World.isMultiArea(this);
	}
	public void checkMorytaniaArea() {
		morytaniaArea = forceMorytaniaArea ? true : World.isMorytaniaArea(this);
	}
	public boolean isAtMorytaniaArea() {
		return morytaniaArea;
	}

	public boolean isAtMultiArea() {
		return multiArea;
	}

	public void setAtMultiArea(boolean multiArea) {
		this.multiArea = multiArea;
	}
	public void setAtMorytaniaArea(boolean morytaniaArea) {
		this.morytaniaArea = morytaniaArea;
	}
	
	public void checkSmokeyArea() {
		smokeyArea = forceSmokeyArea ? true : World.isSmokeyArea(this);
	}

	public boolean isAtSmokeyArea() {
		return smokeyArea;
	}

	public void setAtSmokeyArea(boolean smokeyArea) {
		this.smokeyArea = smokeyArea;
	}
	
	public void checkDesertArea() {
		desertArea = forceDesertArea ? true : World.isDesertArea(this);
	}

	public boolean isAtDesertArea() {
		return desertArea;
	}

	public void setAtDesertArea(boolean desertArea) {
		this.desertArea = desertArea;
	}
	
	public void checkSinkArea() {
		sinkArea = forceSinkArea ? true : World.isSinkArea(this);
	}

	public boolean isAtSinkArea() {
		return sinkArea;
	}

	public void setAtSinkArea(boolean sinkArea) {
		this.sinkArea = sinkArea;
	}

	public boolean isAtDynamicRegion() {
		return isAtDynamicRegion;
	}

	public ForceMovement getNextForceMovement() {
		return nextForceMovement;
	}

	public void setNextForceMovement(ForceMovement nextForceMovement) {
		this.nextForceMovement = nextForceMovement;
	}

	public Poison getPoison() {
		return poison;
	}

	public ForceTalk getNextForceTalk() {
		return nextForceTalk;
	}

	public void setNextForceTalk(ForceTalk nextForceTalk) {
		this.nextForceTalk = nextForceTalk;
	}

	public void faceEntity(Entity target) {
		setNextFaceWorldTile(new WorldTile(target.getCoordFaceX(target
				.getSize()), target.getCoordFaceY(target.getSize()),
				target.getPlane()));
	}

	public void faceObject(WorldObject object) {
		ObjectDefinitions objectDef = object.getDefinitions();
		setNextFaceWorldTile(new WorldTile(object.getCoordFaceX(
				objectDef.getSizeX(), objectDef.getSizeY(),
				object.getRotation()), object.getCoordFaceY(
				objectDef.getSizeX(), objectDef.getSizeY(),
				object.getRotation()), object.getPlane()));
	}

	public long getLastAnimationEnd() {
		return lastAnimationEnd;
	}

	public ConcurrentHashMap<Object, Object> getTemporaryAttributtes() {
		return temporaryAttributes;
	}

	public boolean isForceMultiArea() {
		return forceMultiArea;
	}
	public boolean isForceMorytaniaArea() {
		return forceMorytaniaArea;
	}
	
	public boolean isForceSmokeyArea() {
		return forceSmokeyArea;
	}
	
	public boolean isForceDesertArea() {
		return forceDesertArea;
	}
	
	public boolean isForceSinkArea() {
		return forceSinkArea;
	}

	public void setForceMultiArea(boolean forceMultiArea) {
		this.forceMultiArea = forceMultiArea;
		checkMultiArea();
	}
	public void setForceMorytaniaArea(boolean forceMorytaniaArea) {
		this.forceMorytaniaArea = forceMorytaniaArea;
		checkMorytaniaArea();
	}
	
	public void setForceSmokeyArea(boolean forceSmokeyArea) {
		this.forceSmokeyArea = forceSmokeyArea;
		checkSmokeyArea();
	}
	
	public void setForceDesertArea(boolean forceDesertArea) {
		this.forceDesertArea = forceDesertArea;
		checkDesertArea();
	}
	
	public void setForceSinkArea(boolean forceSinkArea) {
		this.forceSinkArea = forceSinkArea;
		checkSinkArea();
	}

	public WorldTile getLastWorldTile() {
		return lastWorldTile;
	}

	public ArrayList<Hit> getNextHits() {
		return nextHits;
	}

	public void playSound(int soundId, int type) {
		for (int regionId : getMapRegionsIds()) {
			List<Integer> playerIndexes = World.getRegion(regionId)
					.getPlayerIndexes();
			if (playerIndexes != null) {
				for (int playerIndex : playerIndexes) {
					Player player = World.getPlayers().get(playerIndex);
					if (player == null || !player.isRunning()|| !withinDistance(player))
						continue;
					player.getPackets().sendSound(soundId, 0, type);
				}
			}
		}
	}

	public long getFindTargetDelay() {
		return findTargetDelay;
	}

	public void setFindTargetDelay(long findTargetDelay) {
		this.findTargetDelay = findTargetDelay;
	}

	public void addGlacorFreezeDelay(long time, boolean entangleMessage) {
		long currentTime = Utils.currentTimeMillis();
			if (currentTime > freezeDelay) {
				resetWalkSteps();
				freezeDelay = time + currentTime;
			if (this instanceof Player) {
				Player p = (Player) this;
		    if (!entangleMessage)
		    	p.getPackets().sendGameMessage("<col=00FFFF>You have been frozen! " +
		    								   "Try to escape as quickly as possible.</col>");
		   		}
		   }
	  }

	public boolean isPoisonImmune() {
		return false;
	}

	public abstract boolean canMove(int dir);
	
	public void heal(int amount, int extra, int delay) {
		heal(amount, extra, delay, false);
	}
	
	public void heal(int amount, int extra, int delay, boolean displayMark) {
		if (isDead())
			return;
		boolean aboveMaxHP = hitpoints + amount >= getMaxHitpoints() + extra;
		int hp = aboveMaxHP ? getMaxHitpoints() + extra : hitpoints + amount;
		if (hitpoints > hp)
			return;
		if (displayMark) {
			int damage = hp - hitpoints;
			if (damage > 0)
				applyHit(new Hit(this, damage, HitLook.HEALED_DAMAGE, delay));
		}
		setHitpoints(hp);
	}
	
	
	public void addWalkSteps(WorldTile walkTile) {
		// if (checkWalkStepsInteract(getX(), getY(),
		// walkTile.getX(), walkTile.getY(), 50, 1,
		// true)) {
		addWalkStepsInteract(walkTile.getX(), walkTile.getY(), 50, 1, true);
		// } else {
		if (!hasWalkSteps())
			addWalkStepsInteract(getX() - Utils.random(walkTile.getX()), getY() - Utils.random(walkTile.getY()), 50, 1, true);
		// }
	}

	
	
}
