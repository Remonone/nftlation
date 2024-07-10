package remonone.nftilation.utils;

import org.bukkit.util.Vector;

public class VectorUtils {
    public static String convertRoundVectorString(Vector v) {
        if(v == null) {
            return "null";
        }
        return roundValue(v.getX()) + " " + roundValue(v.getY()) + " " + roundValue(v.getZ());
    }
    
    private static float roundValue(double value) {
        return (float)((int)(value * 100)) / 100;
    }
}
