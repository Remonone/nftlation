package remonone.nftilation.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;

public class BlockConstants {
    
    private static final Map<Material, BlockDetails> RESPAWNABLE_BLOCKS = new HashMap<Material, BlockDetails>() {{
        put(Material.COAL_ORE, new BlockDetails(20, 3));
        put(Material.IRON_ORE, new BlockDetails(20, 5));
        put(Material.DIAMOND_ORE, new BlockDetails(40, 20));
        put(Material.GRAVEL, new BlockDetails(10, 0));
        put(Material.MELON_BLOCK, new BlockDetails(10, 0));
        put(Material.HAY_BLOCK, new BlockDetails(10, 0));
        put(Material.LOG, new BlockDetails(10, 0));
    }};
    
    public static boolean isRespawnableBlock(Block block) {
        return RESPAWNABLE_BLOCKS.containsKey(block.getType());
    }
    
    public static boolean isRespawnableBlock(Material material) {
        return RESPAWNABLE_BLOCKS.containsKey(material);
    }
    
    public static int getMaterialCooldown(Material mat) {
        return RESPAWNABLE_BLOCKS.getOrDefault(mat, new BlockDetails(-1, 0)).secondsCooldown;
    }
    public static int getTokensFromBlock(Material mat) {
        return RESPAWNABLE_BLOCKS.getOrDefault(mat, new BlockDetails(-1, 0)).tokenOnCollect;
    }
    
    @Getter
    @AllArgsConstructor
    public static class BlockDetails {
        int secondsCooldown;
        int tokenOnCollect;
    }
}
