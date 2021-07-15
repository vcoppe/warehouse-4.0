package util;

import agent.Mobile;

import java.util.ArrayList;
import java.util.Arrays;

public class DisjunctionCondition extends Condition {

    private final ArrayList<Condition> conditions;

    public DisjunctionCondition(Condition... conditions) {
        this.conditions = new ArrayList<>(Arrays.asList(conditions));
    }

    public void add(Condition condition) {
        this.conditions.add(condition);
    }

    @Override
    public boolean satisfied() {
        for (Condition condition : this.conditions) {
            if (condition.satisfied()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean satisfied(double time, Mobile mobile) {
        for (Condition condition : this.conditions) {
            if (condition.satisfied(time, mobile)) {
                return true;
            }
        }
        return false;
    }
}
