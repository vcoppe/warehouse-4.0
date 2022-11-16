package brain;

import agent.Mobile;
import pathfinding.HungarianAlgorithm;
import util.Pair;
import util.Vector3D;
import warehouse.Mission;
import warehouse.Warehouse;

import java.util.ArrayList;

public class MobileMissionMatchingSelector implements MobileMissionSelector {

    private final Warehouse warehouse;

    public MobileMissionMatchingSelector(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    @Override
    public ArrayList<Pair<Mobile, Mission>> matchMobileMission(double time, ArrayList<Mobile> mobiles, ArrayList<Mission> missions) {
        if (mobiles.size() == 0 || missions.size() == 0) {
            return new ArrayList<>();
        }

        double[][] cost = new double[mobiles.size()][missions.size()];

        for (int i=0; i<mobiles.size(); i++) {
            Mobile mobile = mobiles.get(i);
            double offset = 0; // compute time to be available for a new mission
            Vector3D position = null;
            if (mobile.isAvailable()) {
                Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getTimedPositionsAt(time);
                position = pair.second.first;
                offset = pair.second.second - time;
            } else {
                Mission current = mobile.getMission();
                position = current.getEndPosition();
                offset = mobile.getPathEndTime() - time;
                if (!current.pickedUp()) {
                    offset += this.warehouse.getTravelTime(current.getStartPosition(), current.getEndPosition(), mobile, true);
                }
            }

            for (int j = 0; j < missions.size(); j++) {
                Mission mission = missions.get(j);
                cost[i][j] = offset + this.warehouse.getTravelTime(position, mission.getStartPosition(), mobile, false);
            }
        }

        HungarianAlgorithm algorithm = new HungarianAlgorithm(cost);
        int[] assignment = algorithm.execute();

        ArrayList<Pair<Mobile, Mission>> matching = new ArrayList<>();
        for (int i=0; i<mobiles.size(); i++) {
            int j = assignment[i];
            if (j != -1) {
                matching.add(new Pair<>(mobiles.get(i), missions.get(j)));
            }
        }

        return matching;
    }

}
