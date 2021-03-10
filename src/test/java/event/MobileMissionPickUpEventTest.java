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

public class MobileMissionPickUpEventTest extends TestCase {

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

        this.truckLoad = new Truck(new Position(0, this.configuration.warehouse.getDepth()), toLoad, new ArrayList<>());
        this.truckUnload = new Truck(new Position(this.configuration.dockWidth, this.configuration.warehouse.getDepth()), new ArrayList<>(), toUnload);
    }

    public void testSetPosition() {
        Pallet pallet = this.truckLoad.getToLoad().get(0).second;
        Position startPosition = this.loadPalletPositions.get(0);
        Position endPosition = this.truckLoad.getPosition();
        Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);
        MobileMissionPickUpEvent event = new MobileMissionPickUpEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);
        this.mobile.start(mission);

        assertFalse(startPosition.equals(this.mobile.getPosition()));
        event.run();
        assertTrue(startPosition.equals(this.mobile.getPosition()));
    }

    public void testSetTargetPosition() {
        Pallet pallet = this.truckLoad.getToLoad().get(0).second;
        Position startPosition = this.loadPalletPositions.get(0);
        Position endPosition = this.truckLoad.getPosition();
        Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);
        MobileMissionPickUpEvent event = new MobileMissionPickUpEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);
        this.mobile.start(mission);

        assertFalse(endPosition.equals(this.mobile.getTargetPosition()));
        event.run();
        assertTrue(endPosition.equals(this.mobile.getTargetPosition()));
    }

    public void testRemovePalletFromStock() {
        Pallet pallet = this.truckLoad.getToLoad().get(0).second;
        Position startPosition = this.loadPalletPositions.get(0);
        Position endPosition = this.truckLoad.getPosition();
        Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);
        MobileMissionPickUpEvent event = new MobileMissionPickUpEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);
        this.mobile.start(mission);

        assertEquals(pallet.getType(), this.configuration.stock.get(startPosition).getType());
        event.run();
        assertEquals(Pallet.FREE, this.configuration.stock.get(startPosition));
    }

    public void testRemovePalletFromTruck() {
        Pallet pallet = this.truckUnload.getToUnload().get(0).second;
        Position startPosition = this.truckUnload.getToUnload().get(0).first.add(this.truckUnload.getPosition());
        Position endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(0, pallet, this.truckUnload, null, startPosition, endPosition);
        MobileMissionPickUpEvent event = new MobileMissionPickUpEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);
        this.mobile.start(mission);

        assertEquals(this.unloadPalletPositions.size(), this.truckUnload.getToUnload().size());
        assertEquals(this.unloadPalletPositions.size(), this.truckUnload.getCurrentLoad().size());
        event.run();
        assertEquals(this.unloadPalletPositions.size() - 1, this.truckUnload.getToUnload().size());
        assertEquals(this.unloadPalletPositions.size() - 1, this.truckUnload.getCurrentLoad().size());
    }

    public void testDoneWhenUnloaded() {
        int i = 0;
        while (this.truckUnload.getToUnload().size() > 0) {
            Pair<Position, Pallet> pair = this.truckUnload.getToUnload().get(0);
            Pallet pallet = pair.second;
            Position startPosition = pair.first.add(this.truckUnload.getPosition());
            Position endPosition = this.unloadPalletPositions.get(i);
            Mission mission = new Mission(0, pallet, this.truckUnload, null, startPosition, endPosition);
            MobileMissionPickUpEvent event = new MobileMissionPickUpEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);
            this.mobile.start(mission);

            assertFalse(this.truckUnload.done());
            assertEquals(i, this.configuration.simulation.queueSize()); // one MobileMissionEndEvent for each previous start event
            event.run();

            i++;
        }

        assertTrue(this.truckUnload.done());
        assertEquals(this.unloadPalletPositions.size() + 1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof TruckDoneEvent);
    }

    public void testTriggerMobileMissionEndEvent() {
        Pallet pallet = this.truckUnload.getToUnload().get(0).second;
        Position startPosition = this.truckUnload.getPosition();
        Position endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(0, pallet, this.truckUnload, null, startPosition, endPosition);
        MobileMissionPickUpEvent event = new MobileMissionPickUpEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);
        this.mobile.start(mission);

        assertEquals(0, this.configuration.simulation.queueSize());
        event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof MobileMissionEndEvent);
    }

}