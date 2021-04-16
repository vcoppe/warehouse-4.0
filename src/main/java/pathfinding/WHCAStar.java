package pathfinding;

import agent.Mobile;
import util.DoublePrecisionConstraint;
import util.Pair;
import warehouse.Position;

import java.util.*;

public class WHCAStar {

    public static final int W = 30;

    private double window;
    private Graph graph;
    private final ReservationTable table;
    private final HashMap<Integer, Pair<Position, Position>> lastRoute;
    private final HashMap<Integer, HashMap<Position, Double>> resumableDist;
    private final HashMap<Integer, PriorityQueue<Edge>> resumablePq;
    private final HashMap<Integer, HashSet<Position>> resumableClosed;
    private final Random random;

    public WHCAStar() {
        this.table = new ReservationTable();
        this.lastRoute = new HashMap<>();
        this.resumableDist = new HashMap<>();
        this.resumablePq = new HashMap<>();
        this.resumableClosed = new HashMap<>();
        this.random = new Random(0);
    }

    public double getWindow() {
        return this.window;
    }

    /***
     * computes non-colliding paths for the given mobiles
     * with their respective position and target position, and speed
     */
    public void computePaths(ArrayList<Mobile> mobiles, Graph graph) {
        boolean solution = false;

        while (!solution) {
            solution = true;

            this.window = W;
            this.graph = graph;
            this.table.clear();

            for (Mobile mobile : mobiles) { // reserve current position
                Pair<Pair<Position,Double>,Pair<Position,Double>> pair = mobile.getCurrentPositions();
                if (pair.first.first.equals(pair.second.first)) {
                    this.table.reserve(pair.first.first, pair.first.second, pair.second.second, mobile.getId());
                } else {
                    this.table.reserve(pair.first.first, pair.first.second, mobile.getId());
                    this.table.reserve(pair.second.first, pair.second.second, mobile.getId());
                }
            }

            ArrayList<Mobile> orderedMobiles = this.orderMobiles(mobiles);

            for (Mobile mobile : orderedMobiles) {
                if (!this.computePath(mobile)) {
                    System.out.println("No path was found");
                    solution = false;
                    break;
                }
            }
        }

    }

    private ArrayList<Mobile> orderMobiles(ArrayList<Mobile> mobiles) {
        ArrayList<Mobile> staticMobiles = new ArrayList<>();
        ArrayList<Mobile> otherMobiles = new ArrayList<>();

        for (Mobile mobile : mobiles) {
            if (mobile.getCurrentPosition().equals(mobile.getTargetPosition())) {
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
    private boolean computePath(Mobile mobile) {
        this.initReverseResumableAStar(mobile);

        Position startPosition = mobile.getPosition();
        Position endPosition = mobile.getTargetPosition();

        HashMap<Position, Double> dist = new HashMap<>();
        HashMap<Position, Double> h = new HashMap<>();
        HashMap<Position, Position> prev = new HashMap<>();

        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparing(Edge::getWeight));

        Pair<Pair<Position,Double>,Pair<Position,Double>> pair = mobile.getCurrentPositions();
        this.table.reserve(pair.first.first, pair.first.second, mobile.getId());
        this.table.reserve(pair.second.first, pair.second.second, mobile.getId());

        dist.put(pair.second.first, pair.second.second);
        prev.put(pair.second.first, pair.first.first);
        h.put(pair.second.first, DoublePrecisionConstraint.round(this.reverseResumableAStar(mobile, pair.second.first, startPosition) * mobile.getSpeed()));
        pq.add(new Edge(pair.second.first, h.get(pair.second.first)));

        while (!pq.isEmpty()) {
            Edge e = pq.poll();

            Position u = e.to;
            double estimateU = e.w;

            if (estimateU > h.get(u)) { // not the shortest path anymore
                continue;
            }

            if (u.equals(endPosition)) {
                this.setPath(mobile, dist, prev);
                return true;
            }

            for (Edge edge : this.graph.getEdges(u)) {
                Position v = edge.to;
                double w = edge.w;

                double otherDist = DoublePrecisionConstraint.round(dist.get(u) + w * mobile.getSpeed());

                if (otherDist < W) { // check for collisions only within the time window
                    if (!this.table.isAvailable(v, otherDist, mobile.getId())) { // position already occupied at that time
                        otherDist = this.table.nextAvailability(v, otherDist, mobile.getId()); // get soonest available time
                        if (!this.table.isAvailable(u, dist.get(u), DoublePrecisionConstraint.round(otherDist - w * mobile.getSpeed()), mobile.getId())) {
                            continue; // mobile cannot wait until in current position the next position is available
                        }
                    }
                }

                if (!dist.containsKey(v) || otherDist < dist.get(v)) {
                    double estimateV = DoublePrecisionConstraint.round(otherDist + this.reverseResumableAStar(mobile, v, startPosition) * mobile.getSpeed());
                    dist.put(v, otherDist);
                    h.put(v, estimateV);
                    prev.put(v, u);
                    pq.add(new Edge(v, estimateV));
                }
            }
        }

        return false; // no path was found
    }

    private void setPath(Mobile mobile, HashMap<Position, Double> dist, HashMap<Position, Position> prev) {
        Pair<Pair<Position,Double>,Pair<Position,Double>> pair = mobile.getCurrentPositions();
        Position startPosition = pair.second.first;
        Position endPosition = mobile.getTargetPosition();

        ArrayList<Pair<Position,Double>> path = new ArrayList<>();

        Position u = endPosition;
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
                Position v = path.get(i + 1).first;
                double timeV = path.get(i + 1).second;
                double w = this.graph.getWeight(u, v);

                if (DoublePrecisionConstraint.round(timeV - timeU - w * mobile.getSpeed()) > 0) {
                    double timeLeaveU = DoublePrecisionConstraint.round(timeV - w * mobile.getSpeed());
                    path.add(i + 1, new Pair<>(u, timeLeaveU));
                    this.table.reserve(u, timeU, timeLeaveU, mobile.getId());
                } else {
                    this.table.reserve(u, timeU, mobile.getId());
                }
            } else {
                if (timeU > 0) this.window = Math.min(this.window, timeU); // shrink window to the earliest finished mission
                this.table.reserve(u, timeU, timeU + W, mobile.getId());
            }
        }

        mobile.setPath(path);
    }

