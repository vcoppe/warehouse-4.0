package util;

import agent.Dock;
import agent.Truck;

public class TruckDock {

    public final Truck truck;
    public final Dock dock;

    public TruckDock(Truck truck, Dock dock) {
        this.truck = truck;
        this.dock = dock;
    }

}
