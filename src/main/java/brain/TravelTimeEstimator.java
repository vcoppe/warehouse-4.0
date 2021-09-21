package brain;

import util.Vector3D;

public interface TravelTimeEstimator {

    double estimate(Vector3D startPosition, Vector3D endPosition);

}
