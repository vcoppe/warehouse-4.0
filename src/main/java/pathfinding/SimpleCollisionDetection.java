package pathfinding;

import agent.Mobile;

public class SimpleCollisionDetection implements CollisionDetection<Mobile, Mobile> {

    private static final int THRESHOLD = 3;

    @Override
    public boolean collides(Action a1, Action a2) {
        if (a1.startPosition().equals(a2.startPosition()) && Math.abs(a1.startTime() - a2.startTime()) < THRESHOLD) {
            return true;
        }
        if (a1.startPosition().equals(a2.endPosition()) && Math.abs(a1.startTime() - a2.endTime()) < THRESHOLD) {
            return true;
        }
        if (a1.endPosition().equals(a2.startPosition()) && Math.abs(a1.endTime() - a2.startTime()) < THRESHOLD) {
            return true;
        }
        if (a1.endPosition().equals(a2.endPosition()) && Math.abs(a1.endTime() - a2.endTime()) < THRESHOLD) {
            return true;
        }
        if (a1.startPosition().equals(a2.endPosition()) && a1.endPosition().equals(a2.startPosition())
                && Math.abs(a1.startTime() - a2.startTime()) < 2 * THRESHOLD && Math.abs(a1.endTime() - a2.endTime()) < 2 * THRESHOLD) {
            return true;
        }
        if (a1.startPosition() == a2.startPosition() && a2.startPosition() == a2.endPosition() &&
                (a1.startTime() >= a2.startTime() && a1.startTime() <= a2.endTime())) {
            return true;
        }
        if (a1.endPosition() == a2.endPosition() && a2.startPosition() == a2.endPosition() &&
                (a1.endTime() >= a2.startTime() && a1.endTime() <= a2.endTime())) {
            return true;
        }
        if (a1.startPosition() == a2.startPosition() && a1.startPosition() == a1.endPosition() &&
                (a2.startTime() >= a1.startTime() && a2.startTime() <= a1.endTime())) {
            return true;
        }
        return a1.startPosition() == a2.endPosition() && a1.startPosition() == a1.endPosition() &&
                (a2.endTime() >= a1.startTime() && a2.endTime() <= a1.endTime());
    }

}
