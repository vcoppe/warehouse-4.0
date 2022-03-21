package pathfinding;

import agent.Lift;
import agent.Mobile;
import pathfinding.ConflictBasedSearch.Constraint;
import util.DoublePrecisionConstraint;
import util.Pair;
import util.Vector2D;
import util.Vector3D;

import java.util.*;

public class SafeIntervalPathPlanning {

    private final Graph graph;
    private final ReverseResumableAStar reverseResumableAStar;
    private final HashMap<Vector3D, HashMap<Vector3D, TreeSet<Interval>>> collisionIntervals;
    private final ArrayList<Constraint> landmarks;
    private final PriorityQueue<State> queue;
    private final HashMap<State, Cost> distance;
    private final HashSet<State> visited;
    private final ArrayList<State> successors;

    private Mobile mobile;

    public SafeIntervalPathPlanning(Graph graph) {
        this.graph = graph;
        this.reverseResumableAStar = new ReverseResumableAStar(graph);
        this.collisionIntervals = new HashMap<>();
        this.landmarks = new ArrayList<>();
        this.queue = new PriorityQueue<>();
        this.distance = new HashMap<>();
        this.visited = new HashSet<>();
        this.successors = new ArrayList<>();
    }

    public Path findPath(Mobile mobile, double time, ArrayList<Constraint> constraints) {
        this.mobile = mobile;
        this.reverseResumableAStar.init(mobile); // TODO cannot use this if landmarks

        Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getTimedPositionsAt(time);
        Pair<Vector3D, Double> preStartTimedPosition = pair.first;
        Pair<Vector3D, Double> startTimedPosition = pair.second;
        Vector3D endPosition = mobile.getTargetPosition();

        if (constraints != null) {
            this.addConstraints(constraints);
        }

        ArrayList<State> startStates = this.getStates(new Constraint(startTimedPosition.first, 0, Double.MAX_VALUE), true);
        ArrayList<State> goalStates = this.getStates(new Constraint(endPosition, 0, Double.MAX_VALUE), false);
        if (startStates.isEmpty() || goalStates.isEmpty()) {
            return null;
        }

        ArrayList<State> starts = new ArrayList<>();
        State startState = startStates.get(0); // mobile must start immediately
        startState.cost.g = time;
        starts.add(startState);

        for (Constraint landmark : this.landmarks) {
            ArrayList<State> goals = this.getStates(landmark, false);
            if (goals.isEmpty()) {
                return null;
            }

            ArrayList<State> solutions = this.findPaths(starts, goals, goals.get(goals.size() - 1).interval.end);
            if (solutions.isEmpty()) {
                return null;
            }

            starts = this.getStates(landmark, true);
            if (starts.isEmpty()) {
                return null;
            }

            this.matchSolutionsWithStarts(solutions, starts);
        }

        ArrayList<State> goals = new ArrayList<>();
        goals.add(goalStates.get(goalStates.size() - 1)); // mobile must stay there afterwards
        ArrayList<State> solutions = this.findPaths(starts, goals, Double.MAX_VALUE);

        if (solutions.isEmpty()) {
            return null;
        }

        Path path = new Path();
        State previous = null, current = solutions.get(0);
        while (current != null) {
            if (previous != null && previous.position != current.position) {
                double edgeCost = this.getEdgeCost(current.position, previous.position);
                if (current.cost.g + edgeCost < previous.cost.g) { // mobile waited there
                    path.add(current.position, current.cost.g + edgeCost);
                }
            }

            path.add(current.position, current.cost.g);

            previous = current;
            current = current.parent;
        }
        path.add(preStartTimedPosition);

        return path;
    }

    private void matchSolutionsWithStarts(ArrayList<State> solutions, ArrayList<State> starts) {
        // match solutions with starts (! offset between the two)
        // transfer cost
        // set parent to get whole path at once

        Vector3D from = solutions.get(0).position, to = starts.get(0).position;
        double edgeCost = this.getEdgeCost(from, to);
        TreeSet<Interval> collisionIntervals = null;
        if (this.collisionIntervals.containsKey(from) && this.collisionIntervals.get(from).containsKey(to)) {
            collisionIntervals = this.collisionIntervals.get(from).get(to);
        }

        for (State solution : solutions) {
            for (State start : starts) {
                if (solution.cost.g + edgeCost > start.interval.end) {
                    continue;
                }
                if (solution.cost.g + edgeCost < start.interval.start) {
                    solution.cost.g = start.interval.start - edgeCost;
                    if (solution.cost.g > solution.interval.end) {
                        continue;
                    }
                }
                if (collisionIntervals != null) {
                    Interval key = new Interval(solution.cost.g, Double.MAX_VALUE);
                    Interval collisionInterval = collisionIntervals.floor(key);
                    if (collisionInterval != null) {
                        if (solution.cost.g < collisionInterval.end) { // collision
                            solution.cost.g = collisionInterval.end;

                            if (solution.cost.g + edgeCost > start.interval.end || solution.cost.g > solution.interval.end) {
                                continue;
                            }
                        }
                    }
                }

                Cost startCost = new Cost(solution.cost.g + edgeCost, 0); // transfer cost of solution
                if (start.parent == null || startCost.compareTo(start.cost) < 0) {
                    start.parent = solution;
                    start.cost = startCost;
                }
            }

        }

        // remove starts with no parent
        for (int i = starts.size() - 1; i >= 0; i--) {
            if (starts.get(i).parent == null) {
                starts.remove(i);
            }
        }
    }

