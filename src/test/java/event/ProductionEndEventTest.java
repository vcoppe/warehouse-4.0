package event;

import agent.ProductionLine;
import agent.Stock;
import junit.framework.TestCase;
import util.Pair;
import util.Vector3D;
import warehouse.Configuration;
import warehouse.Mission;
import warehouse.Pallet;
import warehouse.Production;

import java.util.ArrayList;
import java.util.Comparator;

public class ProductionEndEventTest extends TestCase {

    private Configuration configuration;
    private ProductionLine productionLine;
    private Production production;
    private Stock stock;
    private ProductionEndEvent event;

    public void setUp() throws Exception {
        super.setUp();

        this.configuration = new Configuration();
        this.productionLine = this.configuration.productionLines.get(0);
        this.stock = this.configuration.stock;

        ArrayList<Pair<Pallet, Integer>> in = new ArrayList<>();
        ArrayList<Pair<Pallet, Integer>> out = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            in.add(new Pair<>(new Pallet(i), 1));
            out.add(new Pair<>(new Pallet(3 + i), 1));
            this.stock.add(new Vector3D(0, i * Configuration.palletSize), Pallet.FREE);
        }

        this.production = new Production(this.productionLine, in, out, 10, 1, 250);
        this.productionLine.reserveCapacity(this.production.getCapacity());
        this.event = new ProductionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.productionLine, this.production);
    }

    public void tearDown() throws Exception {
        super.tearDown();

        this.configuration = null;
        this.productionLine = null;
        this.production = null;
        this.stock = null;
        this.event = null;
    }

    public void testAddPalletsToBuffer() {
        for (Vector3D position : this.productionLine.getEndBuffer()) {
            assertEquals(Pallet.FREE, this.stock.get(position));
        }

        this.event.run();

        ArrayList<Pallet> productionPallets = new ArrayList<>();
        for (Pair<Pallet, Integer> pair : this.production.getOut()) {
            Pallet pallet = pair.first;
            int quantity = pair.second;
            for (int i = 0; i < quantity; i++) {
                productionPallets.add(pallet);
            }
        }

        ArrayList<Pallet> bufferPallets = new ArrayList<>();
        for (Vector3D position : this.productionLine.getEndBuffer()) {
            Pallet pallet = this.stock.get(position);
            if (pallet != Pallet.FREE) {
                bufferPallets.add(pallet);
            }
        }

        productionPallets.sort(Comparator.comparingInt(Pallet::getProduct));
        bufferPallets.sort(Comparator.comparingInt(Pallet::getProduct));

        // buffer contain the correct pallets
        for (int i = 0; i < productionPallets.size(); i++) {
            assertEquals(productionPallets.get(i).getProduct(), bufferPallets.get(i).getProduct());
        }
    }

    public void testFreeCapacity() {
        assertEquals(this.productionLine.getCapacity() - this.production.getCapacity(), this.productionLine.getCurrentCapacity());
        this.event.run();
        assertEquals(this.productionLine.getCapacity(), this.productionLine.getCurrentCapacity());
    }

    public void testGenerateMissions() {
        assertEquals(0, this.configuration.controller.getAllMissions().size());
        this.event.run();
        assertEquals(this.production.getOut().size(), this.configuration.controller.getAllMissions().size());

        ArrayList<Pallet> productionPallets = new ArrayList<>();
        for (Pair<Pallet, Integer> pair : this.production.getOut()) {
            Pallet pallet = pair.first;
            int quantity = pair.second;
            for (int i = 0; i < quantity; i++) {
                productionPallets.add(pallet);
            }
        }

        ArrayList<Pallet> missionPallets = new ArrayList<>();
        for (Mission mission : this.configuration.controller.getAllMissions()) {
            assertNull(mission.getStartTruck());
            assertNull(mission.getEndTruck());

            missionPallets.add(mission.getPallet());

            assertEquals(mission.getPallet().getProduct(), this.configuration.stock.get(mission.getStartPosition()).getProduct());
        }

        productionPallets.sort(Comparator.comparingInt(Pallet::getProduct));
        missionPallets.sort(Comparator.comparingInt(Pallet::getProduct));

        // missions contain the correct pallets
        for (int i = 0; i < productionPallets.size(); i++) {
            assertEquals(productionPallets.get(i).getProduct(), missionPallets.get(i).getProduct());
        }
    }

    public void testTriggerControllerEvent() {
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof ControllerEvent);
    }

}