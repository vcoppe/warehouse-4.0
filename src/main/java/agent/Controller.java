package agent;

import brain.MobileMissionSelector;
import brain.PalletPositionSelector;
import brain.TruckDockSelector;
import simulation.Agent;
import simulation.Simulation;
import warehouse.Mission;

import java.util.ArrayList;

public class Controller extends Agent {

    public final MobileMissionSelector mobileMissionSelector;
    public final TruckDockSelector truckDockSelector;
    public final PalletPositionSelector palletPositionSelector;
    private final Stock stock;
    private final Production production;
    private final ArrayList<Dock> docks;
    private final ArrayList<Truck> trucks;
    private final ArrayList<Mobile> mobiles;
    private final ArrayList<Mission> missions;

    public Controller(Simulation simulation, Stock stock, Production production, ArrayList<Dock> docks, ArrayList<Mobile> mobiles, MobileMissionSelector mobileMissionSelector, TruckDockSelector truckDockSelector, PalletPositionSelector palletPositionSelector) {
        super(simulation);
        this.mobileMissionSelector = mobileMissionSelector;
        this.truckDockSelector = truckDockSelector;
        this.palletPositionSelector = palletPositionSelector;
        this.production = production;
        this.stock = stock;
        this.docks = docks;
        this.mobiles = mobiles;
        this.trucks = new ArrayList<Truck>();
        this.missions = new ArrayList<Mission>();
    }

    public Stock getStock() {
        return stock;
    }

    public ArrayList<Dock> getDocks() {
        return this.docks;
    }

    public void addDock(Dock dock) {
        this.docks.add(dock);
    }

    public void removeDock(Dock dock) {
        this.docks.remove(dock);
    }

    public ArrayList<Truck> getTrucks() {
        return this.trucks;
    }

    public void addTruck(Truck truck) {
        this.trucks.add(truck);
    }

    public void removeTruck(Truck truck) {
        this.trucks.remove(truck);
    }

    public ArrayList<Mobile> getMobiles() {
        return this.mobiles;
    }

    public void addMobile(Mobile mobile) {
        this.mobiles.add(mobile);
    }

    public void removeMobile(Mobile mobile) {
        this.mobiles.remove(mobile);
    }

    public ArrayList<Mission> getMissions() {
        return this.missions;
    }

    public void addMission(Mission mission) {
        this.missions.add(mission);
    }

    public void removeMission(Mission mission) {
        this.missions.remove(mission);
    }

}
