package scheduling;

import warehouse.Mission;

import java.util.ArrayList;

public abstract class PrecedenceConstraint {

    public static final PrecedenceConstraint emptyConstraint = new PrecedenceConstraint() {
        @Override
        boolean satisfied() {
            return true;
        }

        @Override
        double expectedSatisfactionTime() {
            return 0;
        }
    };

    protected final ArrayList<Mission> precedingMissions;

    public PrecedenceConstraint() {
        this.precedingMissions = new ArrayList<>();
    }

    abstract boolean satisfied();

    abstract double expectedSatisfactionTime();

    public ArrayList<Mission> getPrecedingMissions() {
        return this.precedingMissions;
    }

}
