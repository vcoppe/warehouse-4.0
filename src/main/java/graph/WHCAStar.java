package graph;

import agent.Lift;
import agent.Mobile;
import util.DoublePrecisionConstraint;
import util.Pair;
import util.Vector2D;
import util.Vector3D;

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

        HashMap<AStarState, Double> dist = new HashMap<>();
        HashMap<AStarState, AStarState> prev = new HashMap<>();

        PriorityQueue<AStarState> pq = new PriorityQueue<>(Comparator.comparing(AStarState::getEstimate));

        Vector2D dist2D = this.reverseResumableAStar(mobile, pair.second.first, startPosition);
        if (dist2D == null) {
            System.out.println("no path from " + pair.second.first + " to " + endPosition);
            (new java.util.Scanner(System.in)).nextLine();
        }
        double startEstimate = DoublePrecisionConstraint.round(pair.second.second + dist2D.getX() * mobile.getSpeed() + dist2D.getY() * Lift.speed);
        AStarState initialState = new AStarState(pair.first.first, pair.first.second, 0);
        AStarState startState = new AStarState(pair.second.first, pair.second.second, startEstimate);

        dist.put(startState, pair.second.second);
        prev.put(startState, initialState);
        pq.add(startState);

        if (debug && noPath) {
            System.out.println("path for mobile " + mobile.getId());
        }

        while (!pq.isEmpty()) {
            AStarState state = pq.poll();

            Vector3D u = state.position;
            double distU = state.time;

            if (distU > dist.get(state)) { // not the shortest path anymore
                continue;
            }

            if (debug && noPath) {
                System.out.println("reached pos " + u);
            }

            if (u.equals(endPosition)) {
                this.setPath(time, mobile, prev, state);
                return true;
            }

            double minDist = distU + 1;

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
                minDist = Math.min(minDist, otherDist);

                if (otherDist < time + H) { // check for collisions only within the time window
                    if (!this.table.isAvailable(v, otherDist, mobile.getId())) { // position already occupied at that time
                        /*if (debug && noPath) System.out.println("pos " + v + " is occupied at time " + otherDist);
                        otherDist = this.table.nextAvailability(v, otherDist, mobile.getId()); // get soonest available time
                        if (debug && noPath) System.out.println("soonest time to go there : " + otherDist);
                        if (!this.table.isAvailable(u, distU, DoublePrecisionConstraint.round(otherDist - edgeDist), mobile.getId())) {
                            if (debug && noPath) {
                                System.out.println("cannot wait long enough to reach " + v);
                            }
                            continue; // mobile cannot wait in current position until the next position is available
                        }*/
                        continue;
                    }
                }

                AStarState next = new AStarState(v, otherDist, 0);

                if (!dist.containsKey(next) || otherDist < dist.get(next)) {
                    dist2D = this.reverseResumableAStar(mobile, v, startPosition);
                    if (dist2D == null) continue;
                    //if (debug && noPath) System.out.println(v + " at estimated dist " + estimateV);
                    next.h = DoublePrecisionConstraint.round(dist2D.getX() * mobile.getSpeed() + dist2D.getY() * Lift.speed);
                    dist.put(next, otherDist);
                    prev.put(next, state);
                    pq.add(next);
                }
            }

            // wait move
            if (distU >= time + H || this.table.isAvailable(u, distU, minDist, mobile.getId())) { // check for collisions only within the time window
                AStarState next = new AStarState(u, minDist, state.h);

                if (!dist.containsKey(next) || minDist < dist.get(next)) {
                    dist.put(next, minDist);
                    prev.put(next, state);
                    pq.add(next);
                }
            }
        }

        return false; // no path was found
    }

    private void setPath(double time, Mobile mobile, HashMap<AStarState, AStarState> prev, AStarState endState) {
        Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getPositionsAt(time);
        AStarState startState = new AStarState(pair.second.first, pair.second.second, 0);
        Vector3D startPosition = pair.second.first;
        Vector3D endPosition = mobile.getTargetPosition();

        ArrayList<Pair<Vector3D, Double>> path = new ArrayList<>();

        AStarState current = endState;
        path.add(new Pair<>(current.position, current.time));
        while (!current.equals(startState)) {
            current = prev.get(current);
            path.add(0, new Pair<>(current.position, current.time));
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

    class AStarState {
        Vector3D position;
        double time, h;

        public AStarState(Vector3D position, double time, double h) {
            this.position = position;
            this.time = time;
            this.h = h;
        }

        public double getEstimate() {
            return this.time + this.h;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AStarState that = (AStarState) o;
            return Double.compare(that.time, this.time) == 0 && this.position.equals(that.position);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.position.getX(), this.position.getY(), this.position.getZ(), this.time);
        }
    }

}
