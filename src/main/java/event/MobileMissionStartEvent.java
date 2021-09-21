package event;

import agent.Controller;
import agent.Mobile;
import simulation.Event;
import simulation.Simulation;
import warehouse.Mission;

public class MobileMissionStartEvent extends Event {

    private final Controller controller;
    private final Mobile mobile;
    private final Mission mission;

    public MobileMissionStartEvent(Simulation simulation, double time, Controller controller, Mobile mobile, Mission mission) {
        super(simulation, time);
        this.controller = controller;
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

        this.mobile.start(this.mission);
        this.mission.setExpectedStartTime(this.time);

        PathFinderEvent.enqueue(this.simulation, this.time, this.controller);
    }

}
