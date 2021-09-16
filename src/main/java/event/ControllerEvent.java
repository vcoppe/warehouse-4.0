package event;

import agent.*;
import simulation.Event;
import simulation.Simulation;
import util.Pair;
import util.Vector3D;
import warehouse.Mission;
import warehouse.Production;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ControllerEvent extends Event {

    private final Controller controller;
    private final Stock stock;

    public ControllerEvent(Simulation simulation, double time, Controller controller) {
        super(simulation, time, Integer.MAX_VALUE-1); // set large id to have ControllerEvents at the end of each timestep
        this.controller = controller;
        this.stock = controller.getStock();
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: ControllerEvent\n\t%d available mobiles\n\t%d waiting missions\n\t%d available docks\n\t%d waiting trucks",
                        this.time,
                        this.controller.getAvailableMobiles().size(),
                        this.controller.getAllMissions().size(),
                        this.controller.getDocks().size(),
                        this.controller.getTrucks().size()));

        // get incomplete startable missions and try to complete them
        for (Mission mission : this.controller.getIncompleteStartableMissions()) {
            if (mission.getStartPosition() == null) { // load mission
                Vector3D startPosition = this.controller.palletPositionSelector.selectStartPosition(mission.getPallet(), mission.getEndPosition(), this.stock);

                // TODO check for unaccessible pallets
                if (startPosition == null) continue;

                mission.setStartPosition(startPosition);
                this.stock.lock(startPosition);
            } else { // unload mission
                Vector3D endPosition = this.controller.palletPositionSelector.selectEndPosition(mission.getPallet(), mission.getStartPosition(), this.stock);

                // TODO check for unaccessible free locations
                if (endPosition == null) continue;

                mission.setEndPosition(endPosition);
                this.stock.lock(endPosition);
            }
        }

        // match available mobiles with waiting missions
        ArrayList<Pair<Mobile, Mission>> mobileMissionPairs = this.controller.mobileMissionSelector.matchMobileMission(
                this.time,
                this.controller.getAvailableMobiles(),
                this.controller.getCompleteStartableMissions()
        );

        for (Pair<Mobile, Mission> pair : mobileMissionPairs) {
            Mobile mobile = pair.first;
            Mission mission = pair.second;

            this.controller.remove(mobile);
            this.controller.remove(mission);

            Event event = new MobileMissionStartEvent(this.simulation, this.time, this.controller, mobile, mission);
            this.simulation.enqueueEvent(event);
        }

        // match available docks with waiting trucks
        ArrayList<Pair<Truck, Dock>> truckDockPairs = new ArrayList<>();
        for (Truck.Type type : Truck.Type.values()) {
            truckDockPairs.addAll(this.controller.truckDockSelector.matchTruckDock(
                    this.controller.getTrucks().stream().filter(truck -> truck.getType() == type).collect(Collectors.toCollection(ArrayList::new)),
                    this.controller.getDocks().stream().filter(dock -> dock.getType() == type).collect(Collectors.toCollection(ArrayList::new))
            ));
        }

        for (Pair<Truck, Dock> pair : truckDockPairs) {
            Truck truck = pair.first;
            Dock dock = pair.second;

            Event event = new TruckCallEvent(this.simulation, this.time, this.controller, dock, truck);
            this.simulation.enqueueEvent(event);

            this.controller.remove(truck);
            this.controller.remove(dock);
        }

        for (Mobile mobile : this.controller.getAvailableMobiles()) {
            if (mobile.isAvailable()) {
                mobile.replace(new Vector3D(0, this.controller.getWarehouse().getDepth() - (mobile.getId() + 1) * this.controller.getConfiguration().palletSize * 2, 0));
            }
        }

        // launch waiting productions if components have arrived
        for (ProductionLine productionLine : this.controller.getProductionLines()) {
            ArrayList<Production> startableProductions = productionLine.getStartableProductions();
            for (Production production : startableProductions) {
                Event event = new ProductionStartEvent(this.simulation, this.time, this.controller, productionLine, production);
                this.simulation.enqueueEvent(event);

                productionLine.lockProductionPallets(production);
                productionLine.remove(production);
                productionLine.reserveCapacity(production.getCapacity());
            }
        }

    }

}
