package event;

import agent.Controller;
import agent.Mobile;
import pathfinding.WHCAStar;
import simulation.Event;
import simulation.Simulation;
import warehouse.Warehouse;

public class PathFinderEvent extends Event {

    private final Controller controller;
    private final Warehouse warehouse;
    private final WHCAStar pathFinder;

    public PathFinderEvent(Simulation simulation, double time, Controller controller) {
        super(simulation, time, Integer.MAX_VALUE); // set large id to have PathFinderEvents at the end of each timestep
        this.controller = controller;
        this.warehouse = controller.getWarehouse();
        this.pathFinder = controller.getPathFinder();
    }

    @Override
    public void run() {
        this.simulation.logger.info(String.format("Simulation time %f: PathFinderEvent", this.time));

        this.pathFinder.computePaths(this.controller.getAllMobiles(), this.warehouse.getGraph());

        // update paths at the end of the window
        Event event = new PathUpdateEvent(this.simulation, this.time + this.pathFinder.getWindow(), this.controller);
        this.simulation.enqueueEvent(event);
    }
}