    private void addConstraints(ArrayList<Constraint> constraints) {
        this.collisionIntervals.clear();
        this.landmarks.clear();

        for (Constraint constraint : constraints) {
            if (constraint.positive) {
                this.landmarks.add(constraint);
            } else {
                if (!this.collisionIntervals.containsKey(constraint.from)) {
                    this.collisionIntervals.put(constraint.from, new HashMap<>());
                }
                HashMap<Vector3D, TreeSet<Interval>> collisionIntervalsFrom = this.collisionIntervals.get(constraint.from);
                if (!collisionIntervalsFrom.containsKey(constraint.to)) {
                    collisionIntervalsFrom.put(constraint.to, new TreeSet<>());
                }
                TreeSet<Interval> collisionIntervalsFromTo = collisionIntervalsFrom.get(constraint.to);
                this.addCollisionInterval(collisionIntervalsFromTo, new Interval(constraint.start, constraint.end));
            }
        }

        this.landmarks.sort(Comparator.comparing(c -> c.start));
    }

    private void addCollisionInterval(TreeSet<Interval> intervals, Interval interval) {
        ArrayList<Interval> toRemove = new ArrayList<>();

        for (Interval other : intervals) {
            if (interval.overlaps(other)) {
                interval.merge(other);
                toRemove.add(other);
            }
        }

        intervals.removeAll(toRemove);
        intervals.add(interval);
    }

    private double getEdgeCost(Vector3D from, Vector3D to) {
        Vector2D distance = from == to ? Vector2D.zero : this.graph.getWeight(from, to);
        return DoublePrecisionConstraint.round(distance.getX() * this.mobile.getSpeed() + distance.getY() * Lift.speed);
    }

    private ArrayList<State> getStates(Constraint landmark, boolean startLandmark) {
        Vector3D landmarkPosition = startLandmark ? landmark.to : landmark.from;
        double offset = startLandmark ? 0 : this.getEdgeCost(landmark.from, landmark.to);
        Interval stateInterval = new Interval(landmark.start + offset, Math.max(landmark.end, landmark.end + offset));

        ArrayList<State> states = new ArrayList<>();
        if (!this.collisionIntervals.containsKey(landmarkPosition) || !this.collisionIntervals.get(landmarkPosition).containsKey(landmarkPosition)) {
            states.add(new State(landmarkPosition, 0, stateInterval));
            return states;
        }

        int safeIntervalId = 0;
        TreeSet<Interval> collisionIntervals = this.collisionIntervals.get(landmarkPosition).get(landmarkPosition);
        for (Interval interval : collisionIntervals) {
            if (stateInterval.start >= interval.end) {
                safeIntervalId++;
                continue;
            }
            if (stateInterval.end <= interval.start) {
                break;
            }
            if (stateInterval.start < interval.start) {
                states.add(new State(landmark.from, safeIntervalId, new Interval(stateInterval.start, interval.start)));
            }
            stateInterval.start = interval.end;
            if (stateInterval.start >= stateInterval.end) {
                break;
            }
            safeIntervalId++;
        }

        if (stateInterval.start < stateInterval.end) {
            states.add(new State(landmark.from, collisionIntervals.size(), stateInterval));
        }

        return states;
    }

    private ArrayList<Interval> getSafeIntervals(Vector3D position) {
        double current = 0;
        ArrayList<Interval> intervals = new ArrayList<>();

        if (!this.collisionIntervals.containsKey(position) || !this.collisionIntervals.get(position).containsKey(position)) {
            intervals.add(new Interval(0, Double.MAX_VALUE));
            return intervals;
        }

        TreeSet<Interval> collisionIntervals = this.collisionIntervals.get(position).get(position);
        for (Interval interval : collisionIntervals) {
            if (current < interval.start) {
                intervals.add(new Interval(current, interval.start));
            }
            current = interval.end;
        }

        if (current < Double.MAX_VALUE) {
            intervals.add(new Interval(current, Double.MAX_VALUE));
        }

        return intervals;
    }

