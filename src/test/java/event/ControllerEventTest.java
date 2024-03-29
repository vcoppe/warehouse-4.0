package event;

import agent.Controller;
import agent.ProductionLine;
import agent.Stock;
import agent.Truck;
import junit.framework.TestCase;
import util.Pair;
import util.Vector3D;
import warehouse.*;

import java.util.ArrayList;
import java.util.HashMap;

public class ControllerEventTest extends TestCase {

    private Configuration configuration;
    private Controller controller;
    private Stock stock;
    private ProductionLine productionLine;
    private ControllerEvent event;
    private ArrayList<Mission> missions;
    private ArrayList<Truck> trucks;
    private ArrayList<Production> productions;

    public void setUp() throws Exception {
        super.setUp();

        this.configuration = new Configuration();
        this.controller = this.configuration.controller;
        this.stock = this.configuration.stock;
        this.stock.filter.add(new Rule(0, true, pallet -> true, new ArrayList<>(this.stock.getStockPositions())));
        this.productionLine = this.configuration.productionLines.get(0);

        this.event = new ControllerEvent(this.configuration.simulation, 1, this.controller);

        this.missions = new ArrayList<>();
        this.trucks = new ArrayList<>();
        this.productions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            this.missions.add(new Mission(0, new Pallet(0), new Vector3D(0, 0), null));
            this.trucks.add(new Truck(new Vector3D(0, 0), new HashMap<>(), new HashMap<>()));

            ArrayList<Pair<Pallet, Integer>> in = new ArrayList<>();
            in.add(new Pair<>(new Pallet(0), 1));
            this.productions.add(new Production(this.productionLine, in, new ArrayList<>(), 10, 3, 200));
        }
    }

    public void tearDown() throws Exception {
        super.tearDown();

        this.configuration = null;
        this.controller = null;
        this.stock = null;
        this.productionLine = null;
        this.event = null;
        this.missions = null;
        this.trucks = null;
        this.productions = null;
    }

    public void testNothingToDo() {
        assertEquals(5, this.controller.getAvailableMobiles().size());
        assertEquals(5, this.controller.getDocks().size());
        assertEquals(0, this.controller.getAllMissions().size());
        assertEquals(0, this.controller.getTrucks().size());
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(5, this.controller.getAvailableMobiles().size());
        assertEquals(5, this.controller.getDocks().size());
        assertEquals(0, this.controller.getAllMissions().size());
        assertEquals(0, this.controller.getTrucks().size());
        assertEquals(0, this.configuration.simulation.queueSize());
    }

    public void testAssignMissionsToMobilesEnoughMobiles() {
        this.controller.add(this.missions.get(0));
        this.controller.add(this.missions.get(1));

        assertNull(this.missions.get(0).getEndPosition());
        assertNull(this.missions.get(1).getEndPosition());

        assertEquals(5, this.controller.getAvailableMobiles().size());
        assertEquals(5, this.controller.getDocks().size());
        assertEquals(2, this.controller.getAllMissions().size());
        assertEquals(0, this.controller.getTrucks().size());
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();

        assertNotNull(this.missions.get(0).getEndPosition());
        assertNotNull(this.missions.get(1).getEndPosition());

        assertEquals(3, this.controller.getAvailableMobiles().size());
        assertEquals(5, this.controller.getDocks().size());
        assertEquals(0, this.controller.getAllMissions().size());
        assertEquals(0, this.controller.getTrucks().size());
        assertEquals(2, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof MobileMissionStartEvent);
    }

    public void testAssignMissionsToMobilesNotEnoughMobiles() {
        for (Mission mission : this.missions) {
            this.controller.add(mission);
        }

        assertEquals(5, this.controller.getAvailableMobiles().size());
        assertEquals(5, this.controller.getDocks().size());
        assertEquals(10, this.controller.getAllMissions().size());
        assertEquals(0, this.controller.getTrucks().size());
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(0, this.controller.getAvailableMobiles().size());
        assertEquals(5, this.controller.getDocks().size());
        assertEquals(5, this.controller.getAllMissions().size());
        assertEquals(0, this.controller.getTrucks().size());
        assertEquals(5, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof MobileMissionStartEvent);
    }

    public void testAssignTrucksToDocksEnoughDocks() {
        this.controller.add(this.trucks.get(0));
        this.controller.add(this.trucks.get(1));

        assertEquals(5, this.controller.getAvailableMobiles().size());
        assertEquals(5, this.controller.getDocks().size());
        assertEquals(0, this.controller.getAllMissions().size());
        assertEquals(2, this.controller.getTrucks().size());
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(5, this.controller.getAvailableMobiles().size());
        assertEquals(3, this.controller.getDocks().size());
        assertEquals(0, this.controller.getAllMissions().size());
        assertEquals(0, this.controller.getTrucks().size());
        assertEquals(2, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof TruckCallEvent);
    }

    public void testAssignTrucksToDocksNotEnoughDocks() {
        for (Truck truck : this.trucks) {
            this.controller.add(truck);
        }

        assertEquals(5, this.controller.getAvailableMobiles().size());
        assertEquals(5, this.controller.getDocks().size());
        assertEquals(0, this.controller.getAllMissions().size());
        assertEquals(10, this.controller.getTrucks().size());
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(5, this.controller.getAvailableMobiles().size());
        assertEquals(0, this.controller.getDocks().size());
        assertEquals(0, this.controller.getAllMissions().size());
        assertEquals(5, this.controller.getTrucks().size());
        assertEquals(5, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof TruckCallEvent);
    }

    public void testAddProductionToProductionLine() {
        Production production = this.productions.get(0);
        this.controller.add(production);

        for (Pair<Pallet, Integer> pair : production.getIn()) {
            Pallet pallet = pair.first;
            this.stock.add(this.stock.getEndPositions(pallet).get(0), pallet);
        }

        assertEquals(0, this.productionLine.getProductions().size());
        this.event.run();
        assertEquals(1, this.productionLine.getProductions().size());
        assertEquals(production, this.productionLine.getProductions().get(0));
    }

    public void testGenerateMissions() {
        Production production = this.productions.get(0);
        this.controller.add(production);

        for (Pair<Pallet, Integer> pair : production.getIn()) {
            Pallet pallet = pair.first;
            this.stock.add(this.stock.getEndPositions(pallet).get(0), pallet);
        }

        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(production.getIn().size(), this.configuration.simulation.queueSize());
    }

    public void testStartProduction() {
        Production production1 = this.productions.get(0);
        Production production2 = this.productions.get(1);
        this.productionLine.add(production1);
        for (Pair<Pallet, Integer> pair : production1.getIn()) {
            Pallet pallet = pair.first;
            Vector3D position = this.productionLine.getStartBufferPosition();
            this.stock.add(position, pallet);
        }
        this.productionLine.add(production2);
        for (Pair<Pallet, Integer> pair : production2.getIn()) {
            Pallet pallet = pair.first;
            Vector3D position = this.productionLine.getStartBufferPosition();
            this.stock.add(position, pallet);
        }

        assertEquals(2, this.productionLine.getProductions().size());
        assertEquals(this.productionLine.getCapacity(), this.productionLine.getCurrentCapacity());
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(0, this.productionLine.getProductions().size());
        assertEquals(this.productionLine.getCapacity() - production1.getCapacity() - production2.getCapacity(), this.productionLine.getCurrentCapacity());
        assertEquals(2, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof ProductionStartEvent);
    }

    public void testStartProductionNotEnoughComponents() {
        Production production1 = this.productions.get(0);
        Production production2 = this.productions.get(1);
        this.productionLine.add(production1);
        for (Pair<Pallet, Integer> pair : production1.getIn()) {
            Pallet pallet = pair.first;
            Vector3D position = this.productionLine.getStartBufferPosition();
            this.stock.add(position, pallet);
        }
        this.productionLine.add(production2); // don't add components for that production

        assertEquals(2, this.productionLine.getProductions().size());
        assertEquals(this.productionLine.getCapacity(), this.productionLine.getCurrentCapacity());
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(1, this.productionLine.getProductions().size());
        assertEquals(this.productionLine.getCapacity() - production1.getCapacity(), this.productionLine.getCurrentCapacity());
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof ProductionStartEvent);
    }

    public void testStartProductionNotEnoughCapacity() {
        for (Production production : this.productions) {
            this.productionLine.add(production);
            for (Pair<Pallet, Integer> pair : production.getIn()) {
                Pallet pallet = pair.first;
                Vector3D position = this.productionLine.getStartBufferPosition();
                this.stock.add(position, pallet);
            }
        }

        assertEquals(10, this.productionLine.getProductions().size());
        assertEquals(this.productionLine.getCapacity(), this.productionLine.getCurrentCapacity());
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(7, this.productionLine.getProductions().size());
        assertEquals(1, this.productionLine.getCurrentCapacity());
        assertEquals(3, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof ProductionStartEvent);
    }

    public void testNotStartProduction() {
        for (Production production : this.productions) {
            this.productionLine.add(production); // don't add components
        }

        assertEquals(10, this.productionLine.getProductions().size());
        assertEquals(this.productionLine.getCapacity(), this.productionLine.getCurrentCapacity());
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(10, this.productionLine.getProductions().size());
        assertEquals(this.productionLine.getCapacity(), this.productionLine.getCurrentCapacity());
        assertEquals(0, this.configuration.simulation.queueSize());
    }

}