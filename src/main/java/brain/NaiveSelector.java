package brain;

import agent.Dock;
import agent.Mobile;
import agent.Truck;
import util.MobileMission;
import util.TruckDock;
import warehouse.Mission;
import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;

public class NaiveSelector implements MobileMissionSelector, PalletPositionSelector, TruckDockSelector {

    @Override
    public ArrayList<MobileMission> matchMobileMission(ArrayList<Mobile> mobiles, ArrayList<Mission> missions) {
        ArrayList<MobileMission> pairs = new ArrayList<MobileMission>();
        for (int i=0; i<Math.min(mobiles.size(), missions.size()); i++) {
            pairs.add(new MobileMission(mobiles.get(i), missions.get(i)));
        }
        return pairs;
    }

    @Override
    public Position selectStartPosition(Pallet pallet, ArrayList<Position> positions) {
        return positions.get(0);
    }

    @Override
    public Position selectEndPosition(Pallet pallet, ArrayList<Position> positions) {
        return positions.get(0);
    }

    @Override
    public ArrayList<TruckDock> matchTruckDock(ArrayList<Truck> trucks, ArrayList<Dock> docks) {
        ArrayList<TruckDock> pairs = new ArrayList<TruckDock>();
        for (int i=0; i<Math.min(trucks.size(), docks.size()); i++) {
            pairs.add(new TruckDock(trucks.get(i), docks.get(i)));
        }
        return pairs;
    }

}
