package pathfinding;

import agent.Mobile;

import java.util.ArrayList;
import java.util.Arrays;

public class ConjunctionCondition implements EdgeCondition {

    private final ArrayList<EdgeCondition> conditions;

    public ConjunctionCondition(EdgeCondition... conditions) {
        this.conditions = new ArrayList<>(Arrays.asList(conditions));
    }

    public void add(EdgeCondition condition) {
        this.conditions.add(condition);
    }

    @Override
    public boolean satisfied(Mobile mobile) {
        for (EdgeCondition condition : this.conditions) {
            if (!condition.satisfied(mobile)) {
                return false;
            }
        }
        return true;
    }

}
