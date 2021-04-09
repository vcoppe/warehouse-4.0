package util;

import java.util.*;

public class Graph {

    public final static double timeMargin = 3;

    private final HashMap<Integer, TreeSet<Edge>> g;
    private final HashMap<Integer, HashMap<Integer, Double>> dist;
    private final HashMap<Integer, HashMap<Integer, Integer>> prev;
    private final HashMap<Integer, TreeSet<Reservation>> reservations;

    public Graph() {
        this.g = new HashMap<>();
        this.dist = new HashMap<>();
        this.prev = new HashMap<>();
        this.reservations = new HashMap<>();
    }

    public Set<Integer> getVertices() {
        return this.g.keySet();
    }

    public Set<Edge> getEdges(int u) {
        return this.g.get(u);
    }

    public void addEdge(int u, int v, double w) {
        if (!this.g.containsKey(u)) {
            this.g.put(u, new TreeSet<>());
            this.reservations.put(u, new TreeSet<>());
        }

        if (!this.g.containsKey(v)) {
            this.g.put(v, new TreeSet<>());
            this.reservations.put(v, new TreeSet<>());
        }

        this.g.get(u).add(new Edge(v, w));
    }

    public void reserve(Integer position, double time, int id) {
        this.reserveWithMargin(position, time - timeMargin, time + timeMargin, id);
    }

    public void reserve(Integer position, double start, double end, int id) {
        this.reserveWithMargin(position, start - timeMargin, end + timeMargin, id);
    }

    private void reserveWithMargin(Integer position, double start, double end, int id) {
        Reservation reservation = new Reservation(start, end, id);
        this.reservations.get(position).add(reservation);
    }

    public boolean isAvailable(Integer position, double time, int id) {
        return this.isAvailableWithMargin(position, time - timeMargin, time + timeMargin, id);
    }

    public boolean isAvailable(Integer position, double start, double end, int id) {
        return this.isAvailableWithMargin(position, start - timeMargin, end + timeMargin, id);
    }

    private boolean isAvailableWithMargin(Integer position, double start, double end, int id) {
        Reservation key = new Reservation(start, end, id);
        Reservation before = this.reservations.get(position).floor(key);
        Reservation after = this.reservations.get(position).higher(key);

        if (before != null && before.mobileId != id && (before.start == start || before.end > start)) {
            return false;
        }

        if (after != null && after.mobileId != id && after.start < end) {
            return false;
        }

        return true;
    }

    public double nextAvailability(Integer position, double time, int id) {
        double nextTime = this.nextAvailabilityWithMargin(position, time - timeMargin, 2 * timeMargin, id);
        return nextTime + timeMargin;
    }

    public double nextAvailability(Integer position, double from, double duration, int id) {
        double nextTime = this.nextAvailabilityWithMargin(position, from - timeMargin, duration + 2 * timeMargin, id);
        return nextTime + timeMargin;
    }

    private double nextAvailabilityWithMargin(Integer position, double from, double duration, int id) {
        if (this.reservations.get(position).isEmpty()) {
            return from;
        }

        Reservation key = new Reservation(from, from + duration, id);

        Reservation before = this.reservations.get(position).floor(key);
        Reservation after = this.reservations.get(position).higher(key);

        while (true) {
            while (after != null && after.mobileId == id) {
                after = this.reservations.get(position).higher(after);
            }

            if (before == null || before.mobileId == id) {
                if (after == null || after.start - from >= duration) {
                    return from;
                }
            } else if (after == null || after.start - Math.max(before.end, from) >= duration) {
                return Math.max(before.end, from);
            }

            before = after;
            after = this.reservations.get(position).higher(after);
        }
    }

    public void dijkstra(int s, HashMap<Integer, Double> dist, HashMap<Integer, Integer> prev) {
        this.dijkstra(s, null, null, null, dist, prev);
    }

