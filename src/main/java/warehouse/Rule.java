package warehouse;

import java.util.ArrayList;

public abstract class Rule implements Comparable<Rule> {

    private final int priority;
    private final boolean blocking;

    public Rule(int priority, boolean blocking) {
        this.priority = priority;
        this.blocking = blocking;
    }

    public int getPriority() {
        return this.priority;
    }

    public abstract boolean matches(Pallet pallet);

    public abstract ArrayList<Position> positions();

    @Override
    public int compareTo(Rule other) {
        return Integer.compare(this.priority, other.priority);
    }

}