    private void initReverseResumableAStar(Mobile mobile) {
        Position startPosition = mobile.getPosition();
        Position endPosition = mobile.getTargetPosition();

        if (this.lastRoute.containsKey(mobile.getId())) {
            Pair<Position, Position> route = this.lastRoute.get(mobile.getId());
            if (startPosition.equals(route.first) && endPosition.equals(route.second)) {
                return;
            }
        }

        HashMap<Position, Double> dist = new HashMap<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparing(Edge::getWeight));
        HashSet<Position> closed = new HashSet<>();

        dist.put(endPosition, 0.0);
        pq.add(new Edge(endPosition, dist.get(endPosition) + startPosition.manhattanDistance3D(endPosition)));

        this.resumableDist.put(mobile.getId(), dist);
        this.resumablePq.put(mobile.getId(), pq);
        this.resumableClosed.put(mobile.getId(), closed);

        this.lastRoute.put(mobile.getId(), new Pair<>(startPosition, endPosition));
    }

    private double reverseResumableAStar(Mobile mobile, Position endPosition, Position finalPosition) {
        HashMap<Position, Double> dist = this.resumableDist.get(mobile.getId());
        PriorityQueue<Edge> pq = this.resumablePq.get(mobile.getId());
        HashSet<Position> closed = this.resumableClosed.get(mobile.getId());

        if (closed.contains(endPosition)) {
            return dist.get(endPosition);
        }

        while (!pq.isEmpty()) {
            Edge e = pq.peek();
            Position u = e.to;
            double estimateU = e.w;

            if (estimateU - u.manhattanDistance3D(finalPosition) > dist.get(u)) { // not the shortest path anymore
                pq.poll();
                continue;
            }

            if (u.equals(endPosition)) {
                return dist.get(endPosition);
            }

            pq.poll();
            closed.add(u);

            for (Edge edge : this.graph.getReverseEdges(u)) { // reverse edges for reverse A star
                Position v = edge.to;
                double w = edge.w;

                double otherDist = dist.get(u) + w;

                if (!dist.containsKey(v) || otherDist < dist.get(v)) {
                    dist.put(v, otherDist);
                    double estimateV = otherDist + v.manhattanDistance3D(finalPosition);
                    pq.add(new Edge(v, estimateV));
                }
            }
        }

        Position startPosition = null;
        for (Map.Entry<Position, Double> entry : dist.entrySet()) {
            if (entry.getValue() <= 0) {
                startPosition = entry.getKey();
                break;
            }
        }

        System.out.println("No path found in RRA* from " + endPosition.getX() + "," + endPosition.getY() + " to " + startPosition.getX() + "," + startPosition.getY());
        System.exit(0);

        return Double.MAX_VALUE;
    }

}
