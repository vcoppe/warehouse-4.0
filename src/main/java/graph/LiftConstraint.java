package graph;

import agent.Lift;
import util.Vector2D;
import util.Vector3D;

public class LiftConstraint extends GraphConstraint {

    private final int height;

    public LiftConstraint(Vector3D[] positions) {
        super(positions);
        this.height = positions.length;
    }

    @Override
    public boolean isAvailableWithMargin(ReservationTable reservationTable, Vector3D position, double start, double end, int id) {
        for (int i = 0; i < this.height; i++) {
            Vector2D dist2D = position.manhattanDistance3D(this.positions[i]);
            if (!reservationTable.isAvailableWithMarginHelper(this.positions[i], start, end + dist2D.getY() * Lift.speed, id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void reserveWithMargin(ReservationTable reservationTable, Vector3D position, double start, double end, int id) {
        for (int i = 0; i < this.height; i++) {
            Vector2D dist2D = position.manhattanDistance3D(this.positions[i]);
            reservationTable.reserveWithMarginHelper(this.positions[i], start, end + dist2D.getY() * Lift.speed, id);
        }
    }

    @Override
    public double nextAvailabilityWithMargin(ReservationTable reservationTable, Vector3D position, double from, double duration, int id) {
        boolean done = false;
        while (!done) {
            done = true;
            for (int i = 0; i < this.height; i++) {
                Vector2D dist2D = position.manhattanDistance3D(this.positions[i]);
                double time = reservationTable.nextAvailabilityWithMarginHelper(this.positions[i], from, duration + dist2D.getY() * Lift.speed, id);
                if (time > from) {
                    done = false;
                    from = time;
                }
            }
        }
        return from;
    }
}
