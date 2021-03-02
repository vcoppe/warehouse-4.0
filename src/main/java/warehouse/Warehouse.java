package warehouse;

public class Warehouse {

    public final int width, depth, height; // dimensions

    public Warehouse(int width, int depth) {
        // TODO constructor with number of shops, number of slots per shop etc
        this.width = width;
        this.depth = depth;
        this.height = 2;
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

    // TODO use the topology of the site (represented as a graph), apply speed wrt type of mobile and zone (and time of the day)
    public double getDistance(Position p1, Position p2) {
        return p1.manhattanDistance2D(p2) / 100.0;
    }

}
