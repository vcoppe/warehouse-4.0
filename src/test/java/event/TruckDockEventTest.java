package event;

import agent.Dock;
import agent.Truck;
import junit.framework.TestCase;
import util.Vector3D;
import warehouse.Configuration;
import warehouse.Mission;
import warehouse.Pallet;

import java.util.HashMap;
import java.util.Map;

public class TruckDockEventTest extends TestCase {

    private Configuration configuration;
    private Dock dock;
    private Truck truck;
    private TruckDockEvent event;

    public void setUp() throws Exception {
        super.setUp();

        this.configuration = new Configuration();
        this.dock = this.configuration.docks.get(0);

        HashMap<Vector3D, Pallet> toLoad = new HashMap<>();
        HashMap<Vector3D, Pallet> toUnload = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            toLoad.put(
                    new Vector3D(0, i * Configuration.palletSize),
                    new Pallet(i)
            );
            this.configuration.stock.add(new Vector3D(0, i * Configuration.palletSize), new Pallet(i)); // add pallets to load in the stock
            toUnload.put(
                    new Vector3D(0, i * Configuration.palletSize),
                    new Pallet(5 + i)
            );
        }

        this.truck = new Truck(new Vector3D(0, this.configuration.warehouse.getDepth() + 10), toLoad, toUnload);
        this.event = new TruckDockEvent(this.configuration.simulation, 1, this.configuration.controller, this.dock, this.truck);

        this.truck.go(1, this.dock);
    }

    public void tearDown() throws Exception {
        super.tearDown();

        this.configuration = null;
        this.dock = null;
        this.truck = null;
        this.event = null;
    }

    public void testSetPosition() {
        assertFalse(this.truck.getPosition().equals(this.dock.getPosition()));
        this.event.run();
        assertTrue(this.truck.getPosition().equals(this.dock.getPosition()));
    }

    public void testGenerateMissions() {
        assertEquals(0, this.configuration.controller.getAllMissions().size());
        this.event.run();
        assertEquals(this.truck.getToLoad().size() + this.truck.getToUnload().size(), this.configuration.controller.getAllMissions().size());

        for (Mission mission : this.configuration.controller.getAllMissions()) {
            if (mission.getStartTruck() != null) { // unload mission
                boolean foundPallet = false;
                for (Map.Entry<Vector3D, Pallet> entry : this.truck.getToUnload().entrySet()) {
                    Vector3D position = entry.getKey();
                    Pallet pallet = entry.getValue();
                    if (position.add(this.truck.getPosition()).equals(mission.getStartPosition())) {
                        assertEquals(pallet.getProduct(), mission.getPallet().getProduct());
                        assertEquals(pallet.getId(), mission.getPallet().getId());
                        foundPallet = true;
                        break;
                    }
                }
                assertTrue(foundPallet);
            } else if (mission.getEndTruck() != null) { // load mission
                boolean foundPallet = false;
                for (Map.Entry<Vector3D, Pallet> entry : this.truck.getToLoad().entrySet()) {
                    Vector3D position = entry.getKey();
                    Pallet pallet = entry.getValue();
                    if (position.add(this.truck.getPosition()).equals(mission.getEndPosition())) {
                        assertEquals(pallet.getProduct(), mission.getPallet().getProduct());
                        assertEquals(pallet.getId(), mission.getPallet().getId());
                        foundPallet = true;
                        break;
                    }
                }
                assertTrue(foundPallet);
            } else { // should not happen
                fail();
            }
        }
    }

    public void testTriggerControllerEvent() {
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof ControllerEvent);
    }

}