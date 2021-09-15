package util;

import agent.Mobile;

import java.util.ArrayList;
import java.util.Arrays;

public class ConjunctionCondition extends Condition {

    private final ArrayList<Condition> conditions;

    public ConjunctionCondition(Condition... conditions) {
        this.conditions = new ArrayList<>(Arrays.asList(conditions));
    }

    public void add(Condition condition) {
        this.conditions.add(condition);
    }

    @Override
    public boolean satisfied() {
        for (Condition condition : this.conditions) {
            if (!condition.satisfied()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean satisfied(Mobile mobile) {
        for (Condition condition : this.conditions) {
            if (!condition.satisfied(mobile)) {
                return false;
            }
        }
        return true;
    }
}
