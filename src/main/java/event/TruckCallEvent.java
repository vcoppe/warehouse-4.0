package event;

import agent.Controller;
import agent.Dock;
import agent.Truck;
import simulation.Event;
import simulation.Simulation;
import warehouse.Warehouse;

public class TruckCallEvent extends Event {

    private final Controller controller;
    private final Warehouse warehouse;
    private final Dock dock;
    private final Truck truck;

    public TruckCallEvent(Simulation simulation, double time, Controller controller, Dock dock, Truck truck) {
        super(simulation, time);
        this.controller = controller;
        this.warehouse = controller.getWarehouse();
        this.dock = dock;
        this.truck = truck;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: TruckCallEvent\n\ttruck %d called at dock %d",
                        this.simulation.getCurrentTime(),
                        this.truck.getId(),
                        this.dock.getId()));

        this.truck.setDock(this.dock);
        this.truck.setTargetPosition(this.dock.getPosition());

        double endTime = this.simulation.getCurrentTime() + this.warehouse.getDistance(this.truck.getPosition(), this.dock.getPosition());

        Event event = new TruckDockEvent(this.simulation, endTime, this.controller, this.dock, this.truck);
        this.simulation.enqueueEvent(event);
    }

}
