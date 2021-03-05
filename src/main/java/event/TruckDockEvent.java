package event;

import agent.Controller;
import agent.Dock;
import agent.Stock;
import agent.Truck;
import simulation.Event;
import simulation.Simulation;
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

        this.truck.setPosition(this.dock.getPosition());

        // scan pallets to load and unload
        // TODO add precedence constraint
        for (Pallet pallet : this.truck.getToUnload()) {
            ArrayList<Position> positions = this.stock.getEndPositions(pallet);

            if (positions.size() == 0) {
                this.simulation.logger.warning("FAILURE! Warehouse is full, cannot handle more pallets.");
                return;
            }

            Position endPosition = this.controller.palletPositionSelector.selectEndPosition(pallet, this.truck.getPosition(), positions);
            Mission mission = new Mission(this.time, pallet, this.truck, null, this.dock.getPosition(), endPosition);
            this.controller.add(mission);
            this.stock.lock(endPosition);
        }

        for (Pallet pallet : this.truck.getToLoad()) {
            ArrayList<Position> positions = this.stock.getStartPositions(pallet);

            if (positions.size() == 0) {
                this.simulation.logger.warning("FAILURE! Missing pallets to load truck.");
                return;
            }

            Position startPosition = this.controller.palletPositionSelector.selectStartPosition(pallet, this.truck.getPosition(), positions);
            Mission mission = new Mission(this.time, pallet, null, this.truck, startPosition, this.dock.getPosition());
            this.controller.add(mission);
            this.stock.lock(startPosition);
        }

        Event event = new ControllerEvent(this.simulation, this.time, this.controller);
        this.simulation.enqueueEvent(event);
    }

}
