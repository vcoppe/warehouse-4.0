package scheduling;

import warehouse.Mission;

public class MissionStartedConstraint implements PrecedenceConstraint {

    private final Mission mission;

    public MissionStartedConstraint(Mission mission) {
        this.mission = mission;
    }

    @Override
    public boolean satisfied() {
        return this.mission.started();
    }

    @Override
    public double expectedSatisfactionTime() {
        return this.mission.getExpectedStartTime();
    }
}
