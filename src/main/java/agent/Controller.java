package agent;

import brain.MobileMissionSelector;
import brain.PalletPositionSelector;
import brain.TruckDockSelector;
import simulation.Agent;
import simulation.Simulation;
import warehouse.Mission;
import warehouse.Warehouse;

import java.util.ArrayList;

public class Controller extends Agent {

    public final MobileMissionSelector mobileMissionSelector;
    public final TruckDockSelector truckDockSelector;
    public final PalletPositionSelector palletPositionSelector;
    private final Warehouse warehouse;
    private final Stock stock;
    private final ProductionLine productionLine;
    private final ArrayList<Dock> docks;
    private final ArrayList<Truck> trucks;
    private final ArrayList<Mobile> mobiles;
    private final ArrayList<Mission> missions;

    public Controller(Simulation simulation, Warehouse warehouse, Stock stock, ProductionLine productionLine, ArrayList<Dock> docks, ArrayList<Mobile> mobiles, MobileMissionSelector mobileMissionSelector, TruckDockSelector truckDockSelector, PalletPositionSelector palletPositionSelector) {
        super(simulation);
        this.mobileMissionSelector = mobileMissionSelector;
        this.truckDockSelector = truckDockSelector;
        this.palletPositionSelector = palletPositionSelector;
        this.warehouse = warehouse;
        this.stock = stock;
        this.productionLine = productionLine;
        this.docks = docks;
        this.mobiles = mobiles;
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

    public ArrayList<Dock> getDocks() {
        return this.docks;
    }

    public void add(Dock dock) {
        this.docks.add(dock);
    }

    public void remove(Dock dock) {
        this.docks.remove(dock);
    }

    public ArrayList<Truck> getTrucks() {
        return this.trucks;
    }

    public void add(Truck truck) {
        this.trucks.add(truck);
    }

    public void remove(Truck truck) {
        this.trucks.remove(truck);
    }

    public ArrayList<Mobile> getMobiles() {
        return this.mobiles;
    }

    public void add(Mobile mobile) {
        this.mobiles.add(mobile);
    }

    public void remove(Mobile mobile) {
        this.mobiles.remove(mobile);
    }

    public ArrayList<Mission> getMissions() {
        return this.missions;
    }

    public void add(Mission mission) {
        this.missions.add(mission);
    }

    public void remove(Mission mission) {
        this.missions.remove(mission);
    }

}
