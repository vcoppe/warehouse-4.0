package event;

import agent.ProductionLine;
import agent.Stock;
import brain.NaiveSelector;
import junit.framework.TestCase;
import util.Pair;
import warehouse.*;

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

        NaiveSelector selector = new NaiveSelector();
        this.configuration = new Configuration(1, 1);
        this.productionLine = this.configuration.productionLine;
        this.stock = this.configuration.stock;

        ArrayList<Pair<Pallet,Integer>> in = new ArrayList<>();
        ArrayList<Pair<Pallet,Integer>> out = new ArrayList<>();
        for (int i=0; i<3; i++) {
            in.add(new Pair<>(new Pallet(i), 1));
            out.add(new Pair<>(new Pallet(3 + i), 1));
            this.stock.add(new Position(3, i), Pallet.FREE);
        }

        this.production = new Production(in, out, 10, 1, 250);
        this.productionLine.reserveCapacity(this.production.getCapacity());
        this.event = new ProductionEndEvent(this.configuration.simulation, 1, this.configuration.controller, this.production);
    }

    public void testAddPalletsToBuffer() {
        for (Position position : this.productionLine.getEndBuffer()) {
            assertEquals(Pallet.FREE, this.stock.get(position));
        }

        this.event.run();

        ArrayList<Pallet> productionPallets = new ArrayList<>();
        for (Pair<Pallet,Integer> pair : this.production.getOut()) {
            Pallet pallet = pair.first;
            int quantity = pair.second;
            for (int i=0; i<quantity; i++) {
                productionPallets.add(pallet);
            }
        }

        ArrayList<Pallet> bufferPallets = new ArrayList<>();
        for (Position position : this.productionLine.getEndBuffer()) {
            Pallet pallet = this.stock.get(position);
            if (pallet != Pallet.FREE) {
                bufferPallets.add(pallet);
            }
        }

        productionPallets.sort(Comparator.comparingInt(Pallet::getType));
        bufferPallets.sort(Comparator.comparingInt(Pallet::getType));

        // buffer contain the correct pallets
        for (int i=0; i<productionPallets.size(); i++) {
            assertEquals(productionPallets.get(i).getType(), bufferPallets.get(i).getType());
        }
    }

    public void testFreeCapacity() {
        assertEquals(this.productionLine.getCapacity()-this.production.getCapacity(), this.productionLine.getCurrentCapacity());
        this.event.run();
        assertEquals(this.productionLine.getCapacity(), this.productionLine.getCurrentCapacity());
    }

    public void testGenerateMissions() {
        assertEquals(0, this.configuration.controller.getMissions().size());
        this.event.run();
        assertEquals(this.production.getOut().size(), this.configuration.controller.getMissions().size());

        ArrayList<Pallet> productionPallets = new ArrayList<>();
        for (Pair<Pallet,Integer> pair : this.production.getOut()) {
            Pallet pallet = pair.first;
            int quantity = pair.second;
            for (int i=0; i<quantity; i++) {
                productionPallets.add(pallet);
            }
        }

        ArrayList<Pallet> missionPallets = new ArrayList<>();
        for (Mission mission : this.configuration.controller.getMissions()) {
            assertNull(mission.getStartTruck());
            assertNull(mission.getEndTruck());

            missionPallets.add(mission.getPallet());

            assertEquals(mission.getPallet().getType(), this.configuration.stock.get(mission.getStartPosition()).getType());
            assertTrue(this.configuration.stock.isLocked(mission.getEndPosition()));
        }

        productionPallets.sort(Comparator.comparingInt(Pallet::getType));
        missionPallets.sort(Comparator.comparingInt(Pallet::getType));

        // missions contain the correct pallets
        for (int i=0; i<productionPallets.size(); i++) {
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