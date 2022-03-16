package pathfinding;

import agent.Mobile;

public class SimpleCollisionDetection implements CollisionDetection<Mobile, Mobile> {

    private static final int THRESHOLD = 5;

    @Override
    public boolean collides(Action a1, Action a2) {
        if (a1.startPosition() == a2.startPosition() && Math.abs(a1.startTime() - a2.startTime()) < THRESHOLD) {
            return true;
        }
        if (a1.startPosition() == a2.endPosition() && Math.abs(a1.startTime() - a2.endTime()) < THRESHOLD) {
            return true;
        }
        if (a1.endPosition() == a2.startPosition() && Math.abs(a1.endTime() - a2.startTime()) < THRESHOLD) {
            return true;
        }
        return a1.endPosition() == a2.endPosition() && Math.abs(a1.endTime() - a2.endTime()) < THRESHOLD;
    }

}
