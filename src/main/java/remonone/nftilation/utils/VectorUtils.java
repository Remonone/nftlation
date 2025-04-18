package remonone.nftilation.utils;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.Random;

public class VectorUtils {

    private static final Random random = new Random();
    
    public static final Vector ZERO = new Vector(0, 0, 0);
    public static final Vector ONE = new Vector(1, 1, 1);
    public static final Vector DOWN = new Vector(0, -1, 0);
    public static final Vector UP = new Vector(0, 1, 0);
    public static final Vector LEFT = new Vector(-1, 0, 0);
    public static final Vector RIGHT = new Vector(1, 0, 0);
    public static final Vector FORWARD = new Vector(0, 0, 1);
    public static final Vector BACKWARD = new Vector(0, 0, -1);
    
    public static String convertRoundVectorString(Vector v) {
        if(v == null) {
            return "null";
        }
        return roundValue(v.getX()) + " " + roundValue(v.getY()) + " " + roundValue(v.getZ());
    }
    
    private static float roundValue(double value) {
        return (float)((int)(value * 100)) / 100;
    }

    public static Vector getRandomPosInCircle(Vector vec, float range) {
        double x = random.nextFloat() * range * (random .nextBoolean() ? -1 : 1);
        double zLimit = Math.sqrt(range * range - x * x);
        double z = random.nextFloat() * zLimit * (random .nextBoolean() ? -1 : 1);
        return new Vector(vec.getX() + x, vec.getY(), vec.getZ() + z);
    }
    
    public static Vector getBlockPositionOnDirection(World world, Vector origin, Vector direction, double maxDistance) {
        BlockIterator blockIt = new BlockIterator(world, origin, direction, 0D, (int)maxDistance);
        while(blockIt.hasNext()) {
            Block block = blockIt.next();
            if(!block.getType().equals(Material.AIR)) {
                return block.getLocation().toVector();
            }
        }
        return null;
    }
}
