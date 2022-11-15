package pathfinding;

import agent.Lift;
import util.DoublePrecisionConstraint;
import util.Vector2D;
import util.Vector3D;

public class LiftConstraint extends GraphConstraint {

    private final int height;

    public LiftConstraint(ReservationTable table, Vector3D[] positions) {
        super(table, positions);
        this.height = positions.length;
    }

    @Override
    public void reserve(Vector3D position, double start, double end, int id) {
        for (int i = 0; i < this.height; i++) {
            Vector2D dist2D = position.manhattanDistance3D(this.positions[i]);
            this.table.reserveHelper(
                    this.positions[i],
                    DoublePrecisionConstraint.round(start - dist2D.getY() * Lift.speed),
                    DoublePrecisionConstraint.round(end + dist2D.getY() * Lift.speed),
                    id
            );
        }
    }


}
