package event;

import agent.ProductionLine;
import junit.framework.TestCase;
import util.Pair;
import util.Vector3D;
import warehouse.Configuration;
import warehouse.Mission;
import warehouse.Pallet;
import warehouse.Production;

import java.util.ArrayList;
import java.util.Comparator;

public class ProductionInitEventTest extends TestCase {

    private Configuration configuration;
    private ProductionLine productionLine;
    private Production production;
    private ProductionInitEvent event;

    public void setUp() throws Exception {
        super.setUp();

        this.configuration = new Configuration();
        this.productionLine = this.configuration.productionLines.get(0);

        ArrayList<Pair<Pallet, Integer>> in = new ArrayList<>();
        ArrayList<Pair<Pallet, Integer>> out = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            in.add(new Pair<>(new Pallet(i), 1));
            this.configuration.stock.add(new Vector3D(2 * Configuration.palletSize, (3 + i) * Configuration.palletSize), new Pallet(i)); // add to stock
            out.add(new Pair<>(new Pallet(3 + i), 1));
        }

        this.production = new Production(in, out, 10, 1, 250);
        this.event = new ProductionInitEvent(this.configuration.simulation, 1, this.configuration.controller, this.productionLine, this.production);
    }

    public void tearDown() throws Exception {
        super.tearDown();

        this.configuration = null;
        this.productionLine = null;
        this.production = null;
        this.event = null;
    }

    public void testAddProductionToProductionLine() {
        assertEquals(0, this.productionLine.getProductions().size());
        this.event.run();
        assertEquals(1, this.productionLine.getProductions().size());
        assertEquals(this.production, this.productionLine.getProductions().get(0));
    }

    public void testGenerateMissions() {
        assertEquals(0, this.configuration.controller.getAllMissions().size());
        this.event.run();
        assertEquals(this.production.getIn().size(), this.configuration.controller.getAllMissions().size());

        ArrayList<Pallet> productionPallets = new ArrayList<>();
        for (Pair<Pallet, Integer> pair : this.production.getIn()) {
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

            assertEquals(mission.getPallet().getType(), this.configuration.stock.get(mission.getStartPosition()).getType());
            assertTrue(this.configuration.stock.isLocked(mission.getEndPosition()));
        }

        productionPallets.sort(Comparator.comparingInt(Pallet::getType));
        missionPallets.sort(Comparator.comparingInt(Pallet::getType));

        // missions contain the correct pallets
        for (int i = 0; i < productionPallets.size(); i++) {
            assertEquals(productionPallets.get(i).getType(), missionPallets.get(i).getType());
        }
    }

    public void testTriggerControllerEvent() {
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof ControllerEvent);
    }

}