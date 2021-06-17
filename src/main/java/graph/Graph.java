package graph;

import warehouse.Position;

import java.util.*;

public class Graph {

    private final HashMap<Position, TreeSet<Edge>> g, gReverse;
    private final HashMap<Position, HashMap<Position, Double>> dist;
    private final HashMap<Position, HashMap<Position, Position>> prev;

    public Graph() {
        this.g = new HashMap<>();
        this.gReverse = new HashMap<>();
        this.dist = new HashMap<>();
        this.prev = new HashMap<>();
    }

    public double getWeight(Position u, Position v) {
        Edge e = this.g.get(u).floor(new Edge(v, 0));
        if (e.to.equals(v)) {
            return e.w;
        } else {
            return Double.MAX_VALUE;
        }
    }

    public Set<Position> getVertices() {
        return this.g.keySet();
    }

    public Set<Edge> getEdges(Position u) {
        return this.g.get(u);
    }

    public Set<Edge> getReverseEdges(Position u) {
        return this.gReverse.get(u);
    }

    public void addEdge(Position u, Position v, double w) {
        if (!this.g.containsKey(u)) {
            this.g.put(u, new TreeSet<>());
            this.gReverse.put(u, new TreeSet<>());
        }

        if (!this.g.containsKey(v)) {
            this.g.put(v, new TreeSet<>());
            this.gReverse.put(v, new TreeSet<>());
        }

        this.g.get(u).add(new Edge(v, w));
        this.gReverse.get(v).add(new Edge(u, w));

        this.dist.clear();
        this.prev.clear();
    }

    public void removeEdge(Position u, Position v) {
        if (this.g.containsKey(u)) {
            Edge e = this.g.get(u).floor(new Edge(v, 0));
            if (e != null && e.to.equals(v)) this.g.get(u).remove(e);
        }

        if (this.gReverse.containsKey(v)) {
            Edge e = this.gReverse.get(v).floor(new Edge(u, 0));
            if (e != null && e.to.equals(u)) this.gReverse.get(v).remove(e);
        }

        this.dist.clear();
        this.prev.clear();
    }

    public void dijkstra(Position s, HashMap<Position, Double> dist, HashMap<Position, Position> prev) {
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparing(Edge::getWeight));

        dist.put(s, 0.0);
        pq.add(new Edge(s, 0.0));

        while (!pq.isEmpty()) {
            Edge e1 = pq.poll();
            Position u = e1.to;
            double dist_u = e1.w;

            if (dist_u > dist.get(u)) {
                continue;
            }

            for (Edge e2 : this.g.get(u)) {
                Position v = e2.to;
                double w = e2.w;
                Double dist_v = dist.get(v);
                double other_dist_v = dist_u + w;

                if (dist_v == null || other_dist_v < dist_v) {
                    dist.put(v, other_dist_v);
                    prev.put(v, u);

                    pq.add(new Edge(v, other_dist_v));
                }
            }
        }
    }

    public void computeShortestPaths(Position s) {
        this.dist.put(s, new HashMap<>());
        this.prev.put(s, new HashMap<>());
        this.dijkstra(s, this.dist.get(s), this.prev.get(s));
    }

    public void aStar(Position s, Position t, HashMap<Position, Double> dist, HashMap<Position, Position> prev) {
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparing(Edge::getWeight));

        dist.put(s, 0.0);
        pq.add(new Edge(s, s.manhattanDistance3D(t)));

        while (!pq.isEmpty()) {
            Edge e1 = pq.poll();
            Position u = e1.to;
            double dist_u = e1.w - u.manhattanDistance3D(t);

            if (dist_u > dist.get(u)) {
                continue;
            }

            if (u.equals(t)) {
                return;
            }

            for (Edge e2 : this.g.get(u)) {
                Position v = e2.to;
                double w = e2.w;
                Double dist_v = dist.get(v);
                double other_dist_v = dist_u + w;

                if (dist_v == null || other_dist_v < dist_v) {
                    dist.put(v, other_dist_v);
                    prev.put(v, u);

                    pq.add(new Edge(v, other_dist_v + v.manhattanDistance3D(t)));
                }
            }
        }

        dist.put(t, null); // not found
    }

    public void computeShortestPath(Position s, Position t) {
        this.dist.put(s, new HashMap<>());
        this.prev.put(s, new HashMap<>());
        this.aStar(s, t, this.dist.get(s), this.prev.get(s));
    }

    public double getShortestPath(Position s, Position t) {
        if (!this.dist.containsKey(s) || !this.dist.get(s).containsKey(t)) {
            this.computeShortestPath(s, t);
        }

        Double dist = this.dist.get(s).get(t);

        if (dist == null) {
            System.out.println("No path between " + s + " and " + t);
            System.exit(0);
        }

        return dist;
    }

}
