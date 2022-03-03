package scheduling;

import brain.TravelTimeEstimator;
import warehouse.Mission;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

/**
 * Represents the missions and their precedence constraints as a directed acyclic graph
 */
public class TimeEstimationPropagator {

    private final TravelTimeEstimator travelTimeEstimator;
    private final HashMap<Integer, Mission> missions;
    private final HashMap<Integer, HashSet<Integer>> parents, children;

    public TimeEstimationPropagator(TravelTimeEstimator travelTimeEstimator) {
        this.travelTimeEstimator = travelTimeEstimator;
        this.missions = new HashMap<>();
        this.parents = new HashMap<>();
        this.children = new HashMap<>();
    }

    public void add(Mission mission) {
        if (this.missions.containsKey(mission.getId())) {
            return;
        }

        this.missions.put(mission.getId(), mission);
        this.parents.put(mission.getId(), new HashSet<>());
        this.children.put(mission.getId(), new HashSet<>());
    }

    public void remove(Mission mission) {
        if (!this.missions.containsValue(mission.getId())) {
            return;
        }

        // notify parents that mission is done
        for (int parent : this.parents.get(mission.getId())) {
            HashSet<Integer> parentChildren = this.children.get(parent);
            parentChildren.remove(mission.getId());
            if (parentChildren.isEmpty()) { // parent has no more children, can delete it
                this.missions.remove(parent);
                this.parents.remove(parent);
                this.children.remove(parent);
            }
        }

        // delete mission if no children
        if (this.children.get(mission.getId()).isEmpty()) {
            this.missions.remove(mission.getId());
            this.parents.remove(mission.getId());
            this.children.remove(mission.getId());
        }
    }

    public void propagate() {
        TreeSet<Integer> open = new TreeSet<>();

        HashMap<Integer, Integer> countParents = new HashMap<>();

        // get all missions that have no dependencies
        for (Map.Entry<Integer, HashSet<Integer>> entry : this.parents.entrySet()) {
            if (entry.getValue().isEmpty()) {
                open.add(entry.getKey());
            }
            countParents.put(entry.getKey(), entry.getValue().size());
        }

        // update expected end times in topological order
        while (!open.isEmpty()) {
            int u = open.first();
            open.remove(u);

            Mission mission = this.missions.get(u);
            if (!mission.started()) {
                mission.setExpectedStartTime(mission.getStartConstraint().expectedSatisfactionTime());
                mission.setExpectedPickUpTime(Math.max(
                        mission.getExpectedStartTime(),
                        mission.getPickupConstraint().expectedSatisfactionTime()
                ));
                if (!mission.isComplete()) {
                    mission.setExpectedEndTime(mission.getExpectedPickUpTime());
                } else {
                    mission.setExpectedEndTime(mission.getExpectedPickUpTime() + this.travelTimeEstimator.estimate(
                            mission.getStartPosition(),
                            mission.getEndPosition()
                    ));
                }
            }

            for (int v : this.children.get(u)) {
                if (countParents.compute(v, (key, value) -> value - 1) == 0) {
                    open.add(v);
                }
            }
        }
    }

}
