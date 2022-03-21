package pathfinding;

import util.Pair;
import util.Vector3D;

public class Action {

    Pair<Vector3D, Double> from, to;

    public Action(Pair<Vector3D, Double> from, Pair<Vector3D, Double> to) {
        this.from = from;
        this.to = to;
    }

    public double startTime() {
        return this.from.second;
    }

    public Vector3D startPosition() {
        return this.from.first;
    }

    public double endTime() {
        return this.to.second;
    }

    public Vector3D endPosition() {
        return this.to.first;
    }

    public boolean isWaitAction() {
        return this.from.first == this.to.first;
    }

    @Override
    public String toString() {
        return this.startTime() + " " + this.startPosition() + ", " + this.endTime() + " " + this.endPosition();
    }
}
