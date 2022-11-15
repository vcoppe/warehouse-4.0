package event;

import agent.Controller;
import agent.Mobile;
import brain.TravelTimeEstimator;
import pathfinding.PathFinder;
import scheduling.TimeEstimationPropagator;
import simulation.Event;
import simulation.Simulation;
import warehouse.Mission;

public class MobileMissionStartEvent extends Event {

    private final Controller controller;
    private final TravelTimeEstimator travelTimeEstimator;
    private final TimeEstimationPropagator timeEstimationPropagator;
    private final PathFinder pathFinder;
    private final Mobile mobile;
    private final Mission mission;

    public MobileMissionStartEvent(Simulation simulation, double time, Controller controller, Mobile mobile, Mission mission) {
        super(simulation, time);
        this.controller = controller;
        this.pathFinder = controller.getPathFinder();
        this.travelTimeEstimator = controller.travelTimeEstimator;
        this.timeEstimationPropagator = controller.timeEstimationPropagator;
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

        this.pathFinder.computePath(this.time, this.mobile);

        double pickupTime = mobile.getPathEndTime();

        Event event = new MobileMissionPickUpEvent(this.simulation, pickupTime, this.controller, mobile);
        this.simulation.enqueueEvent(event);

        mission.setExpectedPickUpTime(pickupTime);
        mission.setExpectedEndTime(
                pickupTime +
                        this.travelTimeEstimator.estimate(
                                mission.getStartPosition(),
                                mission.getEndPosition(),
                                true
                        )
        );

        this.timeEstimationPropagator.propagate(this.time);
    }

}
