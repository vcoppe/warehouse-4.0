package pathfinding;

import agent.Lift;
import agent.Mobile;
import util.DoublePrecisionConstraint;
import util.Pair;
import util.Vector2D;
import util.Vector3D;
import warehouse.Mission;

import java.util.*;

public class SafeIntervalPathPlanning extends PathFinder {

    private static final boolean debug = false;

    private final ReverseResumableAStar reverseResumableAStar;
    private final HashMap<Vector3D, ArrayList<Interval>> safeIntervals;
    private final PriorityQueue<State> queue;
    private final HashMap<State, Cost> distance;
    private final HashSet<State> visited;
    private final ArrayList<State> successors;

    private Mobile mobile;

    public SafeIntervalPathPlanning(Graph graph, ArrayList<Mobile> mobiles) {
        super(graph, mobiles);
        this.reverseResumableAStar = new ReverseResumableAStar(graph);
        this.safeIntervals = new HashMap<>();
        this.queue = new PriorityQueue<>();
        this.distance = new HashMap<>();
        this.visited = new HashSet<>();
        this.successors = new ArrayList<>();
    }

    @Override
    protected Path findPath(double time, Mobile mobile) {
        if (debug)
            System.out.println("compute path for mobile " + mobile.getId() + " available? " + mobile.isAvailable());
        this.mobile = mobile;
        this.safeIntervals.clear();

        Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getTimedPositionsAt(time);
        Pair<Vector3D, Double> preStartTimedPosition = pair.first;
        Pair<Vector3D, Double> startTimedPosition = pair.second;
        Vector3D endPosition = mobile.getTargetPosition();
        Vector3D chargingPosition = mobile.getChargingPosition();

        ArrayList<Landmark> landmarks = new ArrayList<>();

        if (mobile.isAvailable()) {
            landmarks.add(new Landmark(startTimedPosition.first, startTimedPosition.second, false));
            landmarks.add(new Landmark(endPosition, startTimedPosition.second, false));
            landmarks.add(new Landmark(endPosition, Double.MAX_VALUE, false));
        } else {
            Mission mission = mobile.getMission();
            if (mission.pickedUp()) {
                landmarks.add(new Landmark(startTimedPosition.first, startTimedPosition.second, true));
                landmarks.add(new Landmark(endPosition, startTimedPosition.second, false));
                landmarks.add(new Landmark(chargingPosition, startTimedPosition.second, false));
            } else {
                landmarks.add(new Landmark(startTimedPosition.first, startTimedPosition.second, false));
                landmarks.add(new Landmark(endPosition, startTimedPosition.second, true));
                landmarks.add(new Landmark(chargingPosition, startTimedPosition.second, false));
            }
        }

        State solution = this.findPath(landmarks);

        if (solution == null) {
            return null;
        }

        Path path = this.rebuildSolution(solution);
        path.add(preStartTimedPosition);
        path.add(chargingPosition, Double.MAX_VALUE);

        return path;
    }

    private State findPath(ArrayList<Landmark> landmarks) {
        ArrayList<State> starts = this.getStates(landmarks.get(0), true, mobile.getId());

        if (debug) {
            System.out.println("landmarks");
            for (Landmark landmark : landmarks) {
                System.out.println(landmark.position);
            }
        }

        if (starts.isEmpty()) {
            if (debug) System.out.println("starts empty right away");
            return null;
        } else {
            State start = starts.get(0);
            start.cost.g = landmarks.get(0).time;
        }

        for (int i = 1; i < landmarks.size(); i++) {
            for (State start : starts) {
                start.loaded = landmarks.get(i - 1).loaded;
            }
            ArrayList<State> goals = this.getStates(landmarks.get(i), false, mobile.getId());
            if (goals.isEmpty()) {
                if (debug) System.out.println("goals empty");
                return null;
            }

            if (debug) System.out.println("from " + starts.get(0).position + " to " + goals.get(0).position);
            starts = this.findPaths(starts, goals);
            if (starts.isEmpty()) {
                if (debug) System.out.println("starts empty");
                return null;
            }
        }

        State goal = starts.get(starts.size() - 1);
        if (goal.interval.end < Double.MAX_VALUE) {
            return null;
        } else {
            return goal;
        }
    }

    private ArrayList<State> findPaths(ArrayList<State> starts, ArrayList<State> goals) {
        Vector3D startPosition = starts.get(0).position;
        Vector3D goalPosition = goals.get(0).position;
        ArrayList<State> solutions = new ArrayList<>();

        this.distance.clear();
        this.visited.clear();
        this.queue.clear();

        this.reverseResumableAStar.init(startPosition, goalPosition);

        for (State start : starts) {
            this.queue.add(start);
            this.distance.put(start, start.cost);
        }

        while (!this.queue.isEmpty()) {
            State state = this.queue.poll();

            if (this.visited.contains(state)) continue;
            if (state.cost.g > this.distance.get(state).g) continue;

            if (debug) System.out.println(state.position + " " + state.cost.g + " " + state.interval);

            this.visited.add(state);

            if (state.position.equals(goalPosition)) {
                for (State goal : goals) {
                    if (state.cost.g <= goal.interval.end && state.interval.end >= goal.interval.start) {
                        solutions.add(new State(
                                state.position,
                                state.intervalId,
                                state.interval,
                                new Cost(Math.max(state.cost.g, goal.interval.start), state.cost.h),
                                state.parent,
                                state.loaded
                        ));
                    }
                }

                if (solutions.size() == goals.size()) {
                    return solutions;
                }
            }

            ArrayList<State> successors = this.getSuccessors(state);
            for (State successor : successors) {
                this.distance.put(successor, successor.cost);
                this.queue.add(successor);
            }
        }

        return solutions;
    }

