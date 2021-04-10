package event;

import agent.Mobile;
import agent.Truck;
import junit.framework.TestCase;
import util.Pair;
import warehouse.Configuration;
import warehouse.Mission;
import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;

public class MobileMissionStartEventTest extends TestCase {

    private Configuration configuration;
    private Mobile mobile;
    private Truck truckLoad, truckUnload;
    private ArrayList<Position> loadPalletPositions, unloadPalletPositions;

    public void setUp() throws Exception {
        super.setUp();

        this.configuration = new Configuration(1, 1);
        this.mobile = this.configuration.mobiles.get(0);

        ArrayList<Pair<Position, Pallet>> toLoad = new ArrayList<>();
        ArrayList<Pair<Position, Pallet>> toUnload = new ArrayList<>();
        this.loadPalletPositions = new ArrayList<>();
        this.unloadPalletPositions = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            toLoad.add(new Pair<>(
                    new Position(0, i * this.configuration.palletSize),
                    new Pallet(i)
            ));
            this.configuration.stock.add(new Position(0, i * this.configuration.palletSize), new Pallet(i)); // add in stock
            this.loadPalletPositions.add(new Position(0, i * this.configuration.palletSize));
            toUnload.add(new Pair<>(
                    new Position(0, i * this.configuration.palletSize),
                    new Pallet(i)
            ));
            this.configuration.stock.lock(new Position(3 * this.configuration.palletSize, i * this.configuration.palletSize));
            this.unloadPalletPositions.add(new Position(3 * this.configuration.palletSize, i * this.configuration.palletSize));
        }

        this.truckLoad = new Truck(new Position(0, 0), toLoad, new ArrayList<>());
        this.truckUnload = new Truck(new Position(10, 0), new ArrayList<>(), toUnload);
    }

    public void testSetTargetPosition() {
        Pallet pallet = this.truckLoad.getToLoad().get(0).second;
        Position startPosition = this.loadPalletPositions.get(0);
        Position endPosition = this.truckLoad.getPosition();
        Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);
        MobileMissionStartEvent event = new MobileMissionStartEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertFalse(startPosition.equals(this.mobile.getTargetPosition()));
        event.run();
        assertTrue(startPosition.equals(this.mobile.getTargetPosition()));
    }

    public void testSetMission() {
        Pallet pallet = this.truckLoad.getToLoad().get(0).second;
        Position startPosition = this.loadPalletPositions.get(0);
        Position endPosition = this.truckLoad.getPosition();
        Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);
        MobileMissionStartEvent event = new MobileMissionStartEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertNull(this.mobile.getMission());
        event.run();
        assertEquals(mission, this.mobile.getMission());
    }

    public void testTriggerPathFinderEvent() {
        Pallet pallet = this.truckUnload.getToUnload().get(0).second;
        Position startPosition = this.truckUnload.getPosition();
        Position endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(0, pallet, this.truckUnload, null, startPosition, endPosition);
        MobileMissionStartEvent event = new MobileMissionStartEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertEquals(0, this.configuration.simulation.queueSize());
        event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof PathFinderEvent);
    }

}