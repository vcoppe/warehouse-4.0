package warehouse;

import java.util.ArrayList;

public class Rule implements Comparable<Rule> {

    private final int priority;
    private final boolean blocking;
    private final ArrayList<Position> positions;

    public Rule(int priority, boolean blocking, ArrayList<Position> positions) {
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

    public ArrayList<Position> getPositions() {
        return this.positions;
    }

    @Override
    public int compareTo(Rule other) {
        return Integer.compare(this.priority, other.priority);
    }

}
