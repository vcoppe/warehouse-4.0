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
        }
    }

    public void computeShortestPaths(int s) {
        this.dist.put(s, new HashMap<>());
        this.prev.put(s, new HashMap<>());
        this.dijkstra(s, this.dist.get(s), this.prev.get(s));
    }

    public double getShortestDistance(int s, int t) {
        if (!this.dist.containsKey(s)) {
            this.computeShortestPaths(s);
        }
        try {
            return this.dist.get(s).get(t);
        } catch (Exception e) {
            System.out.println(s + ", " + t);
            e.printStackTrace();
            System.exit(0);
        }
        return 0;
    }

    public ArrayList<Integer> getShortestPath(int s, int t) {
        if (!this.dist.containsKey(s)) {
            this.computeShortestPaths(s);
        }

        HashMap<Integer, Double> dist = this.dist.get(s);
        HashMap<Integer, Integer> prev = this.prev.get(s);

        ArrayList<Integer> path = new ArrayList<>();

        if (!dist.containsKey(t)) {
            return path;
        }

        int u = t;
        path.add(u);
        while (u != s) {
            u = prev.get(u);
            path.add(0, u);
        }

        return path;
    }

    static class Edge implements Comparable<Edge> {

        int to;
        double w;

        public Edge(int to, double w) {
            this.to = to;
            this.w = w;
        }

        public double getWeight() {
            return this.w;
        }

        @Override
        public int compareTo(Edge other) {
            return Integer.compare(this.to, other.to);
        }

    }

}
