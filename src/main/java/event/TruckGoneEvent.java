package event;

import agent.Controller;
import agent.Dock;
import agent.Truck;
import simulation.Event;
import simulation.Simulation;

public class TruckGoneEvent extends Event {

    private final Controller controller;
    private final Dock dock;
    private final Truck truck;

    public TruckGoneEvent(Simulation simulation, double time, Controller controller, Dock dock, Truck truck) {
        super(simulation, time);
        this.controller = controller;
        this.dock = dock;
        this.truck = truck;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: TruckGoneEvent\n\ttruck at dock %d left",
                        this.time,
                        this.dock.getId()));

        this.dock.dismiss(this.time);
        this.controller.add(this.dock);

        Event event = new ControllerEvent(this.simulation, this.time, this.controller);
        this.simulation.enqueueEvent(event);
    }

}
