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
    public int productionLineWidth = 50;

    public Configuration(int width, int depth, int nDocks, int nMobiles, MobileMissionSelector mobileMissionSelector, PalletPositionSelector palletPositionSelector, TruckDockSelector truckDockSelector, Level level) {
        this.simulation = new Simulation(level);

        this.warehouse = new Warehouse(width, depth);
        this.stock = new Stock(this.warehouse);

        this.productionLine = new ProductionLine(this.stock, new Position(width - productionLineWidth, 0), productionLineWidth, 10 * (depth / 20), 10, new ArrayList<>(), new ArrayList<>());
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

        for (int i = 0; i < 10; i++) {
            this.productionLine.getStartBuffer().add(new Position(this.warehouse.getWidth() - this.productionLine.getWidth() - 2 * this.palletSize + i / 5 * this.palletSize, (i % 5) * this.palletSize));
            this.stock.add(new Position(this.warehouse.getWidth() - this.productionLine.getWidth() - 2 * this.palletSize + i / 5 * this.palletSize, (i % 5) * this.palletSize), Pallet.FREE);
            this.productionLine.getEndBuffer().add(new Position(this.warehouse.getWidth() - this.productionLine.getWidth() + (i % 5) * this.palletSize, this.productionLine.getDepth() + i / 5 * this.palletSize));
            this.stock.add(new Position(this.warehouse.getWidth() - this.productionLine.getWidth() + (i % 5) * this.palletSize, this.productionLine.getDepth() + i / 5 * this.palletSize), Pallet.FREE);
        }

        int nAisles = 30;
        int nSlotsPerAisle = 30;

        for (int i = 0; i < nSlotsPerAisle * nAisles; i++) {
            this.stock.add(
                    new Position(
                            (2 * (i / nSlotsPerAisle) + (i / nSlotsPerAisle) % 2) * this.palletSize,
                            (i % nSlotsPerAisle) * this.palletSize
                    ),
                    Pallet.FREE
            );
        }

        for (int x = 0; x < this.warehouse.getWidth(); x += this.palletSize) {
            for (int y = 0; y <= this.warehouse.getDepth(); y += this.palletSize) {
                Position position = new Position(x, y);
                int type = (x / this.palletSize) % 4;
                if (x >= this.warehouse.getWidth() - this.productionLine.getWidth() && y < this.productionLine.getDepth()) {
                    continue;
                } else if (y >= nSlotsPerAisle * this.palletSize) {
                    if (x - this.palletSize >= 0
                            && (y == nSlotsPerAisle * this.palletSize || y == this.warehouse.getDepth() - 2 * this.palletSize)) {
                        this.warehouse.addEdge(
                                position,
                                new Position(x - this.palletSize, y)
                        );
                    }
                    if (x + this.palletSize < this.warehouse.getWidth()
                            && (y == (nSlotsPerAisle + 1) * this.palletSize || y == this.warehouse.getDepth() - this.palletSize)) {
                        this.warehouse.addEdge(
                                position,
                                new Position(x + this.palletSize, y)
                        );
                    }
                    if (y >= (nSlotsPerAisle + 1) * this.palletSize || type == 2) {
                        this.warehouse.addEdge(
                                position,
                                new Position(x, y - this.palletSize)
                        );
                    }
                    this.warehouse.addEdge(
                            position,
                            new Position(x, y + this.palletSize)
                    );
                } else if (x < 2 * nAisles * this.palletSize) {
                    if (type == 0) {
                        this.warehouse.addEdge(
                                position,
                                new Position(x + this.palletSize, y)
                        );
                    } else if (type == 1) {
                        this.warehouse.addEdge(
                                position,
                                new Position(x - this.palletSize, y)
                        );
                        this.warehouse.addEdge(
                                position,
                                new Position(x, y + this.palletSize)
                        );
                    } else if (type == 2) {
                        this.warehouse.addEdge(
                                position,
                                new Position(x - this.palletSize, y)
                        );
                        this.warehouse.addEdge(
                                position,
                                new Position(x + this.palletSize, y)
                        );
                        if (y - this.palletSize >= 0) {
                            this.warehouse.addEdge(
                                    position,
                                    new Position(x, y - this.palletSize)
                            );
                        }
                    } else {
                        this.warehouse.addEdge(
                                position,
                                new Position(x - this.palletSize, y)
                        );
                    }
                } else {
                    if (x - this.palletSize > 2 * nAisles * this.palletSize) {
                        this.warehouse.addEdge(
                                position,
                                new Position(x - this.palletSize, y)
                        );
                    }
                    if (x + this.palletSize < this.warehouse.getWidth() - this.productionLine.getWidth()) {
                        this.warehouse.addEdge(
                                position,
                                new Position(x + this.palletSize, y)
                        );
                    }
                    if (y - this.palletSize >= 0 && (x / this.palletSize) % 2 == 1) {
                        this.warehouse.addEdge(
                                position,
                                new Position(x, y - this.palletSize)
                        );
                    }
                    if ((x / this.palletSize) % 2 == 0) {
                        this.warehouse.addEdge(
                                position,
                                new Position(x, y + this.palletSize)
                        );
                    }
                }
            }
        }

    }

}
