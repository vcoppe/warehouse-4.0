package event;

import agent.Dock;
import agent.Truck;
import junit.framework.TestCase;
import warehouse.Configuration;

import java.util.HashMap;

public class TruckDoneEventTest extends TestCase {

    private Configuration configuration;
    private Dock dock;
    private Truck truck;
    private TruckDoneEvent event;

    public void setUp() throws Exception {
        super.setUp();

        this.configuration = new Configuration();
        this.dock = this.configuration.docks.get(0);
        this.truck = new Truck(this.dock.getPosition(), new HashMap<>(), new HashMap<>());
        this.truck.go(0, this.dock);
        this.configuration.controller.remove(this.dock); // mark as currently used

        this.event = new TruckDoneEvent(this.configuration.simulation, 1, this.configuration.controller, this.dock, this.truck);
    }

    public void testAddDockToController() {
        assertEquals(0, this.configuration.controller.getDocks().size());
        this.event.run();
        assertEquals(1, this.configuration.controller.getDocks().size());
        assertEquals(this.dock, this.configuration.controller.getDocks().get(0));
    }

    public void testTriggerControllerEvent() {
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof ControllerEvent);
    }

    public void testSetDepartureTime() {
        this.event.run();
        assertEquals(this.event.getTime(), this.truck.getDepartureTime());
    }

}