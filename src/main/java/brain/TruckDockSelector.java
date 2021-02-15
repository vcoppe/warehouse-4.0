package brain;

import agent.Dock;
import agent.Truck;
import util.TruckDock;

import java.util.ArrayList;

public interface TruckDockSelector {

    ArrayList<TruckDock> matchTruckDock(ArrayList<Truck> trucks, ArrayList<Dock> docks);

}
