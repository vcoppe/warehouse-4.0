package brain;

import agent.Stock;
import util.Vector3D;
import warehouse.Pallet;
import warehouse.Warehouse;

public class ClosestPositionSelector implements PalletPositionSelector {

    private final Warehouse warehouse;

    public ClosestPositionSelector(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    @Override
    public Vector3D selectStartPosition(Pallet pallet, Vector3D endPosition, Stock stock) {
        double shortestDist = Double.MAX_VALUE;
        Vector3D closestPosition = null;

        for (Vector3D position : stock.getStartPositions(pallet)) {
            double dist = this.warehouse.getDistance(position, endPosition).norm();
            if (dist < shortestDist) {
                shortestDist = dist;
                closestPosition = position;
            }
        }

        return closestPosition;
    }

    @Override
    public Vector3D selectEndPosition(Pallet pallet, Vector3D startPosition, Stock stock) {
        double shortestDist = Double.MAX_VALUE;
        Vector3D closestPosition = null;

        for (Vector3D position : stock.getEndPositions(pallet)) {
            double dist = this.warehouse.getDistance(startPosition, position).norm();
            if (dist < shortestDist) {
                shortestDist = dist;
                closestPosition = position;
            }
        }

        return closestPosition;
    }

}
