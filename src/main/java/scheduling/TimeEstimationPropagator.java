package scheduling;

import warehouse.Mission;
import warehouse.Warehouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Represents the missions and their precedence constraints as a directed acyclic graph
 */
public class TimeEstimationPropagator {

    private final Warehouse warehouse;
    private final HashMap<Integer, Mission> missions;

    public TimeEstimationPropagator(Warehouse warehouse) {
        this.warehouse = warehouse;
        this.missions = new HashMap<>();
    }

    public void add(Mission mission) {
        if (this.missions.containsKey(mission.getId())) {
            return;
        }

        this.missions.put(mission.getId(), mission);
    }

    public void remove(Mission mission) {
        this.missions.remove(mission.getId());
    }

    public void propagate(double time) {
        TreeSet<Integer> open = new TreeSet<>();

        HashMap<Integer, HashSet<Integer>> children = new HashMap<>();
        HashMap<Integer, Integer> countParents = new HashMap<>();

        // get all missions that have no active dependencies
        for (Mission mission : this.missions.values()) {
            ArrayList<Mission> precedingMissions = mission.getPrecedingMissions();
            int countParent = 0;
            for (Mission parent : precedingMissions) {
                if (this.missions.containsKey(parent.getId())) { // check that mission is still active
                    countParent++;

                    if (!children.containsKey(parent.getId())) {
                        children.put(parent.getId(), new HashSet<>());
                    }

                    children.get(parent.getId()).add(mission.getId());
                }
            }

            if (countParent == 0) {
                open.add(mission.getId());
            }
            mission.setMissionPathMaxLength(0); // reset all to 0 before propagating values
            countParents.put(mission.getId(), countParent);
        }

        // update expected end times in topological order
        while (!open.isEmpty()) {
            int u = open.first();
            open.remove(u);

            Mission mission = this.missions.get(u);
            this.updateEstimates(mission, time);

            if (children.containsKey(u)) {
                for (int v : children.get(u)) {
                    Mission child = this.missions.get(v);
                    child.setMissionPathMaxLength(Math.max(
                            child.getMissionPathMaxLength(),
                            mission.getMissionPathMaxLength() + 1
                    ));
                    if (countParents.compute(v, (key, value) -> value - 1) == 0) {
                        open.add(v);
                    }
                }
            }
        }
    }

    private void updateEstimates(Mission mission, double time) {
        if (mission.done()) {
            return;
        } else if (mission.pickedUp()) {
            mission.setExpectedEndTime(Math.max(
                    mission.getExpectedEndTime(),
                    mission.dropConstraint.expectedSatisfactionTime()
            ));
        } else if (mission.started()) {
            double travelTime = this.warehouse.getTravelTime(
                    mission.getStartPosition(),
                    mission.getEndPosition(),
                    mission.getMobile(),
                    true
            );
            mission.setExpectedPickUpTime(Math.max(
                    mission.getExpectedPickUpTime(),
                    mission.pickupConstraint.expectedSatisfactionTime()
            ));
            mission.setExpectedEndTime(Math.max(
                    mission.getExpectedEndTime(),
                    Math.max(
                            mission.getExpectedPickUpTime() + travelTime,
                            mission.dropConstraint.expectedSatisfactionTime()
                    )
            ));
        } else {
            mission.setExpectedStartTime(Math.max(
                    Math.max(time, mission.getExpectedStartTime()),
                    mission.startConstraint.expectedSatisfactionTime()
            ));
            mission.setExpectedPickUpTime(Math.max(
                    Math.max(mission.getExpectedStartTime(), mission.getExpectedPickUpTime()),
                    mission.pickupConstraint.expectedSatisfactionTime()
            ));
            mission.setExpectedEndTime(Math.max(
                    Math.max(mission.getExpectedPickUpTime(), mission.getExpectedEndTime()),
                    mission.dropConstraint.expectedSatisfactionTime()
            ));
        }
    }

}
