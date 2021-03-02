package event;

import agent.*;
import simulation.Event;
import simulation.Simulation;
import warehouse.Mission;

public class MobileMissionEndEvent extends Event {

    private final Controller controller;
    private final Stock stock;
    private final Mobile mobile;
    private final Mission mission;

    public MobileMissionEndEvent(Simulation simulation, double time, Controller controller, Mobile mobile, Mission mission) {
        super(simulation, time);
        this.controller = controller;
        this.stock = controller.getStock();
        this.mobile = mobile;
        this.mission = mission;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: MobileMissionEndEvent\n\tmobile %d finished mission %d",
                        this.simulation.getCurrentTime(),
                        this.mobile.getId(),
                        this.mission.getId()));

        this.mobile.setPosition(this.mission.getEndPosition());
        this.controller.add(this.mobile);

        // tell truck or stock that pallet has arrived at position
        if (this.mission.getEndTruck() != null) {
            Truck truck = this.mission.getEndTruck();
            truck.add(this.mission.getPallet());
            if (truck.done()) {
                Event event = new TruckDoneEvent(this.simulation, this.simulation.getCurrentTime(), this.controller, this.mission.getEndTruck().getDock(), this.mission.getEndTruck());
                this.simulation.enqueueEvent(event);
            }
        } else {
            this.stock.add(this.mission.getEndPosition(), this.mission.getPallet());
        }

        Event event = new ControllerEvent(this.simulation, this.simulation.getCurrentTime(), this.controller);
        this.simulation.enqueueEvent(event);
    }

}