    public void dijkstra(int s, Double time, Double speed, Integer id, HashMap<Integer, Double> dist, HashMap<Integer, Integer> prev) {
        boolean timeDependent = time != null;
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparing(Edge::getWeight));

        if (timeDependent) {
            if (this.isAvailable(s, time, id)) {
                dist.put(s, time);
                pq.add(new Edge(s, time));
            } else { // wait for the edge to be available
                double nextTime = this.nextAvailability(s, time, id);
                dist.put(s, nextTime);
                pq.add(new Edge(s, nextTime));
            }
        } else {
            dist.put(s, 0.0);
            pq.add(new Edge(s, 0.0));
        }

        while (!pq.isEmpty()) {
            Edge e1 = pq.poll();
            int u = e1.to;
            double dist_u = e1.w;

            if (dist_u > dist.get(u)) {
                continue;
            }

            for (Edge e2 : this.g.get(u)) {
                int v = e2.to;
                double w = e2.w;
                Double dist_v = dist.get(v);
                double other_dist_v = dist_u + w / (timeDependent ? speed : 1);

                if (timeDependent && !this.isAvailable(v, other_dist_v, id)) {
                    other_dist_v = this.nextAvailability(v, other_dist_v, id);
                    if (!this.isAvailable(u, dist_u, other_dist_v - w / speed, id)) continue;
                }

                if (dist_v == null || other_dist_v < dist_v) {
                    dist.put(v, other_dist_v);
                    prev.put(v, u);

                    pq.add(new Edge(v, other_dist_v));
                }
            }
        }
    }

    public void computeShortestPaths(int s) {
        this.dist.put(s, new HashMap<>());
        this.prev.put(s, new HashMap<>());
        this.dijkstra(s, this.dist.get(s), this.prev.get(s));
    }

    public double getShortestPath(int s, int t, ArrayList<Integer> path) {
        if (!this.dist.containsKey(s)) {
            this.computeShortestPaths(s);
        }

        if (!this.dist.get(s).containsKey(t)) {
            System.out.println("No path between " + s + " and " + t);
            System.exit(0);
        } else {
            HashMap<Integer, Double> dist = this.dist.get(s);
            HashMap<Integer, Integer> prev = this.prev.get(s);

            if (path != null) {
                int u = t;
                path.add(u);
                while (u != s) {
                    u = prev.get(u);
                    path.add(0, u);
                }
            }
            return dist.get(t);
        }

        return 0;
    }

    public ArrayList<Pair<Integer, Double>> getShortestPath(int s, int t, double time, double speed, int id) {
        HashMap<Integer, Double> dist = new HashMap<>();
        HashMap<Integer, Integer> prev = new HashMap<>();

        this.dijkstra(s, time, speed, id, dist, prev);

        ArrayList<Pair<Integer, Double>> path = new ArrayList<>();

        if (!dist.containsKey(t)) {
            System.out.println("No path between " + s + " and " + t + " at time " + time);
            System.exit(0);
        } else {
            int u = t;
            path.add(new Pair<>(u, dist.get(u)));
            while (u != s) {
                u = prev.get(u);
                path.add(0, new Pair<>(u, dist.get(u)));
            }
        }

        for (int i = path.size() - 1; i >= 0; i--) {
            int u = path.get(i).first;
            double timeU = path.get(i).second;
            if (i < path.size() - 1) {
                int v = path.get(i + 1).first;
                double timeV = path.get(i + 1).second;

                Edge e = this.g.get(u).floor(new Edge(v, 0));

                if (timeV - timeU > e.w / speed) {
                    double waitingTime = timeV - timeU - e.w / speed;
                    path.add(i + 1, new Pair<>(u, timeU + waitingTime));
                    this.reserve(u, timeU, timeU + waitingTime, id);
                } else {
                    this.reserve(u, timeU, id);
                }
            } else {
                this.reserve(u, timeU, Double.MAX_VALUE / 2, id); // reserve location until mobile is moved again
            }
        }

        return path;
    }

}
