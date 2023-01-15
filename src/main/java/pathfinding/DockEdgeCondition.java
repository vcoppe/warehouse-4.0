package pathfinding;

import agent.Dock;
import agent.Mobile;
import agent.Truck;
import util.Vector3D;
import warehouse.Mission;

public class DockEdgeCondition implements EdgeCondition {

    Dock dock;
    int width, height;
    Edge edge;

    public DockEdgeCondition(Dock dock, int width, int height, Edge edge) {
        this.dock = dock;
        this.width = width;
        this.height = height;
        this.edge = edge;
    }

    private boolean isInside(Vector3D position) {
        Vector3D dockPosition = this.dock.getPosition();
        return position.getX() >= dockPosition.getX() &&
                position.getX() < dockPosition.getX() + this.width &&
                position.getY() >= dockPosition.getY() &&
                position.getY() < dockPosition.getY() + this.height;
    }

    @Override
    public boolean satisfied(Mobile mobile) {
        Vector3D dockPosition = this.dock.getPosition();
        Vector3D mobilePosition = mobile.getPosition(); // start of path
        Truck truck = this.dock.getTruck();

        boolean startedInside = this.isInside(mobilePosition);
        boolean ingoingEdge = !this.isInside(edge.from()) && this.isInside(edge.to());

        if (truck == null) {
            return startedInside && !ingoingEdge;
        }

        Vector3D inTruckPosition = this.edge.to().subtract(dockPosition);
        boolean hasPallet = truck.getCurrentLoad().containsKey(inTruckPosition);

        Mission mission = mobile.getMission();
        if (mission == null || !mission.started()) {
            return startedInside && !ingoingEdge && !hasPallet;
        }

        if (mission.getStartTruck() != truck && mission.getEndTruck() != truck) {
            return startedInside && !ingoingEdge && !hasPallet;
        }

        boolean targetInside = this.isInside(mobile.getTargetPosition());

        /*if (truck.getType() == Truck.Type.SIDES) {
            if (targetInside &&
                    (this.edge.from().getY() != mobile.getTargetPosition().getY() ||
                            this.edge.to().getY() != mobile.getTargetPosition().getY())) {
                return false;
            }
        }*/

        if (!startedInside && !targetInside) {
            return false;
        }

        if (!this.edge.to().equals(mobile.getTargetPosition())) {
            return !hasPallet;
        }

        return true;
    }
}
