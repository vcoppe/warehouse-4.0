package scheduling;

public interface PrecedenceConstraint {

    PrecedenceConstraint emptyConstraint = new PrecedenceConstraint() {
        @Override
        public boolean satisfied() {
            return true;
        }

        @Override
        public double expectedSatisfactionTime() {
            return Double.MIN_VALUE;
        }
    };

    boolean satisfied();

    double expectedSatisfactionTime();

}
