package remonone.nftilation.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class BlockUtils {

    public static Block getNearestEmptySpace(Block b, int maxRadius) {
        BlockFace[] faces = {BlockFace.UP, BlockFace.NORTH, BlockFace.EAST};
        BlockFace[][] orth = {{BlockFace.NORTH, BlockFace.EAST}, {BlockFace.UP, BlockFace.EAST}, {BlockFace.NORTH, BlockFace.UP}};
        for (int r = 0; r <= maxRadius; r++) {
            for (int side = 0; side < 6; side++) {
                BlockFace f = faces[side%3];
                BlockFace[] o = orth[side%3];
                if (side >= 3)
                    f = f.getOppositeFace();
                Block c = b.getRelative(f, r);
                for (int x = -r; x <= r; x++) {
                    for (int y = -r; y <= r; y++) {
                        Block a = c.getRelative(o[0], x).getRelative(o[1], y);
                        if (a.getType() == Material.AIR && a.getRelative(BlockFace.UP).getType() == Material.AIR)
                            return a;
                    }
                }
            }
        }
        return null;// no empty space within a cube of (2*(maxradius+1))^3
    }
}
