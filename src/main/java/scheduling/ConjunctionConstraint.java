package scheduling;

import java.util.ArrayList;
import java.util.Arrays;

public class ConjunctionConstraint extends PrecedenceConstraint {

    private final ArrayList<PrecedenceConstraint> constraints;

    public ConjunctionConstraint(PrecedenceConstraint... constraints) {
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
            if (!constraint.satisfied()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double expectedSatisfactionTime() {
        double expectedSatisfactionTime = 0;
        for (PrecedenceConstraint constraint : this.constraints) {
            expectedSatisfactionTime = Math.max(expectedSatisfactionTime, constraint.expectedSatisfactionTime());
        }
        return expectedSatisfactionTime;
    }

}
