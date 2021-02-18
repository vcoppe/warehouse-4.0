package event;

import agent.Controller;
import agent.ProductionLine;
import agent.Stock;
import simulation.Event;
import simulation.Simulation;
import util.Pair;
import warehouse.Pallet;
import warehouse.Position;
import warehouse.Production;

public class ProductionStartEvent extends Event {

    private final Controller controller;
    private final Stock stock;
    private final ProductionLine productionLine;
    private final Production production;

    public ProductionStartEvent(Simulation simulation, double time, Controller controller, Production production) {
        super(simulation, time);
        this.controller = controller;
        this.stock = controller.getStock();
        this.productionLine = controller.getProductionLine();
        this.production = production;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: ProductionStartEvent\n\tproduction %d started",
                        this.simulation.getCurrentTime(),
                        this.production.getId()));

        for (Pair<Pallet,Integer> pair : production.getIn()) {
            Pallet pallet = pair.first;
            int quantity = pair.second;

            int count = 0;
            for (Position position : this.productionLine.getStartBuffer()) {
                if (this.stock.get(position) != null && this.stock.get(position).getType() == pallet.getType()) {
                    this.stock.remove(position, pallet);
                    count++;
                    if (count == quantity) {
                        break;
                    }
                }
            }
        }

        this.productionLine.remove(production);
        this.productionLine.reserveCapacity(production.getCapacity());

        double endTime = this.simulation.getCurrentTime() + this.production.getTime();
        Event event = new ProductionEndEvent(this.simulation, endTime, this.controller, this.production);
        this.simulation.enqueueEvent(event);
    }

}
