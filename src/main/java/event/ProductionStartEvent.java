package event;

import agent.Controller;
import agent.ProductionLine;
import agent.Stock;
import simulation.Event;
import simulation.Simulation;
import util.Pair;
import util.Vector3D;
import warehouse.Pallet;
import warehouse.Production;

public class ProductionStartEvent extends Event {

    private final Controller controller;
    private final Stock stock;
    private final ProductionLine productionLine;
    private final Production production;

    public ProductionStartEvent(Simulation simulation, double time, Controller controller, ProductionLine productionLine, Production production) {
        super(simulation, time);
        this.controller = controller;
        this.stock = controller.getStock();
        this.productionLine = productionLine;
        this.production = production;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: ProductionStartEvent\n\tproduction %d started",
                        this.time,
                        this.production.getId()));

        for (Pair<Pallet,Integer> pair : production.getIn()) {
            Pallet pallet = pair.first;
            int quantity = pair.second;

            int count = 0;
            for (Vector3D position : this.productionLine.getStartBuffer())
                if (this.stock.isLocked(position)) {
                    if (this.stock.get(position) != null && this.stock.get(position).getProduct() == pallet.getProduct()) {
                        this.stock.remove(position, pallet);
                        count++;
                        if (count == quantity) {
                            break;
                        }
                    }
                }

            if (count < quantity) {
                this.simulation.logger.warning("FAILURE! Missing pallets to start production.");
            }
        }

        double endTime = this.time + this.production.getDuration();
        Event event = new ProductionEndEvent(this.simulation, endTime, this.controller, this.productionLine, this.production);
        this.simulation.enqueueEvent(event);
    }

}
