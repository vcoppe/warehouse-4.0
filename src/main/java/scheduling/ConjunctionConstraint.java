package scheduling;

import java.util.ArrayList;
import java.util.Arrays;

public class ConjunctionConstraint implements PrecedenceConstraint {

    private final ArrayList<PrecedenceConstraint> constraints;

    public ConjunctionConstraint(PrecedenceConstraint... constraints) {
        this.constraints = new ArrayList<>(Arrays.asList(constraints));
    }

    public void add(PrecedenceConstraint constraint) {
        this.constraints.add(constraint);
    }

    @Override
    public boolean satisfied() {
        for (PrecedenceConstraint constraint : this.constraints) {
            if (!constraint.satisfied()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double expectedSatisfactionTime() {
        double expectedSatisfactionTime = Double.MIN_VALUE;
        for (PrecedenceConstraint constraint : this.constraints) {
            expectedSatisfactionTime = Math.max(expectedSatisfactionTime, constraint.expectedSatisfactionTime());
        }
        return expectedSatisfactionTime;
    }

}
