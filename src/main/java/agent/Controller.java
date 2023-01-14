package agent;

import brain.MobileMissionSelector;
import brain.PalletPositionSelector;
import brain.TruckDockSelector;
import observer.Observable;
import pathfinding.PathFinder;
import pathfinding.SafeIntervalPathPlanning;
import scheduling.TimeEstimationPropagator;
import warehouse.Configuration;
import warehouse.Mission;
import warehouse.Production;
import warehouse.Warehouse;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Controller extends Observable {

    public final MobileMissionSelector mobileMissionSelector;
    public final TruckDockSelector truckDockSelector;
    public final PalletPositionSelector palletPositionSelector;
    public final TimeEstimationPropagator timeEstimationPropagator;
    private final Configuration configuration;
    private final Warehouse warehouse;
    private final Stock stock;
    private final PathFinder pathFinder;
    private final ArrayList<ProductionLine> productionLines;
    private final ArrayList<Dock> docks;
    private final ArrayList<Lift> lifts;
    private final ArrayList<Truck> trucks;
    private final ArrayList<Mobile> allMobiles;
    private final ArrayList<Mobile> availableMobiles;
    private final ArrayList<Mission> missions;
    private final ArrayList<Production> productions;

    public Controller(Configuration configuration, MobileMissionSelector mobileMissionSelector, TruckDockSelector truckDockSelector, PalletPositionSelector palletPositionSelector) {
        super();
        this.mobileMissionSelector = mobileMissionSelector;
        this.truckDockSelector = truckDockSelector;
        this.palletPositionSelector = palletPositionSelector;
        this.timeEstimationPropagator = new TimeEstimationPropagator(configuration.warehouse);
        this.configuration = configuration;
        this.warehouse = configuration.warehouse;
        this.stock = configuration.stock;
        this.productionLines = configuration.productionLines;
        this.pathFinder = new SafeIntervalPathPlanning(configuration.warehouse.getGraph(), configuration.mobiles);
        this.docks = configuration.docks;
        this.lifts = configuration.lifts;
        this.allMobiles = configuration.mobiles;
        this.availableMobiles = new ArrayList<>(configuration.mobiles);
        this.trucks = new ArrayList<>();
        this.missions = new ArrayList<>();
        this.productions = new ArrayList<>();
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public Warehouse getWarehouse() {
        return this.warehouse;
    }

    public Stock getStock() {
        return this.stock;
    }

    public ArrayList<ProductionLine> getProductionLines() {
        return this.productionLines;
    }

    public PathFinder getPathFinder() {
        return this.pathFinder;
    }

    public ArrayList<Dock> getDocks() {
        return this.docks;
    }

    public void add(Dock dock) {
        this.docks.add(dock);
        this.changed();
    }

    public void remove(Dock dock) {
        this.docks.remove(dock);
        this.changed();
    }

    public ArrayList<Truck> getTrucks() {
        return this.trucks;
    }

    public void add(Truck truck) {
        this.trucks.add(truck);
        this.changed();
    }

    public void remove(Truck truck) {
        this.trucks.remove(truck);
        this.changed();
    }

    public ArrayList<Mobile> getAllMobiles() {
        return this.allMobiles;
    }

    public ArrayList<Mobile> getAvailableMobiles() {
        return this.availableMobiles;
    }

    public void add(Mobile mobile) {
        this.availableMobiles.add(mobile);
        this.pathFinder.add(mobile);
        this.changed();
    }

    public void remove(Mobile mobile) {
        this.availableMobiles.remove(mobile);
        this.changed();
    }

    public ArrayList<Mission> getAllMissions() {
        return this.missions;
    }

    public ArrayList<Mission> getCompleteMissions() {
        return this.missions.stream().filter(Mission::isComplete).collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Mission> getIncompleteSoonStartableMissions() {
        ArrayList<Mission> incompleteSoonStartableMissions = new ArrayList<>();
        for (Mission mission : this.missions) {
            if (!mission.isComplete()) {
                boolean incompleteParent = false;
                for (Mission parent : mission.getPrecedingMissions()) {
                    if (!parent.isComplete()) {
                        incompleteParent = true;
                        break;
                    }
                }
                if (!incompleteParent) {
                    incompleteSoonStartableMissions.add(mission);
                }
            }
        }
        return incompleteSoonStartableMissions;
    }

    public ArrayList<Mission> getCompleteSoonStartableMissions() {
        return this.missions.stream().filter(m -> m.isComplete() && m.getMissionPathMaxLength() <= 1).collect(Collectors.toCollection(ArrayList::new));
    }

    public void add(Mission mission) {
        this.missions.add(mission);
        this.timeEstimationPropagator.add(mission);
        this.changed();
    }

    public void remove(Mission mission) {
        this.missions.remove(mission);
        this.changed();
    }

    public ArrayList<Production> getProductions() {
        return this.productions;
    }

    public void add(Production production) {
        this.productions.add(production);
        this.changed();
    }

    public void remove(Production production) {
        this.productions.remove(production);
        this.changed();
    }

}
