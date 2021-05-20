package brain;

import agent.Mobile;
import util.Pair;
import warehouse.Mission;

import java.util.ArrayList;

public interface MobileMissionSelector {

    ArrayList<Pair<Mobile, Mission>> matchMobileMission(double time, ArrayList<Mobile> mobiles, ArrayList<Mission> missions);

}
