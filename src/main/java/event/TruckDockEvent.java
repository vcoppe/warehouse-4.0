package event;

import agent.Controller;
import agent.Dock;
import agent.Stock;
import agent.Truck;
import brain.PalletPositionSelector;
import simulation.Event;
import simulation.Simulation;
import warehouse.Mission;
import warehouse.Pallet;
import warehouse.Position;
import warehouse.Warehouse;

import java.util.ArrayList;

public class TruckDockEvent extends Event {

    private final Controller controller;
    private final Warehouse warehouse;
    private final Stock stock;
    private final Truck truck;
    private final Dock dock;

    public TruckDockEvent(Simulation simulation, double time, Controller controller, Warehouse warehouse, Stock stock, Dock dock, Truck truck) {
        super(simulation, time);
        this.controller = controller;
        this.warehouse = warehouse;
        this.stock = stock;
        this.dock = dock;
        this.truck = truck;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f : TruckDockEvent\n\ttruck %d arrived at dock %d",
                    this.simulation.getCurrentTime(),
                    this.truck.getId(),
                    this.dock.getId()));

        this.truck.setPosition(this.dock.getPosition());

        // scan pallets to load and unload
        // TODO add precedence constraint
        for (Pallet pallet : this.truck.getToUnload()) {
            ArrayList<Position> positions = this.stock.getEndPositions(pallet);
            Position endPosition = this.controller.palletPositionSelector.selectEndPosition(pallet, positions);
            Mission mission = new Mission(pallet, this.truck, this.stock, this.dock.getPosition(), endPosition);
            this.controller.addMission(mission);
        }

        for (Pallet pallet : this.truck.getToLoad()) {
            ArrayList<Position> positions = this.stock.getStartPositions(pallet);
            Position startPosition = this.controller.palletPositionSelector.selectStartPosition(pallet, positions);
            Mission mission = new Mission(pallet, this.stock, this.truck, startPosition, this.dock.getPosition());
            this.controller.addMission(mission);
        }

        Event event = new ControllerEvent(this.simulation, this.simulation.getCurrentTime(), this.controller, this.warehouse);
        this.simulation.enqueueEvent(event);
    }

}