    private ArrayList<State> getSuccessors(State parent) {
        this.successors.clear();

        for (Edge edge : this.graph.getEdges(parent.position)) {
            if (!edge.canCross(this.mobile)) {
                if (debug) System.out.println("cannot cross to " + edge.to);
                continue;
            }

            Vector3D position = edge.to;

            if (debug) System.out.println("trying to reach " + position);

            Vector2D dist2D = this.reverseResumableAStar.distance(this.mobile, position);
            if (dist2D == null) {
                if (debug) System.out.println("no reverse path");
                continue;
            }

            double edgeCost = this.getEdgeCost(edge.from, edge.to, parent.loaded);
            double h = DoublePrecisionConstraint.round(dist2D.getX() * Mobile.getSpeed(parent.loaded) + dist2D.getY() * Lift.speed);

            ArrayList<Interval> safeIntervals = this.getSafeIntervals(position, mobile.getId());
            for (int intervalId = 0; intervalId < safeIntervals.size(); intervalId++) {
                Interval safeInterval = safeIntervals.get(intervalId);
                if (debug) System.out.println(safeInterval);
                Cost childCost = new Cost(DoublePrecisionConstraint.round(parent.cost.g + edgeCost), h);

                // check if parent and child intervals are compatible with the move
                if (parent.cost.g < safeInterval.start) { // safe interval needs to cover whole incoming move
                    childCost.g = DoublePrecisionConstraint.round(safeInterval.start + edgeCost);
                }
                if (childCost.g > parent.interval.end) { // cannot wait at parent to reach this safe interval
                    continue;
                }
                if (DoublePrecisionConstraint.round(childCost.g + edgeCost) > safeInterval.end) { // safe interval needs to cover outgoing move too
                    continue;
                }

                State child = new State(position, intervalId, safeInterval, childCost, parent, parent.loaded);

                if (this.visited.contains(child)) {
                    continue;
                }

                if (this.distance.containsKey(child) && childCost.compareTo(this.distance.get(child)) >= 0) {
                    continue;
                }

                if (debug) System.out.println("added");
                this.successors.add(child);
            }
        }

        return this.successors;
    }

    private Path rebuildSolution(State solution) {
        Path path = new Path();
        State previous = null, current = solution;

        while (current != null) {
            if (previous != null && !previous.position.equals(current.position)) {
                double edgeCost = this.getEdgeCost(current.position, previous.position, current.loaded);
                if (DoublePrecisionConstraint.round(current.cost.g + edgeCost) < previous.cost.g) { // mobile waited there
                    path.add(current.position, DoublePrecisionConstraint.round(previous.cost.g - edgeCost));
                }
            }

            path.add(current.position, current.cost.g);

            previous = current;
            current = current.parent;
        }

        return path;
    }

    private ArrayList<State> getStates(Landmark landmark, boolean startLandmark, int id) {
        ArrayList<State> states = new ArrayList<>();

        ArrayList<Interval> safeIntervals = this.getSafeIntervals(landmark.position, id);
        for (int intervalId = 0; intervalId < safeIntervals.size(); intervalId++) {
            Interval interval = safeIntervals.get(intervalId);
            if (startLandmark) {
                if (landmark.time >= interval.start && landmark.time <= interval.end) {
                    states.add(new State(landmark.position, intervalId, interval));
                }
            } else {
                if (landmark.time <= interval.end) {
                    states.add(new State(landmark.position, intervalId, interval));
                }
            }
        }

        return states;
    }

    private ArrayList<Interval> getSafeIntervals(Vector3D position, int id) {
        if (this.safeIntervals.containsKey(position)) {
            return this.safeIntervals.get(position);
        }

        ArrayList<Interval> safeIntervals = this.table.getSafeIntervals(position, id);
        this.safeIntervals.put(position, safeIntervals);

        return safeIntervals;
    }

    private double getEdgeCost(Vector3D from, Vector3D to, boolean loaded) {
        Vector2D distance = from == to ? Vector2D.zero : this.graph.getWeight(from, to);
        return DoublePrecisionConstraint.round(distance.getX() * Mobile.getSpeed(loaded) + distance.getY() * Lift.speed);
    }

    static class State implements Comparable<State> {

        State parent;
        Vector3D position;
        Cost cost;
        int intervalId;
        Interval interval;
        boolean loaded;

        public State(Vector3D position, int intervalId, Interval interval, Cost cost, State parent, boolean loaded) {
            this.position = position;
            this.intervalId = intervalId;
            this.interval = interval;
            this.cost = cost;
            this.parent = parent;
            this.loaded = loaded;
        }

        public State(Vector3D position, int intervalId, Interval interval) {
            this(position, intervalId, interval, new Cost(0, 0), null, false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return this.intervalId == state.intervalId && this.position.equals(state.position);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.position, this.intervalId);
        }

        @Override
        public int compareTo(State other) {
            if (this.cost.getEstimate() == other.cost.getEstimate()) {
                return this.cost.compareTo(other.cost);
            }
            return Double.compare(this.cost.getEstimate(), other.cost.getEstimate());
        }
    }

    private static class Cost implements Comparable<Cost> {

        double g, h;

        public Cost(double g, double h) {
            this.g = g;
            this.h = h;
        }

        public double getEstimate() {
            return this.g + this.h;
        }

        @Override
        public int compareTo(Cost other) {
            return Double.compare(this.g, other.g);
        }

    }

    private static class Landmark {

        Vector3D position;
        double time;
        boolean loaded;

        public Landmark(Vector3D position, double time, boolean loaded) {
            this.position = position;
            this.time = time;
            this.loaded = loaded;
        }

    }

}
