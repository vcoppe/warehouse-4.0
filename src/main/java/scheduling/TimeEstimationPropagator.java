package scheduling;

import warehouse.Mission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Represents the missions and their precedence constraints as a directed acyclic graph
 */
public class TimeEstimationPropagator {

    private final HashMap<Integer, Mission> missions;

    public TimeEstimationPropagator() {
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
            countParents.put(mission.getId(), countParent);
        }

        // update expected end times in topological order
        while (!open.isEmpty()) {
            int u = open.first();
            open.remove(u);

            Mission mission = this.missions.get(u);
            if (!mission.started()) {
                mission.setExpectedStartTime(Math.max(
                        time,
                        mission.getStartConstraint().expectedSatisfactionTime()
                ));
                mission.setExpectedPickUpTime(Math.max(
                        mission.getExpectedStartTime(),
                        mission.getPickupConstraint().expectedSatisfactionTime()
                ));
                if (!mission.isComplete()) {
                    mission.setExpectedEndTime(mission.getExpectedPickUpTime());
                } else {
                    mission.setExpectedEndTime(mission.getExpectedPickUpTime());
                }
            }

            if (children.containsKey(u)) {
                for (int v : children.get(u)) {
                    if (countParents.compute(v, (key, value) -> value - 1) == 0) {
                        open.add(v);
                    }
                }
            }
        }
    }

}
