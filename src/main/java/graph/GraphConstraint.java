package graph;

import util.Vector3D;

public abstract class GraphConstraint {

    protected Vector3D[] positions;

    public GraphConstraint(Vector3D[] positions) {
        this.positions = positions;
    }

    public Vector3D[] getPositions() {
        return this.positions;
    }

    public abstract void clear();

    public abstract boolean isAvailableWithMargin(ReservationTable reservationTable, Vector3D position, double start, double end, int id);

    public abstract void reserveWithMargin(ReservationTable reservationTable, Vector3D position, double start, double end, int id);

}
