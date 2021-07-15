package event;

import agent.Controller;
import agent.Dock;
import agent.Stock;
import agent.Truck;
import simulation.Event;
import simulation.Simulation;
import util.*;
import warehouse.Mission;
import warehouse.Pallet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class TruckDockEvent extends Event {

    private final Controller controller;
    private final Stock stock;
    private final Truck truck;
    private final Dock dock;

    public TruckDockEvent(Simulation simulation, double time, Controller controller, Dock dock, Truck truck) {
        super(simulation, time);
        this.controller = controller;
        this.stock = controller.getStock();
        this.dock = dock;
        this.truck = truck;
    }

    @Override
    public void run() {
        this.simulation.logger.info(
                String.format("Simulation time %f: TruckDockEvent\n\ttruck %d arrived at dock %d",
                        this.time,
                        this.truck.getId(),
                        this.dock.getId()));

        this.truck.dock();

        Vector3D deltaX = new Vector3D(this.controller.getConfiguration().palletSize, 0);
        Vector3D deltaY = new Vector3D(0, this.controller.getConfiguration().palletSize);

        HashMap<Vector3D, Pallet> toUnload = this.truck.getToUnload();
        HashMap<Vector3D, Mission> unloadMissions = new HashMap<>();
        ArrayList<Vector3D> unloadPositions = new ArrayList<>(toUnload.keySet());
        unloadPositions.sort(Comparator.comparingInt(Vector3D::getY));

        // scan pallets to load and unload
        for (Vector3D palletPosition : unloadPositions) {
            Pallet pallet = toUnload.get(palletPosition);

            Vector3D startPosition = this.truck.getPosition().add(palletPosition);
            Vector3D endPosition = this.controller.palletPositionSelector.selectEndPosition(pallet, startPosition, this.stock);

            if (endPosition == null) {
                this.simulation.logger.warning("FAILURE! Warehouse is full, cannot handle more pallets.");
                return;
            }

            Mission mission = new Mission(this.time, pallet, this.truck, null, startPosition, endPosition);
            this.controller.add(mission);
            this.stock.lock(endPosition);

            switch (this.truck.getType()) {
                case BACK:
                    Vector3D abovePosition = palletPosition.subtract(deltaY);
                    if (unloadMissions.containsKey(abovePosition)) {
                        Mission aboveMission = unloadMissions.get(abovePosition);
                        mission.addStartCondition(new MissionPickedUpCondition(aboveMission));
                    }
                case SIDES:
                    DisjunctionCondition startCondition = new DisjunctionCondition();
                    Vector3D leftPosition = palletPosition.subtract(deltaX);
                    if (unloadMissions.containsKey(leftPosition)) {
                        Mission leftMission = unloadMissions.get(leftPosition);
                        startCondition.add(new MissionPickedUpCondition(leftMission));
                    } else {
                        startCondition.add(Condition.emptyCondition);
                    }
                    Vector3D rightPosition = palletPosition.add(deltaX);
                    if (unloadMissions.containsKey(rightPosition)) {
                        Mission rightMission = unloadMissions.get(rightPosition);
                        startCondition.add(new MissionPickedUpCondition(rightMission));
                    } else {
                        startCondition.add(Condition.emptyCondition);
                    }
                    mission.addStartCondition(startCondition);
            }

            unloadMissions.put(palletPosition, mission);
        }

        HashMap<Vector3D, Pallet> toLoad = this.truck.getToLoad();
        HashMap<Vector3D, Mission> loadMissions = new HashMap<>();
        ArrayList<Vector3D> loadPositions = new ArrayList<>(toLoad.keySet());
        loadPositions.sort((p1, p2) -> p2.getY() - p1.getY());

        for (Vector3D palletPosition : loadPositions) {
            Pallet pallet = toLoad.get(palletPosition);

            Vector3D endPosition = this.truck.getPosition().add(palletPosition);
            Vector3D startPosition = this.controller.palletPositionSelector.selectStartPosition(pallet, endPosition, this.stock);

            if (startPosition == null) {
                this.simulation.logger.warning("FAILURE! Missing pallets to load truck.");
                return;
            }

            Mission mission = new Mission(this.time, pallet, null, this.truck, startPosition, endPosition);
            this.controller.add(mission);
            this.stock.lock(startPosition);

            for (Mission unloadMission : unloadMissions.values()) {
                mission.addStartCondition(new MissionPickedUpCondition(unloadMission));
            }

            switch (this.truck.getType()) {
                case BACK:
                    Vector3D belowPosition = palletPosition.add(deltaY);
                    if (loadMissions.containsKey(belowPosition)) {
                        Mission belowMission = loadMissions.get(belowPosition);
                        mission.addStartCondition(new MissionDoneCondition(belowMission));
                    }
                case SIDES:
                    Vector3D leftPosition = palletPosition.subtract(deltaX);
                    Vector3D rightPosition = palletPosition.add(deltaX);
                    if (loadMissions.containsKey(leftPosition) && !loadMissions.containsKey(rightPosition)) {
                        Mission leftMission = loadMissions.get(leftPosition);
                        mission.addStartCondition(new MissionDoneCondition(leftMission));
                    }
                    if (loadMissions.containsKey(rightPosition) && !loadMissions.containsKey(leftPosition)) {
                        Mission rightMission = loadMissions.get(rightPosition);
                        mission.addStartCondition(new MissionDoneCondition(rightMission));
                    }
            }

            loadMissions.put(palletPosition, mission);
        }

        Event event = new ControllerEvent(this.simulation, this.time, this.controller);
        this.simulation.enqueueEvent(event);
    }

}
