package brain;

import agent.Mobile;
import util.MobileMission;
import warehouse.Mission;

import java.util.ArrayList;

public interface MobileMissionSelector {

    ArrayList<MobileMission> matchMobileMission(ArrayList<Mobile> mobiles, ArrayList<Mission> missions);

}
