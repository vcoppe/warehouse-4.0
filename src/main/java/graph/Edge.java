package graph;

import warehouse.Position;

public class Edge implements Comparable<Edge> {

    Position to;
    double w;

    public Edge(Position to, double w) {
        this.to = to;
        this.w = w;
    }

    public double getWeight() {
        return this.w;
    }

    @Override
    public int compareTo(Edge other) {
        return this.to.compareTo(other.to);
    }

}