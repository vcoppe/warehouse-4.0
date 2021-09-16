package brain;

import util.Vector3D;
import warehouse.Pallet;
import warehouse.Warehouse;

import java.util.ArrayList;

public class ClosestPositionSelector implements PalletPositionSelector {

    private final Warehouse warehouse;

    public ClosestPositionSelector(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    @Override
    public Vector3D selectStartPosition(Pallet pallet, Vector3D endPosition, ArrayList<Vector3D> positions) {
        double shortestDist = Double.MAX_VALUE;
        Vector3D closestPosition = null;

        for (Vector3D position : positions) {
            double dist = this.warehouse.getDistance(position, endPosition).norm();
            if (dist < shortestDist) {
                shortestDist = dist;
                closestPosition = position;
            }
        }

        return closestPosition;
    }

    @Override
    public Vector3D selectEndPosition(Pallet pallet, Vector3D startPosition, ArrayList<Vector3D> positions) {
        double shortestDist = Double.MAX_VALUE;
        Vector3D closestPosition = null;

        for (Vector3D position : positions) {
            double dist = this.warehouse.getDistance(startPosition, position).norm();
            if (dist < shortestDist) {
                shortestDist = dist;
                closestPosition = position;
            }
        }

        return closestPosition;
    }

}
