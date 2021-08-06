package graph;

import util.Pair;
import util.Vector2D;
import util.Vector3D;

import java.util.*;

public class Graph {

    private static final Comparator<Pair<Vector3D, Vector2D>> manhattanDistanceComparator = (a, b) -> Vector2D.manhattanDistanceComparator.compare(a.second, b.second);
    private final HashMap<Vector3D, TreeSet<Edge>> g, gReverse;
    private final HashMap<Vector3D, HashMap<Vector3D, Vector2D>> dist;
    private final HashMap<Vector3D, HashMap<Vector3D, Vector3D>> prev;

    public Graph() {
        this.g = new HashMap<>();
        this.gReverse = new HashMap<>();
        this.dist = new HashMap<>();
        this.prev = new HashMap<>();
    }

    public Edge getEdge(Vector3D u, Vector3D v) {
        Edge e = this.g.get(u).floor(new Edge(u, v));
        if (e != null && e.to.equals(v)) {
            return e;
        } else {
            return null;
        }
    }

    public Vector2D getWeight(Vector3D u, Vector3D v) {
        Edge e = this.getEdge(u, v);
        if (e != null) {
            return e.weight;
        } else {
            return new Vector2D(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
    }

    public Set<Vector3D> getVertices() {
        return this.g.keySet();
    }

    public Set<Edge> getEdges(Vector3D u) {
        return this.g.get(u);
    }

    public Set<Edge> getReverseEdges(Vector3D u) {
        return this.gReverse.get(u);
    }

    public Edge addEdge(Vector3D u, Vector3D v) {
        if (!this.g.containsKey(u)) {
            this.g.put(u, new TreeSet<>());
            this.gReverse.put(u, new TreeSet<>());
        }

        if (!this.g.containsKey(v)) {
            this.g.put(v, new TreeSet<>());
            this.gReverse.put(v, new TreeSet<>());
        }

        Edge edge = new Edge(u, v);

        this.g.get(u).add(edge);
        this.gReverse.get(v).add(new Edge(v, u));

        this.dist.clear();
        this.prev.clear();

        return edge;
    }

    public void removeEdge(Vector3D u, Vector3D v) {
        if (this.g.containsKey(u)) {
            Edge e = this.g.get(u).floor(new Edge(u, v));
            if (e != null && e.to.equals(v)) this.g.get(u).remove(e);
        }

        if (this.gReverse.containsKey(v)) {
            Edge e = this.gReverse.get(v).floor(new Edge(v, u));
            if (e != null && e.to.equals(u)) this.gReverse.get(v).remove(e);
        }

        this.dist.clear();
        this.prev.clear();
    }

    public void aStar(Vector3D s, Vector3D t, HashMap<Vector3D, Vector2D> dist, HashMap<Vector3D, Vector3D> prev) {
        PriorityQueue<Pair<Vector3D, Vector2D>> pq = new PriorityQueue<>(manhattanDistanceComparator);

        dist.put(s, new Vector2D(0, 0));
        pq.add(new Pair<>(s, s.manhattanDistance3D(t)));

        while (!pq.isEmpty()) {
            Pair<Vector3D, Vector2D> p1 = pq.poll();
            Vector3D u = p1.first;
            Vector2D dist_u = p1.second.subtract(u.manhattanDistance3D(t));

            if (Vector2D.manhattanDistanceComparator.compare(dist_u, dist.get(u)) > 0) {
                continue;
            }

            if (u.equals(t)) {
                return;
            }

            for (Edge e2 : this.g.get(u)) {
                Vector3D v = e2.to;
                Vector2D w = e2.weight;
                Vector2D dist_v = dist.get(v);
                Vector2D other_dist_v = dist_u.add(w);

                if (dist_v == null || Vector2D.manhattanDistanceComparator.compare(other_dist_v, dist_v) < 0) {
                    dist.put(v, other_dist_v);
                    prev.put(v, u);

                    pq.add(new Pair<>(v, other_dist_v.add(v.manhattanDistance3D(t))));
                }
            }
        }

        dist.put(t, null); // not found
    }

    public void computeShortestPath(Vector3D s, Vector3D t) {
        this.dist.put(s, new HashMap<>());
        this.prev.put(s, new HashMap<>());
        this.aStar(s, t, this.dist.get(s), this.prev.get(s));
    }

    public Vector2D getShortestPath(Vector3D s, Vector3D t) {
        if (!this.dist.containsKey(s) || !this.dist.get(s).containsKey(t)) {
            this.computeShortestPath(s, t);
        }

        Vector2D dist = this.dist.get(s).get(t);

        if (dist == null) {
            System.out.println("No path between " + s + " and " + t);
            //System.exit(0);
        }

        return dist;
    }

}
