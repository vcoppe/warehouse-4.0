package event;

import agent.Truck;
import brain.NaiveSelector;
import junit.framework.TestCase;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;

public class TruckArriveEventTest extends TestCase {

    private Configuration configuration;
    private Truck truck;
    private TruckArriveEvent event;

    public void setUp() throws Exception {
        super.setUp();

        NaiveSelector selector = new NaiveSelector();
        this.configuration = new Configuration(1, 1, selector, selector, selector);

        ArrayList<Pallet> toLoad = new ArrayList<>();
        ArrayList<Pallet> toUnload = new ArrayList<>();

        for (int i=0; i<5; i++) {
            toUnload.add(new Pallet(i));
        }

        this.truck = new Truck(new Position(0, 0), toLoad, toUnload);
        this.event = new TruckArriveEvent(this.configuration.simulation, 1, this.configuration.controller, this.truck);
    }

    public void testDone() {
        assertFalse(this.truck.done());
    }

    public void testAddTruckToController() {
        assertEquals(0, this.configuration.controller.getTrucks().size());
        this.event.run();
        assertEquals(1, this.configuration.controller.getTrucks().size());
        assertEquals(this.truck, this.configuration.controller.getTrucks().get(0));
    }

    public void testTriggerControllerEvent() {
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof ControllerEvent);
    }

}