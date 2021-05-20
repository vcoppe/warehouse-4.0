package warehouse;

public class MissionStartedCondition extends Condition {

    private final Mission mission;

    public MissionStartedCondition(Mission mission) {
        this.mission = mission;
    }

    @Override
    public boolean satisfied() {
        return this.mission.started();
    }

}
