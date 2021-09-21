package scheduling;

import warehouse.Mission;

public class MissionDoneConstraint implements PrecedenceConstraint {

    private final Mission mission;

    public MissionDoneConstraint(Mission mission) {
        this.mission = mission;
    }

    @Override
    public boolean satisfied() {
        return this.mission.done();
    }

    @Override
    public double expectedSatisfactionTime() {
        return this.mission.getExpectedEndTime();
    }

}
