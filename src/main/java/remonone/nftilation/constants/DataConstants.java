package remonone.nftilation.constants;

public class DataConstants {
    public static final int ONE_SECOND = 1000;
    public static final int ONE_MINUTE = 60 * ONE_SECOND;
    public static final int TICKS_IN_SECOND = 20;
    public static final int TICKS_IN_MINUTE = TICKS_IN_SECOND * 60;
    
    public static final int TOKEN_PER_KILL = 10;
    public static final int TOKEN_PER_DESTRUCTION = 1000;

    public static final float PLAYER_SPEED  = 0.2F;
    public static final float PLAYER_HEALTH = 20F;

    public static final String NBT_TYPE_CATEGORY = "category";
    public static final String NBT_TYPE_ITEM = "item";
    public static final String NBT_TYPE_SERVICE = "service";
    public static final String NBT_CATEGORY_POTIONS = "potions";
    public static final String NBT_CATEGORY_UPGRADES = "upgrades";
    public static final String NBT_CATEGORY_FOOD = "food";
    public static int CONSTANT_POTION_DURATION = Integer.MAX_VALUE;
}
