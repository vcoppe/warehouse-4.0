package util;

import java.util.Comparator;
import java.util.Objects;

public class Vector2D implements Comparable<Vector2D> {

    public static final Comparator<Vector2D> manhattanDistanceComparator = Comparator.comparing(Vector2D::norm);
    private int x, y;

    public Vector2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Vector2D add(Vector2D vec) {
        return new Vector2D(this.x + vec.x, this.y + vec.y);
    }

    public Vector2D subtract(Vector2D vec) {
        return new Vector2D(this.x - vec.x, this.y - vec.y);
    }

    public int norm() {
        return Math.abs(this.x) + Math.abs(this.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2D vec = (Vector2D) o;
        return x == vec.x &&
                y == vec.y;
    }

    @Override
    public int compareTo(Vector2D other) {
        if (this.x == other.x) {
            return Integer.compare(this.y, other.y);
        }
        return Integer.compare(this.x, other.x);
    }

    @Override
    public String toString() {
        return "Vector3D{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

}
