package pathfinding;

import util.Vector3D;

import java.util.Objects;

public class Reservation implements Comparable<Reservation> {

    Vector3D position;
    int mobileId;
    double start, end;

    public Reservation(Vector3D position, double start, double end, int id) {
        this.position = position;
        this.start = start;
        this.end = end;
        this.mobileId = id;
    }

    public boolean conflicts(Reservation other) {
        return this.mobileId != other.mobileId && this.start < other.end && this.end > other.start;
    }

    @Override
    public int compareTo(Reservation other) {
        if (this.start == other.start) {
            return Double.compare(this.end, other.end);
        }
        return Double.compare(this.start, other.start);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return mobileId == that.mobileId &&
                Double.compare(that.start, start) == 0 &&
                Double.compare(that.end, end) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mobileId, start, end);
    }
}