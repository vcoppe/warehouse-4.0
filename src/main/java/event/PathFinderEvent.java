package event;

import agent.Controller;
import agent.Mobile;
import pathfinding.WHCAStar;
import simulation.Event;
import simulation.Simulation;
import warehouse.Warehouse;

public class PathFinderEvent extends Event {

    private static PathFinderEvent instance;

    private final Controller controller;
    private final Warehouse warehouse;
    private final WHCAStar pathFinder;
    private double lastEventTime;

    private PathFinderEvent(Simulation simulation, double time, Controller controller) {
        super(simulation, time, Integer.MAX_VALUE); // set large id to have PathFinderEvents at the end of each timestep
        this.controller = controller;
        this.warehouse = controller.getWarehouse();
        this.pathFinder = controller.getPathFinder();
    }

    public static void enqueue(Simulation simulation, double time, Controller controller) {
        if (instance == null) {
            instance = new PathFinderEvent(simulation, Double.MAX_VALUE, controller);
        }

        if (instance.time == instance.lastEventTime || time < instance.time) {
            instance.simulation.removeEvent(instance);
            instance.time = time;
            instance.simulation.enqueueEvent(instance);
        }
    }

    public static double getLastEventTime() {
        if (instance == null) {
            return 0;
        } else {
            return instance.lastEventTime;
        }
    }

    @Override
    public void run() {
        this.simulation.logger.info(String.format("Simulation time %f: PathFinderEvent", this.time));

        this.lastEventTime = this.time;

        if (this.controller.getAvailableMobiles().size() == this.controller.getAllMobiles().size()) {
            return;
        }

        this.pathFinder.computePaths(this.controller.getAllMobiles(), this.warehouse.getGraph());

        // update paths at the end of the window
        PathUpdateEvent.enqueue(this.simulation, this.time + this.pathFinder.getWindow(), this.controller);
    }
}
