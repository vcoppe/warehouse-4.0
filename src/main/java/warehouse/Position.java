package warehouse;

import java.util.Objects;

public class Position {

    private int x, y, z;

    public Position(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int manhattanDistance2D(Position other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    public int manhattanDistance3D(Position other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y) + Math.abs(this.z - other.z);
    }

    public boolean equals(Position other) {
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }

}
