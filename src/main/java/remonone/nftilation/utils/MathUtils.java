package remonone.nftilation.utils;

import org.bukkit.util.Vector;

public class MathUtils {

    public static double boundValues(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static Vector getRotationVector(float rotation) {
        double x = Math.sin(clamp(rotation)*Math.PI/180);
        double z = Math.cos(clamp(rotation)*Math.PI/180);
        return new Vector(x, 0, z);
    }

    private static float clamp(float value) {
        return value > 180 ? value - 360 : value < -180 ? value + 360 : value;
    }
}
