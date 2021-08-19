package graph;

import agent.Lift;
import util.DoublePrecisionConstraint;
import util.Vector2D;
import util.Vector3D;

import java.util.TreeSet;

public class LiftConstraint extends GraphConstraint {

    private final int height;

    public LiftConstraint(Vector3D[] positions) {
        super(positions);
        this.height = positions.length;
    }

    @Override
    public Reservation firstConflictWithMargin(ReservationTable reservationTable, Vector3D position, double start, double end, int id) {
        TreeSet<Reservation> conflicts = new TreeSet<>();

        for (int i = 0; i < this.height; i++) {
            Vector2D dist2D = position.manhattanDistance3D(this.positions[i]);
            Reservation conflict = reservationTable.firstConflictWithMarginHelper(
                    this.positions[i],
                    DoublePrecisionConstraint.round(start - dist2D.getY() * Lift.speed),
                    DoublePrecisionConstraint.round(end + dist2D.getY() * Lift.speed),
                    id
            );
            if (conflict != null) conflicts.add(conflict);
        }

        if (conflicts.isEmpty()) return null;
        else return conflicts.first();
    }

    /*@Override
    public void reserveWithMargin(ReservationTable reservationTable, Vector3D position, double start, double end, int id) {
        for (int i = 0; i < this.height; i++) {
            Vector2D dist2D = position.manhattanDistance3D(this.positions[i]);
            reservationTable.reserveWithMarginHelper(this.positions[i], start, end + dist2D.getY() * Lift.speed, id);
        }
    }*/

    @Override
    public double nextAvailabilityWithMargin(ReservationTable reservationTable, Vector3D position, double from, double duration, int id) {
        boolean done = false;
        while (!done) {
            done = true;
            for (int i = 0; i < this.height; i++) {
                Vector2D dist2D = position.manhattanDistance3D(this.positions[i]);
                double time = DoublePrecisionConstraint.round(
                        dist2D.getY() * Lift.speed + reservationTable.nextAvailabilityWithMarginHelper(
                                this.positions[i],
                                DoublePrecisionConstraint.round(from - dist2D.getY() * Lift.speed),
                                DoublePrecisionConstraint.round(duration + 2 * dist2D.getY() * Lift.speed),
                                id
                        )
                );
                if (time > from) {
                    done = false;
                    from = time;
                }
            }
        }
        return from;
    }
}
