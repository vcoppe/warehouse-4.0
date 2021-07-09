package graph;

import agent.Lift;
import agent.Mobile;
import util.DoublePrecisionConstraint;
import util.Pair;
import util.Vector2D;
import util.Vector3D;

import java.util.*;

public class WHCAStar {

    public static final int W = 30;

    private double window;
    private Graph graph;
    private final ReservationTable table;
    private static final Comparator<Pair<Vector3D, Vector2D>> manhattanDistanceComparator = (a, b) -> Vector2D.manhattanDistanceComparator.compare(a.second, b.second);
    private final HashMap<Integer, Pair<Vector3D, Vector3D>> lastRoute;
    private final HashMap<Integer, HashMap<Vector3D, Vector2D>> resumableDist;
    private final HashMap<Integer, PriorityQueue<Pair<Vector3D, Vector2D>>> resumablePq;
    private final Random random;
    private final HashMap<Integer, HashSet<Vector3D>> resumableClosed;

    public WHCAStar() {
        this.table = new ReservationTable();
        this.lastRoute = new HashMap<>();
        this.resumableDist = new HashMap<>();
        this.resumablePq = new HashMap<>();
        this.resumableClosed = new HashMap<>();
        this.random = new Random(0);
    }

    public void addGraphConstraint(GraphConstraint constraint) {
        this.table.addGraphConstraint(constraint);
    }

    public double getWindow() {
        return this.window;
    }

    /***
     * computes non-colliding paths for the given mobiles
     * with their respective position and target position, and speed
     */
    public void computePaths(double time, ArrayList<Mobile> mobiles, Graph graph) {
        boolean solution = false;

        while (!solution) {
            solution = true;

            this.window = W;
            this.graph = graph;
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

            ArrayList<Mobile> orderedMobiles = this.orderMobiles(time, mobiles);

            for (Mobile mobile : orderedMobiles) {
                if (!this.computePath(time, mobile)) {
                    System.out.println("No path was found");
                    solution = false;
                    break;
                }
            }
        }

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

        Vector3D startPosition = mobile.getPosition();
        Vector3D endPosition = mobile.getTargetPosition();

        HashMap<Vector3D, Double> dist = new HashMap<>();
        HashMap<Vector3D, Double> h = new HashMap<>();
        HashMap<Vector3D, Vector3D> prev = new HashMap<>();

        PriorityQueue<Pair<Vector3D, Double>> pq = new PriorityQueue<>(Comparator.comparing(Pair::getSecond));

        Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getPositionsAt(time);

        dist.put(pair.second.first, pair.second.second);
        prev.put(pair.second.first, pair.first.first);
        Vector2D dist2D = this.reverseResumableAStar(mobile, pair.second.first, startPosition);
        h.put(pair.second.first, pair.second.second + dist2D.getX() * mobile.getSpeed() + dist2D.getY() * Lift.speed);
        pq.add(new Pair<>(pair.second.first, h.get(pair.second.first)));

        while (!pq.isEmpty()) {
            Pair<Vector3D, Double> p = pq.poll();

            Vector3D u = p.first;
            double estimateU = p.second;

            if (estimateU > h.get(u)) { // not the shortest path anymore
                continue;
            }

            if (u.equals(endPosition)) {
                this.setPath(time, mobile, dist, prev);
                return true;
            }

            double distU = dist.get(u);

            for (Edge edge : this.graph.getEdges(u)) {
                Vector3D v = edge.to;
                Vector2D w = edge.weight;

                if (!edge.canCross(distU, mobile)) continue;

                double edgeDist = DoublePrecisionConstraint.round(w.getX() * mobile.getSpeed() + w.getY() * Lift.speed);
                double otherDist = DoublePrecisionConstraint.round(distU + edgeDist);

                if (otherDist < time + W) { // check for collisions only within the time window
                    if (!this.table.isAvailable(v, otherDist, mobile.getId())) { // position already occupied at that time
                        otherDist = this.table.nextAvailability(v, otherDist, mobile.getId()); // get soonest available time
                        if (!this.table.isAvailable(u, dist.get(u), DoublePrecisionConstraint.round(otherDist - edgeDist), mobile.getId())) {
                            continue; // mobile cannot wait until in current position the next position is available
                        }
                    }
                }

                if (!dist.containsKey(v) || otherDist < dist.get(v)) {
                    dist2D = this.reverseResumableAStar(mobile, v, startPosition);
                    double estimateV = DoublePrecisionConstraint.round(otherDist + dist2D.getX() * mobile.getSpeed() + dist2D.getY() * Lift.speed);
                    dist.put(v, otherDist);
                    h.put(v, estimateV);
                    prev.put(v, u);
                    pq.add(new Pair<>(v, estimateV));
                }
            }
        }

        return false; // no path was found
    }

