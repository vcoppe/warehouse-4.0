package util;

import java.util.*;

public class Graph {

    private final HashMap<Integer, TreeSet<Edge>> g;
    private final HashMap<Integer, HashMap<Integer, Double>> dist;
    private final HashMap<Integer, HashMap<Integer, Integer>> next;

    public Graph() {
        this.g = new HashMap<>();
        this.dist = new HashMap<>();
        this.next = new HashMap<>();
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

    public void computeAllPairsShortestPath() {
        this.dist.clear();
        this.next.clear();

        for (int u : this.g.keySet()) {
            this.dist.put(u, new HashMap<>());
            this.next.put(u, new HashMap<>());

            this.dist.get(u).put(u, 0.0);
            this.next.get(u).put(u, u);

            for (Edge e : this.g.get(u)) {
                int v = e.to;
                double w = e.w;

                this.dist.get(u).put(v, w);
                this.next.get(u).put(v, v);
            }
        }

        for (int k : this.g.keySet()) {
            for (int i : this.g.keySet()) {
                for (int j : this.g.keySet()) {
                    Double dist_ij = this.dist.get(i).get(j);
                    Double dist_ik = this.dist.get(i).get(k);
                    Double dist_kj = this.dist.get(k).get(j);

                    if (dist_ik == null || dist_kj == null) {
                        continue;
                    }

                    if (dist_ij == null || dist_ij > dist_ik + dist_kj) {
                        this.dist.get(i).put(j, dist_ik + dist_kj);
                        this.next.get(i).put(j, this.next.get(i).get(k));
                    }
                }
            }
        }
    }

    public ArrayList<Integer> shortestPath(int s, int t) {
        /*
        Using Floyd-Warshall
        ArrayList<Integer> path = new ArrayList<>();

        if (!this.next.get(u).containsKey(v)) {
            return path;
        }

        path.add(u);
        while (u != v) {
            u = this.next.get(u).get(v);
            path.add(u);
        }

        return path;*/

        HashMap<Integer, Double> dist = new HashMap<>();
        HashMap<Integer, Integer> prev = new HashMap<>();
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
