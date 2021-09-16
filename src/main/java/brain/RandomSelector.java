package brain;

import agent.Dock;
import agent.Mobile;
import agent.Truck;
import util.Pair;
import util.Vector3D;
import warehouse.Mission;
import warehouse.Pallet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class RandomSelector implements PalletPositionSelector, MobileMissionSelector, TruckDockSelector {

    private final Random random;

    public RandomSelector() {
        this.random = new Random(0);
    }

    @Override
    public Vector3D selectStartPosition(Pallet pallet, Vector3D endPosition, ArrayList<Vector3D> positions) {
        return positions.get(this.random.nextInt(positions.size()));
    }

    @Override
    public Vector3D selectEndPosition(Pallet pallet, Vector3D startPosition, ArrayList<Vector3D> positions) {
        return positions.get(this.random.nextInt(positions.size()));
    }

    @Override
    public ArrayList<Pair<Mobile, Mission>> matchMobileMission(double time, ArrayList<Mobile> mobiles, ArrayList<Mission> missions) {
        ArrayList<Mobile> shuffledMobiles = new ArrayList<>(mobiles);
        ArrayList<Mission> shuffledMissions = new ArrayList<>(missions);

        Collections.shuffle(shuffledMobiles, this.random);
        Collections.shuffle(shuffledMissions, this.random);

        ArrayList<Pair<Mobile, Mission>> pairs = new ArrayList<>();

        for (int i = 0; i < Math.min(mobiles.size(), missions.size()); i++) {
            pairs.add(new Pair<>(shuffledMobiles.get(i), shuffledMissions.get(i)));
        }

        return pairs;
    }

    @Override
    public ArrayList<Pair<Truck, Dock>> matchTruckDock(ArrayList<Truck> trucks, ArrayList<Dock> docks) {
        ArrayList<Truck> shuffledTrucks = new ArrayList<>(trucks);
        ArrayList<Dock> shuffledDocks = new ArrayList<>(docks);

        Collections.shuffle(shuffledTrucks, this.random);
        Collections.shuffle(shuffledDocks, this.random);

        ArrayList<Pair<Truck, Dock>> pairs = new ArrayList<>();

        for (int i = 0; i < Math.min(trucks.size(), docks.size()); i++) {
            pairs.add(new Pair<>(shuffledTrucks.get(i), shuffledDocks.get(i)));
        }

        return pairs;
    }
}
