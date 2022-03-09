package scheduling;

import warehouse.Mission;

public class MissionDoneConstraint extends PrecedenceConstraint {

    private final Mission mission;

    public MissionDoneConstraint(Mission mission) {
        this.mission = mission;
        this.precedingMissions.add(mission);
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
