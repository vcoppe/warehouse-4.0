package graph;

import util.DoublePrecisionConstraint;
import util.Vector3D;

import java.util.*;

public class ReservationTable {

    public final static double timeMargin = 1.5;

    protected final HashMap<Vector3D, TreeSet<Reservation>> reservations;
    protected final HashMap<Vector3D, GraphConstraint> constraints;

    public ReservationTable() {
        this.reservations = new HashMap<>();
        this.constraints = new HashMap<>();
    }

    public ReservationTable(HashMap<Vector3D, GraphConstraint> constraints) {
        this.reservations = new HashMap<>();
        this.constraints = constraints;
    }

    public void addGraphConstraint(GraphConstraint constraint) {
        for (Vector3D position : constraint.getPositions()) {
            this.constraints.put(position, constraint);
        }
    }

    public void clear() {
        this.reservations.clear();
    }

    public ReservationTable clone() {
        ReservationTable clone = new ReservationTable(this.constraints);
        for (Map.Entry<Vector3D, TreeSet<Reservation>> entry : this.reservations.entrySet()) {
            clone.reservations.put(entry.getKey(), new TreeSet<>(entry.getValue()));
        }
        return clone;
    }

    public ReservationTable clone(double time) {
        double finalTime = time - timeMargin;
        ReservationTable clone = new ReservationTable(this.constraints);
        for (Map.Entry<Vector3D, TreeSet<Reservation>> entry : this.reservations.entrySet()) {
            //TreeSet<Reservation> set = entry.getValue().stream().filter(r -> r.end > finalTime).collect(Collectors.toCollection(TreeSet::new));

            TreeSet<Reservation> set = new TreeSet<>();
            Iterator<Reservation> it = entry.getValue().descendingIterator();
            while (it.hasNext()) {
                Reservation reservation = it.next();
                if (reservation.end > finalTime) {
                    set.add(reservation);
                } else break;
            }
            if (!set.isEmpty()) {
                clone.reservations.put(entry.getKey(), set);
            }
        }
        return clone;
    }

    public Reservation reserve(Vector3D position, double time, int id) {
        return this.reserveWithMargin(position, DoublePrecisionConstraint.round(time - timeMargin), DoublePrecisionConstraint.round(time + timeMargin), id);
    }

    public Reservation reserve(Vector3D position, double start, double end, int id) {
        return this.reserveWithMargin(position, DoublePrecisionConstraint.round(start - timeMargin), DoublePrecisionConstraint.round(end + timeMargin), id);
    }

    private Reservation reserveWithMargin(Vector3D position, double start, double end, int id) {
//        if (this.constraints.containsKey(position)) {
//            this.constraints.get(position).reserveWithMargin(this, position, start, end, id);
//        } else {
        return this.reserveWithMarginHelper(position, start, end, id);
//        }
    }

    protected Reservation reserveWithMarginHelper(Vector3D position, double start, double end, int id) {
        if (!this.reservations.containsKey(position)) {
            this.reservations.put(position, new TreeSet<>());
        }

        Reservation reservation = new Reservation(position, start, end, id);
        ArrayList<Reservation> toDelete = new ArrayList<>();

        // merge with overlapping reservations of same mobile
        for (Reservation other : this.reservations.get(position)) {
            if (other.start > reservation.end) break;
            if (reservation.mobileId == other.mobileId &&
                    reservation.start <= other.end && reservation.end >= other.start) {
                toDelete.add(other);
                reservation.start = Math.min(reservation.start, other.start);
                reservation.end = Math.max(reservation.end, other.end);
            }
        }

        this.reservations.get(position).removeAll(toDelete);
        this.reservations.get(position).add(reservation);

        return reservation;
    }

    public boolean isAvailable(Vector3D position, double time, int id) {
        return this.isAvailableWithMargin(position, DoublePrecisionConstraint.round(time - timeMargin), DoublePrecisionConstraint.round(time + timeMargin), id);
    }

    public boolean isAvailable(Vector3D position, double start, double end, int id) {
        return this.isAvailableWithMargin(position, DoublePrecisionConstraint.round(start - timeMargin), DoublePrecisionConstraint.round(end + timeMargin), id);
    }

    private boolean isAvailableWithMargin(Vector3D position, double start, double end, int id) {
        if (this.constraints.containsKey(position)) {
            return this.constraints.get(position).firstConflictWithMargin(this, position, start, end, id) == null;
        } else {
            return this.isAvailableWithMarginWithMarginHelper(position, start, end, id);
        }
    }

    protected boolean isAvailableWithMarginWithMarginHelper(Vector3D position, double start, double end, int id) {
        return this.firstConflictWithMarginHelper(position, start, end, id) == null;
    }

    public Reservation firstConflict(Vector3D position, double time, int id) {
        return this.firstConflictWithMargin(position, DoublePrecisionConstraint.round(time - timeMargin), DoublePrecisionConstraint.round(time + timeMargin), id);
    }

    public Reservation firstConflict(Vector3D position, double start, double end, int id) {
        return this.firstConflictWithMargin(position, DoublePrecisionConstraint.round(start - timeMargin), DoublePrecisionConstraint.round(end + timeMargin), id);
    }

    private Reservation firstConflictWithMargin(Vector3D position, double start, double end, int id) {
        if (this.constraints.containsKey(position)) {
            return this.constraints.get(position).firstConflictWithMargin(this, position, start, end, id);
        } else {
            return this.firstConflictWithMarginHelper(position, start, end, id);
        }
    }

    protected Reservation firstConflictWithMarginHelper(Vector3D position, double start, double end, int id) {
        if (!this.reservations.containsKey(position)) {
            return null;
        }

        /*Reservation key = new Reservation(start, end, id);
        Reservation before = this.reservations.get(position).floor(key);
        Reservation after = this.reservations.get(position).higher(key);

        if (before != null && before.mobileId != id && (before.start == start || before.end > start)) {
            return false;
        }

        return after == null || after.mobileId == id || !(after.start < end);*/

        for (Reservation reservation : this.reservations.get(position)) {
            if (reservation.start > end) break;
            if (reservation.end > start && reservation.start < end && reservation.mobileId != id) {
                return reservation;
            }
        }

        return null;
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

        double start = from;

        for (Reservation reservation : this.reservations.get(position)) {
            if (reservation.mobileId == id) continue;

            if (reservation.start - start >= duration) {
                return start;
            }

            start = Math.max(start, reservation.end);
        }

        return start;
    }

}