    private void setPath(double time, Mobile mobile, HashMap<Vector3D, Double> dist, HashMap<Vector3D, Vector3D> prev) {
        Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getPositionsAt(time);
        Vector3D startPosition = pair.second.first;
        Vector3D endPosition = mobile.getTargetPosition();

        ArrayList<Pair<Vector3D, Double>> path = new ArrayList<>();

        Vector3D u = endPosition;
        path.add(new Pair<>(u, dist.get(u)));
        while (!u.equals(startPosition)) {
            u = prev.get(u);
            path.add(0, new Pair<>(u, dist.get(u)));
        }

        if (!pair.first.first.equals(startPosition)) {
            path.add(0, new Pair<>(pair.first.first, pair.first.second));
        }

        // reserve path in table
        for (int i = path.size() - 1; i >= 0; i--) {
            u = path.get(i).first;
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
                if (timeU - time > 0) this.window = Math.min(this.window, timeU - time); // shrink window to the earliest finished mission
                this.table.reserve(u, timeU, timeU + W, mobile.getId());
            }
        }

        mobile.setPath(time, path);
    }

    private void initReverseResumableAStar(Mobile mobile) {
        Vector3D startPosition = mobile.getPosition();
        Vector3D endPosition = mobile.getTargetPosition();

        if (this.lastRoute.containsKey(mobile.getId())) {
            Pair<Vector3D, Vector3D> route = this.lastRoute.get(mobile.getId());
            if (startPosition.equals(route.first) && endPosition.equals(route.second)) {
                return;
            }
        }

        HashMap<Vector3D, Vector2D> dist = new HashMap<>();
        PriorityQueue<Pair<Vector3D, Vector2D>> pq = new PriorityQueue<>(manhattanDistanceComparator);
        HashSet<Vector3D> closed = new HashSet<>();

        dist.put(endPosition, new Vector2D(0, 0));
        pq.add(new Pair<>(endPosition, dist.get(endPosition).add(startPosition.manhattanDistance3D(endPosition))));

        this.resumableDist.put(mobile.getId(), dist);
        this.resumablePq.put(mobile.getId(), pq);
        this.resumableClosed.put(mobile.getId(), closed);

        this.lastRoute.put(mobile.getId(), new Pair<>(startPosition, endPosition));
    }

    private Vector2D reverseResumableAStar(Mobile mobile, Vector3D endPosition, Vector3D finalPosition) {
        HashMap<Vector3D, Vector2D> dist = this.resumableDist.get(mobile.getId());
        PriorityQueue<Pair<Vector3D, Vector2D>> pq = this.resumablePq.get(mobile.getId());
        HashSet<Vector3D> closed = this.resumableClosed.get(mobile.getId());

        if (closed.contains(endPosition)) {
            return dist.get(endPosition);
        }

        while (!pq.isEmpty()) {
            Pair<Vector3D, Vector2D> p = pq.peek();
            Vector3D u = p.first;
            Vector2D estimateU = p.second;

            if (Vector2D.manhattanDistanceComparator.compare(estimateU.subtract(u.manhattanDistance3D(finalPosition)), dist.get(u)) > 0) { // not the shortest path anymore
                pq.poll();
                continue;
            }

            if (u.equals(endPosition)) {
                return dist.get(endPosition);
            }

            pq.poll();
            closed.add(u);

            for (Edge edge : this.graph.getReverseEdges(u)) { // reverse edges for reverse A star
                Vector3D v = edge.to;
                Vector2D w = edge.weight;

                Vector2D otherDist = dist.get(u).add(w);

                if (!dist.containsKey(v) || Vector2D.manhattanDistanceComparator.compare(otherDist, dist.get(v)) < 0) {
                    dist.put(v, otherDist);
                    Vector2D estimateV = otherDist.add(v.manhattanDistance3D(finalPosition));
                    pq.add(new Pair<>(v, estimateV));
                }
            }
        }

        Vector3D startPosition = null;
        for (Map.Entry<Vector3D, Vector2D> entry : dist.entrySet()) {
            if (entry.getValue().getX() + entry.getValue().getY() <= 0) {
                startPosition = entry.getKey();
                break;
            }
        }

        System.out.println("No path found in RRA* from " + endPosition + " to " + startPosition);
        System.exit(0);

        return new Vector2D(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

}
