package event;

import agent.Dock;
import agent.Truck;
import brain.NaiveSelector;
import junit.framework.TestCase;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;

public class TruckCallEventTest extends TestCase {

    private Configuration configuration;
    private Dock dock;
    private Truck truck;
    private TruckCallEvent event;

    public void setUp() throws Exception {
        super.setUp();

        NaiveSelector selector = new NaiveSelector();
        this.configuration = new Configuration(5, 1, selector, selector, selector);
        this.dock = this.configuration.docks.get(0);

        ArrayList<Pallet> toLoad = new ArrayList<>();
        ArrayList<Pallet> toUnload = new ArrayList<>();

        for (int i=0; i<5; i++) {
            toUnload.add(new Pallet(i));
        }

        this.truck = new Truck(new Position(0, 0), toLoad, toUnload);
        this.event = new TruckCallEvent(this.configuration.simulation, 1, this.configuration.controller, this.dock, this.truck);
    }

    public void testDockSet() {
        assertNull(this.truck.getDock());
        this.event.run();
        assertEquals(this.dock, this.truck.getDock());
    }

    public void testTriggerTruckDockEvent() {
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof TruckDockEvent);
    }
}