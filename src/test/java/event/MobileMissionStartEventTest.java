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
            this.configuration.stock.lock(new Position(7, i));
            this.unloadPalletPositions.add(new Position(7, i));
        }

        this.truckLoad = new Truck(new Position(0, 0), toLoad, new ArrayList<>());
        this.truckUnload = new Truck(new Position(10, 0), new ArrayList<>(), toUnload);
    }

    public void testTriggerMobileMissionPickUpEvent() {
        Pallet pallet = this.truckUnload.getToUnload().get(0);
        Position startPosition = this.truckUnload.getPosition();
        Position endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(pallet, this.truckUnload, null, startPosition, endPosition);
        MobileMissionStartEvent event = new MobileMissionStartEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile, mission);

        assertEquals(0, this.configuration.simulation.queueSize());
        event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof MobileMissionPickUpEvent);
    }

}