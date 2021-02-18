package brain;

import agent.Dock;
import agent.Truck;
import util.Pair;

import java.util.ArrayList;

public interface TruckDockSelector {

    ArrayList<Pair<Truck,Dock>> matchTruckDock(ArrayList<Truck> trucks, ArrayList<Dock> docks);

}
