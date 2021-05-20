package event;

import agent.Dock;
import agent.Truck;
import junit.framework.TestCase;
import util.Pair;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;
import java.util.HashMap;

public class TruckCallEventTest extends TestCase {

    private Configuration configuration;
    private Dock dock;
    private Truck truck;
    private TruckCallEvent event;

    public void setUp() throws Exception {
        super.setUp();

        this.configuration = new Configuration(5, 1);
        this.dock = this.configuration.docks.get(0);

        HashMap<Position, Pallet> toLoad = new HashMap<>();
        HashMap<Position, Pallet> toUnload = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            toUnload.put(
                    new Position(0, i * this.configuration.palletSize),
                    new Pallet(i)
            );
        }

        this.truck = new Truck(new Position(0, 0), toLoad, toUnload);
        this.event = new TruckCallEvent(this.configuration.simulation, 1, this.configuration.controller, this.dock, this.truck);
    }

    public void testDockSet() {
        assertNull(this.truck.getDock());
        this.event.run();
        assertEquals(this.dock, this.truck.getDock());
    }

    public void testSetTargetPosition() {
        assertFalse(this.truck.getTargetPosition().equals(this.dock.getPosition()));
        this.event.run();
        assertTrue(this.truck.getTargetPosition().equals(this.dock.getPosition()));
    }

    public void testTriggerTruckDockEvent() {
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof TruckDockEvent);
    }
}