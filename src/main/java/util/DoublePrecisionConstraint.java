package util;

public class DoublePrecisionConstraint {
    
    private static double precision = 100;

    public static double round(double value) {
        return Math.round(precision * value) / precision;
    }

}
