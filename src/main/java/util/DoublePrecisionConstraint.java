package util;

public class DoublePrecisionConstraint {

    public static double round(double value) {
        return Math.round(100 * value) / 100.0;
    }

}
