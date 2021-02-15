package event;

import agent.*;
import simulation.Event;
import simulation.Simulation;
import warehouse.Mission;
import warehouse.Warehouse;

public class MobileMissionEndEvent extends Event {

    private final Controller controller;
    private final Warehouse warehouse;
    private final Dock dock;
    private final Mobile mobile;
    private final Mission mission;

    public MobileMissionEndEvent(Simulation simulation, double time, Controller controller, Warehouse warehouse, Dock dock, Mobile mobile, Mission mission) {
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
                String.format("Simulation time %f : MobileMissionEndEvent\n\tmobile %d finished mission %d",
                        this.simulation.getCurrentTime(),
                        this.mobile.getId(),
                        this.mission.getId()));

        this.controller.addMobile(this.mobile);
        this.mobile.setPosition(this.mission.getEndPosition());

        // tell truck or stock that pallet has arrived at position
        if (this.mission.getEndTruck() != null) {
            Truck truck = this.mission.getEndTruck();
            truck.add(this.mission.getPallet());
            if (truck.done()) {
                Event event = new TruckDoneEvent(this.simulation, this.simulation.getCurrentTime(), this.controller, this.warehouse, this.dock);
                this.simulation.enqueueEvent(event);
            }
        } else {
            Stock stock = this.mission.getStock();
            stock.add(this.mission.getPallet(), this.mission.getEndPosition());
        }

        Event event = new ControllerEvent(this.simulation, this.simulation.getCurrentTime(), this.controller, this.warehouse);
        this.simulation.enqueueEvent(event);
    }
}
