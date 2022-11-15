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
    private final HashMap<Vector3D, Vector2D> resumableDist;
    private final PriorityQueue<Pair<Vector3D, Vector2D>> resumablePq;
    private final HashSet<Vector3D> resumableClosed;
    private Pair<Vector3D, Vector3D> lastRoute;

    public ReverseResumableAStar(Graph graph) {
        this.graph = graph;
        this.lastRoute = null;
        this.resumableDist = new HashMap<>();
        this.resumablePq = new PriorityQueue<>(manhattanDistanceComparator);
        this.resumableClosed = new HashSet<>();
    }

    public void init(Vector3D startPosition, Vector3D endPosition) {
        if (this.lastRoute != null && startPosition.equals(lastRoute.first) && endPosition.equals(lastRoute.second)) {
            return;
        }

        this.resumableDist.clear();
        this.resumablePq.clear();
        this.resumableClosed.clear();

        this.resumableDist.put(endPosition, Vector2D.zero);
        this.resumablePq.add(new Pair<>(endPosition, this.resumableDist.get(endPosition).add(startPosition.manhattanDistance3D(endPosition))));

        this.lastRoute = new Pair<>(startPosition, endPosition);
    }

    public Vector2D distance(Mobile mobile, Vector3D endPosition) {
        if (this.resumableClosed.contains(endPosition)) {
            return this.resumableDist.get(endPosition);
        }

        while (!this.resumablePq.isEmpty()) {
            Pair<Vector3D, Vector2D> p = this.resumablePq.peek();
            Vector3D u = p.first;
            Vector2D estimateU = p.second;

            if (Vector2D.manhattanDistanceComparator.compare(estimateU.subtract(u.manhattanDistance3D(this.lastRoute.first)), this.resumableDist.get(u)) > 0) { // not the shortest path anymore
                this.resumablePq.poll();
                continue;
            }

            if (u.equals(endPosition)) {
                return this.resumableDist.get(endPosition);
            }

            this.resumablePq.poll();
            this.resumableClosed.add(u);

            for (Edge edge : this.graph.getReverseEdges(u)) { // reverse edges for reverse A star
                Vector3D v = edge.to;
                Vector2D w = edge.weight;

                if (!this.graph.getEdge(v, u).canCross(mobile)) continue;

                Vector2D otherDist = this.resumableDist.get(u).add(w);

                if (!this.resumableDist.containsKey(v) || Vector2D.manhattanDistanceComparator.compare(otherDist, this.resumableDist.get(v)) < 0) {
                    this.resumableDist.put(v, otherDist);
                    Vector2D estimateV = otherDist.add(v.manhattanDistance3D(this.lastRoute.first));
                    this.resumablePq.add(new Pair<>(v, estimateV));
                }
            }
        }

        return null;
    }

}
