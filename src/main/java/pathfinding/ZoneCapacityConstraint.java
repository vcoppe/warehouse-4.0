package pathfinding;

import util.Vector3D;

public class ZoneCapacityConstraint extends GraphConstraint {

    int capacity;
    ReservationTree reservations;

    public ZoneCapacityConstraint(Vector3D[] positions, int capacity) {
        super(positions);
        this.capacity = capacity;
        this.reservations = new ReservationTree();
    }

    @Override
    public void clear() {
        this.reservations = new ReservationTree();
    }

    @Override
    public boolean isAvailableWithMargin(ReservationTable reservationTable, Vector3D position, double start, double end, int id) {
        return this.reservations.allConflictingMobileIds(new Reservation(position, start, end, id)).size() < this.capacity &&
                reservationTable.isAvailableWithMarginWithMarginHelper(position, start, end, id);
    }

    @Override
    public void reserveWithMargin(ReservationTable reservationTable, Vector3D position, double start, double end, int id) {
        this.reservations.insert(new Reservation(position, start, end, id));
    }

}
