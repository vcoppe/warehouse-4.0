package event;

import agent.Controller;
import agent.Mobile;
import agent.Stock;
import agent.Truck;
import brain.TravelTimeEstimator;
import scheduling.TimeEstimationPropagator;
import simulation.Event;
import simulation.Simulation;
import warehouse.Mission;

public class MobileMissionEndEvent extends Event {

    private final Controller controller;
    private final TravelTimeEstimator travelTimeEstimator;
    private final TimeEstimationPropagator timeEstimationPropagator;
    private final Stock stock;
    private final Mobile mobile;
    private final Mission mission;

    public MobileMissionEndEvent(Simulation simulation, double time, Controller controller, Mobile mobile) {
        super(simulation, time);
        this.controller = controller;
        this.travelTimeEstimator = controller.travelTimeEstimator;
        this.timeEstimationPropagator = controller.timeEstimationPropagator;
        this.stock = controller.getStock();
        this.mobile = mobile;
        this.mission = mobile.getMission();
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: MobileMissionEndEvent\n\tmobile %d finished mission %d",
                        this.time,
                        this.mobile.getId(),
                        this.mission.getId()));

        this.mobile.drop();
        this.controller.add(this.mobile);
        this.travelTimeEstimator.update(
                this.mission.getStartPosition(),
                this.mission.getEndPosition(),
                this.mission.getExpectedEndTime() - this.mission.getExpectedPickUpTime()
        );
        this.timeEstimationPropagator.remove(this.mission);

        // tell truck or stock that pallet has arrived at position
        if (this.mission.getEndTruck() != null) {
            Truck truck = this.mission.getEndTruck();
            truck.add(this.mission.getEndPosition(), this.mission.getPallet());
            if (truck.done()) {
                Event event = new TruckDoneEvent(this.simulation, this.time, this.controller, this.mission.getEndTruck().getDock(), this.mission.getEndTruck());
                this.simulation.enqueueEvent(event);
            }
        } else {
            this.stock.add(this.mission.getEndPosition(), this.mission.getPallet());
        }

        this.simulation.enqueueEvent(new ControllerEvent(this.simulation, this.time, this.controller));
    }

}
