package warehouse;

import agent.Lift;
import agent.Mobile;
import pathfinding.Edge;
import pathfinding.Graph;
import util.DoublePrecisionConstraint;
import util.Vector2D;
import util.Vector3D;

import java.util.ArrayDeque;

public class Warehouse {

    private final int width, depth, height; // dimensions
    private final Graph graph;

    public Warehouse(int width, int depth, int height) {
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.graph = new Graph();
    }

    public int getWidth() {
        return this.width;
    }

    public int getDepth() {
        return this.depth;
    }

    public int getHeight() {
        return this.height;
    }

    public Graph getGraph() {
        return this.graph;
    }

    public Edge addEdge(Vector3D p1, Vector3D p2, boolean force) {
        if (!force) {
            if (p1.getX() < 0 || p1.getX() >= this.width) return null;
            if (p1.getY() < 0 || p1.getY() >= this.depth) return null;
            if (p1.getZ() < 0 || p1.getZ() >= this.height) return null;
            if (p2.getX() < 0 || p2.getX() >= this.width) return null;
            if (p2.getY() < 0 || p2.getY() >= this.depth) return null;
            if (p2.getZ() < 0 || p2.getZ() >= this.height) return null;
        }
        Edge edge = this.graph.getEdge(p1, p2);
        if (edge != null) return edge;
        return this.graph.addEdge(p1, p2);
    }

    public Edge addEdge(Vector3D p1, Vector3D p2) {
        return this.addEdge(p1, p2, false);
    }

    public void removeEdge(Vector3D p1, Vector3D p2) {
        this.graph.removeEdge(p1, p2);
    }

    public Edge getEdge(Vector3D p1, Vector3D p2) {
        return this.graph.getEdge(p1, p2);
    }

    public Vector2D getDistance(Vector3D p1, Vector3D p2) {
        return this.graph.getShortestDistance(p1, p2);
    }

    public ArrayDeque<Vector3D> getPath(Vector3D p1, Vector3D p2) {
        return this.graph.getShortestPath(p1, p2);
    }

    public double getTravelTime(Vector3D p1, Vector3D p2, boolean loaded) {
        Vector2D dist2D = this.getDistance(p1, p2);
        return DoublePrecisionConstraint.round(dist2D.getX() * Mobile.getSpeed(loaded) + dist2D.getY() * Lift.speed);
    }

}
