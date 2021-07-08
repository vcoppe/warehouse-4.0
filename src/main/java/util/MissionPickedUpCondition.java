package util;

import warehouse.Mission;

public class MissionPickedUpCondition extends Condition {

    private final Mission mission;

    public MissionPickedUpCondition(Mission mission) {
        this.mission = mission;
    }

    @Override
    public boolean satisfied() {
        return this.mission.pickedUp();
    }

}
