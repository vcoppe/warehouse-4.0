package event;

import agent.Controller;
import agent.Dock;
import agent.Stock;
import agent.Truck;
import simulation.Event;
import simulation.Simulation;
import util.Pair;
import warehouse.Mission;
import warehouse.Pallet;
import warehouse.Position;
import warehouse.Warehouse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

        Position delta = new Position(0, this.controller.getConfiguration().palletSize);

        HashMap<Position,Pallet> toUnload = this.truck.getToUnload();
        HashMap<Position,Mission> unloadMissions = new HashMap<>();
        ArrayList<Position> unloadPositions = new ArrayList<>(toUnload.keySet());
        unloadPositions.sort(Comparator.comparingInt(Position::getY));

        // scan pallets to load and unload
        for (Position palletPosition : unloadPositions) {
            Pallet pallet = toUnload.get(palletPosition);

            Position startPosition = this.truck.getPosition().add(palletPosition);
            Position endPosition = this.controller.palletPositionSelector.selectEndPosition(pallet, startPosition, this.stock);

            if (endPosition == null) {
                this.simulation.logger.warning("FAILURE! Warehouse is full, cannot handle more pallets.");
                return;
            }

            Mission mission = new Mission(this.time, pallet, this.truck, null, startPosition, endPosition);
            this.controller.add(mission);
            this.stock.lock(endPosition);

            Position previousPosition = palletPosition.subtract(delta);
            if (unloadMissions.containsKey(previousPosition)) {
                Mission previousMission = unloadMissions.get(previousPosition);
                //mission.startAfterEnd(previousMission);
            }

            unloadMissions.put(palletPosition, mission);
        }

        HashMap<Position,Pallet> toLoad = this.truck.getToLoad();
        HashMap<Position,Mission> loadMissions = new HashMap<>();
        ArrayList<Position> loadPositions = new ArrayList<>(toLoad.keySet());
        loadPositions.sort((p1,p2) -> p2.getY() - p1.getY());

        for (Position palletPosition : loadPositions) {
            Pallet pallet = toLoad.get(palletPosition);

            Position endPosition = this.truck.getPosition().add(palletPosition);
            Position startPosition = this.controller.palletPositionSelector.selectStartPosition(pallet, endPosition, this.stock);

            if (startPosition == null) {
                this.simulation.logger.warning("FAILURE! Missing pallets to load truck.");
                return;
            }

            Mission mission = new Mission(this.time, pallet, null, this.truck, startPosition, endPosition);
            this.controller.add(mission);
            this.stock.lock(startPosition);

            for (Mission unloadMission : unloadMissions.values()) {
               // mission.startAfterStart(unloadMission);
            }

            Position previousPosition = palletPosition.add(delta);
            if (loadMissions.containsKey(previousPosition)) {
                Mission previousMission = loadMissions.get(previousPosition);
                //mission.startAfterStart(previousMission);
            }

            loadMissions.put(palletPosition, mission);
        }

        Event event = new ControllerEvent(this.simulation, this.time, this.controller);
        this.simulation.enqueueEvent(event);
    }

}
