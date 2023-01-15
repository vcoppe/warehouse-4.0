package pathfinding;

import agent.Mobile;

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

    public Path computePath(double time, Mobile mobile) {
        Path newPath = this.findPath(time, mobile);
        if (newPath == null) {
            return null;
        }

        this.table.removeAll(mobile.getId());

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

        return newPath;
    }

    protected abstract Path findPath(double time, Mobile mobile);

    public void addGraphConstraint(GraphConstraint constraint) {
        this.table.addGraphConstraint(constraint);
    }

    public void add(Mobile mobile) {
        this.table.reserve(mobile.getPosition(), 0.0, Double.MAX_VALUE, mobile.getId());
    }
}
