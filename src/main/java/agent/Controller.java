package agent;

import brain.MobileMissionSelector;
import brain.PalletPositionSelector;
import brain.TruckDockSelector;
import graph.PathFinder;
import graph.WHCAStar;
import observer.Observable;
import warehouse.Configuration;
import warehouse.Mission;
import warehouse.Warehouse;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Controller extends Observable {

    public final MobileMissionSelector mobileMissionSelector;
    public final TruckDockSelector truckDockSelector;
    public final PalletPositionSelector palletPositionSelector;
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

    public Controller(Configuration configuration, MobileMissionSelector mobileMissionSelector, TruckDockSelector truckDockSelector, PalletPositionSelector palletPositionSelector) {
        super();
        this.mobileMissionSelector = mobileMissionSelector;
        this.truckDockSelector = truckDockSelector;
        this.palletPositionSelector = palletPositionSelector;
        this.configuration = configuration;
        this.warehouse = configuration.warehouse;
        this.stock = configuration.stock;
        this.productionLines = configuration.productionLines;
        this.pathFinder = new WHCAStar(configuration.warehouse.getGraph());
        this.docks = configuration.docks;
        this.lifts = configuration.lifts;
        this.allMobiles = configuration.mobiles;
        this.availableMobiles = new ArrayList<>(configuration.mobiles);
        this.trucks = new ArrayList<>();
        this.missions = new ArrayList<>();
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
        this.changed();
    }

    public void remove(Mobile mobile) {
        this.availableMobiles.remove(mobile);
        this.changed();
    }

    public ArrayList<Mission> getAllMissions() {
        return this.missions;
    }

    public ArrayList<Mission> getIncompleteStartableMissions() {
        return this.missions.stream().filter(m -> (m.getStartPosition() == null || m.getEndPosition() == null) && m.canStart()).collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Mission> getCompleteStartableMissions() {
        return this.missions.stream().filter(m -> m.getStartPosition() != null && m.getEndPosition() != null && m.canStart()).collect(Collectors.toCollection(ArrayList::new));
    }

    public void add(Mission mission) {
        this.missions.add(mission);
        this.changed();
    }

    public void remove(Mission mission) {
        this.missions.remove(mission);
        this.changed();
    }

}
