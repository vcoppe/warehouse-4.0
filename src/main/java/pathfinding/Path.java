package pathfinding;

import util.Pair;
import util.Vector3D;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public class Path implements Iterable<Pair<Vector3D, Double>> {

    private final TreeSet<Pair<Vector3D, Double>> path;

    public Path() {
        this.path = new TreeSet<>(Comparator.comparing(Pair::getSecond));
    }

    public void add(Vector3D position, Double time) {
        this.path.add(new Pair<>(position, time));
    }

    public void add(Pair<Vector3D, Double> timedPosition) {
        this.path.add(timedPosition);
    }

    public Pair<Vector3D, Double> getStartTimedPosition() {
        return this.path.first();
    }

    public Pair<Vector3D, Double> getEndTimedPosition() {
        return this.path.last();
    }

    public Vector3D getPositionAt(double time) {
        Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = this.getTimedPositionsAt(time);

        double alpha = (pair.second.second - time) / (pair.second.second - pair.first.second);
        return new Vector3D(
                (int) (alpha * pair.first.first.getX() + (1 - alpha) * pair.second.first.getX()),
                (int) (alpha * pair.first.first.getY() + (1 - alpha) * pair.second.first.getY()),
                (int) (alpha * pair.first.first.getZ() + (1 - alpha) * pair.second.first.getZ())
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

    @Override
    public Iterator<Pair<Vector3D, Double>> iterator() {
        return this.path.iterator();
    }
}
