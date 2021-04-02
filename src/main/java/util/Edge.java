package util;

public class Edge implements Comparable<Edge> {

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