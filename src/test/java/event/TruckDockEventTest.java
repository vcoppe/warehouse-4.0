package event;

import agent.Dock;
import agent.Truck;
import brain.NaiveSelector;
import junit.framework.TestCase;
import util.Configuration;
import warehouse.Mission;
import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;
import java.util.Comparator;

public class TruckDockEventTest extends TestCase {

    private Configuration configuration;
    private Dock dock;
    private Truck truck;
    private TruckDockEvent event;

    public void setUp() throws Exception {
        super.setUp();

        NaiveSelector selector = new NaiveSelector();
        this.configuration = new Configuration(5, 1, selector, selector, selector);
        this.dock = this.configuration.docks.get(0);

        ArrayList<Pallet> toLoad = new ArrayList<>();
        ArrayList<Pallet> toUnload = new ArrayList<>();

        for (int i=0; i<5; i++) {
            toLoad.add(new Pallet(i));
            this.configuration.stock.add(new Position(i, 10), new Pallet(i)); // add pallets to load in the stock
            toUnload.add(new Pallet(5+i));
        }

        this.truck = new Truck(new Position(0, -10), toLoad, toUnload);
        this.event = new TruckDockEvent(this.configuration.simulation, 1, this.configuration.controller, this.dock, this.truck);
    }

    public void testSetPosition() {
        assertFalse(this.truck.getPosition().equals(this.dock.getPosition()));
        this.event.run();
        assertTrue(this.truck.getPosition().equals(this.dock.getPosition()));
    }

    public void testGenerateMissions() {
        assertEquals(0, this.configuration.controller.getMissions().size());
        this.event.run();
        assertEquals(this.truck.getToLoad().size() + this.truck.getToUnload().size(), this.configuration.controller.getMissions().size());

        ArrayList<Pallet> loadMissionPallets = new ArrayList<>();
        ArrayList<Pallet> unloadMissionPallets = new ArrayList<>();

        // missions start or end at dock
        for (Mission mission : this.configuration.controller.getMissions()) {
            if (mission.getStartTruck() != null) { // unload mission
                assertEquals(this.dock.getPosition(), mission.getStartPosition());
                unloadMissionPallets.add(mission.getPallet());
                assertEquals(Pallet.RESERVED, this.configuration.stock.get(mission.getEndPosition())); // reserve spot for pallet
            } else if (mission.getEndTruck() != null) { // load mission
                assertEquals(this.dock.getPosition(), mission.getEndPosition());
                loadMissionPallets.add(mission.getPallet());
            } else { // should not happen
                fail();
            }
        }

        this.truck.getToLoad().sort(Comparator.comparingInt(Pallet::getType));
        this.truck.getToUnload().sort(Comparator.comparingInt(Pallet::getType));
        loadMissionPallets.sort(Comparator.comparingInt(Pallet::getType));
        unloadMissionPallets.sort(Comparator.comparingInt(Pallet::getType));

        // missions contain the correct pallets
        for (int i=0; i<loadMissionPallets.size(); i++) {
            assertEquals(this.truck.getToLoad().get(i).getType(), loadMissionPallets.get(i).getType());
        }
        for (int i=0; i<unloadMissionPallets.size(); i++) {
            assertEquals(this.truck.getToUnload().get(i).getType(), unloadMissionPallets.get(i).getType());
        }
    }

    public void testTriggerControllerEvent() {
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof ControllerEvent);
    }

}