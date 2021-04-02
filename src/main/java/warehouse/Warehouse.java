package warehouse;

import agent.Mobile;
import util.Graph;
import util.Pair;

import java.util.ArrayList;

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

    public int toInt(Position position) {
        return position.getX() + this.width *
                (position.getZ() + this.height * position.getY());
    }

    public Position toPosition(int hash) {
        return new Position(
                hash % this.width,
                hash / (this.width * this.height),
                (hash / this.width) % this.height
        );
    }

    public void addEdge(Position p1, Position p2) {
        this.graph.addEdge(this.toInt(p1), this.toInt(p2), p1.manhattanDistance3D(p2));
    }

    public double getDistance(Position p1, Position p2) {
        return this.graph.getShortestPath(this.toInt(p1), this.toInt(p2), null);
    }

    public double getTravelTime(Position p1, Position p2, Mobile mobile) {
        return this.graph.getShortestPath(this.toInt(p1), this.toInt(p2), null) / mobile.getSpeed();
    }

    public ArrayList<Pair<Position, Double>> getPath(Position p1, Position p2, double time, Mobile mobile) {
        ArrayList<Pair<Integer, Double>> integerPath = this.graph.getShortestPath(this.toInt(p1), this.toInt(p2), time, mobile.getSpeed());

        ArrayList<Pair<Position, Double>> path = new ArrayList<>();
        for (Pair<Integer, Double> pair : integerPath) {
            path.add(new Pair<>(this.toPosition(pair.first), pair.second));
        }

        return path;
    }

}
