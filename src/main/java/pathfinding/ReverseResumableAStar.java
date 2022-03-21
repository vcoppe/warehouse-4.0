package pathfinding;

import agent.Mobile;
import util.Pair;
import util.Vector2D;
import util.Vector3D;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class ReverseResumableAStar {

    private static final Comparator<Pair<Vector3D, Vector2D>> manhattanDistanceComparator = (a, b) -> Vector2D.manhattanDistanceComparator.compare(a.second, b.second);
    private final Graph graph;
    private final HashMap<Integer, Pair<Vector3D, Vector3D>> lastRoute;
    private final HashMap<Integer, HashMap<Vector3D, Vector2D>> resumableDist;
    private final HashMap<Integer, PriorityQueue<Pair<Vector3D, Vector2D>>> resumablePq;
    private final HashMap<Integer, HashSet<Vector3D>> resumableClosed;

    public ReverseResumableAStar(Graph graph) {
        this.graph = graph;
        this.lastRoute = new HashMap<>();
        this.resumableDist = new HashMap<>();
        this.resumablePq = new HashMap<>();
        this.resumableClosed = new HashMap<>();
    }

    public void init(Mobile mobile) {
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

        dist.put(endPosition, Vector2D.zero);
        pq.add(new Pair<>(endPosition, dist.get(endPosition).add(startPosition.manhattanDistance3D(endPosition))));

        this.resumableDist.put(mobile.getId(), dist);
        this.resumablePq.put(mobile.getId(), pq);
        this.resumableClosed.put(mobile.getId(), closed);

        this.lastRoute.put(mobile.getId(), new Pair<>(startPosition, endPosition));
    }

    public Vector2D distance(Mobile mobile, Vector3D endPosition, Vector3D finalPosition) {
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

                if (!this.graph.getEdge(v, u).canCross(mobile)) continue;

                Vector2D otherDist = dist.get(u).add(w);

                if (!dist.containsKey(v) || Vector2D.manhattanDistanceComparator.compare(otherDist, dist.get(v)) < 0) {
                    dist.put(v, otherDist);
                    Vector2D estimateV = otherDist.add(v.manhattanDistance3D(finalPosition));
                    pq.add(new Pair<>(v, estimateV));
                }
            }
        }

        return null;
    }

}
