package event;

import agent.Controller;
import agent.Dock;
import agent.Truck;
import scheduling.DisjunctionConstraint;
import scheduling.MissionDoneConstraint;
import scheduling.MissionPickedUpConstraint;
import scheduling.PrecedenceConstraint;
import simulation.Event;
import simulation.Simulation;
import util.Vector3D;
import warehouse.Configuration;
import warehouse.Mission;
import warehouse.Pallet;

import java.util.ArrayList;
import java.util.HashMap;

public class TruckDockEvent extends Event {

    private final Controller controller;
    private final Truck truck;
    private final Dock dock;

    public TruckDockEvent(Simulation simulation, double time, Controller controller, Dock dock, Truck truck) {
        super(simulation, time);
        this.controller = controller;
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

        Vector3D deltaX = new Vector3D(Configuration.palletSize, 0);
        Vector3D deltaY = new Vector3D(0, Configuration.palletSize);

        HashMap<Vector3D, Pallet> toUnload = this.truck.getToUnload();
        HashMap<Vector3D, Mission> unloadMissions = new HashMap<>();
        ArrayList<Vector3D> unloadPositions = new ArrayList<>(toUnload.keySet());

        // scan pallets to load and unload
        for (Vector3D palletPosition : unloadPositions) {
            Pallet pallet = toUnload.get(palletPosition);

            Vector3D startPosition = this.truck.getPosition().add(palletPosition);

            Mission mission = new Mission(this.time, pallet, this.truck, null, startPosition, null);
            this.controller.add(mission);

            unloadMissions.put(palletPosition, mission);
        }

        for (Vector3D palletPosition : unloadPositions) {
            Mission mission = unloadMissions.get(palletPosition);

            switch (this.truck.getType()) {
                case BACK:
                    Vector3D abovePosition = palletPosition.subtract(deltaY);
                    if (unloadMissions.containsKey(abovePosition)) {
                        Mission aboveMission = unloadMissions.get(abovePosition);
                        mission.addStartConstraint(new MissionPickedUpConstraint(aboveMission));
                    }
                    break;
                case SIDES:
                    DisjunctionConstraint startCondition = new DisjunctionConstraint();
                    Vector3D leftPosition = palletPosition.subtract(deltaX);
                    if (unloadMissions.containsKey(leftPosition)) {
                        Mission leftMission = unloadMissions.get(leftPosition);
                        startCondition.add(new MissionPickedUpConstraint(leftMission));
                    } else {
                        startCondition.add(PrecedenceConstraint.emptyConstraint);
                    }
                    Vector3D rightPosition = palletPosition.add(deltaX);
                    if (unloadMissions.containsKey(rightPosition)) {
                        Mission rightMission = unloadMissions.get(rightPosition);
                        startCondition.add(new MissionPickedUpConstraint(rightMission));
                    } else {
                        startCondition.add(PrecedenceConstraint.emptyConstraint);
                    }
                    mission.addStartConstraint(startCondition);
                    break;
            }
        }

        HashMap<Vector3D, Pallet> toLoad = this.truck.getToLoad();
        HashMap<Vector3D, Mission> loadMissions = new HashMap<>();
        ArrayList<Vector3D> loadPositions = new ArrayList<>(toLoad.keySet());

        for (Vector3D palletPosition : loadPositions) {
            Pallet pallet = toLoad.get(palletPosition);

            Vector3D endPosition = this.truck.getPosition().add(palletPosition);

            Mission mission = new Mission(this.time, pallet, null, this.truck, null, endPosition);
            this.controller.add(mission);

            for (Mission unloadMission : unloadMissions.values()) {
                mission.addStartConstraint(new MissionPickedUpConstraint(unloadMission));
            }

            loadMissions.put(palletPosition, mission);
        }

        for (Vector3D palletPosition : loadPositions) {
            Mission mission = loadMissions.get(palletPosition);

            switch (this.truck.getType()) {
                case BACK:
                    Vector3D belowPosition = palletPosition.add(deltaY);
                    if (loadMissions.containsKey(belowPosition)) {
                        Mission belowMission = loadMissions.get(belowPosition);
                        mission.addStartConstraint(new MissionDoneConstraint(belowMission));
                    }
                    break;
                case SIDES:
                    Vector3D leftPosition = palletPosition.subtract(deltaX);
                    Vector3D leftPosition2 = leftPosition.subtract(deltaX);
                    Vector3D rightPosition = palletPosition.add(deltaX);
                    Vector3D rightPosition2 = rightPosition.add(deltaX);

                    if (loadMissions.containsKey(leftPosition) && loadMissions.containsKey(leftPosition2)) {
                        Mission leftMission = loadMissions.get(leftPosition);
                        mission.addStartConstraint(new MissionDoneConstraint(leftMission));
                    }

                    if (loadMissions.containsKey(rightPosition) && loadMissions.containsKey(rightPosition2)) {
                        Mission rightMission = loadMissions.get(rightPosition);
                        mission.addStartConstraint(new MissionDoneConstraint(rightMission));
                    }
                    break;
            }
        }

        Event event = new ControllerEvent(this.simulation, this.time, this.controller);
        this.simulation.enqueueEvent(event);
    }

}
