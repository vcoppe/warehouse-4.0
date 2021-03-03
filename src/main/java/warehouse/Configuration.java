package warehouse;

import agent.*;
import brain.MobileMissionSelector;
import brain.PalletPositionSelector;
import brain.TruckDockSelector;
import simulation.Simulation;

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

    public int palletSize = 10;
    public int dockWidth = 30;

    public Configuration(int width, int depth, int nDocks, int nMobiles, MobileMissionSelector mobileMissionSelector, PalletPositionSelector palletPositionSelector, TruckDockSelector truckDockSelector, Level level) {
        this.simulation = new Simulation(level);

        this.warehouse = new Warehouse(width, depth);
        this.stock = new Stock(this.warehouse);

        int productionLineWidth = 50;

        ArrayList<Position> startBuffer = new ArrayList<>();
        ArrayList<Position> endBuffer = new ArrayList<>();
        for (int i=0; i<10; i++) {
            startBuffer.add(new Position(width-productionLineWidth-2*palletSize+i/5*palletSize, (i%5)*palletSize));
            this.stock.add(new Position(width-productionLineWidth-2*palletSize+i/5*palletSize, (i%5)*palletSize), Pallet.FREE);
            endBuffer.add(new Position(width-productionLineWidth+(i%5)*palletSize, depth/2+i/5*palletSize));
            this.stock.add(new Position(width-productionLineWidth+(i%5)*palletSize, depth/2+i/5*palletSize), Pallet.FREE);
        }

        this.productionLine = new ProductionLine(this.stock, new Position(width-productionLineWidth, 0), productionLineWidth, depth/2, 10, startBuffer, endBuffer);
        this.docks = new ArrayList<>();
        this.mobiles = new ArrayList<>();

        for (int i=0; i<nDocks; i++) this.docks.add(new Dock(new Position(dockWidth*i, depth)));
        for (int i=0; i<nMobiles; i++) this.mobiles.add(new Mobile(new Position(dockWidth*i, depth-palletSize)));

        this.controller = new Controller(this.warehouse, this.stock, this.productionLine, this.docks, this.mobiles, mobileMissionSelector, truckDockSelector, palletPositionSelector);

        this.mobileMissionSelector = mobileMissionSelector;
        this.palletPositionSelector = palletPositionSelector;
        this.truckDockSelector = truckDockSelector;
    }

    public Configuration(int width, int height, int nDocks, int nMobiles, MobileMissionSelector mobileMissionSelector, PalletPositionSelector palletPositionSelector, TruckDockSelector truckDockSelector) {
        this(width, height, nDocks, nMobiles, mobileMissionSelector, palletPositionSelector, truckDockSelector, Level.ALL);
    }

    public Configuration(int nDocks, int nMobiles, MobileMissionSelector mobileMissionSelector, PalletPositionSelector palletPositionSelector, TruckDockSelector truckDockSelector) {
        this(700, 350, nDocks, nMobiles, mobileMissionSelector, palletPositionSelector, truckDockSelector, Level.ALL);
    }

}
