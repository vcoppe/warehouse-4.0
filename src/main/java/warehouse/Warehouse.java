package warehouse;

import agent.Mobile;
import pathfinding.Graph;

public class Warehouse {

    private final int width, depth, height; // dimensions
    private final Graph graph;

    public Warehouse(int width, int depth) {
        // TODO constructor with number of shops, number of slots per shop etc
        this.width = width;
        this.depth = depth;
        this.height = 2;
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

    public void addEdge(Position p1, Position p2) {
        this.graph.addEdge(p1, p2, p1.manhattanDistance3D(p2));
    }

    public double getDistance(Position p1, Position p2) {
        return this.graph.getShortestPath(p1, p2);
    }

    public double getTravelTime(Position p1, Position p2, Mobile mobile) {
        return this.getDistance(p1, p2) * mobile.getSpeed();
    }

}
