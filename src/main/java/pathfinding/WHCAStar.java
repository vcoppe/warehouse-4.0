package pathfinding;

import agent.Lift;
import agent.Mobile;
import util.DoublePrecisionConstraint;
import util.Pair;
import util.Vector2D;
import util.Vector3D;
import warehouse.Configuration;

import java.util.*;

public class WHCAStar extends PathFinder {

    public static final int W = 30, H = 40;

    private final Random random;

    private final boolean debug = true;
    private boolean noPath;

    public WHCAStar(Graph graph) {
        super(graph);
        this.random = new Random(0);
    }

    /***
     * computes non-colliding paths for the given mobiles
     * with their respective position and target position, and speed
     */
    @Override
    public void computePaths(double time, ArrayList<Mobile> mobiles) {
        boolean solution = false;

        int count = 0;

        while (!solution) {
            solution = true;

            this.nextUpdateTime = time + W;
            this.table.clear();

            for (Mobile mobile : mobiles) { // reserve current position
                Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getPositionsAt(time);
                if (pair.first.first.equals(pair.second.first)) {
                    this.table.reserve(pair.first.first, pair.first.second, pair.second.second, mobile.getId());
                } else {
                    this.table.reserve(pair.first.first, pair.first.second, mobile.getId());
                    this.table.reserve(pair.second.first, pair.second.second, mobile.getId());
                }
            }

            ArrayList<Mobile> orderedMobiles;
            if (count == 0) orderedMobiles = this.heuristicOrderMobiles(time, mobiles);
            else orderedMobiles = this.orderMobiles(time, mobiles);

            if (count == 50) noPath = true;

            for (Mobile mobile : orderedMobiles) {
                if (!this.computePath(time, mobile)) {
                    System.out.println("No path was found");
                    if (debug && noPath) {
                        System.out.println("For mobile " + mobile.getId());
                        for (Mobile m : orderedMobiles) {
                            System.out.println(m.getId() + ": " + m.getPositionAt(time) + " -> " + m.getTargetPosition());
                        }
                    }
                    solution = false;
                    break;
                }
            }

            if (noPath) {
                (new java.util.Scanner(System.in)).nextLine();
            }

            count++;
        }
    }

    private ArrayList<Mobile> heuristicOrderMobiles(double time, ArrayList<Mobile> mobiles) {
        ArrayList<Mobile> staticMobiles = new ArrayList<>();
        ArrayList<Mobile> otherMobiles = new ArrayList<>();

        for (Mobile mobile : mobiles) {
            if (mobile.getPositionAt(time).equals(mobile.getTargetPosition())) {
                staticMobiles.add(mobile);
            } else {
                otherMobiles.add(mobile);
            }
        }

        otherMobiles.sort(Comparator.comparing(m -> m.getCurrentPosition().manhattanDistance2D(m.getTargetPosition())));

        staticMobiles.addAll(otherMobiles); // first plan static mobiles

        return staticMobiles;
    }

    private ArrayList<Mobile> orderMobiles(double time, ArrayList<Mobile> mobiles) {
        ArrayList<Mobile> staticMobiles = new ArrayList<>();
        ArrayList<Mobile> otherMobiles = new ArrayList<>();

        for (Mobile mobile : mobiles) {
            if (mobile.getPositionAt(time).equals(mobile.getTargetPosition())) {
                staticMobiles.add(mobile);
            } else {
                otherMobiles.add(mobile);
            }
        }

        Collections.shuffle(otherMobiles, this.random);

        staticMobiles.addAll(otherMobiles); // first plan static mobiles

        return staticMobiles;
    }

