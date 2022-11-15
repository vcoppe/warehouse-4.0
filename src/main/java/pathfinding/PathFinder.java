package pathfinding;

import agent.Mobile;
import util.Pair;
import util.Vector3D;

import java.util.ArrayList;

public abstract class PathFinder {

    protected final Graph graph;
    protected final ReservationTable table;

    public PathFinder(Graph graph, ArrayList<Mobile> mobiles) {
        this.graph = graph;
        this.table = new ReservationTable();
    }

    public ReservationTable getReservationTable() {
        return this.table;
    }

    public void computePath(double time, Mobile mobile) {
        this.table.removeAll(mobile.getId());

        Path newPath = this.findPath(time, mobile);
        if (newPath == null) {
            Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getTimedPositionsAt(time);
        }

        // make reservations that begins when leaving previous position and ends when reaching next position
        for (Action action : newPath.getActions()) {
            if (action.isWaitAction()) {
                this.table.reserve(action.startPosition(), action.startTime(), action.endTime(), mobile.getId());
            } else {
                this.table.reserve(action.startPosition(), action.startTime(), action.endTime(), mobile.getId());
                this.table.reserve(action.endPosition(), action.startTime(), action.endTime(), mobile.getId());
            }
        }

        newPath.truncate(mobile.getTargetPosition());

        mobile.setPath(time, newPath);
    }

    protected abstract Path findPath(double time, Mobile mobile);

    public void addGraphConstraint(GraphConstraint constraint) {
        this.table.addGraphConstraint(constraint);
    }

    public void add(Mobile mobile) {
        this.table.reserve(mobile.getPosition(), 0.0, Double.MAX_VALUE, mobile.getId());
    }
}
