package event;

import agent.*;
import simulation.Event;
import simulation.Simulation;
import warehouse.Mission;
import warehouse.Warehouse;

public class MobileMissionStartEvent extends Event {

    private final Controller controller;
    private final Warehouse warehouse;
    private final Dock dock;
    private final Mobile mobile;
    private final Mission mission;

    public MobileMissionStartEvent(Simulation simulation, double time, Controller controller, Warehouse warehouse, Dock dock, Mobile mobile, Mission mission) {
        super(simulation, time);
        this.controller = controller;
        this.warehouse = warehouse;
        this.dock = dock;
        this.mobile = mobile;
        this.mission = mission;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f : MobileMissionStartEvent\n\tmobile %d started mission %d",
                        this.simulation.getCurrentTime(),
                        this.mobile.getId(),
                        this.mission.getId()));

        // tell truck or stock that pallet has left the position
        if (this.mission.getStartTruck() != null) {
            Truck truck = this.mission.getStartTruck();
            truck.remove(this.mission.getPallet());
            if (truck.done()) {
                Event event = new TruckDoneEvent(this.simulation, this.simulation.getCurrentTime(), this.controller, this.warehouse, this.dock);
                this.simulation.enqueueEvent(event);
            }
        } else {
            Stock stock = this.mission.getStock();
            stock.remove(this.mission.getPallet(), this.mission.getStartPosition());
        }

        double missionEndTime = this.simulation.getCurrentTime();
        missionEndTime += this.warehouse.getDistance(this.mobile.getPosition(), this.mission.getStartPosition());
        missionEndTime += this.warehouse.getDistance(this.mission.getStartPosition(), this.mission.getEndPosition());

        Event event = new MobileMissionEndEvent(this.simulation, missionEndTime, this.controller, this.warehouse, this.dock, this.mobile, this.mission);
        this.simulation.enqueueEvent(event);
    }

}
