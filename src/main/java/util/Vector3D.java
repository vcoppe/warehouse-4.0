package util;

import java.util.Objects;

public class Vector3D implements Comparable<Vector3D> {

    private int x, y, z;

    public Vector3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(int x, int y) {
        this(x, y, 0);
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return this.x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return this.y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getZ() {
        return this.z;
    }

    public Vector3D add(Vector3D position) {
        return new Vector3D(this.x + position.x, this.y + position.y, this.z + position.z);
    }

    public Vector3D subtract(Vector3D position) {
        return new Vector3D(this.x - position.x, this.y - position.y, this.z - position.z);
    }

    public double manhattanDistance2D(Vector3D other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    public Vector2D manhattanDistance3D(Vector3D other) {
        return new Vector2D(Math.abs(this.x - other.x) + Math.abs(this.y - other.y), Math.abs(this.z - other.z));
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3D position = (Vector3D) o;
        return x == position.x &&
                y == position.y &&
                z == position.z;
    }

    @Override
    public int compareTo(Vector3D other) {
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
        return "Vector3D{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

}
