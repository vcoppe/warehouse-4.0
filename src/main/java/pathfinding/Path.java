package pathfinding;

import util.Pair;
import util.Vector3D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public class Path {

    private final TreeSet<Pair<Vector3D, Double>> path;
    private ArrayList<Action> actions;

    public Path() {
        this.path = new TreeSet<>(Comparator.comparing(Pair::getSecond));
        this.actions = null;
    }

    public void add(Vector3D position, Double time) {
        this.path.add(new Pair<>(position, time));
        this.actions = null;
    }

    public void add(Pair<Vector3D, Double> timedPosition) {
        this.path.add(timedPosition);
        this.actions = null;
    }

    public Pair<Vector3D, Double> getStartTimedPosition() {
        return this.path.first();
    }

    public double getStartTime() {
        return this.path.first().second;
    }

    public Vector3D getStartPosition() {
        return this.path.first().first;
    }

    public Pair<Vector3D, Double> getEndTimedPosition() {
        return this.path.last();
    }

    public double getEndTime() {
        return this.path.last().second;
    }

    public Vector3D getEndPosition() {
        return this.path.last().first;
    }

    public Vector3D getPositionAt(double time) {
        Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = this.getTimedPositionsAt(time);

        double alpha = (pair.second.second - time) / (pair.second.second - pair.first.second);
        return new Vector3D(
                (int) Math.round(alpha * pair.first.first.getX() + (1 - alpha) * pair.second.first.getX()),
                (int) Math.round(alpha * pair.first.first.getY() + (1 - alpha) * pair.second.first.getY()),
                (int) Math.round(alpha * pair.first.first.getZ() + (1 - alpha) * pair.second.first.getZ())
        );
    }

    public Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> getTimedPositionsAt(double time) {
        Pair<Vector3D, Double> key = new Pair<>(null, time);
        Pair<Vector3D, Double> previous = this.path.lower(key);
        Pair<Vector3D, Double> next = this.path.ceiling(key);

        if (previous == null && next == null) {
            return null;
        } else if (previous == null) {
            return new Pair<>(
                    new Pair<>(next.first, time - 1.0),
                    new Pair<>(next.first, time)
            );
        } else if (next == null) {
            return new Pair<>(
                    new Pair<>(previous.first, time - 1.0),
                    new Pair<>(previous.first, time)
            );
        } else {
            return new Pair<>(previous, next);
        }
    }

    public int size() {
        return this.path.size();
    }

    public boolean isEmpty() {
        return this.path.isEmpty();
    }

    public double getCost() {
        return this.path.last().second - this.path.first().second;
    }

    public Iterable<Pair<Vector3D, Double>> getTimedPositions() {
        return this.path;
    }

    public Iterable<Action> getActions() {
        if (this.actions == null) {
            this.actions = new ArrayList<>();

            Pair<Vector3D, Double> previous = null;
            for (Pair<Vector3D, Double> current : this.getTimedPositions()) {
                if (previous != null) {
                    this.actions.add(new Action(previous, current));
                }
                previous = current;
            }
        }
        return this.actions;
    }

    public void truncate(Vector3D position) {
        Iterator<Pair<Vector3D, Double>> iterator = this.path.iterator();
        boolean found = false;

        while (iterator.hasNext()) {
            Pair<Vector3D, Double> timedPosition = iterator.next();
            if (timedPosition.first.equals(position) && timedPosition.second < Double.MAX_VALUE) {
                found = true;
            } else if (found) {
                iterator.remove();
            }
        }
    }
}
