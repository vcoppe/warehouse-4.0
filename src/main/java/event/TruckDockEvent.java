package event;

import agent.Controller;
import agent.Dock;
import agent.Stock;
import agent.Truck;
import simulation.Event;
import simulation.Simulation;
import util.Pair;
import warehouse.Mission;
import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;

public class TruckDockEvent extends Event {

    private final Controller controller;
    private final Stock stock;
    private final Truck truck;
    private final Dock dock;

    public TruckDockEvent(Simulation simulation, double time, Controller controller, Dock dock, Truck truck) {
        super(simulation, time);
        this.controller = controller;
        this.stock = controller.getStock();
        this.dock = dock;
        this.truck = truck;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: TruckDockEvent\n\ttruck %d arrived at dock %d",
                        this.time,
                        this.truck.getId(),
                        this.dock.getId()));

        this.truck.dock();

        // scan pallets to load and unload
        // TODO add precedence constraint
        for (Pair<Position, Pallet> pair : this.truck.getToUnload()) {
            Position palletPosition = pair.first;
            Pallet pallet = pair.second;

            ArrayList<Position> positions = this.stock.getEndPositions(pallet);

            if (positions.size() == 0) {
                this.simulation.logger.warning("FAILURE! Warehouse is full, cannot handle more pallets.");
                return;
            }

            Position startPosition = this.truck.getPosition().add(palletPosition);
            Position endPosition = this.controller.palletPositionSelector.selectEndPosition(pallet, startPosition, positions);
            Mission mission = new Mission(this.time, pallet, this.truck, null, startPosition, endPosition);
            this.controller.add(mission);
            this.stock.lock(endPosition);
        }

        for (Pair<Position, Pallet> pair : this.truck.getToLoad()) {
            Position palletPosition = pair.first;
            Pallet pallet = pair.second;

            ArrayList<Position> positions = this.stock.getStartPositions(pallet);

            if (positions.size() == 0) {
                this.simulation.logger.warning("FAILURE! Missing pallets to load truck.");
                return;
            }

            Position endPosition = this.truck.getPosition().add(palletPosition);
            Position startPosition = this.controller.palletPositionSelector.selectStartPosition(pallet, endPosition, positions);
            Mission mission = new Mission(this.time, pallet, null, this.truck, startPosition, endPosition);
            this.controller.add(mission);
            this.stock.lock(startPosition);
        }

        Event event = new ControllerEvent(this.simulation, this.time, this.controller);
        this.simulation.enqueueEvent(event);
    }

}
