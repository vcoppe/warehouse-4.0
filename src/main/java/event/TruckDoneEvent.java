package event;

import agent.Controller;
import agent.Dock;
import simulation.Event;
import simulation.Simulation;
import warehouse.Warehouse;

public class TruckDoneEvent extends Event {

    private final Controller controller;
    private final Warehouse warehouse;
    private final Dock dock;

    public TruckDoneEvent(Simulation simulation, double time, Controller controller, Warehouse warehouse, Dock dock) {
        super(simulation, time);
        this.controller = controller;
        this.warehouse = warehouse;
        this.dock = dock;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f : TruckDoneEvent\n\tfinished service of truck at dock %d",
                        this.simulation.getCurrentTime(),
                        this.dock.getId()));

        this.controller.addDock(this.dock);

        Event event = new ControllerEvent(this.simulation, this.simulation.getCurrentTime(), this.controller, this.warehouse);
        this.simulation.enqueueEvent(event);
    }

}
