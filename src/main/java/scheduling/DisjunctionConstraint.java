package scheduling;

import java.util.ArrayList;
import java.util.Arrays;

public class DisjunctionConstraint extends PrecedenceConstraint {

    private final ArrayList<PrecedenceConstraint> constraints;

    public DisjunctionConstraint(PrecedenceConstraint... constraints) {
        this.constraints = new ArrayList<>(Arrays.asList(constraints));
        for (PrecedenceConstraint precedenceConstraint : constraints) {
            this.precedingMissions.addAll(precedenceConstraint.precedingMissions);
        }
    }

    public void add(PrecedenceConstraint constraint) {
        this.constraints.add(constraint);
        this.precedingMissions.addAll(constraint.precedingMissions);
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
