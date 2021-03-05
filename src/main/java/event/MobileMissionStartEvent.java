package event;

import agent.Controller;
import agent.Mobile;
import agent.Stock;
import simulation.Event;
import simulation.Simulation;
import warehouse.Mission;
import warehouse.Warehouse;

public class MobileMissionStartEvent extends Event {

    private final Controller controller;
    private final Warehouse warehouse;
    private final Stock stock;
    private final Mobile mobile;
    private final Mission mission;

    public MobileMissionStartEvent(Simulation simulation, double time, Controller controller, Mobile mobile, Mission mission) {
        super(simulation, time);
        this.controller = controller;
        this.warehouse = controller.getWarehouse();
        this.stock = controller.getStock();
        this.mobile = mobile;
        this.mission = mission;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: MobileMissionStartEvent\n\tmobile %d started mission %d",
                        this.simulation.getCurrentTime(),
                        this.mobile.getId(),
                        this.mission.getId()));

        this.mobile.setTargetPosition(this.mission.getStartPosition());

        double missionPickUpTime = this.simulation.getCurrentTime() + this.warehouse.getTravelTime(this.mobile.getPosition(), this.mission.getStartPosition(), this.mobile);

        Event event = new MobileMissionPickUpEvent(this.simulation, missionPickUpTime, this.controller, this.mobile, this.mission);
        this.simulation.enqueueEvent(event);
    }

}
