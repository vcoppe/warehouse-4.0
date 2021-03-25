package event;

import agent.Controller;
import agent.ProductionLine;
import agent.Stock;
import simulation.Event;
import simulation.Simulation;
import util.Pair;
import warehouse.Mission;
import warehouse.Pallet;
import warehouse.Position;
import warehouse.Production;

import java.util.ArrayList;

public class ProductionInitEvent extends Event {

    private final Controller controller;
    private final Stock stock;
    private final ProductionLine productionLine;
    private final Production production;

    public ProductionInitEvent(Simulation simulation, double time, Controller controller, Production production) {
        super(simulation, time);
        this.controller = controller;
        this.stock = controller.getStock();
        this.productionLine = controller.getProductionLine();
        this.production = production;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: ProductionInitEvent\n\tproduction %d initiated",
                        this.time,
                        this.production.getId()));

        for (Pair<Pallet,Integer> pair : this.production.getIn()) {
            Pallet pallet = pair.first;
            int quantity = pair.second;

            for (int i=0; i<quantity; i++) {
                Position endPosition = this.productionLine.getStartBufferPosition();

                if (endPosition == null) {
                    this.simulation.logger.warning("FAILURE! Start buffer of production line is full.");
                    return;
                }

                Position startPosition = this.controller.palletPositionSelector.selectStartPosition(pallet, endPosition, this.stock);

                if (startPosition == null) {
                    this.simulation.logger.warning("FAILURE! Missing pallets to launch production.");
                    return;
                }

                Mission mission = new Mission(this.time, pallet, startPosition, endPosition);
                this.controller.add(mission);
                this.stock.lock(startPosition);
                this.stock.lock(endPosition);
            }
        }

        this.productionLine.add(this.production);

        Event event = new ControllerEvent(this.simulation, this.time, this.controller);
        this.simulation.enqueueEvent(event);
    }

}
