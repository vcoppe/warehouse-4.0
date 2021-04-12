package pathfinding;

import agent.Mobile;
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
        //System.out.println("WHCA*: asking paths for " + mobiles.size() + " mobiles");

        boolean solution = false;

        while (!solution) {
            solution = true;

            this.window = W;
            this.graph = graph;
            this.table.clear();

            for (Mobile mobile : mobiles) { // reserve current position
                this.table.reserve(mobile.getCurrentPosition(), 0, mobile.getId());
            }

            ArrayList<Mobile> orderedMobiles = this.orderMobiles(mobiles);

            for (Mobile mobile : orderedMobiles) {
                if (!this.computePath(mobile)) {
                    System.out.println("No path was found");
                    solution = false;
                    break;
                    //System.exit(0);
                }
            }
        }

    }

    private ArrayList<Mobile> orderMobiles(ArrayList<Mobile> mobiles) {
        /*ArrayList<Pair<Mobile,Integer>> orderedMobiles = new ArrayList<>();

        for (Mobile mobile : mobiles) {
            orderedMobiles.add(new Pair<>(mobile, -mobile.getCurrentPosition().getY()));
        }

        orderedMobiles.sort(Comparator.comparing(Pair::getSecond));

        return (ArrayList<Mobile>) orderedMobiles.stream().map(p -> p.first).collect(Collectors.toList());*/

        ArrayList<Mobile> orderedMobiles = new ArrayList<>(mobiles);
        Collections.shuffle(orderedMobiles, this.random);
        return orderedMobiles;
    }

    /**
     * computes the shortest path from the current position to the target position
     * while avoiding other mobiles for which the path has already been computed
     */
    private boolean computePath(Mobile mobile) {
        this.initReverseResumableAStar(mobile);

        //System.out.println("WHCA*: compute path for mobile " + mobile.getId());

        // TODO adapt if mobile is between 2 positions
        Position startPosition = mobile.getPosition();
        Position currentPosition = mobile.getCurrentPosition();
        Position endPosition = mobile.getTargetPosition();

        HashMap<Position, Double> dist = new HashMap<>();
        HashMap<Position, Double> h = new HashMap<>();
        HashMap<Position, Position> prev = new HashMap<>();

        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparing(Edge::getWeight));

        dist.put(currentPosition, 0.0);
        h.put(currentPosition, this.reverseResumableAStar(mobile, currentPosition, startPosition) / mobile.getSpeed());
        pq.add(new Edge(currentPosition, dist.get(currentPosition) + h.get(currentPosition)));

        while (!pq.isEmpty()) {
            Edge e = pq.poll();

            Position u = e.to;
            double estimateU = e.w;

            //System.out.println("WHCA*: position " + u.getX() + "," + u.getY() + " at dist " + dist.get(u));

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

                double otherDist = dist.get(u) + w / mobile.getSpeed();
                if (otherDist < W/*this.window*/) { // check for collisions only within the time window
                    if (!this.table.isAvailable(v, otherDist, mobile.getId())) { // position already occupied at that time
                        otherDist = this.table.nextAvailability(v, otherDist, mobile.getId()); // get soonest available time
                        if (!this.table.isAvailable(u, dist.get(u), otherDist - w / mobile.getSpeed(), mobile.getId())) {
                            continue; // mobile cannot wait until in current position the next position is available
                        }
                    }
                }

                if (!dist.containsKey(v) || otherDist < dist.get(v)) {
                    double estimateV = otherDist + this.reverseResumableAStar(mobile, v, startPosition) / mobile.getSpeed();
                    dist.put(v, otherDist);
                    h.put(v, estimateV);
                    prev.put(v, u);
                    pq.add(new Edge(v, estimateV));
                }
            }
        }

        return false; // no path was found
    }

    private void initReverseResumableAStar(Mobile mobile) {
        Position startPosition = mobile.getPosition();
        Position endPosition = mobile.getTargetPosition();

        //System.out.println("init RRA* for mobile " + mobile.getId());

        if (this.resumableDist.containsKey(mobile.getId())) {
            Pair<Position, Position> route = this.lastRoute.get(mobile.getId());
            if (startPosition.equals(route.first) && endPosition.equals(route.second)) {
                //System.out.println("\tsame path as last time");
                return;
            }
        }

        this.resumableDist.clear();
        this.resumablePq.clear();
        this.resumableClosed.clear();

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

    private void setPath(Mobile mobile, HashMap<Position, Double> dist, HashMap<Position, Position> prev) {
        Position currentPosition = mobile.getCurrentPosition();
        Position endPosition = mobile.getTargetPosition();

        ArrayList<Pair<Position,Double>> path = new ArrayList<>();

        Position u = endPosition;
        path.add(new Pair<>(u, dist.get(u)));
        while (!u.equals(currentPosition)) {
            u = prev.get(u);
            path.add(0, new Pair<>(u, dist.get(u)));
        }

        // reserve path in table
        for (int i = path.size() - 1; i >= 0; i--) {
            u = path.get(i).first;
            double timeU = path.get(i).second;
            if (i < path.size() - 1) {
                Position v = path.get(i + 1).first;
                double timeV = path.get(i + 1).second;
                double w = this.graph.getWeight(u, v);

                if (timeV - timeU > w / mobile.getSpeed()) {
                    double waitingTime = timeV - timeU - w / mobile.getSpeed();
                    path.add(i + 1, new Pair<>(u, timeU + waitingTime));
                    this.table.reserve(u, timeU, timeU + waitingTime, mobile.getId());
                } else {
                    this.table.reserve(u, timeU, mobile.getId());
                }
            } else {
                if (timeU > 0) this.window = Math.min(this.window, timeU); // shrink window to the earliest finished mission
                this.table.reserve(u, timeU, timeU+W, mobile.getId());
            }
        }

        mobile.setPath(path);
    }

    private double reverseResumableAStar(Mobile mobile, Position endPosition, Position finalPosition) {
        HashMap<Position, Double> dist = this.resumableDist.get(mobile.getId());
        PriorityQueue<Edge> pq = this.resumablePq.get(mobile.getId());
        HashSet<Position> closed = this.resumableClosed.get(mobile.getId());

        //System.out.println("RRA*: asking for " + endPosition.getX() + "," + endPosition.getY());

        if (closed.contains(endPosition)) {
            //System.out.println("\talready computed : " + dist.get(endPosition));
            return dist.get(endPosition);
        }

        while (!pq.isEmpty()) {
            Edge e = pq.peek();
            Position u = e.to;
            double estimateU = e.w;

            //System.out.println("RRA*: position " + u.getX() + "," + u.getY() + " at dist " + dist.get(u));

            if (estimateU - u.manhattanDistance3D(finalPosition) > dist.get(u)) { // not the shortest path anymore
                pq.poll();
                //System.out.println("\tignored");
                continue;
            }

            if (u.equals(endPosition)) {
                //System.out.println("RRA*: found dist " + dist.get(endPosition) + " for " + endPosition.getX() + "," + endPosition.getY());
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
            if (entry.getValue() == 0) {
                startPosition = entry.getKey();
                break;
            }
        }

        System.out.println("No path found in RRA* from " + endPosition.getX() + "," + endPosition.getY() + " to " + startPosition.getX() + "," + startPosition.getY());
        System.exit(0);

        return Double.MAX_VALUE;
    }

}
