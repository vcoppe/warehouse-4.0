package graph;

import util.DoublePrecisionConstraint;
import util.Vector3D;

import java.util.HashMap;
import java.util.TreeSet;

public class ReservationTable {

    public final static double timeMargin = 1.5;

    private final HashMap<Vector3D, TreeSet<Reservation>> reservations;
    private final HashMap<Vector3D, GraphConstraint> constraints;

    public ReservationTable() {
        this.reservations = new HashMap<>();
        this.constraints = new HashMap<>();
    }

    public void addGraphConstraint(GraphConstraint constraint) {
        for (Vector3D position : constraint.getPositions()) {
            this.constraints.put(position, constraint);
        }
    }

    public void clear() {
        this.reservations.clear();
    }

    public void reserve(Vector3D position, double time, int id) {
        this.reserveWithMargin(position, DoublePrecisionConstraint.round(time - timeMargin), DoublePrecisionConstraint.round(time + timeMargin), id);
    }

    public void reserve(Vector3D position, double start, double end, int id) {
        this.reserveWithMargin(position, DoublePrecisionConstraint.round(start - timeMargin), DoublePrecisionConstraint.round(end + timeMargin), id);
    }

    private void reserveWithMargin(Vector3D position, double start, double end, int id) {
        if (this.constraints.containsKey(position)) {
            this.constraints.get(position).reserveWithMargin(this, position, start, end, id);
        } else {
            this.reserveWithMarginHelper(position, start, end, id);
        }
    }

    protected void reserveWithMarginHelper(Vector3D position, double start, double end, int id) {
        if (!this.reservations.containsKey(position)) {
            this.reservations.put(position, new TreeSet<>());
        }
        Reservation reservation = new Reservation(start, end, id);
        this.reservations.get(position).add(reservation);
    }

    public boolean isAvailable(Vector3D position, double time, int id) {
        return this.isAvailableWithMargin(position, DoublePrecisionConstraint.round(time - timeMargin), DoublePrecisionConstraint.round(time + timeMargin), id);
    }

    public boolean isAvailable(Vector3D position, double start, double end, int id) {
        return this.isAvailableWithMargin(position, DoublePrecisionConstraint.round(start - timeMargin), DoublePrecisionConstraint.round(end + timeMargin), id);
    }

    private boolean isAvailableWithMargin(Vector3D position, double start, double end, int id) {
        if (this.constraints.containsKey(position)) {
            return this.constraints.get(position).isAvailableWithMargin(this, position, start, end, id);
        } else {
            return this.isAvailableWithMarginHelper(position, start, end, id);
        }
    }

    protected boolean isAvailableWithMarginHelper(Vector3D position, double start, double end, int id) {
        if (!this.reservations.containsKey(position)) {
            return true;
        }

        Reservation key = new Reservation(start, end, id);
        Reservation before = this.reservations.get(position).floor(key);
        Reservation after = this.reservations.get(position).higher(key);

        if (before != null && before.mobileId != id && (before.start == start || before.end > start)) {
            return false;
        }

        return after == null || after.mobileId == id || !(after.start < end);
    }

    public double nextAvailability(Vector3D position, double time, int id) {
        double nextTime = this.nextAvailabilityWithMargin(position, DoublePrecisionConstraint.round(time - timeMargin), DoublePrecisionConstraint.round(2 * timeMargin), id);
        return DoublePrecisionConstraint.round(nextTime + timeMargin);
    }

    public double nextAvailability(Vector3D position, double from, double duration, int id) {
        double nextTime = this.nextAvailabilityWithMargin(position, DoublePrecisionConstraint.round(from - timeMargin), DoublePrecisionConstraint.round(duration + 2 * timeMargin), id);
        return DoublePrecisionConstraint.round(nextTime + timeMargin);
    }

    private double nextAvailabilityWithMargin(Vector3D position, double from, double duration, int id) {
        if (this.constraints.containsKey(position)) {
            return this.constraints.get(position).nextAvailabilityWithMargin(this, position, from, duration, id);
        } else {
            return this.nextAvailabilityWithMarginHelper(position, from, duration, id);
        }
    }

    protected double nextAvailabilityWithMarginHelper(Vector3D position, double from, double duration, int id) {
        if (!this.reservations.containsKey(position) || this.reservations.get(position).isEmpty()) {
            return from;
        }

        Reservation key = new Reservation(from, from + duration, id);

        Reservation before = this.reservations.get(position).floor(key);
        Reservation after = this.reservations.get(position).higher(key);

        while (true) {
            while (after != null && after.mobileId == id) {
                after = this.reservations.get(position).higher(after);
            }

            if (before == null || before.mobileId == id) {
                if (after == null || after.start - from >= duration) {
                    return from;
                }
            } else if (after == null || after.start - Math.max(before.end, from) >= duration) {
                return Math.max(before.end, from);
            }

            before = after;
            after = this.reservations.get(position).higher(after);
        }
    }
}
