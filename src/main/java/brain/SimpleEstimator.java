package brain;

import agent.Lift;
import agent.Mobile;
import util.Vector2D;
import util.Vector3D;
import warehouse.Warehouse;

public class SimpleEstimator implements TravelTimeEstimator {

    private final Warehouse warehouse;

    public SimpleEstimator(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    @Override
    public double estimate(Vector3D startPosition, Vector3D endPosition) {
        Vector2D dist = this.warehouse.getDistance(startPosition, endPosition);
        return dist.getX() * Mobile.getSpeed() + dist.getY() * Lift.speed;
    }

    @Override
    public void update(Vector3D startPosition, Vector3D endPosition, double time) {

    }
}
