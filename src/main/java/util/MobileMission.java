package util;

import agent.Mobile;
import warehouse.Mission;

public class MobileMission {

    public final Mobile mobile;
    public final Mission mission;

    public MobileMission(Mobile mobile, Mission mission) {
        this.mobile = mobile;
        this.mission = mission;
    }

}
