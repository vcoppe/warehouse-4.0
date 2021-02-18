package event;

import agent.Controller;
import agent.Truck;
import simulation.Event;
import simulation.Simulation;

public class TruckArriveEvent extends Event {

    private final Controller controller;
    private final Truck truck;

    public TruckArriveEvent(Simulation simulation, double time, Controller controller, Truck truck) {
        super(simulation, time);
        this.controller = controller;
        this.truck = truck;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: TruckArriveEvent\n\ttruck %d arrived",
                        this.simulation.getCurrentTime(),
                        this.truck.getId()));

        this.controller.add(this.truck);
        Event event = new ControllerEvent(this.simulation, this.simulation.getCurrentTime(), this.controller);
        this.simulation.enqueueEvent(event);
    }
}

