package scheduling;

import warehouse.Mission;

public class MissionStartedConstraint extends PrecedenceConstraint {

    private final Mission mission;

    public MissionStartedConstraint(Mission mission) {
        this.mission = mission;
        this.precedingMissions.add(mission);
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
