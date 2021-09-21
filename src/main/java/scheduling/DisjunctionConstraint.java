package scheduling;

import java.util.ArrayList;
import java.util.Arrays;

public class DisjunctionConstraint implements PrecedenceConstraint {

    private final ArrayList<PrecedenceConstraint> constraints;

    public DisjunctionConstraint(PrecedenceConstraint... constraints) {
        this.constraints = new ArrayList<>(Arrays.asList(constraints));
    }

    public void add(PrecedenceConstraint constraint) {
        this.constraints.add(constraint);
    }

    @Override
    public boolean satisfied() {
        for (PrecedenceConstraint constraint : this.constraints) {
            if (constraint.satisfied()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double expectedSatisfactionTime() {
        double expectedSatisfactionTime = Double.MAX_VALUE;
        for (PrecedenceConstraint constraint : this.constraints) {
            expectedSatisfactionTime = Math.min(expectedSatisfactionTime, constraint.expectedSatisfactionTime());
        }
        return expectedSatisfactionTime;
    }

}
