package event;

import agent.Controller;
import agent.ProductionLine;
import agent.Stock;
import simulation.Event;
import simulation.Simulation;
import util.Pair;
import util.Vector3D;
import warehouse.Mission;
import warehouse.Pallet;
import warehouse.Production;

public class ProductionEndEvent extends Event {

    private final Controller controller;
    private final Stock stock;
    private final ProductionLine productionLine;
    private final Production production;

    public ProductionEndEvent(Simulation simulation, double time, Controller controller, ProductionLine productionLine, Production production) {
        super(simulation, time);
        this.controller = controller;
        this.stock = controller.getStock();
        this.productionLine = productionLine;
        this.production = production;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: ProductionEndEvent\n\tproduction %d ended",
                        this.time,
                        this.production.getId()));

        for (Pair<Pallet,Integer> pair : this.production.getOut()) {
            Pallet pallet = pair.first;
            int quantity = pair.second;

            for (int i=0; i<quantity; i++) {
                Vector3D startPosition = this.productionLine.getEndBufferPosition();

                // TODO handle full buffer
                if (startPosition == null) {
                    this.simulation.logger.warning("FAILURE! End buffer of production line is full.");
                    return;
                }

                this.stock.add(startPosition, pallet);

                Mission mission = new Mission(this.time, pallet, startPosition, null);
                this.controller.add(mission);
            }
        }

        this.productionLine.freeCapacity(this.production.getCapacity());

        this.simulation.enqueueEvent(new ControllerEvent(this.simulation, this.time, this.controller));
    }

}