    /**
     * computes the shortest path from the current position to the target position
     * while avoiding other mobiles for which the path has already been computed
     */
    private boolean computePath(double time, Mobile mobile) {
        this.initReverseResumableAStar(mobile);

        Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getPositionsAt(time);

        Vector3D startPosition = mobile.getPosition();
        Vector3D endPosition = mobile.getTargetPosition();

        HashMap<AStarState, AStarCost> dist = new HashMap<>();
        HashMap<AStarState, AStarState> prev = new HashMap<>();

        PriorityQueue<AStarState> pq = new PriorityQueue<>();

        Vector2D dist2D = this.reverseResumableAStar(mobile, pair.second.first, startPosition);
        if (dist2D == null) {
            System.out.println("no path from " + pair.second.first + " to " + endPosition);
            (new java.util.Scanner(System.in)).nextLine();
        }
        double startEstimate = DoublePrecisionConstraint.round(pair.second.second + dist2D.getX() * mobile.getSpeed() + dist2D.getY() * Lift.speed);
        AStarState initialState = new AStarState(pair.first.first, new AStarCost(pair.first.second, 0), 0);
        AStarState startState = new AStarState(pair.second.first, new AStarCost(pair.second.second, 0), startEstimate);

        dist.put(startState, new AStarCost(pair.second.second, 0));
        prev.put(startState, initialState);
        pq.add(startState);

        if (debug && noPath) {
            System.out.println("path for mobile " + mobile.getId());
        }

        while (!pq.isEmpty()) {
            AStarState state = pq.poll();

            Vector3D u = state.position;
            AStarCost costU = state.cost;
            double distU = state.cost.time;

            if (costU.compareTo(dist.get(state)) > 0) { // not the shortest path anymore
                continue;
            }

            if (debug && noPath) {
                System.out.println("reached pos " + u);
            }

            if (u.equals(endPosition)) {
                this.setPath(time, mobile, prev, state);
                return true;
            }

            for (Edge edge : this.graph.getEdges(u)) {
                Vector3D v = edge.to;
                Vector2D w = edge.weight;

                if (!edge.canCross(mobile)) {
                    if (debug && noPath) {
                        System.out.println("cannot cross edge to pos " + v);
                    }
                    continue;
                }

                double edgeDist = DoublePrecisionConstraint.round(w.getX() * mobile.getSpeed() + w.getY() * Lift.speed);
                double otherDist = DoublePrecisionConstraint.round(distU + edgeDist);

                if (otherDist < time + H) { // check for collisions only within the time window
                    if (!this.table.isAvailable(v, otherDist, mobile.getId())) { // position already occupied at that time
                        continue;
                    }
                }

                AStarCost costV = new AStarCost(otherDist, costU.moves + 1);
                AStarState next = new AStarState(v, costV, 0);

                if (!dist.containsKey(next) || costV.compareTo(dist.get(next)) < 0) {
                    dist2D = this.reverseResumableAStar(mobile, v, startPosition);
                    if (dist2D == null) continue;
                    next.h = DoublePrecisionConstraint.round(dist2D.getX() * mobile.getSpeed() + dist2D.getY() * Lift.speed);
                    dist.put(next, costV);
                    prev.put(next, state);
                    pq.add(next);
                }
            }

            double distWaitU = DoublePrecisionConstraint.round(distU + mobile.getSpeed() * Configuration.palletSize);

            // wait move
            if (distU >= time + H || this.table.isAvailable(u, distU, distWaitU, mobile.getId())) { // check for collisions only within the time window
                AStarCost costWaitU = new AStarCost(distWaitU, costU.moves);
                AStarState next = new AStarState(u, costWaitU, state.h);

                if (!dist.containsKey(next) || costWaitU.compareTo(dist.get(next)) < 0) {
                    dist.put(next, costWaitU);
                    prev.put(next, state);
                    pq.add(next);
                }
            }
        }

        return false; // no path was found
    }

    private void setPath(double time, Mobile mobile, HashMap<AStarState, AStarState> prev, AStarState endState) {
        Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getPositionsAt(time);
        AStarState startState = new AStarState(pair.second.first, new AStarCost(pair.second.second, 0), 0);
        Vector3D startPosition = pair.second.first;

        ArrayList<Pair<Vector3D, Double>> path = new ArrayList<>();

        AStarState current = endState;
        path.add(new Pair<>(current.position, current.cost.time));
        while (!current.equals(startState)) {
            current = prev.get(current);
            path.add(0, new Pair<>(current.position, current.cost.time));
        }

        if (!pair.first.first.equals(startPosition)) {
            path.add(0, new Pair<>(pair.first.first, pair.first.second));
        }

        // reserve path in table
        for (int i = path.size() - 1; i >= 0; i--) {
            Vector3D u = path.get(i).first;
            double timeU = path.get(i).second;
            if (i < path.size() - 1) {
                Vector3D v = path.get(i + 1).first;
                double timeV = path.get(i + 1).second;
                Vector2D w = this.graph.getWeight(u, v);

                if (DoublePrecisionConstraint.round(timeV - timeU - w.getX() * mobile.getSpeed() - w.getY() * Lift.speed) > 0) {
                    double timeLeaveU = DoublePrecisionConstraint.round(timeV - w.getX() * mobile.getSpeed() - w.getY() * Lift.speed);
                    path.add(i + 1, new Pair<>(u, timeLeaveU));
                    this.table.reserve(u, timeU, timeLeaveU, mobile.getId());
                } else {
                    this.table.reserve(u, timeU, mobile.getId());
                }
            } else {
                if (timeU > time) this.nextUpdateTime = Math.min(this.nextUpdateTime, timeU);
                this.table.reserve(u, timeU, timeU + H, mobile.getId());
            }
        }

        if (debug && noPath) {
            System.out.println("set path for mobile " + mobile.getId() + ":");
            for (Pair<Vector3D, Double> p : path) {
                System.out.println(p.first + " , " + p.second);
            }
        }

        mobile.setPath(time, path);
    }

    class AStarState implements Comparable<AStarState> {
        Vector3D position;
        AStarCost cost;
        double h;

        public AStarState(Vector3D position, AStarCost cost, double h) {
            this.position = position;
            this.cost = cost;
            this.h = h;
        }

        public double getEstimate() {
            return this.cost.time + this.h;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AStarState that = (AStarState) o;
            return Double.compare(that.cost.time, this.cost.time) == 0 && this.position.equals(that.position);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.position.getX(), this.position.getY(), this.position.getZ(), this.cost.time);
        }

        @Override
        public int compareTo(AStarState other) {
            if (this.getEstimate() == other.getEstimate()) {
                return this.cost.compareTo(other.cost);
            }
            return Double.compare(this.getEstimate(), other.getEstimate());
        }
    }

    class AStarCost implements Comparable<AStarCost> {

        double time;
        int moves;

        public AStarCost(double time, int moves) {
            this.time = time;
            this.moves = moves;
        }

        @Override
        public int compareTo(AStarCost other) {
            if (this.time == other.time) {
                return this.moves - other.moves;
            }
            return Double.compare(this.time, other.time);
        }
    }

}
