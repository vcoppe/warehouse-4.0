package event;

import agent.Controller;
import agent.Mobile;
import simulation.Event;
import simulation.Simulation;

public class PathUpdateEvent extends Event {

    private static PathUpdateEvent instance;

    private Controller controller;

    private PathUpdateEvent(Simulation simulation, double time, Controller controller) {
        super(simulation, time, Integer.MIN_VALUE);
        this.controller = controller;
    }

    public static void enqueue(Simulation simulation, double time, Controller controller) {
        if (instance == null) {
            instance = new PathUpdateEvent(simulation, Double.MAX_VALUE, controller);
        }

        instance.simulation.removeEvent(instance);
        instance.time = time;
        instance.simulation.enqueueEvent(instance);
    }

    public static void reset() {
        instance = null;
    }

    @Override
    public void run() {
        this.simulation.logger.info(String.format("Simulation time %f: PathUpdateEvent", this.time));

        // check if some mobiles are done
        for (Mobile mobile : this.controller.getAllMobiles()) {
            if (!mobile.isAvailable()) {
                mobile.forward(this.time - PathFinderEvent.getLastEventTime());
                if (mobile.atPickUp()) {
                    Event event = new MobileMissionPickUpEvent(this.simulation, this.time, this.controller, mobile);
                    this.simulation.enqueueEvent(event);
                } else if (mobile.atDrop()) {
                    Event event = new MobileMissionEndEvent(this.simulation, this.time, this.controller, mobile);
                    this.simulation.enqueueEvent(event);
                }
            }
        }

        PathFinderEvent.enqueue(this.simulation, this.time, this.controller);
    }
}
