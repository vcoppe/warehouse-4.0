package warehouse;

public class Warehouse {

    public final int X, Y, Z; // dimensions

    public Warehouse(int X, int Y) {
        // TODO constructor with number of shops, number of slots per shop etc
        this.X = X;
        this.Y = Y;
        this.Z = 2;
    }

    public int getWidth() {
        return this.X;
    }

    public int getDepth() {
        return this.Y;
    }

    public int getHeight() {
        return this.Z;
    }

    // TODO use the topology of the site (represented as a graph), apply speed wrt type of mobile and zone (and time of the day)
    public int getDistance(Position p1, Position p2) {
        return p1.manhattanDistance2D(p2);
    }

}
