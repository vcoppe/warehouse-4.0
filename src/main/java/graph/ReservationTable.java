package graph;

import util.DoublePrecisionConstraint;
import util.Vector3D;

import java.util.HashMap;

public class ReservationTable {

    public final static double timeMargin = 1.5;

    protected final HashMap<Vector3D, ReservationTree> reservations;
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
        for (GraphConstraint constraint : this.constraints.values()) {
            constraint.clear();
        }
    }

    public Reservation reserve(Vector3D position, double time, int id) {
        if (this.constraints.containsKey(position)) {
            this.constraints.get(position).reserveWithMargin(this, position, DoublePrecisionConstraint.round(time - timeMargin), DoublePrecisionConstraint.round(time + timeMargin), id);
        }
        return this.reserveWithMargin(position, DoublePrecisionConstraint.round(time - timeMargin), DoublePrecisionConstraint.round(time + timeMargin), id);
    }

    public Reservation reserve(Vector3D position, double start, double end, int id) {
        if (this.constraints.containsKey(position)) {
            this.constraints.get(position).reserveWithMargin(this, position, DoublePrecisionConstraint.round(start - timeMargin), DoublePrecisionConstraint.round(end + timeMargin), id);
        }
        return this.reserveWithMargin(position, DoublePrecisionConstraint.round(start - timeMargin), DoublePrecisionConstraint.round(end + timeMargin), id);
    }

    private Reservation reserveWithMargin(Vector3D position, double start, double end, int id) {
        return this.reserveWithMarginHelper(position, start, end, id);
    }

    protected Reservation reserveWithMarginHelper(Vector3D position, double start, double end, int id) {
        if (!this.reservations.containsKey(position)) {
            this.reservations.put(position, new ReservationTree());
        }

        Reservation reservation = new Reservation(position, start, end, id);

        this.reservations.get(position).insert(reservation);

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
            return this.constraints.get(position).isAvailableWithMargin(this, position, start, end, id);
        } else {
            return this.isAvailableWithMarginWithMarginHelper(position, start, end, id);
        }
    }

    protected boolean isAvailableWithMarginWithMarginHelper(Vector3D position, double start, double end, int id) {
        if (!this.reservations.containsKey(position)) {
            return true;
        }
        return this.reservations.get(position).isAvailable(new Reservation(position, start, end, id));
    }

}
