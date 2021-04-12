package event;

import agent.Controller;
import agent.Mobile;
import agent.Stock;
import agent.Truck;
import simulation.Event;
import simulation.Simulation;
import util.Pair;
import warehouse.Mission;
import warehouse.Position;
import warehouse.Warehouse;

import java.util.ArrayList;

public class MobileMissionPickUpEvent extends Event {

    private final Controller controller;
    private final Warehouse warehouse;
    private final Stock stock;
    private final Mobile mobile;
    private final Mission mission;

    public MobileMissionPickUpEvent(Simulation simulation, double time, Controller controller, Mobile mobile) {
        super(simulation, time);
        this.controller = controller;
        this.warehouse = controller.getWarehouse();
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

        PathFinderEvent.enqueue(this.simulation, this.time, this.controller);
    }

}
