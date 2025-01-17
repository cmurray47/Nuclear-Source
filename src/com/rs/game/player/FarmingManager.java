package com.rs.game.player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.achievements.impl.FarmingDummy;
import com.rs.game.player.achievements.impl.TheRealFarmerAchievement;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.Woodcutting.HatchetDefinitions;
import com.rs.game.player.actions.Woodcutting.TreeDefinitions;
import com.rs.game.player.content.perks.Perks.Perk;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class FarmingManager implements Serializable {

    private static final long serialVersionUID = -6487741852718632170L;

    private static final int REGENERATION_CONSTANT = //Settings.DEBUG ? 5000 : 
    		120000; // Twelve minutes
    private static final int ALLOTMENT = 0, TREES = 1, HOPS = 2, FLOWERS = 3,
	    FRUIT_TREES = 4, BUSHES = 5, HERBS = 6;

	public static final int COMPOST = 7;

	private static final int HERBLORE_BUSH = 8;

	private static final int HERBLORE_HERB = 9;

	private static final int HERBLORE_FLOWER = 10;

	private static final int LUCKY_PATCH = 11;

	private static final int RAKE = 5341;

	private static final int EMPTY_BUCKET = 1925;
    private static final String[] PATCH_NAMES = { "allotment", "tree", "hops", "flower", "fruit tree", "bush", "herb","vine bush", "vine herb", "vine flower","bush","bush"};
    private static final int[][] HARVEST_AMOUNTS = { { 3, 53 }, { 1, 1 }, { 3, 41 }, { 1, 3 }, { 3, 5 }, { 3, 5 }, { 3, 18 } , {4,8}, {4,8}, {1,1}, {1,1}, {1,1}, {1,1}};
    public static final int[] COMPOST_ORGANIC = { 6055, 1942, 1957, 1965, 5986, 5504, 5982, 249, 251, 253, 255, 257, 2998, 259, 261, 263, 3000, 265, 2481, 267, 269, 1951, 753, 2126, 247, 239, 6018 };
    public static final int[] SUPER_COMPOST_ORGANIC = { 2114, 5978, 5980, 5982, 6004, 247, 6469 };
    private static final Animation RAKING_ANIMATION = new Animation(24913),
	    WATERING_ANIMATION = new Animation(24924),
	    SEED_DIPPING_ANIMATION = new Animation(24922),
	    SPADE_ANIMATION = new Animation(830),
	    HERB_PICKING_ANIMATION = new Animation(2282),
	    MAGIC_PICKING_ANIMATION = new Animation(24907),
	    CURE_PLANT_ANIMATION = new Animation(2288),
	    CHECK_TREE_ANIMATION = new Animation(832),
	    PRUNING_ANIMATION = new Animation(2275),
	    FLOWER_PICKING_ANIMATION = new Animation(2292),
	    FRUIT_PICKING_ANIMATION = new Animation(24921),
	    COMPOST_ANIMATION = new Animation(24901),
	    BUSH_PICKING_ANIMATION = new Animation(24921),
	    FILL_COMPOST_ANIMATION = new Animation(832);

    private List<FarmingSpot> spots;
    private transient Player player;

    public FarmingManager() {
	spots = new CopyOnWriteArrayList<FarmingSpot>();
    }

    public void setPlayer(Player player) {
	this.player = player;
    }

    public void init() {
	for (FarmingSpot spot : spots)
	    spot.refresh();
    }

    public void process() {
	for (FarmingSpot spot : spots)
	    spot.process();
    }

   
    public static enum ProductInfo {
  
    	/**
    	 * Allotments
    	 */
    	
    	Potato(5318, 1, 1942, 0, 8, 9, 10, ALLOTMENT), Onion(5319, 5, 1957, 1, 9.5, 10.5, 10, ALLOTMENT), 
    	Cabbage(5324, 7, 1965, 2, 10, 11.5, 10, ALLOTMENT), Tomato(5322, 12, 1982, 3, 12.5, 14, 10, ALLOTMENT), 
    	Sweetcorn(5320, 20, 5986, 4, 17, 19, 10, 6, ALLOTMENT), Strawberry(5323, 31, 5504, 5, 26, 29, 10, 6, 2, ALLOTMENT), 
    	Watermelon(5321, 47, 5982, 6, 48.5, 54.5, 10, 8, 4, ALLOTMENT),

    	/**
    	 * Herbs
    	 */
    	Guam(5291, 9, 199, 0, 11, 12.5, 20, HERBS), Marrentill(5292, 14, 201, 1, 13.5, 15, 20, HERBS), 
    	Tarromin(5293, 19, 203, 2, 16, 18, 20, HERBS), Harralander(5294, 26, 205, 3, 21.5, 24, 20, HERBS), 
    	Rannar(5295, 32, 207, 4, 27, 30.5, 20, HERBS), Toadflax(5296, 38, 3049, 5, 34, 38.5, 20, HERBS), 
    	Irit(5297, 44, 209, 6, 43, 48.5, 20, HERBS), Avantoe(5298, 50, 211, 7, 54.4, 61.5, 20, HERBS), 
    	Kwuarm(5299, 56, 213, 6, 69, 78, 20, HERBS), Snapdragon(5300, 62, 3051, 6, 87.5, 98.5, 20, HERBS), 
    	Cadantine(5301, 67, 215, 6, 106.5, 120, 20, HERBS), Lantadyme(5302, 73, 2485, 6, 134.5, 151.5, 20, HERBS), 
    	Dwarf(5303, 79, 217, 6, 170.5, 192, 20, HERBS), Torstol(5304, 85, 219, 6, 199.5, 224.5, 20, HERBS), 
    	Fellstalk(21621, 91, 21626, 6, 225, 315.6, 20, HERBS), Wergali(14870, 46, 14836, 8, 52.8, 52.8, 20, HERBS), 
    	Gout(6311, 65, 3261, 27, 105, 45, 20, HERBS), Spirit(12176, 36, 12174, 6, 32, 36, 20, HERBS),

    	/**
    	 * Flowers
    	 */
    	Marigold(5096, 2, 6010, 0, 8.5, 47, 5, FLOWERS), Rosemary(5097, 11, 6014, 1, 12, 66.5, 5, FLOWERS), 
    	Nasturtium(5098, 24, 6012, 2, 19.5, 111, 5, FLOWERS), Woad(5099, 25, 1793, 3, 20.5, 115.5, 5, FLOWERS), 
    	Limpwurt(5100, 26, 225, 4, 21.5, 120, 5, FLOWERS), White_lily(14589, 52, 14583, 6, 70, 250, 20, 4, -1, FLOWERS),

    	/**
    	 * Hops
    	 */
    	Barley(5305, 3, 6006, 9, 8.5, 9.5, 10, 4, 1, HOPS), Hammerstone(5307, 4, 5994, 0, 9, 10, 10, 4, 1, HOPS), 
    	Asgarnian(5308, 8, 5996, 1, 10.9, 12, 10, 5, 3, HOPS), Jute(5306, 13, 5931, 10, 13, 14.5, 10, 5, 3, HOPS), 
    	Yanillian(5309, 16, 5998, 3, 14.5, 16, 10, 6, 1, HOPS), Krandorian(5310, 21, 6000, 5, 17.5, 19.5, 10, 7, HOPS), 
    	Wildbood(5311, 28, 6002, 7, 23, 26, 10, 7, 1, HOPS),

    	/**
    	 * Trees
    	 */
    	Oak(5370, 15, 6043, 1, 467.3, 14, 40, TREES), 
		Willow(5371, 30, 6045, 6, 1456.5, 25, 40, 6, TREES), 
    	Maple(5372, 45, 6047, 17, 3403.4, 45, 40, 8, TREES),
		Yew(5373, 60, 6049, 26, 7069.9, 81, 40, 10, TREES), 
    	Magic(5374, 75, 6051, 41, 13768.3, 145.5, 40, 12, TREES),

    	/**
    	 * Trees of the fruits :)
    	 */
    	Apple(5496, 27, 1955, 1, 1199.5, 22, 160, 6, FRUIT_TREES), 
		Banana(5497, 33, 1963, 26, 1841.5, 28, 160, 6, FRUIT_TREES), 
    	Orange(5498, 39, 2108, 65, 2470.2, 35.5, 160, 6, FRUIT_TREES), 
		Curry(5499, 42, 5970, 90, 2906.9, 40, 160, 6, FRUIT_TREES), 
    	Pineapple(5500, 51, 2114, 129, 4605.7, 57, 160, 6, FRUIT_TREES),
		Papaya(5501, 57, 5972, 154, 6146.4, 72, 160, 6, FRUIT_TREES), 
    	Palm(5502, 68, 5974, 193, 10150.1, 110.5, 160, 6, FRUIT_TREES),

    	/**
    	 * Bushes of the bush
    	 */
    	Redberry(5101, 10, 1951, -4, 64, 11.5, 20, 5, BUSHES), Cadavaberry(5102, 22, 753, 6, 102.5, 18, 20, 6, BUSHES), 
    	Dwellberry(5103, 36, 2126, 19, 177.5, 31.5, 20, 7, BUSHES), Jangerberry(5104, 48, 247, 31, 284.5, 50.5, 20, 8, BUSHES), 
    	Whiteberry(5105, 59, 239, 42, 437.5, 78, 20, 8, BUSHES), Poison_ivy(5106, 70, 6018, 188, 675, 120, 20, 8, BUSHES),
    	//int configIndex, double plantingExperience, double experience, int cycleTime, int maxStage, int stageSkip, int type) {
    	WHISHING_WELL(28267, 96, 28311, 64,437.5, 78, 20, 6,  LUCKY_PATCH), //63 248
        /*
         * compost bin
         */
    	Compost_Bin(7836, 1, -1, 0, 8, 14, 2, 15, COMPOST),
    	/**
    	 * herblore habitat
    	 *  seedId,  level,  productId,  configIndex, do plantingExperience, double experience, cycleTime,  maxStage, type
    	 */
    	//BUSHES
    	LERGBERRY(19937,61,19969,4,145.5,31.5,1,6,HERBLORE_BUSH),
    	KALFERBERRY(19942,77,19970,15,220.2,52.5,1,5,HERBLORE_BUSH),
    	/*
    	 * herbs
    	 */
    	ERZILLE(19944,58,19970,87,220.2,52.5,20,6,HERBLORE_HERB);
    	//LERGBERRY(19937,61,19969,-5,145.5,31.5,-1,-1,HERBLORE_BUSH),
    	//KALFERBERRY(19942,77,19970,6,220.2,52.5,-1,-1,HERBLORE_BUSH);


	private static Map<Short, ProductInfo> products = new HashMap<Short, ProductInfo>();

	public static ProductInfo getProduct(int itemId) {
	    return products.get((short) itemId);
	}

	static {
	    for (ProductInfo product : ProductInfo.values()) {
		products.put((short) product.seedId, product);
	    }
	}

	private int seedId;
	private int level;
	private int productId;
	private int configIndex;
	private int type;
	private int maxStage;
	private int stageSkip;
	private double experience, plantingExperience;
	private int cycleTime;

	private ProductInfo(int seedId, int level, int productId, int configIndex, double plantingExperience, double experience, int cycleTime, int maxStage, int stageSkip, int type) {
	    this.seedId = seedId;
	    this.level = level;
	    this.productId = productId;
	    this.configIndex = configIndex;
	    this.plantingExperience = plantingExperience;
	    this.experience = experience;
	    this.cycleTime = cycleTime;
	    this.maxStage = maxStage;
	    this.stageSkip = stageSkip;
	    this.type = type;
	}

	private ProductInfo(int seedId, int level, int productId, int configIndex, double plantingExperience, double experience, int cycleTime, int type) {
	    this(seedId, level, productId, configIndex, plantingExperience, experience, cycleTime, 4, 0, type);
	}

	private ProductInfo(int seedId, int level, int productId, int configIndex, double plantingExperience, double experience, int cycleTime, int maxStage, int type) {
	    this(seedId, level, productId, configIndex, plantingExperience, experience, cycleTime, maxStage, 0, type);
	}
    }

    public static enum SpotInfo {

	Talvery_Tree(8388, TREES),PRIFDINAS_Tree(93288, TREES), Falador_Garden_Tree(8389, TREES), Varrock_Tree(8390, TREES), Lumbridge_Tree(8391, TREES), Gnome_Tree(19147, TREES),

	Gnome_Strong_Fruit_Tree(7962, FRUIT_TREES), Gnome_Fruit_Tree(7963, FRUIT_TREES), Brimhaven_Fruit_Tree(7964, FRUIT_TREES), Catherby_Fruit_Tree(7965, FRUIT_TREES), Lletya_Fruit_Tree(28919, FRUIT_TREES),Herblore_Fruit_Tree(56667,FRUIT_TREES),

	Falador_Allotment_North(8550, ALLOTMENT), Falador_Allotment_South(8551, ALLOTMENT), Catherby_Allotment_North(8552, ALLOTMENT), Catherby_Allotment_South(8553, ALLOTMENT), Ardougne_Allotment_North(8554, ALLOTMENT), Ardougne_Allotment_South(8555, ALLOTMENT), Canfis_Allotment_North(8556, ALLOTMENT), Canfis_Allotment_South(8557, ALLOTMENT),

	Yannile_Hops_Patch(8173, HOPS), Talvery_Hops_Patch(8174, HOPS), Lumbridge_Hops_Patch(8175, HOPS), McGrubor_Hops_Patch(8176, HOPS),

	Falador_Flower(7847, FLOWERS), Catherby_Flower(7848, FLOWERS), Ardougne_Flower(7849, FLOWERS), Canfis_Flower(7850, FLOWERS),

	Champions_Bush(7577, BUSHES),Champions_Bush2(7577, LUCKY_PATCH), Rimmington_Bush(7578, BUSHES), Etceteria_Bush(7579, BUSHES), South_Arddougne_Bush(7580, BUSHES),

	Falador_Herb_Patch(8150, HERBS), Catherby_Herb_Patch(8151, HERBS), Ardougne_Herb_Patch(8152, HERBS), Canfis_Herb_Patch(8153, HERBS), Trollheim_Herb_Patch(18816, HERBS),

	Falador_Compost_Bin(7836, COMPOST), Catherby_Bin(7837, COMPOST), Port_Phasymatis_Bin(7838, COMPOST), Ardougn_Bin(7839, COMPOST), Taverly_Bin(66577, COMPOST),
	
	VINE_BUSH(56562,HERBLORE_BUSH),VINE_FLOWER(56685,HERBLORE_FLOWER),VINE_HERB(56682,HERBLORE_HERB),;

	private static Map<Short, SpotInfo> informations = new HashMap<Short, SpotInfo>();

	public static SpotInfo getInfo(int objectId) {
	    return informations.get((short) objectId);
	}

	static {
	    for (SpotInfo information : SpotInfo.values())
		informations.put((short) information.objectId, information);
	}

	private int objectId;
	private int configFileId;
	private int type;

	private SpotInfo(int objectId, int type) {
	    this.objectId = objectId;
	    this.configFileId = ObjectDefinitions.getObjectDefinitions(objectId).configFileId;
	    this.type = type;
	}

	public int getType() {
			return type;
		}
    }

    public FarmingSpot getSpot(SpotInfo info) {
	for (FarmingSpot spot : spots)
	    if (spot.spotInfo.equals(info))
		return spot;
	return null;
    }
      public void ItemObjectActio(int objectId, Item item){
    	  SpotInfo info = SpotInfo.getInfo(objectId);
    		FarmingSpot spot = getSpot(info);
    		if (info != null  && !spot.isDead() && !spot.isDiseased()) {
    		if(item.getId() == 30770){
   			player.getFarmingManager().handelSuperGrowPotion(spot,info);
   			} else if(item.getId() == 31186){
   				player.getFarmingManager().handelPatchbomb(spot);
   			}
    		}
       }
	   
	   
    public boolean isFarming(int objectId, Item item, int optionId) {
	SpotInfo info = SpotInfo.getInfo(objectId);
	if (info != null) {
	    handleFarming(info, item, optionId);
	    return true;
	}
	return false;
    }

  

	public void handleFarming(SpotInfo info, Item item, int optionId) {
	FarmingSpot spot = getSpot(info);
	if (spot == null)
	    spot = new FarmingSpot(info);
	if (!spot.isCleared()) {
	    if (item != null) {
		if (info.type == COMPOST)
		    fillCompostBin(spot, item);
	    } else {
		switch (optionId) {
		    case 1: // rake
			if (info.type == COMPOST) {
			    if (spot.getHarvestAmount() == 15) {
				spot.setCleared(true);
				spot.setActive(ProductInfo.Compost_Bin);
				spot.setHarvestAmount(15);
				spot.refresh();
				player.getPackets().sendGameMessage("You close the compost bin.");
				player.getPackets().sendGameMessage("The vegetation begins to decompose.");
			    }
			} else
			    startRakeAction(spot); // creates usable spot
			break;
		    case 2: // inspect
			sendNeedsWeeding(spot.isCleared());
			break;
		    case 3: // guide
			openGuide();
			break;
		}
	    }
	} else {
	    if (item != null) {
		String itemName = item.getName().toLowerCase();
		if (itemName.startsWith("watering can ("))
		    startWateringAction(spot, item);
		else if (itemName.contains("compost"))
		    startCompostAction(spot, item, itemName.equals("supercompost"));
		else if (item.getId() == 6036)
		    startCureAction(spot, item);
		else if (!player.getInventory().containsItemToolBelt(5343)) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need a seed dibber in order to plant "+item.getName().toLowerCase()+"s.");
		    return;
		}
		startFarmingCycle(spot, item);
	    } else if (spot.productInfo != null) {
		switch (optionId) {
		    case 1:
			if (info.type == TREES) {
			    if (spot.reachedMaxStage() && !spot.hasChecked())
				checkHealth(spot);
			    else if (spot.reachedMaxStage() && !spot.isEmpty())
				collectTreeProducts(spot, TreeDefinitions.valueOf(spot.productInfo.name().toUpperCase()));
			    //player.getDialogueManager().startDialogue("FarmingOptions", spot);
			    else if (spot.reachedMaxStage() && spot.isEmpty())
				startHarvestingAction(spot);
			    else if (spot.isDead())
				clearFarmingPatch(spot);
			    else if (spot.isDiseased())
				startCureAction(spot, null);
			} else if (info.type == FRUIT_TREES) {
			    if (spot.reachedMaxStage() && !spot.hasChecked())
				checkHealth(spot);
			    else if (spot.reachedMaxStage() && !spot.hasEmptyHarvestAmount())
				startPickingAction(spot);
			    else if (spot.reachedMaxStage() && !spot.isEmpty())
				collectTreeProducts(spot, TreeDefinitions.FRUIT_TREES);
			    else if (spot.reachedMaxStage() && spot.isEmpty() || spot.isDead())
				clearFarmingPatch(spot);
			    else if (spot.isDiseased())
				startCureAction(spot, null);
			} else if (info.type == BUSHES) {
			    if (spot.reachedMaxStage() && !spot.hasChecked())
				checkHealth(spot);
			    else if (spot.reachedMaxStage() && !spot.hasEmptyHarvestAmount())
				startPickingAction(spot);
			    else if (spot.isDead())
				clearFarmingPatch(spot);
			    else if (spot.isDiseased())
				startCureAction(spot, null);
			} else if (info.type == COMPOST) {
			    if (spot.reachedMaxStage() && !spot.hasChecked()) {
				spot.setChecked(true);
				spot.refresh();
				player.getPackets().sendGameMessage("You open the compost bin.");
			    } else if (!spot.reachedMaxStage())
				player.getDialogueManager().startDialogue("SimpleMessage", "The weeds haven't finished decomposing yet.");
			    else
				clearCompostAction(spot);
			} else {
			    if (spot.reachedMaxStage() && !spot.isDead())
				startHarvestingAction(spot);
			    else if (spot.isDead())
				clearFarmingPatch(spot);
			}
			break;
		    case 2:// inspect... usless tbh
			break;
		    case 3:// clear & guide
			if (spot.isDiseased() || spot.reachedMaxStage()) {
			    if (info.type == TREES) {
			//	if (spot.isEmpty()) // stump
				    startHarvestingAction(spot);
				//	else {
				 //   player.getPackets().sendGameMessage("You need to chop the tree down before removing it.");
				   // return;
					//}
			    } else
				clearFarmingPatch(spot);
			} else if (spot.productInfo.type == FRUIT_TREES) {
			    if (spot.reachedMaxStage())
				return;
			    clearFarmingPatch(spot);
			} else
			    openGuide();
			break;
		}
	    }
	}
    }

    private void startRakeAction(final FarmingSpot spot) {
	player.getActionManager().setAction(new Action() {

	    @Override
	    public boolean start(Player player) {
		if (!player.getInventory().containsItemToolBelt(RAKE)) {
		    player.getPackets().sendGameMessage("You'll need a rake to get rid of the weeds.");
		    return false;
		}
		return true;
	    }

	    @Override
	    public boolean process(Player player) {
		return spot.stage != 3;
	    }

	    @Override
	    public int processWithDelay(Player player) {
		player.setNextAnimation(RAKING_ANIMATION);
		if (Utils.random(3) == 0) {
		    spot.increaseStage();
		    if (spot.stage == 3)
			spot.setCleared(true);
		    if (!player.getInventory().addItem(6055, 1)) {
		    	World.addGroundItem(new Item(6055, 1), new WorldTile(player), player, true, 180,true);
		    }
		    player.getSkills().addXp(Skills.FARMING, 8);
		}
		return 2;
	    }

	    @Override
	    public void stop(Player player) {
		setActionDelay(player, 3);
	    }
	});
    }
	

    public void startHarvestingAction(final FarmingSpot spot) {
	final String patchName = getPatchName(spot.productInfo.type);
	player.getActionManager().setAction(new Action() {

	    @Override
	    public boolean start(Player player) {
		if (!player.getInventory().containsItemToolBelt(952)) {
		    player.getPackets().sendGameMessage("You need a spade to harvest your crops.");
		    return false;
		}
		if (spot.hasEmptyHarvestAmount() && !spot.hasGivenAmount()) {
		    spot.setHarvestAmount(getRandomHarvestAmount(spot.productInfo.type));
		    spot.setHasGivenAmount(true);
		} else if (spot.harvestAmount <= 0) {
		    player.getPackets().sendGameMessage("You have successfully harvested this patch for new crops.");
		    player.setNextAnimation(new Animation(-1));
		    spot.setIdle();
		    return false;
		}
		player.getPackets().sendGameMessage("You begin to harvest the " + patchName + " patch.");
		setActionDelay(player, 1);
		return true;
	    }

	    @Override
	    public boolean process(Player player) {
		if (spot.harvestAmount > 0)
		    return true;
		else {
		    player.getPackets().sendGameMessage("You harvest the produce in the " + patchName + " patch and get it ready for new crops.");
		    player.setNextAnimation(new Animation(-1));
		    spot.setIdle();
		    return false;
			}
	    }

	    @Override
	    public int processWithDelay(Player player) {
	    int glovesId = player.getEquipment().getGlovesId();
		int amount = glovesId == 33883 ? 2 : 1;
		spot.harvestAmount--;
		player.setNextAnimation(getHarvestAnimation(spot.productInfo.type));
		player.getSkills().addXp(Skills.FARMING, spot.productInfo.experience * amount);
		if(player.getPerkHandler().perks.contains(Perk.FARMING_PERK)){
			if(Utils.random(100) <= 5){
				player.getInventory().addItem(spot.productInfo.seedId, 1);
				player.sm("Thanks to your green finger perk you found a seed while harvesting.");
			}
		}
		if(player.getEquipment().getCapeId() == 31291){
		if (!player.getInventory().addItem(spot.productInfo.productId +1, amount)) {
	    	World.addGroundItem(new Item(spot.productInfo.productId +1, amount), new WorldTile(player), player, true, 180,true);
	    }
		} else {
		if (!player.getInventory().addItem(spot.productInfo.productId, amount)) {
	    	World.addGroundItem(new Item(spot.productInfo.productId , amount), new WorldTile(player), player, true, 180,true);
	    }	
		}
		return 2;
	    }

	    @Override
	    public void stop(Player player) {
		setActionDelay(player, 3);
	    }
	});
    }

    private void startPickingAction(final FarmingSpot spot) {
	player.getActionManager().setAction(new Action() {

	    @Override
	    public boolean start(Player player) {
		return true;
	    }

	    @Override
	    public boolean process(Player player) {
		if (spot.harvestAmount > 0)
		    return true;
		else {
		    player.getPackets().sendGameMessage("You pick all of the " + (spot.productInfo.type == FRUIT_TREES ? "fruits" : "berries") + " from the " + getPatchName(spot.productInfo.type) + " patch.");
		    player.setNextAnimation(new Animation(-1));
		    return false;
		}
	    }

	    @Override
	    public int processWithDelay(Player player) {
		player.getPackets().sendGameMessage("You pick a " + ItemDefinitions.getItemDefinitions(spot.productInfo.productId).getName().toLowerCase() + ".");
		player.setNextAnimation(getHarvestAnimation(spot.productInfo.type));
		player.getSkills().addXp(Skills.FARMING, spot.productInfo.experience);
		if (!player.getInventory().addItem(spot.productInfo.productId, 1)) {
	    	World.addGroundItem(new Item(spot.productInfo.productId, 1), new WorldTile(player), player, true, 180,true);
	    }
		spot.harvestAmount--;
		spot.refresh();
		if (spot.cycleTime < Utils.currentTimeMillis())
		    spot.setCycleTime(REGENERATION_CONSTANT);
		return 2;
	    }

	    @Override
	    public void stop(Player player) {
		setActionDelay(player, 1);
	    }
	});
    }

    public void clearCompostAction(final FarmingSpot spot) {
	player.getActionManager().setAction(new Action() {

	    @Override
	    public boolean start(Player player) {
		if (spot == null)
		    return false;
		else if (!player.getInventory().containsItemToolBelt(EMPTY_BUCKET, 1)) {
		    player.getPackets().sendGameMessage("You need an empty bucket to clear the compost.");
		    return false;
		}
		return true;
	    }

	    @Override
	    public boolean process(Player player) {
		if (!player.getInventory().containsItemToolBelt(EMPTY_BUCKET, 1)) {
		    player.getPackets().sendGameMessage("You need an empty bucket to clear the compost.");
		    return false;
		} else if (spot.harvestAmount > 0)
		    return true;
		else {
		    spot.setCleared(false);
		    spot.refresh();
		    spot.setProductInfo(null);
		    spot.remove();
		    player.setNextAnimation(new Animation(-1));
		    return false;
		}
	    }

	    @Override
	    public int processWithDelay(Player player) {
		player.setNextAnimation(FILL_COMPOST_ANIMATION);
		player.getSkills().addXp(Skills.FARMING, 5);
		player.getInventory().deleteItem(EMPTY_BUCKET, 1);
		if (!player.getInventory().addItem(spot.getCompost() ? 6032 : 6034, 1)) {
	    	World.addGroundItem(new Item(spot.getCompost() ? 6032 : 6034, 1), new WorldTile(player), player, true, 180,true);
	    }
		spot.harvestAmount--;
		spot.refresh();
		return 2;
	    }

	    @Override
	    public void stop(Player player) {
		setActionDelay(player, 1);
	    }
	});
    }

    public void clearFarmingPatch(final FarmingSpot spot) {
	final String patchName = getPatchName(spot.productInfo.type);
	player.getActionManager().setAction(new Action() {

	    private int stage;

	    @Override
	    public boolean start(Player player) {
		if (!player.getInventory().containsItemToolBelt(952)) {
		    player.getPackets().sendGameMessage("You need a spade to clear this "+patchName+" patch.");
		    return false;
		}
		player.getPackets().sendGameMessage("You start digging up the produce in the " + patchName + " patch.");
		return true;
	    }

	    @Override
	    public boolean process(Player player) {
		if (stage != 2)
		    return true;
		else {
		    player.getPackets().sendGameMessage("You clear the "+patchName+" and get it ready for new crops.");
		    player.setNextAnimation(new Animation(-1));
		    spot.setIdle();
		    return false;
		}
	    }

	    @Override
	    public int processWithDelay(Player player) {
		player.setNextAnimation(SPADE_ANIMATION);
		if (Utils.random(3) == 0)
		    stage++;
		return 2;
	    }

	    @Override
	    public void stop(Player player) {
		setActionDelay(player, 3);
	    }
	});
    }

    public boolean startFarmingCycle(FarmingSpot spot, Item item) {
	ProductInfo productInfo = ProductInfo.getProduct(item.getId());
	if (spot == null || productInfo == null || spot.spotInfo.type != productInfo.type || !spot.isCleared() || spot.productInfo != null || spot.spotInfo.type == COMPOST)
	    return false;
	String patchName = getPatchName(productInfo.type);
	String itemName = item.getDefinitions().getName().toLowerCase();
	int requiredAmount = (productInfo.type == ALLOTMENT || productInfo.type == HOPS) ? 3 : 1;
	boolean isTree = productInfo.type == TREES || productInfo.type == FRUIT_TREES;
	int level = productInfo.level;
	if (!player.getInventory().containsItemToolBelt(isTree ? 952 : 5343)) {
	    player.getPackets().sendGameMessage(isTree ? "You need a spade to plant the sappling into the "+patchName+" patch." : "You need a seed dibber to plant the seed in the "+patchName+" patch.");
	    return true;
	} else if (!player.getInventory().containsItemToolBelt(item.getId(), requiredAmount)) {
	    player.getPackets().sendGameMessage("You don't have enough " + item.getDefinitions().getName().toLowerCase() + " to plant " + (patchName.startsWith("(?i)[^aeiou]") ? "an" : "a") + " " + patchName + " patch.");
	    return true;
	} else if (player.getSkills().getLevel(Skills.FARMING) < level) {
	    player.getPackets().sendGameMessage("You need a farming level of " + level + " to plant this " + (isTree ? "sapling" : "seed") + ".");
	    return true;
	}
	player.getPackets().sendGameMessage("You plant the " + itemName + " in the " + patchName + " patch.");
	player.setNextAnimation(isTree ? SPADE_ANIMATION : SEED_DIPPING_ANIMATION);
	player.getSkills().addXp(Skills.FARMING, isTree ? productInfo.experience : productInfo.plantingExperience);
	if(player.getPerkHandler().perks.contains(Perk.FARMING_PERK) && Utils.random(10) == 1){
		player.sm("Your green finger perk saved you a seed.");
	} else
	player.getInventory().deleteItem(new Item(item.getId(), requiredAmount));
	if (player.getDailyTask() != null)
		player.getDailyTask().incrementTask(player, 3, item.getId(), Skills.FARMING);
	spot.setActive(productInfo);
	return true;
    }

    public boolean startWateringAction(final FarmingSpot spot, final Item item) {
	if (spot == null || spot.productInfo == null || item == null)
	    return false;
	if (!item.getName().toLowerCase().startsWith("watering can (")) {
	    player.getPackets().sendGameMessage("You can't water your plants with an empty watering can!");
	    return true;
	} else if (spot.isWatered()) {
	    player.getPackets().sendGameMessage("This patch has already been watered.");
	    return true;
	} else if (spot.reachedMaxStage() || spot.productInfo.type == HERBS || spot.productInfo.type == COMPOST || spot.productInfo.type == TREES || spot.productInfo.type == FRUIT_TREES || spot.productInfo == ProductInfo.White_lily || spot.productInfo.type == BUSHES) {
	    player.getPackets().sendGameMessage("This patch doesn't need watering.");
	    return true;
	} else if (spot.isDiseased()) {
	    player.getPackets().sendGameMessage("This crop is diseased and needs to be cured.");
	    return true;
	} else if (spot.isDead()) {
	    player.getPackets().sendGameMessage("You can't water dead produce!");
	    return true;
	}
	player.getPackets().sendGameMessage("You water the "+spot.productInfo+".");
	player.setNextAnimation(WATERING_ANIMATION);
	spot.setWatered(true);
	WorldTasksManager.schedule(new WorldTask() {

	    @Override
	    public void run() {
	    	final int id = item.getId();
	    	item.setId(id == 5333 ? 5331 : id - 1);
	    	player.getInventory().refresh();
	    	spot.refresh();
	    }
	}, 2);
	return true;
    }

    public boolean startCureAction(final FarmingSpot spot, final Item item) {
	if (spot == null || spot.productInfo == null || spot.productInfo.type == COMPOST)
	    return false;
	final boolean isTree = spot.productInfo.type == TREES || spot.productInfo.type == FRUIT_TREES;
	final boolean isBush = spot.productInfo.type == BUSHES;
	if (!spot.isDiseased()) {
	    player.getPackets().sendGameMessage("The produce in this patch isn't diseased and it doesn't need to be cured.");
	    return true;
	} else if (isTree || isBush) {
	    if (!(player.getInventory().containsItemToolBelt(5329) || player.getInventory().containsItemToolBelt(7409))) {
	    	player.getPackets().sendGameMessage("You need some secateurs to prune this "+spot.productInfo.name().toLowerCase()+" "+(isBush ? "bush" : "tree")+".");
	    	return true;
	    }
	}
	player.getPackets().sendGameMessage(isTree ? "You prune the " + spot.productInfo.name().toLowerCase() + " tree's diseased branches." : isBush ? "You prune the " + spot.productInfo.name().toLowerCase() + " bush's diseased leaves." : "You treat the " + getPatchName(spot.spotInfo.type) + " patch with the plant cure.");
	player.setNextAnimation((isTree || isBush) ? PRUNING_ANIMATION : CURE_PLANT_ANIMATION);
	spot.setDiseased(false);
	WorldTasksManager.schedule(new WorldTask() {

	    @Override
	    public void run() {
		if (!isTree && !isBush) {
		    player.getInventory().deleteItem(item);
		    player.getInventory().addItem(new Item(229, 1));
		} else
		    player.setNextAnimation(new Animation(-1));
		player.getPackets().sendGameMessage("The produce in this patch has been restored to its natural health.");
		spot.refresh();
	    }
	}, 2);
	return true;
    }

    public boolean startCompostAction(final FarmingSpot spot, final Item item, boolean superCompost) {
	if (spot == null || spot.spotInfo.type == COMPOST)
	    return false;
	if (spot.hasCompost()) {
	    player.getPackets().sendGameMessage("This patch is saturated by "+(superCompost ? "supercompost" : "compost")+".");
	    return true;
	} else if (!spot.isCleared()) {
	    player.getPackets().sendGameMessage("The patch needs to be cleared in order to saturate it with compost.");
	    return true;
	}
	player.getPackets().sendGameMessage("You saturate the patch with "+(superCompost ? "supercompost" : "compost")+".");
	player.setNextAnimation(COMPOST_ANIMATION);
	if (superCompost)
	    spot.setSuperCompost(true);
	else
	    spot.setCompost(true);
	WorldTasksManager.schedule(new WorldTask() {

	    @Override
	    public void run() {
		player.getInventory().deleteItem(item);
		player.getInventory().addItem(EMPTY_BUCKET, 1);
		player.getSkills().addXp(Skills.FARMING, 8);
		spot.refresh();
	    }
	}, 2);
	return true;
    }

    public void collectTreeProducts(final FarmingSpot spot, final TreeDefinitions definitions) {
	player.getActionManager().setAction(new Action() {

	    private HatchetDefinitions hatchet;

	    @Override
	    public boolean start(Player player) {
		if (!checkAll(player))
		    return false;
		player.getPackets().sendGameMessage("You swing your hatchet at the tree...", true);
		setActionDelay(player, getWoodcuttingDelay(player));
		return true;
	    }

	    private int getWoodcuttingDelay(Player player) {
		int summoningBonus = player.getFamiliar() != null ? (player.getFamiliar().getId() == 6808 || player.getFamiliar().getId() == 6807) ? 10 : 0 : 0;
		int wcTimer = definitions.getLogBaseTime() - (player.getSkills().getLevel(8) + summoningBonus) - Utils.getRandom(hatchet.getAxeTime());
		if (wcTimer < 1 + definitions.getLogRandomTime())
		    wcTimer = 1 + Utils.getRandom(definitions.getLogRandomTime());
		wcTimer /= player.getAuraManager().getWoodcuttingAccurayMultiplier();
		return wcTimer;
	    }

	    private boolean checkAll(Player player) {
		for (HatchetDefinitions def : HatchetDefinitions.values()) {
		    if (player.getInventory().containsItemToolBelt(def.getItemId()) || player.getEquipment().getWeaponId() == def.getItemId()) {
			hatchet = def;
			if (player.getSkills().getLevel(Skills.WOODCUTTING) < hatchet.getLevelRequried()) {
			    hatchet = null;
			    break;
			}
		    }
		}
		if (hatchet == null) {
		    player.getPackets().sendGameMessage("You dont have the required level to use that axe or you don't have a hatchet.");
		    return false;
		}
		if (!hasWoodcuttingLevel(player))
		    return false;
		if (!player.getInventory().hasFreeSlots()) {
		    player.getPackets().sendGameMessage("Not enough space in your inventory.");
		    return false;
		}
		return true;
	    }

	    private boolean hasWoodcuttingLevel(Player player) {
		if (definitions.getLevel() > player.getSkills().getLevel(8)) {
		    player.getPackets().sendGameMessage("You need a woodcutting level of " + definitions.getLevel() + " to chop down this tree.");
		    return false;
		}
		return true;
	    }

	    @Override
	    public boolean process(Player player) {
		player.setNextAnimation(new Animation(hatchet.getEmoteId()));
		return checkTree(player);
	    }

	    private boolean usedDeplateAurora;

	    @Override
	    public int processWithDelay(Player player) {
	    	player.getSkills().addXp(8, definitions.getXp());
			player.getInventory().addItem(definitions.getLogsId(), 1);
			String logName = ItemDefinitions
					.getItemDefinitions(definitions.getLogsId()).getName()
					.toLowerCase();
			player.sm("You get some "+logName+".");
		if (!usedDeplateAurora && (1 + Math.random()) < player.getAuraManager().getChanceNotDepleteMN_WC()) {
		    usedDeplateAurora = true;
		} else if (Utils.getRandom(definitions.getRandomLifeProbability()) == 0) {
		    int time = definitions.getRespawnDelay();
		    spot.setEmpty(true);
		    spot.refresh();
		    spot.setCycleTime(true, time * 1000); // time in seconds
		    player.setNextAnimation(new Animation(-1));
		    return -1;
		}
		if (!player.getInventory().hasFreeSlots()) {
		    player.setNextAnimation(new Animation(-1));
		    player.getPackets().sendGameMessage("Not enough space in your inventory.");
		    return -1;
		}
		return getWoodcuttingDelay(player);
	    }

	    private boolean checkTree(Player player) {
		return spot != null && !spot.isEmpty();
	    }

	    @Override
	    public void stop(Player player) {
		setActionDelay(player, 3);
	    }
	});
    }

    private void fillCompostBin(final FarmingSpot spot, final Item item) {
	final boolean[] attributes = isOrganicItem(item.getId());
	player.getActionManager().setAction(new Action() {

	    @Override
	    public boolean start(Player player) {
		if (item == null || !player.getInventory().containsItemToolBelt(item.getId(), 1) || spot.isCleared())
		    return false;
		else if (!attributes[0]) {
		    player.getPackets().sendGameMessage("You cannot add "+item.getId()+" "+(item.getAmount() > 1 ? "s" : "")+" to the compost bin.");
		    return false;
		}
		return true;
	    }

	    @Override
	    public boolean process(Player player) {
		return spot.harvestAmount != 15 && player.getInventory().containsItemToolBelt(item.getId(), 1);
	    }

	    @Override
	    public int processWithDelay(Player player) {
		player.setNextAnimation(FILL_COMPOST_ANIMATION);
		player.getInventory().deleteItem(item);
		spot.harvestAmount++;
		spot.refresh();
		return 2;
	    }

	    @Override
	    public void stop(Player player) {
		setActionDelay(player, 3);
	    }
	});
    }

    private void checkHealth(final FarmingSpot spot) {
	player.getPackets().sendGameMessage("You examine the " + ((spot.productInfo.type == TREES || spot.productInfo.type == FRUIT_TREES) ? "tree" : "bush") + " for signs of disease, and quickly realise that it is at full health.");
	player.getSkills().addXp(Skills.FARMING, spot.productInfo.plantingExperience);
	if(spot.productInfo.seedId == 5374){
		player.magicTreesHarvest++;
		player.getAchievementManager().notifyUpdate(TheRealFarmerAchievement.class);
	}if(spot.productInfo.type == TREES){
		player.harvestedTrees++;
		player.getAchievementManager().notifyUpdate(FarmingDummy.class);
	}player.setNextAnimation(CHECK_TREE_ANIMATION); 
	spot.setChecked(true);
	spot.refresh();
    }

    private String getPatchName(int type) {
	return PATCH_NAMES[type];
    }

    private int getRandomHarvestAmount(int type) {
	int maximumAmount = 0, baseAmount = 0, totalAmount = 0;
	baseAmount = HARVEST_AMOUNTS[type][0];
	maximumAmount = HARVEST_AMOUNTS[type][1];
	totalAmount = Utils.random(baseAmount, maximumAmount);
	if (player.getEquipment().getWeaponId() == 7409)
	    totalAmount *= 1.1;
	return totalAmount;
    }

    private Animation getHarvestAnimation(int type) {
	if (type == ALLOTMENT || type == HOPS || type == TREES)
	    return SPADE_ANIMATION;
	else if (type == HERBS || type == FLOWERS) {
	    if (player.getEquipment().getWeaponId() == 7409)
		return MAGIC_PICKING_ANIMATION;
	    return type == HERBS ? HERB_PICKING_ANIMATION : FLOWER_PICKING_ANIMATION;
	} else if (type == FRUIT_TREES) {
	    return FRUIT_PICKING_ANIMATION;
	} else if (type == BUSHES) {
	    return BUSH_PICKING_ANIMATION;
	}
	return null;
    }

    private void sendNeedsWeeding(boolean cleared) {
	player.getPackets().sendGameMessage(cleared ? "The patch is ready for planting." : "The patch needs weeding.");
    }

    private void openGuide() {
	player.getTemporaryAttributtes().put("skillMenu", 21);
	player.getVarBitManager().sendVar(965, 21);
	//ButtonHandler.openSkillGuide(player);
    }

    private boolean[] isOrganicItem(int itemId) {
	boolean[] bools = new boolean[2];
	for (int organicId : COMPOST_ORGANIC) {
	    if (itemId == organicId) {
		bools[0] = true;
		bools[1] = false;
	    }
	}
	for (int organicId : SUPER_COMPOST_ORGANIC) {
	    if (itemId == organicId) {
		bools[0] = true;
		bools[1] = false;
	    }
	}
	return bools;
    }

    public void resetSpots() {
	spots.clear();
    }

    public void resetTreeTrunks() {
	for (FarmingSpot spot : spots) {
	    if (spot.spotInfo.type == TREES || spot.spotInfo.type == FRUIT_TREES) {
		if (spot.isEmpty()) {
		    spot.setEmpty(false);
		    spot.refresh();
		}
	    }
	}
    }

    public class FarmingSpot implements Serializable {

	private static final long serialVersionUID = -732322970478931771L;

	private SpotInfo spotInfo;
	public ProductInfo productInfo;
	private int stage;
	private long cycleTime;
	private int harvestAmount;
	private boolean[] attributes;

	public FarmingSpot(SpotInfo spotInfo) {
	    this.spotInfo = spotInfo;
	    cycleTime = Utils.currentTimeMillis();
	    stage = 0; // stage 0 is default null
	    harvestAmount = 0;
	    attributes = new boolean[10]; // diseased, watered, dead,
					  // firstCycle, usingCompost,
					  // usingSuperCompost;
	    renewCycle();
	    spots.add(this);
	}

	public void setActive(ProductInfo productInfo) {
	    setProductInfo(productInfo);
	    stage = -1;
	    resetCycle();
	}

	private void resetCycle() {
	    cycleTime = Utils.currentTimeMillis();
	    harvestAmount = 0;
	    for (int index = 0; index < attributes.length; index++) {
		if (index == 4)
		    continue;
		attributes[index] = false;
	    }
	}

	public void setCycleTime(long cycleTime) {
	    setCycleTime(false, cycleTime);
	}

	public void setCycleTime(boolean reset, long cycleTime) {
	    if (reset)
		this.cycleTime = 0;
	    if (this.cycleTime == 0)
		this.cycleTime = Utils.currentTimeMillis();
	    this.cycleTime += cycleTime;
	}

	public void setIdle() {
	    stage = 3;
	    setProductInfo(null);
	    refresh();
	    resetCycle();
	}

	public void process() {
	    if (cycleTime == 0)
		return;
	    while (cycleTime < Utils.currentTimeMillis()) {
		if (productInfo != null) {
		    if (hasChecked() && (isEmpty() || !hasMaximumRegeneration())) {
			if (isEmpty()) {
			    setEmpty(false);
			    if (productInfo.type == FRUIT_TREES)
				setCycleTime(REGENERATION_CONSTANT);
			    else
				cycleTime = 0;
			} else if (!hasMaximumRegeneration()) {
			    if (harvestAmount == 5)
				cycleTime = 0;
			    else
				cycleTime += REGENERATION_CONSTANT;
			    harvestAmount++;
			} else
			    cycleTime = 0;
			refresh();
			return;
		    } else {
			increaseStage();
			if (reachedMaxStage() || isDead()) {
			    cycleTime = 0;
			    break;
			}
		    }
		} else {
		    if (spotInfo.type != COMPOST) {
			desecreaseStage();
			if (stage <= 0) {
			    remove();
			    break;
			}
		    }
		}
		renewCycle();
	    }
	}

	public int getConfigBaseValue() {
	    if (productInfo != null) {
		if (productInfo.type == ALLOTMENT)
		    return 6 + (productInfo.configIndex * 7);
		else if (productInfo.type == HERBS)
		    return 4 + (productInfo.configIndex * 7);
		else if (productInfo.type == FLOWERS)
		    return 8 + (productInfo.configIndex * 5);
		else if (productInfo.type == HOPS)
		    return 3 + (productInfo.configIndex * 5);
		else if(productInfo.type == LUCKY_PATCH ||productInfo.type == HERBLORE_BUSH ||productInfo.type == HERBLORE_HERB || productInfo.type == HERBLORE_FLOWER)
			return (productInfo.configIndex);
		else if (productInfo.type == TREES || productInfo.type == FRUIT_TREES || productInfo.type == BUSHES)
		    return 8 + (productInfo.configIndex ^ 2 - 1);
	    }
	    return stage;
	}

	private int getConfigValue(int type) {
	    if (type == HERBS)
		return isDead() ? stage + 169 : getConfigBaseValue() + ((isDiseased() && stage != 0) ? stage + 127 : stage);
	    else if (type == TREES) {
		int baseValue = getConfigBaseValue() + (isDead() ? stage + 128 : (isDiseased() && stage != 0) ? stage + 64 : stage);
		if (hasChecked()) {
		    baseValue += 2;
		    if (!isEmpty())
			baseValue--;
		}
		return baseValue;
	    } else if (type == LUCKY_PATCH  ||productInfo.type == HERBLORE_HERB || productInfo.type == HERBLORE_FLOWER){
	    	int baseValue = stage + getConfigBaseValue();
			if (hasChecked())
			    baseValue = 0;
			else if (isDead())
			    baseValue += 20;
			else if (isDiseased())
			    baseValue += 12;
			else if (!hasChecked() && reachedMaxStage()){ 
			    baseValue = 248;
			}if (isEmpty())
			    baseValue = 0;
			return baseValue;
	    } else if (type == HERBLORE_BUSH){
	    	int baseValue = stage + getConfigBaseValue();
			if (hasChecked())
			    baseValue = 25;
			else if (!hasChecked() && reachedMaxStage()){ //if productInfo
			    baseValue = 51;
			}if (isEmpty())
			    baseValue = 0;
			return baseValue;
	    }
	    else if (type == FRUIT_TREES) {
		int baseValue = stage + getConfigBaseValue();
		if (hasChecked())
		    baseValue += getHarvestAmount();
		else if (isDead())
		    baseValue += 20;
		else if (isDiseased())
		    baseValue += 12;
		else if (!hasChecked() && reachedMaxStage())
		    baseValue += 20;
		if (isEmpty())
		    baseValue += 19;
		return baseValue;
	    } else if (type == BUSHES) {	
		int baseValue = stage + getConfigBaseValue();
		if (hasChecked())
		    baseValue += getHarvestAmount();
		else if (isDead())
		    baseValue += 128;
		else if (isDiseased())
		    baseValue += 65;
		else if (!hasChecked() && reachedMaxStage()) // 250
		    baseValue += 240;
		return baseValue;
	    } else if (type == COMPOST) {
		return isCleared() ? harvestAmount + 16 + (hasChecked() ? -1 : 0) : productInfo != null && reachedMaxStage() ? 0 : harvestAmount - stage;
	    } else if (type == FLOWERS || type == HOPS || type == ALLOTMENT)
		return getConfigBaseValue() + (isDead() ? stage + 192 : (isDiseased() && stage != 0) ? stage + 128 : isWatered() ? 64 + stage : stage);
	    return stage + getConfigBaseValue();
	}

	private void checkFactors() {
	    if (isDiseased()) {
		if (reachedMaxStage()) {
		    setDead(false);
		    setDiseased(false);
		} else {
		    if (isFirstCycle())
			setFirstCycle(false);
		    else
			setDead(true);
		}
	    }
	    if (productInfo.type == FRUIT_TREES || productInfo.type == BUSHES) {
		if (reachedMaxStage())
		    setHarvestAmount(productInfo.type == BUSHES || productInfo == ProductInfo.Palm ? 4 : 6);
	    }
	    setWatered(false);
	    checkDisease();
	}

	public boolean reachedMaxStage() {
	    return stage == productInfo.maxStage;
	}

	private boolean hasMaximumRegeneration() {
	    if (spotInfo.type != FRUIT_TREES && spotInfo.type != BUSHES)
		return true;
	    else if (getHarvestAmount() != HARVEST_AMOUNTS[productInfo.type][1])
		return false;
	    return true;
	}

	public void renewCycle() {
	    long constant = 30000L;
	    if (productInfo != null)
		cycleTime += (stage == 0/* || Settings.DEBUG*/) ? 5000 : constant * productInfo.cycleTime;
	    else
		cycleTime += constant * 3;
	}

	public boolean canBeDiseased() {
	    if (stage == 0 && productInfo.type != BUSHES || reachedMaxStage() || isDiseased() || productInfo == ProductInfo.White_lily || productInfo == ProductInfo.Poison_ivy || productInfo.type == COMPOST)
		return false;
	    return true;
	}

	private void checkDisease() {
	    if (canBeDiseased()) {
		int baseValue = 35;
		if (isWatered())
		    baseValue += 10;
		if (getCompost())
		    baseValue += 10;
		else if (getSuperCompost())
		    baseValue += 20;
		if (Utils.getRandom(baseValue) == 0) {
		    setDiseased(true);
		    refresh();
		}
	    }
	}

	public void increaseStage() {
	    stage++;
	    if (productInfo != null)
		checkFactors();
	    refresh();
	}

	public void desecreaseStage() {
	    setCleared(false);
	    stage--;
	    refresh();
	}

	private void remove() {
	    spots.remove(this);
	}
	/**
	 * 
	 */


	public void refresh() {
	    int value = spotInfo.type == COMPOST ? getConfigValue(spotInfo.type) : productInfo != null ? getConfigValue(spotInfo.type) + productInfo.stageSkip : stage;
	    if(spotInfo.type == HERBLORE_BUSH && productInfo != null){
	    	if(reachedMaxStage()){
				value = 51;
			}
	    	value = 15 + stage;
	    }
	    	
	    player.getVarBitManager().sendVarBit(spotInfo.configFileId, value);
	    //if (Settings.DEBUG)
		System.out.println(value);
	}

	public void setProductInfo(ProductInfo productInfo) {
	    this.productInfo = productInfo;
	}

	public boolean isDiseased() {
	    return attributes[0];
	}

	public void setDiseased(boolean diseased) {
	    this.attributes[0] = diseased;
	}

	public boolean isWatered() {
	    return attributes[1];
	}

	public void setWatered(boolean watered) {
	    this.attributes[1] = watered;
	}

	public boolean isDead() {
	    return attributes[2];
	}

	public void setDead(boolean dead) {
	    this.attributes[2] = dead;
	    if (dead)
		setDiseased(false);
	}

	public boolean isFirstCycle() {
	    return attributes[3];
	}

	public void setFirstCycle(boolean firstCycle) {
	    this.attributes[3] = firstCycle;
	}

	public boolean isCleared() {
	    return attributes[4];
	}

	public void setCleared(boolean cleared) {
	    this.attributes[4] = cleared;
	}

	public boolean hasChecked() {
	    return attributes[5];
	}

	public void setChecked(boolean checked) {
	    this.attributes[5] = checked;
	}

	public boolean isEmpty() {
	    return attributes[6];
	}

	public void setEmpty(boolean empty) {
	    this.attributes[6] = empty;
	}

	public boolean hasCompost() {
	    return attributes[7] || attributes[8];
	}

	public boolean getSuperCompost() {
	    return attributes[8];
	}

	public void setSuperCompost(boolean superCompost) {
	    this.attributes[8] = superCompost;
	}

	public boolean hasGivenAmount() {
	    return attributes[9];
	}

	public void setHasGivenAmount(boolean amount) {
	    this.attributes[9] = amount;
	}

	public boolean getCompost() {
	    return attributes[7];
	}

	public void setCompost(boolean compost) {
	    this.attributes[7] = compost;
	}

	public void setHarvestAmount(int harvestAmount) {
	    this.harvestAmount = harvestAmount;
	}

	public boolean hasEmptyHarvestAmount() {
	    return harvestAmount == 0;
	}
	
	
	public int getHarvestAmount() {
	    return harvestAmount;
	}
    }
    /**
     * handles the suport grown potion effect
     * @param spot
     * @param info
     */
	public void handelSuperGrowPotion(final FarmingSpot spot,SpotInfo info){
		if(!player.getInventory().contains(30770))
			return;
		if(spot.reachedMaxStage()){
			player.sm("You can't use this on a grown patch.");
			return;
		}
		spot.stage = spot.productInfo.maxStage;
		spot.refresh();
	}
	public void handelPatchbomb(final FarmingSpot spot){
		if(!player.getInventory().contains(31186))
			return;
		if(spot.stage  != spot.productInfo.maxStage){
			player.sm("You have to use this on a fully grown patch.");
			return;
		}
		spot.setHarvestAmount(getRandomHarvestAmount(spot.productInfo.type));
	    spot.setHasGivenAmount(true);
	    int amount = spot.harvestAmount;
		player.getInventory().addItem(spot.productInfo.productId +1, amount);
	    player.getPackets().sendGameMessage("You have throw the bomb into your patch and received the harvest in return.");
	    spot.setIdle();
		
	}
	
	private Item fruitRewards[] = 
		{ 
			new Item(8781, 100),
			new Item(5302, Utils.random(3,7)),
			new Item(5304, Utils.random(2,7)),
			new Item(5316, Utils.random(1,4)),
			new Item(5315, Utils.random(3,5)),
			new Item(246, Utils.random(50,100)),
			new Item(28257, Utils.random(50,100)),
			new Item(570, 100),
			new Item(572, 100),
			new Item(574, 100),
			new Item(576, 100),
			new Item(448, 250),
			new Item(2508, 100),
			new Item(2510, 100),
			new Item(384, 80),
			new Item(537, 60),
			new Item(454, 100),
			new Item(1624, 80),
			new Item(533, 100)				
		};
	/**
	 * handles noting from products
	 */

	public static int product5[] =  {1942,1965,5986,5982,1957,1982,5504,199,203,207,209,213,215,
	    		217,21626,3261,201,205,3049,211,3054,2485,219,14836,12174,6010,6014,6012,1793,14583,
	    		225,1955,1963,2108,5970,2114,5972,5974,1951,2126,239,753,247,6018};
	
	public static  boolean canNote(Item item){
		for(int i = 0; i < product5.length; i ++){
			if(product5[i] == item.getId())
				return true;
		}
		return false;
	}
	public static  void handelLeprechaun(Player player,Item product){
		int amount = player.getInventory().getAmountOf(product.getId());
		player.getInventory().deleteItem(new Item(product.getId(), amount));
		player.getInventory().addItem(new Item(product.getId()+1,amount));
		player.getDialogueManager().startDialogue("SimpleNPCMessage",3021, "Here you go.");
	}
	/*
	 * handles wishing well fruit.
	 */
	public void handelWishingWellFruit(Item item){
		if(player.getInventory().getFreeSlots() < 9){
			player.sm("You need atleast 9 free inventory spaces.");
			return;
		}
		if(!player.getInventory().contains(28311)){
			player.sm("Tell this to the admins, Code : FRUIT_14A5T. Your credentials have been logged.");
			return;
		}
		player.getInventory().deleteItem(new Item(28311));
		player.lock();
		for(int i = 9; i > 0 ; i--){
			player.getInventory().addItem(fruitRewards[Utils.random(fruitRewards.length-1)]);
		}
			player.unlock();
			player.sm("You opened the fruit and received great rewards.");
	}
}