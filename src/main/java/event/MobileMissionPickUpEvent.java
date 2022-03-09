package event;

import agent.Controller;
import agent.Mobile;
import agent.Stock;
import agent.Truck;
import brain.TravelTimeEstimator;
import simulation.Event;
import simulation.Simulation;
import warehouse.Mission;

public class MobileMissionPickUpEvent extends Event {

    private final Controller controller;
    private final TravelTimeEstimator travelTimeEstimator;
    private final Stock stock;
    private final Mobile mobile;
    private final Mission mission;

    public MobileMissionPickUpEvent(Simulation simulation, double time, Controller controller, Mobile mobile) {
        super(simulation, time);
        this.controller = controller;
        this.travelTimeEstimator = controller.travelTimeEstimator;
        this.stock = controller.getStock();
        this.mobile = mobile;
        this.mission = mobile.getMission();
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: MobileMissionPickUpEvent\n\tmobile %d picked up pallet of mission %d",
                        this.time,
                        this.mobile.getId(),
                        this.mission.getId()));

        this.travelTimeEstimator.update(
                this.mobile.getPosition(),
                this.mission.getStartPosition(),
                this.mission.getExpectedEndTime() - this.mission.getExpectedPickUpTime()
        );
        this.mobile.pickUp();

        // tell truck or stock that pallet has left the position
        if (this.mission.getStartTruck() != null) {
            Truck truck = this.mission.getStartTruck();
            truck.remove(this.mission.getStartPosition(), this.mission.getPallet());
            if (truck.done()) {
                Event event = new TruckDoneEvent(this.simulation, this.time, this.controller, this.mission.getStartTruck().getDock(), this.mission.getStartTruck());
                this.simulation.enqueueEvent(event);
            }
        } else {
            this.stock.remove(this.mission.getStartPosition(), this.mission.getPallet());
        }

        this.simulation.enqueueEvent(new ControllerEvent(this.simulation, this.time, this.controller)); // some missions might have become startable after pickup
        PathFinderEvent.enqueue(this.simulation, this.time, this.controller);
    }

}
