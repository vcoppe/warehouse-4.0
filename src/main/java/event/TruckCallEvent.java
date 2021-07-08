package event;

import agent.Controller;
import agent.Dock;
import agent.Truck;
import simulation.Event;
import simulation.Simulation;

public class TruckCallEvent extends Event {

    private final Controller controller;
    private final Dock dock;
    private final Truck truck;

    public TruckCallEvent(Simulation simulation, double time, Controller controller, Dock dock, Truck truck) {
        super(simulation, time);
        this.controller = controller;
        this.dock = dock;
        this.truck = truck;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: TruckCallEvent\n\ttruck %d called at dock %d",
                        this.time,
                        this.truck.getId(),
                        this.dock.getId()));

        this.dock.call(this.time, this.truck);

        double endTime = this.time + this.truck.getPosition().manhattanDistance2D(this.dock.getPosition()) / Truck.getSpeed();

        Event event = new TruckDockEvent(this.simulation, endTime, this.controller, this.dock, this.truck);
        this.simulation.enqueueEvent(event);
    }

}
