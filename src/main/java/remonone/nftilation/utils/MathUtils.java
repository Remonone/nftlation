package remonone.nftilation.utils;

public class MathUtils {

    public static double boundValues(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
