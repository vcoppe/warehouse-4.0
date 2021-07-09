package event;

import agent.Mobile;
import agent.Truck;
import junit.framework.TestCase;
import util.Pair;
import util.Vector3D;
import warehouse.Configuration;
import warehouse.Mission;
import warehouse.Pallet;

import java.util.ArrayList;
import java.util.HashMap;

public class MobileMissionStartEventTest extends TestCase {

    private Configuration configuration;
    private Mobile mobile;
    private Truck truckLoad, truckUnload;
    private ArrayList<Pair<Vector3D, Pallet>> toLoad, toUnload;
    private ArrayList<Vector3D> loadPalletPositions, unloadPalletPositions;

    public void setUp() throws Exception {
        super.setUp();

        PathFinderEvent.reset();

        this.configuration = new Configuration();
        this.mobile = this.configuration.mobiles.get(0);

        this.toLoad = new ArrayList<>();
        this.toUnload = new ArrayList<>();
        HashMap<Vector3D, Pallet> toLoad = new HashMap<>();
        HashMap<Vector3D, Pallet> toUnload = new HashMap<>();
        this.loadPalletPositions = new ArrayList<>();
        this.unloadPalletPositions = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Pair<Vector3D, Pallet> pair = new Pair<>(
                    new Vector3D(0, i * this.configuration.palletSize),
                    new Pallet(i)
            );
            toLoad.put(pair.first, pair.second);
            this.toLoad.add(pair);
            this.configuration.stock.add(pair.first, pair.second); // add in stock
            this.loadPalletPositions.add(pair.first);

            toUnload.put(pair.first, pair.second);
            this.toUnload.add(pair);

            pair = new Pair<>(
                    new Vector3D(3 * this.configuration.palletSize, i * this.configuration.palletSize),
                    new Pallet(i)
            );
            this.configuration.stock.lock(pair.first);
            this.unloadPalletPositions.add(pair.first);
        }

        this.truckLoad = new Truck(new Vector3D(0, 0), toLoad, new HashMap<>());
        this.truckUnload = new Truck(new Vector3D(10, 0), new HashMap<>(), toUnload);
    }

    public void testSetTargetPosition() {
        Pallet pallet = this.toLoad.get(0).second;
        Vector3D startPosition = this.loadPalletPositions.get(0);
        Vector3D endPosition = this.truckLoad.getPosition();
        Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);
        MobileMissionStartEvent event = new MobileMissionStartEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertFalse(startPosition.equals(this.mobile.getTargetPosition()));
        event.run();
        assertTrue(startPosition.equals(this.mobile.getTargetPosition()));
    }

    public void testSetMission() {
        Pallet pallet = this.toLoad.get(0).second;
        Vector3D startPosition = this.loadPalletPositions.get(0);
        Vector3D endPosition = this.truckLoad.getPosition();
        Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);
        MobileMissionStartEvent event = new MobileMissionStartEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertNull(this.mobile.getMission());
        event.run();
        assertEquals(mission, this.mobile.getMission());
    }

    public void testTriggerPathFinderEvent() {
        Pallet pallet = this.toUnload.get(0).second;
        Vector3D startPosition = this.truckUnload.getPosition();
        Vector3D endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(0, pallet, this.truckUnload, null, startPosition, endPosition);
        MobileMissionStartEvent event = new MobileMissionStartEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertEquals(0, this.configuration.simulation.queueSize());
        event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof PathFinderEvent);
    }

}