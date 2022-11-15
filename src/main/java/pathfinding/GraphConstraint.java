package pathfinding;

import util.Vector3D;

public abstract class GraphConstraint {

    protected ReservationTable table;
    protected Vector3D[] positions;

    public GraphConstraint(ReservationTable table, Vector3D[] positions) {
        this.table = table;
        this.positions = positions;
    }

    public Vector3D[] getPositions() {
        return this.positions;
    }

    public abstract void reserve(Vector3D position, double start, double end, int id);

}
