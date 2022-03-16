package pathfinding;

import agent.Mobile;

public interface CollisionDetection<A extends Mobile, B extends Mobile> {

    boolean collides(Action a1, Action a2);

}
