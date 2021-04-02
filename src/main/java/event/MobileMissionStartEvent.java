package event;

import agent.Controller;
import agent.Mobile;
import agent.Stock;
import simulation.Event;
import simulation.Simulation;
import util.Pair;
import warehouse.Mission;
import warehouse.Position;
import warehouse.Warehouse;

import java.util.ArrayList;

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
                        this.time,
                        this.mobile.getId(),
                        this.mission.getId()));

        ArrayList<Pair<Position, Double>> path = this.warehouse.getPath(this.mobile.getPosition(), this.mission.getStartPosition(), this.time, this.mobile);
        double missionPickUpTime = path.get(path.size() - 1).second;

        this.mobile.start(this.mission, path);

        Event event = new MobileMissionPickUpEvent(this.simulation, missionPickUpTime, this.controller, this.mobile, this.mission);
        this.simulation.enqueueEvent(event);
    }

}
