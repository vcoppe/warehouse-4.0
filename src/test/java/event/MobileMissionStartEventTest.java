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

public class MobileMissionStartEventTest extends TestCase {

    private Configuration configuration;
    private Mobile mobile;
    private Truck truckLoad, truckUnload;
    private ArrayList<Position> loadPalletPositions, unloadPalletPositions;

    public void setUp() throws Exception {
        super.setUp();

        NaiveSelector selector = new NaiveSelector();
        this.configuration = new Configuration(1, 1, selector, selector, selector);
        this.mobile = this.configuration.mobiles.get(0);

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

        this.truckLoad = new Truck(this.configuration.simulation, new Position(0, 0), toLoad, new ArrayList<>());
        this.truckUnload = new Truck(this.configuration.simulation, new Position(10, 0), new ArrayList<>(), toUnload);
    }

    public void testRemovePalletFromStock() {
        Pallet pallet = this.truckLoad.getToLoad().get(0);
        Position startPosition = this.loadPalletPositions.get(0);
        Position endPosition = this.truckLoad.getPosition();
        Mission mission = new Mission(pallet, null, this.truckLoad, startPosition, endPosition);
        MobileMissionStartEvent event = new MobileMissionStartEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertEquals(pallet.getType(), this.configuration.stock.get(startPosition).getType());
        event.run();
        assertEquals(Pallet.FREE, this.configuration.stock.get(startPosition));
    }

    public void testDoneWhenUnloaded() {
        for (int i=0; i<this.truckUnload.getToUnload().size(); i++) {
            Pallet pallet = this.truckUnload.getToUnload().get(i);
            Position startPosition = this.truckUnload.getPosition();
            Position endPosition = this.unloadPalletPositions.get(i);
            Mission mission = new Mission(pallet, this.truckUnload, null, startPosition, endPosition);
            MobileMissionStartEvent event = new MobileMissionStartEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

            assertFalse(this.truckUnload.done());
            assertEquals(i, this.configuration.simulation.queueSize()); // one MobileMissionEndEvent for each previous start event
            event.run();
        }

        assertTrue(this.truckUnload.done());
        assertEquals(this.truckUnload.getToUnload().size()+1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof TruckDoneEvent);
    }

    public void testTriggerMobileMissionEndEvent() {
        Pallet pallet = this.truckUnload.getToUnload().get(0);
        Position startPosition = this.truckUnload.getPosition();
        Position endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(pallet, this.truckUnload, null, startPosition, endPosition);
        MobileMissionStartEvent event = new MobileMissionStartEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertEquals(0, this.configuration.simulation.queueSize());
        event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof MobileMissionEndEvent);
    }

}