package event;

import agent.Mobile;
import agent.Truck;
import brain.NaiveSelector;
import junit.framework.TestCase;
import util.Configuration;
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

        NaiveSelector selector = new NaiveSelector();
        this.configuration = new Configuration(1, 1, selector, selector, selector);
        this.mobile = this.configuration.mobiles.get(0);
        this.configuration.controller.remove(this.mobile); //  mark as used

        ArrayList<Pallet> toLoad = new ArrayList<>();
        ArrayList<Pallet> toUnload = new ArrayList<>();
        this.loadPalletPositions = new ArrayList<>();
        this.unloadPalletPositions = new ArrayList<>();

        for (int i=0; i<5; i++) {
            toLoad.add(new Pallet(i));
            this.configuration.stock.add(new Position(5, i), new Pallet(i)); // add in stock
            this.loadPalletPositions.add(new Position(5, i));
            toUnload.add(new Pallet(i));
            this.configuration.stock.add(new Position(7, i), Pallet.RESERVED);
            this.unloadPalletPositions.add(new Position(7, i));
        }

        this.truckLoad = new Truck(new Position(0, 0), toLoad, new ArrayList<>());
        this.truckUnload = new Truck(new Position(10, 0), new ArrayList<>(), toUnload);


    }

    public void testAddPalletToStock() {
        Pallet pallet = this.truckUnload.getToUnload().get(0);
        Position startPosition = this.truckUnload.getPosition();
        Position endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(pallet, truckUnload, null, startPosition, endPosition);
        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertEquals(Pallet.RESERVED, this.configuration.stock.get(endPosition));
        event.run();
        assertEquals(pallet.getType(), this.configuration.stock.get(endPosition).getType());
    }

    public void testAddMobileToController() {
        Pallet pallet = this.truckUnload.getToUnload().get(0);
        Position startPosition = this.truckUnload.getPosition();
        Position endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(pallet, truckUnload, null, startPosition, endPosition);
        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertEquals(0, this.configuration.controller.getMobiles().size());
        event.run();
        assertEquals(1, this.configuration.controller.getMobiles().size());
        assertEquals(this.mobile, this.configuration.controller.getMobiles().get(0));
    }

    public void testDoneWhenLoaded() {
        for (int i=0; i<this.truckLoad.getToLoad().size(); i++) {
            Pallet pallet = this.truckLoad.getToLoad().get(i);
            Position startPosition = this.loadPalletPositions.get(i);
            Position endPosition = this.truckLoad.getPosition();
            Mission mission = new Mission(pallet, null, this.truckLoad, startPosition, endPosition);
            MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

            assertFalse(this.truckLoad.done());
            event.run();
        }

        assertTrue(this.truckLoad.done());
        assertEquals(2, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof TruckDoneEvent);
    }

    public void testTriggerControllerEvent() {
        Pallet pallet = this.truckUnload.getToUnload().get(0);
        Position startPosition = this.truckUnload.getPosition();
        Position endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(pallet, this.truckUnload, null, startPosition, endPosition);
        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertEquals(0, this.configuration.simulation.queueSize());
        event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof ControllerEvent);
    }

}