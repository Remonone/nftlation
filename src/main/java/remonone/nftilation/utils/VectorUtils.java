package remonone.nftilation.utils;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Random;

public class VectorUtils {

    private static final Random random = new Random();
    
    
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
    
    public static Vector getRandomPosInSphere(Vector vec, float range) {
        double u = Math.random();
        double v = Math.random();
        double theta = u * 2.0 * Math.PI;
        double phi = Math.acos(2.0 * v - 1.0);
        double r = Math.cbrt(Math.random() * range);
        double sinTheta = Math.sin(theta);
        double cosTheta = Math.cos(theta);
        double sinPhi = Math.sin(phi);
        double cosPhi = Math.cos(phi);
        double x = r * sinPhi * cosTheta;
        double y = r * sinPhi * sinTheta;
        double z = r * cosPhi;
        return new Vector(x, y, z).add(vec);
    }
    
    public static Vector getBlockPositionOnDirection(World world, Vector origin, Vector direction, double maxDistance) {
        Vector stepper = origin.clone();
        double initialPoint = 0;
        while(initialPoint < maxDistance) {
            Vector newStep = stepper.add(direction);
            Block block = world.getBlockAt(newStep.getBlockX(), newStep.getBlockY(), newStep.getBlockZ());
            if(!block.getType().equals(Material.AIR)) {
                return newStep;
            }
            initialPoint = origin.distance(newStep);
        }
        return null;
    }
}
