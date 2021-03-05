package brain;

import warehouse.Pallet;
import warehouse.Position;
import warehouse.Warehouse;

import java.util.ArrayList;

public class ClosestPositionSelector implements PalletPositionSelector {

    private final Warehouse warehouse;

    public ClosestPositionSelector(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    @Override
    public Position selectStartPosition(Pallet pallet, Position endPosition, ArrayList<Position> positions) {
        double shortestDist = Double.MAX_VALUE;
        Position closestPosition = null;

        for (Position position : positions) {
            double dist = this.warehouse.getDistance(position, endPosition);
            if (dist < shortestDist) {
                shortestDist = dist;
                closestPosition = position;
            }
        }

        return closestPosition;
    }

    @Override
    public Position selectEndPosition(Pallet pallet, Position startPosition, ArrayList<Position> positions) {
        double shortestDist = Double.MAX_VALUE;
        Position closestPosition = null;

        for (Position position : positions) {
            double dist = this.warehouse.getDistance(startPosition, position);
            if (dist < shortestDist) {
                shortestDist = dist;
                closestPosition = position;
            }
        }

        return closestPosition;
    }

}
