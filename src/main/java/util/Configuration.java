package util;

import agent.*;
import brain.MobileMissionSelector;
import brain.PalletPositionSelector;
import brain.TruckDockSelector;
import simulation.Simulation;
import warehouse.Pallet;
import warehouse.Position;
import warehouse.Warehouse;

import java.util.ArrayList;
import java.util.logging.Level;

public class Configuration {

    public final Simulation simulation;
    public final Controller controller;
    public final Warehouse warehouse;
    public final Stock stock;
    public final ProductionLine productionLine;
    public final ArrayList<Dock> docks;
    public final ArrayList<Mobile> mobiles;

    public final MobileMissionSelector mobileMissionSelector;
    public final PalletPositionSelector palletPositionSelector;
    public final TruckDockSelector truckDockSelector;

    public Configuration(int nDocks, int nMobiles, MobileMissionSelector mobileMissionSelector, PalletPositionSelector palletPositionSelector, TruckDockSelector truckDockSelector, Level level) {
        this.simulation = new Simulation(level);

        this.warehouse = new Warehouse(100, 100);
        this.stock = new Stock(this.warehouse);

        ArrayList<Position> startBuffer = new ArrayList<>();
        ArrayList<Position> endBuffer = new ArrayList<>();
        for (int i=0; i<10; i++) {
            startBuffer.add(new Position(100-i, 100));
            this.stock.add(new Position(100-i, 100), Pallet.FREE);
            endBuffer.add(new Position(100-i, 50));
            this.stock.add(new Position(100-i, 50), Pallet.FREE);
        }

        this.productionLine = new ProductionLine(this.stock, 10, startBuffer, endBuffer);
        this.docks = new ArrayList<>();
        this.mobiles = new ArrayList<>();

        for (int i=0; i<nDocks; i++) this.docks.add(new Dock(new Position(5+10*i, 0)));
        for (int i=0; i<nMobiles; i++) this.mobiles.add(new Mobile(new Position(5+10*i, 0)));

        this.controller = new Controller(this.warehouse, this.stock, this.productionLine, this.docks, this.mobiles, mobileMissionSelector, truckDockSelector, palletPositionSelector);

        this.mobileMissionSelector = mobileMissionSelector;
        this.palletPositionSelector = palletPositionSelector;
        this.truckDockSelector = truckDockSelector;
    }

    public Configuration(int nDocks, int nMobiles, MobileMissionSelector mobileMissionSelector, PalletPositionSelector palletPositionSelector, TruckDockSelector truckDockSelector) {
        this(nDocks, nMobiles, mobileMissionSelector, palletPositionSelector, truckDockSelector, Level.ALL);
    }

}
