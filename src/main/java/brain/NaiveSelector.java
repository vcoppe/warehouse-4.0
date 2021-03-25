package brain;

import agent.Dock;
import agent.Mobile;
import agent.Stock;
import agent.Truck;
import util.Pair;
import warehouse.Mission;
import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;

public class NaiveSelector implements MobileMissionSelector, PalletPositionSelector, TruckDockSelector {

    @Override
    public ArrayList<Pair<Mobile, Mission>> matchMobileMission(ArrayList<Mobile> mobiles, ArrayList<Mission> missions) {
        ArrayList<Pair<Mobile, Mission>> pairs = new ArrayList<>();
        for (int i = 0; i < Math.min(mobiles.size(), missions.size()); i++) {
            pairs.add(new Pair<>(mobiles.get(i), missions.get(i)));
        }
        return pairs;
    }

    @Override
    public Position selectStartPosition(Pallet pallet, Position endPosition, Stock stock) {
        return stock.getStartPositions(pallet).get(0);
    }

    @Override
    public Position selectEndPosition(Pallet pallet, Position startPosition, Stock stock) {
        return stock.getEndPositions(pallet).get(0);
    }

    @Override
    public ArrayList<Pair<Truck, Dock>> matchTruckDock(ArrayList<Truck> trucks, ArrayList<Dock> docks) {
        ArrayList<Pair<Truck, Dock>> pairs = new ArrayList<>();
        for (int i = 0; i < Math.min(trucks.size(), docks.size()); i++) {
            pairs.add(new Pair<>(trucks.get(i), docks.get(i)));
        }
        return pairs;
    }

}
