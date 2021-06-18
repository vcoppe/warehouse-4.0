package event;

import agent.Truck;
import junit.framework.TestCase;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;

import java.util.HashMap;

public class TruckArriveEventTest extends TestCase {

    private Configuration configuration;
    private Truck truck;
    private TruckArriveEvent event;

    public void setUp() throws Exception {
        super.setUp();

        this.configuration = new Configuration();

        HashMap<Position, Pallet> toLoad = new HashMap<>();
        HashMap<Position, Pallet> toUnload = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            toUnload.put(
                    new Position(0, i * this.configuration.palletSize),
                    new Pallet(i)
            );
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

    public void testSetArrivalTime() {
        this.event.run();
        assertEquals(this.event.getTime(), this.truck.getArrivalTime());
    }

}