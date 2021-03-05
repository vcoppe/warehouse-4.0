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

public class ProductionEndEvent extends Event {

    private final Controller controller;
    private final Stock stock;
    private final ProductionLine productionLine;
    private final Production production;

    public ProductionEndEvent(Simulation simulation, double time, Controller controller, Production production) {
        super(simulation, time);
        this.controller = controller;
        this.stock = controller.getStock();
        this.productionLine = controller.getProductionLine();
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
                Position startPosition = this.productionLine.getEndBufferPosition();

                if (startPosition == null) {
                    this.simulation.logger.warning("FAILURE! End buffer of production line is full.");
                    return;
                }

                this.stock.add(startPosition, pallet);

                ArrayList<Position> positions = this.stock.getEndPositions(pallet);

                if (positions.size() == 0) {
                    this.simulation.logger.warning("FAILURE! Warehouse is full, cannot handle more pallets.");
                    return;
                }

                Position endPosition = this.controller.palletPositionSelector.selectEndPosition(pallet, startPosition, positions);
                Mission mission = new Mission(this.time, pallet, startPosition, endPosition);
                this.controller.add(mission);
                this.stock.lock(endPosition);
            }
        }

        this.productionLine.freeCapacity(this.production.getCapacity());

        Event event = new ControllerEvent(this.simulation, this.time, this.controller);
        this.simulation.enqueueEvent(event);
    }

}
