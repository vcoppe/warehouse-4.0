package pathfinding;

import agent.Lift;
import util.DoublePrecisionConstraint;
import util.Vector2D;
import util.Vector3D;

public class LiftConstraint extends GraphConstraint {

    private final int height;

    public LiftConstraint(Vector3D[] positions) {
        super(positions);
        this.height = positions.length;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean isAvailableWithMargin(ReservationTable reservationTable, Vector3D position, double start, double end, int id) {
        for (int i = 0; i < this.height; i++) {
            Vector2D dist2D = position.manhattanDistance3D(this.positions[i]);
            boolean available = reservationTable.isAvailableWithMarginWithMarginHelper(
                    this.positions[i],
                    DoublePrecisionConstraint.round(start - dist2D.getY() * Lift.speed),
                    DoublePrecisionConstraint.round(end + dist2D.getY() * Lift.speed),
                    id
            );
            if (!available) return false;
        }

        return true;
    }

    @Override
    public void reserveWithMargin(ReservationTable reservationTable, Vector3D position, double start, double end, int id) {

    }

}
