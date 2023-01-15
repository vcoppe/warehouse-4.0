package pathfinding;

import agent.Controller;
import agent.Dock;
import agent.Lift;
import util.Pair;
import util.Vector3D;
import warehouse.Configuration;

import java.util.*;

public class ClosestOpenPositionFinder {

    private final Controller controller;
    private final Graph graph;
    private final HashSet<Vector3D> closed;
    private final HashMap<Vector3D, Integer> distToClosedPosition;

    public ClosestOpenPositionFinder(Controller controller) {
        this.controller = controller;
        this.graph = controller.getWarehouse().getGraph();
        this.closed = new HashSet<>();
        this.distToClosedPosition = new HashMap<>();
        this.updatePositions();
    }

    public void updatePositions() {
        this.closed.addAll(this.controller.getStock().getAllPositions());
        for (Dock dock : this.controller.getDocks()) {
            Vector3D dockPosition = dock.getPosition();
            for (int dx = 0; dx < Configuration.dockWidth; dx += Configuration.palletSize) {
                for (int dy = 0; dy < Configuration.truckDepth; dy += Configuration.palletSize) {
                    Vector3D delta = new Vector3D(dx, dy);
                    Vector3D position = dockPosition.add(delta);
                    this.closed.add(position);
                }
            }
        }
        for (Lift lift : this.controller.getLifts()) {
            Vector3D liftPosition = lift.getPosition();
            for (int dz = 0; dz < lift.getHeight(); dz += Configuration.palletSize) {
                Vector3D delta = new Vector3D(0, 0, dz);
                Vector3D position = liftPosition.add(delta);
                this.closed.add(position);
            }
        }
        this.computeDistances();

        /*for (int y = 0; y < 300; y += 10) {
            for (int x = 0; x < 800; x += 10) {
                Vector3D position = new Vector3D(x, y);
                if (this.distToClosedPosition.containsKey(position)) {
                    System.out.printf("%3d ", this.distToClosedPosition.get(position));
                }
            }
            System.out.println();
        }
        System.out.println();*/
    }

    private void computeDistances() {
        this.distToClosedPosition.clear();
        PriorityQueue<Pair<Vector3D, Integer>> pq = new PriorityQueue<>(Comparator.comparing(Pair::getSecond));

        for (Vector3D position : this.closed) {
            pq.add(new Pair<>(position, 0));
            this.distToClosedPosition.put(position, 0);
        }

        while (!pq.isEmpty()) {
            Pair<Vector3D, Integer> pair = pq.poll();
            Vector3D u = pair.first;
            int distFromClosed = pair.second;

            if (distFromClosed > this.distToClosedPosition.get(u)) continue;

            for (Edge e : this.graph.getEdges(u)) {
                Vector3D v = e.to();

                int distFromClosedV = distFromClosed + 1;

                if (!this.distToClosedPosition.containsKey(v) || distFromClosedV < this.distToClosedPosition.get(v)) {
                    this.distToClosedPosition.put(v, distFromClosedV);
                    pq.add(new Pair<>(v, distFromClosedV));
                }
            }
        }
    }

    public ArrayList<Vector3D> getClosestOpenPositions(Vector3D position) {
        ArrayList<Vector3D> closestOpenPositions = new ArrayList<>();

        Queue<Vector3D> queue = new LinkedList<>();
        queue.add(position);

        HashSet<Vector3D> visited = new HashSet<>();
        visited.add(position);

        while (!queue.isEmpty()) {
            Vector3D u = queue.poll();

            if (this.distToClosedPosition.get(u) >= 2) {
                closestOpenPositions.add(u);

                if (closestOpenPositions.size() == 20) {
                    break;
                }
            }

            for (Edge e : this.graph.getEdges(u)) {
                Vector3D v = e.to();

                if (!visited.contains(v)) {
                    queue.add(v);
                    visited.add(v);
                }
            }
        }

        return closestOpenPositions;
    }

}
