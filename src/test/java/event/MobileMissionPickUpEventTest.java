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
import java.util.HashMap;
import java.util.Map;

public class MobileMissionPickUpEventTest extends TestCase {

    private Configuration configuration;
    private Mobile mobile;
    private Truck truckLoad, truckUnload;
    private ArrayList<Pair<Position, Pallet>> toLoad, toUnload;
    private ArrayList<Position> loadPalletPositions, unloadPalletPositions;

    public void setUp() throws Exception {
        super.setUp();

        PathFinderEvent.reset();

        this.configuration = new Configuration();
        this.mobile = this.configuration.mobiles.get(0);

        this.toLoad = new ArrayList<>();
        this.toUnload = new ArrayList<>();
        HashMap<Position, Pallet> toLoad = new HashMap<>();
        HashMap<Position, Pallet> toUnload = new HashMap<>();
        this.loadPalletPositions = new ArrayList<>();
        this.unloadPalletPositions = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Pair<Position,Pallet> pair = new Pair<>(
                    new Position(0, i * this.configuration.palletSize),
                    new Pallet(i)
            );
            toLoad.put(pair.first, pair.second);
            this.toLoad.add(pair);
            this.configuration.stock.add(pair.first, pair.second); // add in stock
            this.loadPalletPositions.add(pair.first);

            toUnload.put(pair.first, pair.second);
            this.toUnload.add(pair);

            pair = new Pair<>(
                    new Position(3 * this.configuration.palletSize, i * this.configuration.palletSize),
                    new Pallet(i)
            );
            this.configuration.stock.lock(pair.first);
            this.unloadPalletPositions.add(pair.first);
        }

        this.truckLoad = new Truck(new Position(0, this.configuration.warehouse.getDepth()), toLoad, new HashMap<>());
        this.truckUnload = new Truck(new Position(this.configuration.dockWidth, this.configuration.warehouse.getDepth()), new HashMap<>(), toUnload);
    }

    public void testSetPosition() {
        Pallet pallet = this.toLoad.get(0).second;
        Position startPosition = this.loadPalletPositions.get(0);
        Position endPosition = this.truckLoad.getPosition();
        Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);

        this.mobile.start(mission);

        MobileMissionPickUpEvent event = new MobileMissionPickUpEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

        assertFalse(startPosition.equals(this.mobile.getPosition()));
        event.run();
        assertTrue(startPosition.equals(this.mobile.getPosition()));
    }

    public void testSetTargetPosition() {
        Pallet pallet = this.toLoad.get(0).second;
        Position startPosition = this.loadPalletPositions.get(0);
        Position endPosition = this.truckLoad.getPosition();
        Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);

        this.mobile.start(mission);

        MobileMissionPickUpEvent event = new MobileMissionPickUpEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

        assertFalse(endPosition.equals(this.mobile.getTargetPosition()));
        event.run();
        assertTrue(endPosition.equals(this.mobile.getTargetPosition()));
    }

    public void testRemovePalletFromStock() {
        Pallet pallet = this.toLoad.get(0).second;
        Position startPosition = this.loadPalletPositions.get(0);
        Position endPosition = this.truckLoad.getPosition();
        Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);

        this.mobile.start(mission);

        MobileMissionPickUpEvent event = new MobileMissionPickUpEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

        assertEquals(pallet.getType(), this.configuration.stock.get(startPosition).getType());
        event.run();
        assertEquals(Pallet.FREE, this.configuration.stock.get(startPosition));
    }

    public void testRemovePalletFromTruck() {
        Pallet pallet = this.toUnload.get(0).second;
        Position startPosition = this.toUnload.get(0).first.add(this.truckUnload.getPosition());
        Position endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(0, pallet, this.truckUnload, null, startPosition, endPosition);

        this.mobile.start(mission);

        MobileMissionPickUpEvent event = new MobileMissionPickUpEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

        assertEquals(this.unloadPalletPositions.size(), this.truckUnload.getToUnload().size());
        assertEquals(this.unloadPalletPositions.size(), this.truckUnload.getCurrentLoad().size());
        event.run();
        assertEquals(this.unloadPalletPositions.size() - 1, this.truckUnload.getToUnload().size());
        assertEquals(this.unloadPalletPositions.size() - 1, this.truckUnload.getCurrentLoad().size());
    }

    public void testDoneWhenUnloaded() {
        int i = 0, total = this.truckUnload.getToUnload().size();
        while (this.truckUnload.getToUnload().size() > 0) {
            Map.Entry<Position, Pallet> entry = this.truckUnload.getToUnload().entrySet().iterator().next();
            Pallet pallet = entry.getValue();
            Position startPosition = entry.getKey().add(this.truckUnload.getPosition());
            Position endPosition = this.unloadPalletPositions.get(i);
            Mission mission = new Mission(0, pallet, this.truckUnload, null, startPosition, endPosition);

            this.mobile.start(mission);

            MobileMissionPickUpEvent event = new MobileMissionPickUpEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

            assertFalse(this.truckUnload.done());
            event.run();

            i++;

            if (i == total) {
                assertTrue(this.truckUnload.done());
                assertEquals(3, this.configuration.simulation.queueSize());
                assertTrue(this.configuration.simulation.nextEvent() instanceof TruckDoneEvent);
                assertTrue(this.configuration.simulation.getEventAt(1) instanceof ControllerEvent);
                assertTrue(this.configuration.simulation.getEventAt(2) instanceof PathFinderEvent);
            } else {
                assertEquals(2, this.configuration.simulation.queueSize());
                assertTrue(this.configuration.simulation.nextEvent() instanceof ControllerEvent);
                assertTrue(this.configuration.simulation.getEventAt(1) instanceof PathFinderEvent);
            }
        }
    }

    public void testTriggerPathFinderEvent() {
        Pallet pallet = this.toUnload.get(0).second;
        Position startPosition = this.truckUnload.getPosition();
        Position endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(0, pallet, this.truckUnload, null, startPosition, endPosition);

        this.mobile.start(mission);

        MobileMissionPickUpEvent event = new MobileMissionPickUpEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

        assertEquals(0, this.configuration.simulation.queueSize());
        event.run();
        assertEquals(2, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof ControllerEvent);
        assertTrue(this.configuration.simulation.getEventAt(1) instanceof PathFinderEvent);
    }

}