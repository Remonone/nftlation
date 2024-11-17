package remonone.nftilation.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;

public class BlockConstants {
    
    private static final Map<Material, BlockDetails> RESPAWNABLE_BLOCKS = new HashMap<Material, BlockDetails>() {{
        put(Material.COAL_ORE, new BlockDetails(20, 3, null));
        put(Material.IRON_ORE, new BlockDetails(20, 5, null));
        put(Material.DIAMOND_ORE, new BlockDetails(40, 20, null));
        put(Material.GRAVEL, new BlockDetails(10, 0, Arrays.asList(Material.FEATHER, Material.STRING, Material.FLINT)));
        put(Material.MELON_BLOCK, new BlockDetails(10, 0, Arrays.asList(Material.MELON, Material.APPLE, Material.RAW_BEEF, Material.PORK, Material.COOKIE, Material.RAW_FISH, Material.MUSHROOM_SOUP, Material.BEETROOT_SOUP)));
        put(Material.LOG, new BlockDetails(10, 0, null));
        put(Material.GOLD_ORE, new BlockDetails(30, 10, Collections.singletonList(Material.GOLD_NUGGET)));
    }};
    
    public static boolean isRespawnableBlock(Block block) {
        return RESPAWNABLE_BLOCKS.containsKey(block.getType());
    }
    
    public static boolean isRespawnableBlock(Material material) {
        return RESPAWNABLE_BLOCKS.containsKey(material);
    }
    
    public static int getMaterialCooldown(Material mat) {
        return RESPAWNABLE_BLOCKS.getOrDefault(mat, new BlockDetails(-1, 0, null)).secondsCooldown;
    }
    public static int getTokensFromBlock(Material mat) {
        return RESPAWNABLE_BLOCKS.getOrDefault(mat, new BlockDetails(-1, 0, null)).tokenOnCollect;
    }
    
    public static List<Material> getCustomDrops(Material mat) {
        return RESPAWNABLE_BLOCKS.getOrDefault(mat, new BlockDetails(-1, 0, null)).customDrops;
    }
    
    @Getter
    @AllArgsConstructor
    public static class BlockDetails {
        int secondsCooldown;
        int tokenOnCollect;
        List<Material> customDrops;
    }
}
