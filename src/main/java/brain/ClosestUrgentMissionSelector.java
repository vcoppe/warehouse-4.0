package brain;

import agent.Mobile;
import util.Pair;
import warehouse.Mission;
import warehouse.Position;
import warehouse.Warehouse;

import java.util.ArrayList;
import java.util.HashSet;

public class ClosestUrgentMissionSelector implements MobileMissionSelector {

    private final Warehouse warehouse;

    public ClosestUrgentMissionSelector(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    @Override
    public ArrayList<Pair<Mobile, Mission>> matchMobileMission(double time, ArrayList<Mobile> mobiles, ArrayList<Mission> missions) {
        ArrayList<Pair<Mobile, Mission>> matching = new ArrayList<>();
        HashSet<Integer> taken = new HashSet<>();

        for (Mobile mobile : mobiles) {
            double shortestDist = Double.MAX_VALUE;
            Mission closestMission = null;

            Pair<Pair<Position,Double>,Pair<Position,Double>> pair = mobile.getPositionsAt(time);
            Position position = pair.second.first;
            double offset = pair.second.second - time;

            for (Mission mission : missions) {
                if (!taken.contains(mission.getId())) {
                    double dist = offset + this.warehouse.getTravelTime(position, mission.getStartPosition(), mobile);
                    if ((dist < shortestDist && (closestMission == null || mission.getInitTime() <= closestMission.getInitTime()))
                            || (closestMission != null && mission.getInitTime() <= closestMission.getInitTime() - 300)) {
                        shortestDist = dist;
                        closestMission = mission;
                    }
                }
            }

            if (closestMission != null) {
                taken.add(closestMission.getId());
                matching.add(new Pair<>(mobile, closestMission));
            }
        }

        return matching;
    }

}