    private ArrayList<State> findPaths(ArrayList<State> starts, ArrayList<State> goals, double maxTime) {
        Vector3D goalPosition = goals.get(0).position;
        ArrayList<State> solutions = new ArrayList<>();

        this.distance.clear();
        this.visited.clear();
        this.queue.clear();

        for (State start : starts) {
            this.queue.add(start);
            this.distance.put(start, start.cost);
        }

        while (!this.queue.isEmpty()) {
            State state = this.queue.poll();

            if (this.visited.contains(state)) continue;
            if (state.cost.g > this.distance.get(state).g) continue;

            this.visited.add(state);

            if (state.position.equals(goalPosition)) {
                for (State goal : goals) {
                    if (state.cost.g <= goal.interval.end && state.interval.end >= goal.interval.start) {
                        solutions.add(new State(
                                state.position,
                                state.intervalId,
                                state.interval,
                                new Cost(Math.max(state.cost.g, goal.interval.start), state.cost.h),
                                state.parent
                        ));
                    }
                }

                if (solutions.size() == goals.size()) {
                    return solutions;
                }
            }

            ArrayList<State> successors = this.getSuccessors(state);
            for (State successor : successors) {
                if (successor.cost.g > maxTime) {
                    continue;
                }
                if (this.visited.contains(successor)) {
                    continue;
                }
                if (this.distance.containsKey(successor) && state.cost.g >= this.distance.get(state).g) {
                    continue;
                }

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
                continue;
            }

            Vector3D position = edge.to;

            //Vector2D dist2D = this.reverseResumableAStar.distance(this.mobile, position, this.mobile.getPosition());
            //if (dist2D == null) continue;

            double edgeCost = this.getEdgeCost(edge.from, edge.to);
            double h = 0;//DoublePrecisionConstraint.round(dist2D.getX() * mobile.getSpeed() + dist2D.getY() * Lift.speed);

            TreeSet<Interval> collisionIntervals = null;
            if (this.collisionIntervals.containsKey(parent.position) && this.collisionIntervals.get(parent.position).containsKey(position)) {
                collisionIntervals = this.collisionIntervals.get(parent.position).get(position);
            }

            ArrayList<Interval> safeIntervals = this.getSafeIntervals(position);
            for (int intervalId = 0; intervalId < safeIntervals.size(); intervalId++) {
                Interval safeInterval = safeIntervals.get(intervalId);
                Cost childCost = new Cost(parent.cost.g + edgeCost, h);
                if (childCost.g > safeInterval.end) {
                    continue;
                }
                if (childCost.g < safeInterval.start) {
                    childCost.g = safeInterval.start;
                    if (childCost.g - edgeCost > parent.interval.end) {
                        continue; // cannot wait at parent to reach this safe interval
                    }
                }

                State child = new State(position, intervalId, safeInterval, childCost, parent);

                if (this.visited.contains(child)) {
                    continue;
                }

                if (collisionIntervals != null) {
                    Interval key = new Interval(childCost.g - edgeCost, Double.MAX_VALUE);
                    Interval collisionInterval = collisionIntervals.floor(key);
                    if (collisionInterval != null) {
                        if (childCost.g - edgeCost < collisionInterval.end) { // collision
                            childCost.g = collisionInterval.end + edgeCost;

                            if (childCost.g - edgeCost > parent.interval.end || childCost.g > safeInterval.end) {
                                continue;
                            }
                        }
                    }
                }

                if (this.distance.containsKey(child) && childCost.compareTo(this.distance.get(child)) > 0) {
                    continue;
                }

                this.successors.add(child);
            }
        }

        return this.successors;
    }

    private static class Interval implements Comparable<Interval> {

        double start, end;

        public Interval(double start, double end) {
            this.start = start;
            this.end = end;
        }

        public boolean overlaps(Interval other) {
            return (this.start <= other.end) && (this.end >= other.start);
        }

        public void merge(Interval other) {
            if (other.start < this.start) {
                this.start = other.start;
            }
            if (other.end > this.end) {
                this.end = other.end;
            }
        }

        @Override
        public int compareTo(Interval other) {
            if (this.start == other.start) {
                return Double.compare(this.end, other.end);
            }
            return Double.compare(this.start, other.start);
        }
    }

    static class State implements Comparable<State> {

        State parent;
        Vector3D position;
        Cost cost;
        int intervalId;
        Interval interval;

        public State(Vector3D position, int intervalId, Interval interval, Cost cost, State parent) {
            this.position = position;
            this.intervalId = intervalId;
            this.interval = interval;
            this.cost = cost;
            this.parent = parent;
        }

        public State(Vector3D position, int intervalId, Interval interval) {
            this(position, intervalId, interval, new Cost(0, 0), null);
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

}
