package util;

import agent.Dock;
import agent.Mobile;
import agent.Truck;
import warehouse.Mission;
import warehouse.Position;

public class DockEdgeCondition extends Condition {

    Dock dock;
    int width, height;

    public DockEdgeCondition(Dock dock, int width, int height) {
        this.dock = dock;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean satisfied(double time, Mobile mobile) {
        Position dockPosition = this.dock.getPosition();
        Position mobilePosition = mobile.getPosition(); // start of path
        if (mobilePosition.getX() >= dockPosition.getX() &&
                mobilePosition.getX() < dockPosition.getX() + width &&
                mobilePosition.getY() >= dockPosition.getY() &&
                mobilePosition.getY() < dockPosition.getY() + height) {
            return true;
        }

        Truck truck = this.dock.getTruck();
        if (truck == null) {
            return false;
        }

        Mission mission = mobile.getMission();
        if (mission == null) {
            return false;
        }

        return mission.getStartTruck() == truck || mission.getEndTruck() == truck;
    }
}
