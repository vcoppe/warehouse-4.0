package event;

import agent.Controller;
import agent.Dock;
import agent.Truck;
import simulation.Event;
import simulation.Simulation;

public class TruckDoneEvent extends Event {

    private final Controller controller;
    private final Dock dock;
    private final Truck truck;

    public TruckDoneEvent(Simulation simulation, double time, Controller controller, Dock dock, Truck truck) {
        super(simulation, time);
        this.controller = controller;
        this.dock = dock;
        this.truck = truck;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: TruckDoneEvent\n\tfinished service of truck at dock %d",
                        this.time,
                        this.dock.getId()));

        Event event = new TruckGoneEvent(this.simulation, this.time + 10, this.controller, this.dock, this.truck);
        this.simulation.enqueueEvent(event);
    }

}
