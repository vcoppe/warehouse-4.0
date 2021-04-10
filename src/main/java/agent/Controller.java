package agent;

import brain.MobileMissionSelector;
import brain.PalletPositionSelector;
import brain.TruckDockSelector;
import observer.Observable;
import pathfinding.WHCAStar;
import warehouse.Mission;
import warehouse.Warehouse;

import java.util.ArrayList;

public class Controller extends Observable {

    public final MobileMissionSelector mobileMissionSelector;
    public final TruckDockSelector truckDockSelector;
    public final PalletPositionSelector palletPositionSelector;
    private final Warehouse warehouse;
    private final Stock stock;
    private final ProductionLine productionLine;
    private final WHCAStar pathFinder;
    private final ArrayList<Dock> docks;
    private final ArrayList<Truck> trucks;
    private final ArrayList<Mobile> allMobiles;
    private final ArrayList<Mobile> availableMobiles;
    private final ArrayList<Mission> missions;

    public Controller(Warehouse warehouse, Stock stock, ProductionLine productionLine, WHCAStar pathFinder, ArrayList<Dock> docks, ArrayList<Mobile> mobiles, MobileMissionSelector mobileMissionSelector, TruckDockSelector truckDockSelector, PalletPositionSelector palletPositionSelector) {
        super();
        this.mobileMissionSelector = mobileMissionSelector;
        this.truckDockSelector = truckDockSelector;
        this.palletPositionSelector = palletPositionSelector;
        this.warehouse = warehouse;
        this.stock = stock;
        this.productionLine = productionLine;
        this.pathFinder = pathFinder;
        this.docks = docks;
        this.allMobiles = mobiles;
        this.availableMobiles = new ArrayList<>(mobiles);
        this.trucks = new ArrayList<>();
        this.missions = new ArrayList<>();
    }

    public Warehouse getWarehouse() {
        return this.warehouse;
    }

    public Stock getStock() {
        return this.stock;
    }

    public ProductionLine getProductionLine() {
        return this.productionLine;
    }

    public WHCAStar getPathFinder() {
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

    public ArrayList<Mission> getMissions() {
        return this.missions;
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
