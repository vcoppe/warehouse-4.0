package pathfinding;

public class Interval implements Comparable<Interval> {

    double start, end;

    public Interval(double start, double end) {
        this.start = start;
        this.end = end;
    }

    public boolean overlaps(Interval other) {
        return (this.start <= other.end) && (this.end >= other.start);
    }

    public void merge(Interval other) {
        if (other.start < this.start) {
            this.start = other.start;
        }
        if (other.end > this.end) {
            this.end = other.end;
        }
    }

    @Override
    public int compareTo(Interval other) {
        if (this.start == other.start) {
            return Double.compare(this.end, other.end);
        }
        return Double.compare(this.start, other.start);
    }

}
