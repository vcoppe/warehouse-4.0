package event;

import agent.Controller;
import agent.Dock;
import simulation.Event;
import simulation.Simulation;
import util.MobileMission;
import util.TruckDock;
import warehouse.Warehouse;

import java.util.ArrayList;

public class ControllerEvent extends Event {

    private final Controller controller;
    private final Warehouse warehouse;

    public ControllerEvent(Simulation simulation, double time, Controller controller, Warehouse warehouse) {
        super(simulation, time);
        this.controller = controller;
        this.warehouse = warehouse;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f : ControllerEvent\n\t%d available mobiles\n\t%d waiting missions\n\t%d available docks\n\t%d waiting trucks",
                        this.simulation.getCurrentTime(),
                        this.controller.getMobiles().size(),
                        this.controller.getMissions().size(),
                        this.controller.getDocks().size(),
                        this.controller.getTrucks().size()));

        // match available mobiles with waiting missions
        ArrayList<MobileMission> mobileMissionPairs = this.controller.mobileMissionSelector.matchMobileMission(
                this.controller.getMobiles(),
                this.controller.getMissions()
        );

        for (MobileMission pair : mobileMissionPairs) {
            Dock dock = pair.mission.getStartTruck() == null ? (pair.mission.getEndTruck() == null ? null : pair.mission.getEndTruck().getDock()) : pair.mission.getStartTruck().getDock();
            Event event = new MobileMissionStartEvent(this.simulation, this.simulation.getCurrentTime(), this.controller, this.warehouse, dock, pair.mobile, pair.mission);
            this.simulation.enqueueEvent(event);

            this.controller.removeMission(pair.mission);
            this.controller.removeMobile(pair.mobile);
        }

        // match available docks with waiting trucks
        ArrayList<TruckDock> truckDockPairs = this.controller.truckDockSelector.matchTruckDock(
                this.controller.getTrucks(),
                this.controller.getDocks()
        );

        for (TruckDock pair : truckDockPairs) {
            Event event = new TruckCallEvent(this.simulation, this.simulation.getCurrentTime(), this.controller, this.warehouse, this.controller.getStock(), pair.dock, pair.truck);
            this.simulation.enqueueEvent(event);

            this.controller.removeTruck(pair.truck);
            this.controller.removeDock(pair.dock);
        }
    }

}
