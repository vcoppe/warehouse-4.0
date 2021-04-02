package util;

import java.util.*;

public class Graph {

    private final HashMap<Integer, TreeSet<Edge>> g;
    private final HashMap<Integer, HashMap<Integer, Double>> dist;
    private final HashMap<Integer, HashMap<Integer, Integer>> prev;

    public Graph() {
        this.g = new HashMap<>();
        this.dist = new HashMap<>();
        this.prev = new HashMap<>();
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
        }

        if (!this.g.containsKey(v)) {
            this.g.put(v, new TreeSet<>());
        }

        this.g.get(u).add(new Edge(v, w));
    }

    public void dijkstra(int s, HashMap<Integer, Double> dist, HashMap<Integer, Integer> prev) {
        this.dijkstra(s, null, null, dist, prev);
    }

    public void dijkstra(int s, Double time, Double speed, HashMap<Integer, Double> dist, HashMap<Integer, Integer> prev) {
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparing(Edge::getWeight));

        dist.put(s, 0.0);
        pq.add(new Edge(s, 0));

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

                if (dist_v == null || dist_u + w < dist_v) {
                    dist.put(v, dist_u + w);
                    prev.put(v, u);

                    pq.add(new Edge(v, dist_u + w));
                }
            }

            // si données de temps, ajouter un mouvement "attendre" ici
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

    public ArrayList<Pair<Integer, Double>> getShortestPath(int s, int t, double time, double speed) {
        HashMap<Integer, Double> dist = new HashMap<>();
        HashMap<Integer, Integer> prev = new HashMap<>();
        this.dijkstra(s, time, speed, dist, prev);
        ArrayList<Pair<Integer, Double>> path = new ArrayList<>();

        if (!dist.containsKey(t)) {
            System.out.println("No path between " + s + " and " + t + " at time " + time);
            System.exit(0);
        } else {
            int u = t;
            path.add(new Pair<>(u, time + dist.get(u) / speed));
            while (u != s) {
                u = prev.get(u);
                path.add(0, new Pair<>(u, time + dist.get(u) / speed));
            }
        }

        return path;
    }

}
