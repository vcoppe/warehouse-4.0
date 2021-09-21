package pathfinding;

import agent.Mobile;

public interface EdgeCondition {

    boolean satisfied(Mobile mobile);

}
