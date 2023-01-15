package pathfinding;

import util.Vector3D;

import java.util.ArrayList;
import java.util.HashMap;

public class ReservationTable {

    private final HashMap<Vector3D, ReservationTree> reservations;
    private final HashMap<Vector3D, GraphConstraint> constraints;
    private final HashMap<Integer, ArrayList<Reservation>> reservationsById;

    public ReservationTable() {
        this.reservations = new HashMap<>();
        this.constraints = new HashMap<>();
        this.reservationsById = new HashMap<>();
    }

    public void addGraphConstraint(GraphConstraint constraint) {
        for (Vector3D position : constraint.getPositions()) {
            this.constraints.put(position, constraint);
        }
    }

    public void removeAll(int id) {
        if (this.reservationsById.containsKey(id)) {
            ArrayList<Reservation> reservations = this.reservationsById.get(id);
            for (Reservation reservation : reservations) {
                this.reservations.get(reservation.position).remove(reservation);
            }
            this.reservationsById.remove(id);
        }
    }

    public void reserve(Vector3D position, double start, double end, int id) {
        if (this.constraints.containsKey(position)) {
            this.constraints.get(position).reserve(position, start, end, id);
        }

        this.reserveHelper(position, start, end, id);
    }

    protected void reserveHelper(Vector3D position, double start, double end, int id) {
        if (!this.reservations.containsKey(position)) { // init reservation tree for position if needed
            this.reservations.put(position, new ReservationTree());
        }

        Reservation reservation = new Reservation(position, start, end, id);
        this.reservations.get(position).insert(reservation);

        if (!this.reservationsById.containsKey(id)) { // init collection of reservations for id if needed
            this.reservationsById.put(id, new ArrayList<>());
        }

        this.reservationsById.get(id).add(reservation);
    }

    public ArrayList<Interval> getSafeIntervals(Vector3D position, int id) {
        if (!this.reservations.containsKey(position)) {
            ArrayList<Interval> safeIntervals = new ArrayList<>();
            safeIntervals.add(new Interval(-Double.MAX_VALUE, Double.MAX_VALUE));
            return safeIntervals;
        }

        ReservationTree reservationTree = this.reservations.get(position);

        return reservationTree.getSafeIntervals(id);
    }
}
