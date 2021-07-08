package warehouse;

import agent.Mobile;
import graph.Edge;
import graph.Graph;

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

    public Edge addEdge(Position p1, Position p2, boolean force) {
        if (!force) {
            if (p1.getX() < 0 || p1.getX() >= this.width) return null;
            if (p1.getY() < 0 || p1.getY() >= this.depth) return null;
            if (p1.getZ() < 0 || p1.getZ() >= this.height) return null;
            if (p2.getX() < 0 || p2.getX() >= this.width) return null;
            if (p2.getY() < 0 || p2.getY() >= this.depth) return null;
            if (p2.getZ() < 0 || p2.getZ() >= this.height) return null;
        }
        return this.graph.addEdge(p1, p2, p1.manhattanDistance3D(p2));
    }

    public Edge addEdge(Position p1, Position p2) {
        return this.addEdge(p1, p2, false);
    }

    public void removeEdge(Position p1, Position p2) {
        this.graph.removeEdge(p1, p2);
    }

    public double getDistance(Position p1, Position p2) {
        return this.graph.getShortestPath(p1, p2);
    }

    public double getTravelTime(Position p1, Position p2, Mobile mobile) {
        return this.getDistance(p1, p2) * mobile.getSpeed();
    }

}
