package warehouse;

import java.util.Objects;

public class Position implements Comparable<Position> {

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

    public Position add(Position position) {
        return new Position(this.x + position.x, this.y + position.y, this.z + position.z);
    }

    public Position subtract(Position position) {
        return new Position(this.x - position.x, this.y - position.y, this.z - position.z);
    }

    public double manhattanDistance2D(Position other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    public double manhattanDistance3D(Position other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y) + Math.abs(this.z - other.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x &&
                y == position.y &&
                z == position.z;
    }

    @Override
    public int compareTo(Position other) {
        if (this.x == other.x) {
            if (this.y == other.y) {
                return Integer.compare(this.z, other.z);
            }
            return Integer.compare(this.y, other.y);
        }
        return Integer.compare(this.x, other.x);
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

}
