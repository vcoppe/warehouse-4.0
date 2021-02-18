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
                        this.simulation.getCurrentTime(),
                        this.production.getId()));

        for (Pair<Pallet,Integer> pair : this.production.getIn()) {
            Pallet pallet = pair.first;
            int quantity = pair.second;

            ArrayList<Position> positions = this.stock.getStartPositions(pallet);

            if (quantity > positions.size()) {
                this.simulation.logger.warning("FAILURE! Missing pallets to launch production.");
                return;
            }

            for (int i=0; i<quantity; i++) {
                Position startPosition = this.controller.palletPositionSelector.selectStartPosition(pallet, positions);
                positions.remove(startPosition);
                Position endPosition = this.productionLine.getStartBufferPosition();

                if (endPosition == null) {
                    this.simulation.logger.warning("FAILURE! Start buffer of production line is full.");
                    return;
                }

                Mission mission = new Mission(pallet, startPosition, endPosition);
                this.controller.add(mission);
                this.stock.add(endPosition, Pallet.RESERVED);
            }
        }

        this.productionLine.add(this.production);

        Event event = new ControllerEvent(this.simulation, this.simulation.getCurrentTime(), this.controller);
        this.simulation.enqueueEvent(event);
    }

}
