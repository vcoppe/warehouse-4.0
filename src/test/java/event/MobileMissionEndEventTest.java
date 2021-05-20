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

public class MobileMissionEndEventTest extends TestCase {

    private Configuration configuration;
    private Mobile mobile;
    private Truck truckLoad, truckUnload;
    private ArrayList<Pair<Position, Pallet>> toLoad, toUnload;
    private ArrayList<Position> loadPalletPositions, unloadPalletPositions;

    public void setUp() throws Exception {
        super.setUp();

        this.configuration = new Configuration(1, 1);
        this.mobile = this.configuration.mobiles.get(0);
        this.configuration.controller.remove(this.mobile); //  mark as used

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
        this.mobile.pickUp();

        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

        assertFalse(endPosition.equals(this.mobile.getPosition()));
        event.run();
        assertTrue(endPosition.equals(this.mobile.getPosition()));
    }

    public void testAddPalletToStock() {
        Pallet pallet = this.toUnload.get(0).second;
        Position startPosition = this.truckUnload.getPosition();
        Position endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(0, pallet, truckUnload, null, startPosition, endPosition);

        this.mobile.start(mission);
        this.mobile.pickUp();

        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

        assertTrue(this.configuration.stock.isLocked(endPosition));
        event.run();
        assertFalse(this.configuration.stock.isLocked(endPosition));
        assertEquals(pallet.getType(), this.configuration.stock.get(endPosition).getType());
    }

    public void testAddPalletToTruck() {
        Pallet pallet = this.toLoad.get(0).second;
        Position startPosition = this.loadPalletPositions.get(0);
        Position endPosition = this.toLoad.get(0).first.add(this.truckLoad.getPosition());
        Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);

        this.mobile.start(mission);
        this.mobile.pickUp();

        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

        assertEquals(this.loadPalletPositions.size(), this.truckLoad.getToLoad().size());
        assertEquals(0, this.truckLoad.getCurrentLoad().size());
        event.run();
        assertEquals(this.loadPalletPositions.size() - 1, this.truckLoad.getToLoad().size());
        assertEquals(1, this.truckLoad.getCurrentLoad().size());
    }


    public void testAddMobileToController() {
        Pallet pallet = this.toUnload.get(0).second;
        Position startPosition = this.truckUnload.getPosition();
        Position endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(0, pallet, truckUnload, null, startPosition, endPosition);

        this.mobile.start(mission);
        this.mobile.pickUp();

        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

        assertEquals(0, this.configuration.controller.getAvailableMobiles().size());
        event.run();
        assertEquals(1, this.configuration.controller.getAvailableMobiles().size());
        assertEquals(this.mobile, this.configuration.controller.getAvailableMobiles().get(0));
    }

    public void testDoneWhenLoaded() {
        int i = 0;
        while (this.truckLoad.getToLoad().size() > 0) {
            Map.Entry<Position, Pallet> entry = this.truckLoad.getToLoad().entrySet().iterator().next();
            Pallet pallet = entry.getValue();
            Position startPosition = this.loadPalletPositions.get(i);
            Position endPosition = entry.getKey().add(this.truckLoad.getPosition());
            Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);

            this.mobile.start(mission);
            this.mobile.pickUp();

            MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

            assertFalse(this.truckLoad.done());
            event.run();

            i++;
        }

        assertTrue(this.truckLoad.done());
        assertEquals(2, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof TruckDoneEvent);
    }

    public void testTriggerControllerEvent() {
        Pallet pallet = this.toUnload.get(0).second;
        Position startPosition = this.truckUnload.getPosition();
        Position endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(0, pallet, this.truckUnload, null, startPosition, endPosition);

        this.mobile.start(mission);
        this.mobile.pickUp();

        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

        assertEquals(0, this.configuration.simulation.queueSize());
        event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof ControllerEvent);
    }

}