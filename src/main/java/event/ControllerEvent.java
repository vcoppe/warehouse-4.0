package event;

import agent.*;
import simulation.Event;
import simulation.Simulation;
import util.Pair;
import warehouse.Mission;
import warehouse.Production;

import java.util.ArrayList;

public class ControllerEvent extends Event {

    private final Controller controller;
    private final ProductionLine productionLine;

    public ControllerEvent(Simulation simulation, double time, Controller controller) {
        super(simulation, time);
        this.controller = controller;
        this.productionLine = this.controller.getProductionLine();
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: ControllerEvent\n\t%d available mobiles\n\t%d waiting missions\n\t%d available docks\n\t%d waiting trucks\n\t%d waiting productions",
                        this.simulation.getCurrentTime(),
                        this.controller.getMobiles().size(),
                        this.controller.getMissions().size(),
                        this.controller.getDocks().size(),
                        this.controller.getTrucks().size(),
                        this.productionLine.getProductions().size()));

        // match available mobiles with waiting missions
        ArrayList<Pair<Mobile,Mission>> mobileMissionPairs = this.controller.mobileMissionSelector.matchMobileMission(
                this.controller.getMobiles(),
                this.controller.getMissions()
        );

        for (Pair<Mobile,Mission> pair : mobileMissionPairs) {
            Mobile mobile = pair.first;
            Mission mission = pair.second;

            Event event = new MobileMissionStartEvent(this.simulation, this.simulation.getCurrentTime(), this.controller, mobile, mission);
            this.simulation.enqueueEvent(event);

            this.controller.remove(mobile);
            this.controller.remove(mission);
        }

        // match available docks with waiting trucks
        ArrayList<Pair<Truck,Dock>> truckDockPairs = this.controller.truckDockSelector.matchTruckDock(
                this.controller.getTrucks(),
                this.controller.getDocks()
        );

        for (Pair<Truck,Dock> pair : truckDockPairs) {
            Truck truck = pair.first;
            Dock dock = pair.second;

            Event event = new TruckCallEvent(this.simulation, this.simulation.getCurrentTime(), this.controller, dock, truck);
            this.simulation.enqueueEvent(event);

            this.controller.remove(truck);
            this.controller.remove(dock);
        }

        // launch waiting productions if components have arrived
        ArrayList<Production> startableProductions = this.productionLine.getStartableProductions();
        for (Production production : startableProductions) {
            Event event = new ProductionStartEvent(this.simulation, this.simulation.getCurrentTime(), this.controller, production);
            this.simulation.enqueueEvent(event);

            this.productionLine.lockProductionPallets(production);
            this.productionLine.remove(production);
            this.productionLine.reserveCapacity(production.getCapacity());
        }

    }

}
