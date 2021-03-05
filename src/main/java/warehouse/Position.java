package warehouse;

public class Position {

    private final int x, y, z;

    public Position(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Position(int x, int y) {
        this(x, y, 0);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public double manhattanDistance2D(Position other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    public double manhattanDistance3D(Position other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y) + Math.abs(this.z - other.z);
    }

    public boolean equals(Position other) {
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }

}
