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

public class MobileMissionEndEventTest extends TestCase {

    private Configuration configuration;
    private Mobile mobile;
    private Truck truckLoad, truckUnload;
    private ArrayList<Position> loadPalletPositions, unloadPalletPositions;

    public void setUp() throws Exception {
        super.setUp();

        this.configuration = new Configuration(1, 1);
        this.mobile = this.configuration.mobiles.get(0);
        this.configuration.controller.remove(this.mobile); //  mark as used

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
        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertFalse(endPosition.equals(this.mobile.getPosition()));
        event.run();
        assertTrue(endPosition.equals(this.mobile.getPosition()));
    }

    public void testAddPalletToStock() {
        Pallet pallet = this.truckUnload.getToUnload().get(0).second;
        Position startPosition = this.truckUnload.getPosition();
        Position endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(0, pallet, truckUnload, null, startPosition, endPosition);
        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertTrue(this.configuration.stock.isLocked(endPosition));
        event.run();
        assertFalse(this.configuration.stock.isLocked(endPosition));
        assertEquals(pallet.getType(), this.configuration.stock.get(endPosition).getType());
    }

    public void testAddPalletToTruck() {
        Pallet pallet = this.truckLoad.getToLoad().get(0).second;
        Position startPosition = this.loadPalletPositions.get(0);
        Position endPosition = this.truckLoad.getToLoad().get(0).first.add(this.truckLoad.getPosition());
        Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);
        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertEquals(this.loadPalletPositions.size(), this.truckLoad.getToLoad().size());
        assertEquals(0, this.truckLoad.getCurrentLoad().size());
        event.run();
        assertEquals(this.loadPalletPositions.size() - 1, this.truckLoad.getToLoad().size());
        assertEquals(1, this.truckLoad.getCurrentLoad().size());
    }


    public void testAddMobileToController() {
        Pallet pallet = this.truckUnload.getToUnload().get(0).second;
        Position startPosition = this.truckUnload.getPosition();
        Position endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(0, pallet, truckUnload, null, startPosition, endPosition);
        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertEquals(0, this.configuration.controller.getMobiles().size());
        event.run();
        assertEquals(1, this.configuration.controller.getMobiles().size());
        assertEquals(this.mobile, this.configuration.controller.getMobiles().get(0));
    }

    public void testDoneWhenLoaded() {
        int i = 0;
        while (this.truckLoad.getToLoad().size() > 0) {
            Pair<Position, Pallet> pair = this.truckLoad.getToLoad().get(0);
            Pallet pallet = pair.second;
            Position startPosition = this.loadPalletPositions.get(i);
            Position endPosition = pair.first.add(this.truckLoad.getPosition());
            Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);
            MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

            assertFalse(this.truckLoad.done());
            event.run();

            i++;
        }

        assertTrue(this.truckLoad.done());
        assertEquals(2, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof TruckDoneEvent);
    }

    public void testTriggerControllerEvent() {
        Pallet pallet = this.truckUnload.getToUnload().get(0).second;
        Position startPosition = this.truckUnload.getPosition();
        Position endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(0, pallet, this.truckUnload, null, startPosition, endPosition);
        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertEquals(0, this.configuration.simulation.queueSize());
        event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof ControllerEvent);
    }

}