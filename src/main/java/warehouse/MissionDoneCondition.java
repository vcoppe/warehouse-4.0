package warehouse;

public class MissionDoneCondition extends Condition {

    private final Mission mission;

    public MissionDoneCondition(Mission mission) {
        this.mission = mission;
    }

    @Override
    public boolean satisfied() {
        return this.mission.done();
    }

}
