package event;

import agent.ProductionLine;
import agent.Stock;
import brain.NaiveSelector;
import junit.framework.TestCase;
import util.Configuration;
import util.Pair;
import warehouse.Pallet;
import warehouse.Position;
import warehouse.Production;

import java.util.ArrayList;
import java.util.Comparator;

public class ProductionStartEventTest extends TestCase {

    private Configuration configuration;
    private ProductionLine productionLine;
    private Production production;
    private Stock stock;
    private ProductionStartEvent event;

    public void setUp() throws Exception {
        super.setUp();

        NaiveSelector selector = new NaiveSelector();
        this.configuration = new Configuration(1, 1, selector, selector, selector);
        this.productionLine = this.configuration.productionLine;
        this.stock = this.configuration.stock;

        ArrayList<Pair<Pallet,Integer>> in = new ArrayList<>();
        ArrayList<Pair<Pallet,Integer>> out = new ArrayList<>();
        for (int i=0; i<3; i++) {
            in.add(new Pair<>(new Pallet(i), 1));
            this.configuration.stock.add(this.productionLine.getStartBuffer().get(i), new Pallet(i)); // add to stock
            out.add(new Pair<>(new Pallet(3 + i), 1));
        }

        this.production = new Production(in, out, 10, 1, 250);
        this.productionLine.add(this.production);
        this.event = new ProductionStartEvent(this.configuration.simulation, 1, this.configuration.controller, this.production);
    }

    public void testRemovePalletsFromBuffer() {
        ArrayList<Pallet> productionPallets = new ArrayList<>();
        for (Pair<Pallet,Integer> pair : this.production.getIn()) {
            Pallet pallet = pair.first;
            int quantity = pair.second;
            for (int i=0; i<quantity; i++) {
                productionPallets.add(pallet);
            }
        }

        ArrayList<Pallet> bufferPallets = new ArrayList<>();
        for (Position position : this.productionLine.getStartBuffer()) {
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

        this.event.run();

        for (Position position : this.productionLine.getStartBuffer()) {
            assertEquals(Pallet.FREE, this.stock.get(position));
        }
    }

    public void testTriggerProductionEndEvent() {
        assertEquals(0, this.configuration.simulation.queueSize());
        this.event.run();
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof ProductionEndEvent);
    }

}