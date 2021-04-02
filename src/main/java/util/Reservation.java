package util;

public class Reservation implements Comparable<Reservation> {

    double start, end;

    public Reservation(double start, double end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public int compareTo(Reservation other) {
        if (this.start == other.start) {
            return Double.compare(this.end, other.end);
        }
        return Double.compare(this.start, other.start);
    }
}