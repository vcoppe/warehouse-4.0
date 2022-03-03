package event;

import agent.Mobile;
import agent.Truck;
import junit.framework.TestCase;
import util.Pair;
import util.Vector3D;
import warehouse.Configuration;
import warehouse.Mission;
import warehouse.Pallet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MobileMissionEndEventTest extends TestCase {

    private Configuration configuration;
    private Mobile mobile;
    private Truck truckLoad, truckUnload;
    private ArrayList<Pair<Vector3D, Pallet>> toLoad, toUnload;
    private ArrayList<Vector3D> loadPalletPositions, unloadPalletPositions;

    public void setUp() throws Exception {
        super.setUp();

        this.configuration = new Configuration();
        this.mobile = this.configuration.mobiles.get(0);
        this.configuration.controller.remove(this.mobile); //  mark as used

        this.toLoad = new ArrayList<>();
        this.toUnload = new ArrayList<>();
        HashMap<Vector3D, Pallet> toLoad = new HashMap<>();
        HashMap<Vector3D, Pallet> toUnload = new HashMap<>();
        this.loadPalletPositions = new ArrayList<>();
        this.unloadPalletPositions = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Pair<Vector3D, Pallet> pair = new Pair<>(
                    new Vector3D(0, i * Configuration.palletSize),
                    new Pallet(i)
            );
            toLoad.put(pair.first, pair.second);
            this.toLoad.add(pair);
            this.configuration.stock.add(pair.first, pair.second); // add in stock
            this.loadPalletPositions.add(pair.first);

            toUnload.put(pair.first, pair.second);
            this.toUnload.add(pair);

            pair = new Pair<>(
                    new Vector3D(3 * Configuration.palletSize, i * Configuration.palletSize),
                    new Pallet(i)
            );
            this.configuration.stock.lock(pair.first);
            this.unloadPalletPositions.add(pair.first);
        }

        this.truckLoad = new Truck(new Vector3D(0, this.configuration.warehouse.getDepth()), toLoad, new HashMap<>());
        this.truckUnload = new Truck(new Vector3D(this.configuration.dockWidth, this.configuration.warehouse.getDepth()), new HashMap<>(), toUnload);
    }

    public void tearDown() throws Exception {
        super.tearDown();

        this.configuration = null;
        this.mobile = null;
        this.truckLoad = null;
        this.truckUnload = null;
        this.toLoad = null;
        this.toUnload = null;
        this.loadPalletPositions = null;
        this.unloadPalletPositions = null;
    }

    public void testSetPosition() {
        Pallet pallet = this.toLoad.get(0).second;
        Vector3D startPosition = this.loadPalletPositions.get(0);
        Vector3D endPosition = this.truckLoad.getPosition();
        Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);

        this.mobile.start(mission);
        this.mobile.pickUp();

        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

        assertFalse(endPosition.equals(this.mobile.getPosition()));
        event.run();
        assertTrue(endPosition.equals(this.mobile.getPosition()));
    }

    public void testAddPalletToStock() {
        Pallet pallet = this.toUnload.get(0).second;
        Vector3D startPosition = this.truckUnload.getPosition();
        Vector3D endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(0, pallet, truckUnload, null, startPosition, endPosition);

        this.mobile.start(mission);
        this.mobile.pickUp();

        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

        assertTrue(this.configuration.stock.isLocked(endPosition));
        event.run();
        assertFalse(this.configuration.stock.isLocked(endPosition));
        assertEquals(pallet.getType(), this.configuration.stock.get(endPosition).getType());
    }

    public void testAddPalletToTruck() {
        Pallet pallet = this.toLoad.get(0).second;
        Vector3D startPosition = this.loadPalletPositions.get(0);
        Vector3D endPosition = this.toLoad.get(0).first.add(this.truckLoad.getPosition());
        Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);

        this.mobile.start(mission);
        this.mobile.pickUp();

        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

        assertEquals(this.loadPalletPositions.size(), this.truckLoad.getToLoad().size());
        assertEquals(0, this.truckLoad.getCurrentLoad().size());
        event.run();
        assertEquals(this.loadPalletPositions.size() - 1, this.truckLoad.getToLoad().size());
        assertEquals(1, this.truckLoad.getCurrentLoad().size());
    }


    public void testAddMobileToController() {
        Pallet pallet = this.toUnload.get(0).second;
        Vector3D startPosition = this.truckUnload.getPosition();
        Vector3D endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(0, pallet, truckUnload, null, startPosition, endPosition);

        this.mobile.start(mission);
        this.mobile.pickUp();

        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

        assertEquals(4, this.configuration.controller.getAvailableMobiles().size());
        event.run();
        assertEquals(5, this.configuration.controller.getAvailableMobiles().size());
        assertEquals(this.mobile, this.configuration.controller.getAvailableMobiles().get(4));
    }

    public void testDoneWhenLoaded() {
        int i = 0;
        while (this.truckLoad.getToLoad().size() > 0) {
            Map.Entry<Vector3D, Pallet> entry = this.truckLoad.getToLoad().entrySet().iterator().next();
            Pallet pallet = entry.getValue();
            Vector3D startPosition = this.loadPalletPositions.get(i);
            Vector3D endPosition = entry.getKey().add(this.truckLoad.getPosition());
            Mission mission = new Mission(0, pallet, null, this.truckLoad, startPosition, endPosition);

            this.mobile.start(mission);
            this.mobile.pickUp();

            MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

            assertFalse(this.truckLoad.done());
            event.run();

            i++;
        }

        assertTrue(this.truckLoad.done());
        assertEquals(2, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof TruckDoneEvent);
    }

    public void testTriggerControllerEvent() {
        Pallet pallet = this.toUnload.get(0).second;
        Vector3D startPosition = this.truckUnload.getPosition();
        Vector3D endPosition = this.unloadPalletPositions.get(0);
        Mission mission = new Mission(0, pallet, this.truckUnload, null, startPosition, endPosition);

        this.mobile.start(mission);
        this.mobile.pickUp();

        MobileMissionEndEvent event = new MobileMissionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.mobile);

        assertEquals(0, this.configuration.simulation.queueSize());
        event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof ControllerEvent);
    }

}