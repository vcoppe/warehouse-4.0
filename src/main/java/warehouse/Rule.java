package warehouse;

import util.Vector3D;

import java.util.ArrayList;
import java.util.function.Function;

public class Rule implements Comparable<Rule> {

    private final int priority;
    private final boolean blocking;
    private final Function<Pallet, Boolean> matcher;
    private final ArrayList<Vector3D> positions;

    public Rule(int priority, boolean blocking, Function<Pallet, Boolean> matcher, ArrayList<Vector3D> positions) {
        this.priority = priority;
        this.blocking = blocking;
        this.matcher = matcher;
        this.positions = positions;
    }

    public boolean isBlocking() {
        return this.blocking;
    }

    public int getPriority() {
        return this.priority;
    }

    public boolean matches(Pallet pallet) {
        return this.matcher.apply(pallet);
    }

    public ArrayList<Vector3D> getPositions() {
        return this.positions;
    }

    @Override
    public int compareTo(Rule other) {
        return Integer.compare(this.priority, other.priority);
    }

}
