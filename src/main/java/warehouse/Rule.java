package warehouse;

import util.Vector3D;

import java.util.ArrayList;

public class Rule implements Comparable<Rule> {

    private final int priority;
    private final boolean blocking;
    private final ArrayList<Vector3D> positions;

    public Rule(int priority, boolean blocking, ArrayList<Vector3D> positions) {
        this.priority = priority;
        this.blocking = blocking;
        this.positions = positions;
    }

    public boolean isBlocking() {
        return this.blocking;
    }

    public int getPriority() {
        return this.priority;
    }

    public boolean matches(Pallet pallet) {
        return true;
    }

    public ArrayList<Vector3D> getPositions() {
        return this.positions;
    }

    @Override
    public int compareTo(Rule other) {
        return Integer.compare(this.priority, other.priority);
    }

}
