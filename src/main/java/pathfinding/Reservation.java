package pathfinding;

public class Reservation implements Comparable<Reservation> {

    int mobileId;
    double start, end;

    public Reservation(double start, double end, int id) {
        this.start = start;
        this.end = end;
        this.mobileId = id;
    }

    @Override
    public int compareTo(Reservation other) {
        if (this.start == other.start) {
            return Double.compare(this.end, other.end);
        }
        return Double.compare(this.start, other.start);
    }
}