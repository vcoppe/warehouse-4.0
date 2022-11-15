package pathfinding;

import util.Vector3D;

public class ZoneConstraint extends GraphConstraint {

    public ZoneConstraint(ReservationTable table, Vector3D[] positions) {
        super(table, positions);
    }

    @Override
    public void reserve(Vector3D position, double start, double end, int id) {

    }
}
