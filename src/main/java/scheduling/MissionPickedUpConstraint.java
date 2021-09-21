package scheduling;

import warehouse.Mission;

public class MissionPickedUpConstraint implements PrecedenceConstraint {

    private final Mission mission;

    public MissionPickedUpConstraint(Mission mission) {
        this.mission = mission;
    }

    @Override
    public boolean satisfied() {
        return this.mission.pickedUp();
    }

    @Override
    public double expectedSatisfactionTime() {
        return this.mission.getExpectedPickUpTime();
    }

}
