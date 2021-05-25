package warehouse;

import agent.*;
import brain.*;
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
    public int truckWidth = dockWidth;
    public int truckHeight = 3 * truckWidth;

    public final int nAisles, nSlotsPerAisle;

    public Configuration(int width, int depth, int nDocks, int nMobiles) {
        this.simulation = new Simulation();
        this.warehouse = new Warehouse(width, depth);

        int productionLineWidth = 50, productionLineDepth = 10 * (depth / 20);
        
        ArrayList<Position> stockPositions = new ArrayList<>();
        ArrayList<Position> productionLineStartBuffer = new ArrayList<>();
        ArrayList<Position> productionLineEndBuffer = new ArrayList<>();

        this.nAisles = (width - productionLineWidth - 50) / (2 * this.palletSize);
        this.nSlotsPerAisle = (depth - 50) / this.palletSize;

        for (int i = 0; i < this.nSlotsPerAisle * this.nAisles; i++) {
            stockPositions.add(
                    new Position(
                            (2 * (i / this.nSlotsPerAisle) + (i / this.nSlotsPerAisle) % 2) * this.palletSize,
                            (i % this.nSlotsPerAisle) * this.palletSize
                    )
            );
            stockPositions.add(
                    new Position(
                            (2 * (i / this.nSlotsPerAisle) + (i / this.nSlotsPerAisle) % 2) * this.palletSize,
                            (i % this.nSlotsPerAisle) * this.palletSize,
                            this.palletSize
                    )
            );
        }

        for (int i = 0; i < 10; i++) {
            stockPositions.add(new Position(width - productionLineWidth - 2 * this.palletSize + i / 5 * this.palletSize, (i % 5) * this.palletSize));
            productionLineStartBuffer.add(new Position(width - productionLineWidth - 2 * this.palletSize + i / 5 * this.palletSize, (i % 5) * this.palletSize));
            stockPositions.add(new Position(width - productionLineWidth + (i % 5) * this.palletSize, productionLineDepth + i / 5 * this.palletSize));
            productionLineEndBuffer.add(new Position(width - productionLineWidth + (i % 5) * this.palletSize, productionLineDepth + i / 5 * this.palletSize));
        }

        for (int x = 0; x < width; x += this.palletSize) {
            for (int y = 0; y < depth + this.truckHeight; y += this.palletSize) {
                Position position = new Position(x, y);
                int type = (x / this.palletSize) % 4;
                if (x >= width - productionLineWidth && y < productionLineDepth) {
                    continue;
                } else if (y >= this.nSlotsPerAisle * this.palletSize) {
                    if (x - this.palletSize >= 0
                            && (y == this.nSlotsPerAisle * this.palletSize ||
                            y == depth - 2 * this.palletSize ||
                            (y >= depth && (x / this.palletSize) % (this.dockWidth / this.palletSize) > 0))) {
                        this.warehouse.addEdge(
                                position,
                                new Position(x - this.palletSize, y)
                        );
                    }
                    if (x + this.palletSize < width
                            && (y == (nSlotsPerAisle + 1) * this.palletSize ||
                            y == depth - this.palletSize ||
                            (y >= depth && (x / this.palletSize) % (this.dockWidth / this.palletSize) < (this.dockWidth / this.palletSize - 1)))) {
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
                    if (y + this.palletSize < depth + this.truckHeight) {
                        this.warehouse.addEdge(
                                position,
                                new Position(x, y + this.palletSize)
                        );
                    }
                } else if (x < 2 * this.nAisles * this.palletSize) {
                    if (type == 0) {
                        this.warehouse.addEdge(
                                position,
                                new Position(x + this.palletSize, y)
                        );
                        this.warehouse.addEdge(
                                position,
                                new Position(x, y, this.palletSize)
                        );
                        this.warehouse.addEdge(
                                new Position(x, y, this.palletSize),
                                position
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
                        this.warehouse.addEdge(
                                position,
                                new Position(x, y, this.palletSize)
                        );
                        this.warehouse.addEdge(
                                new Position(x, y, this.palletSize),
                                position
                        );
                    }
                } else {
                    if (x - this.palletSize >= 2 * this.nAisles * this.palletSize || this.nAisles % 2 == 1) {
                        this.warehouse.addEdge(
                                position,
                                new Position(x - this.palletSize, y)
                        );
                    }
                    if (x + this.palletSize < width - productionLineWidth ||
                            (x + this.palletSize < width && y >= productionLineDepth)) {
                        this.warehouse.addEdge(
                                position,
                                new Position(x + this.palletSize, y)
                        );
                    }
                    if (y - this.palletSize >= productionLineDepth ||
                            (y - this.palletSize >= 0 && x < width - productionLineWidth)) {
                        this.warehouse.addEdge(
                                position,
                                new Position(x, y - this.palletSize)
                        );
                    }
                    this.warehouse.addEdge(
                            position,
                            new Position(x, y + this.palletSize)
                    );
                }
            }
        }

        this.stock = new Stock(stockPositions);
        this.productionLine = new ProductionLine(this.stock, new Position(width - productionLineWidth, 0), productionLineWidth, productionLineDepth, 10, productionLineStartBuffer, productionLineEndBuffer);

        this.docks = new ArrayList<>();
        this.mobiles = new ArrayList<>();

        for (int i = 0; i < nDocks; i++) this.docks.add(new Dock(new Position(dockWidth * i, depth)));
        for (int i = 0; i < nMobiles; i++) this.mobiles.add(new Mobile(new Position(dockWidth * i, depth - palletSize)));

        this.mobileMissionSelector = new MobileMissionMatchingSelector(this.warehouse);
        this.palletPositionSelector = new ClosestPositionSelector(this.warehouse);
        this.truckDockSelector = new NaiveSelector();

        this.controller = new Controller(this, this.mobileMissionSelector, this.truckDockSelector, this.palletPositionSelector);
    }

    public Configuration(int nDocks, int nMobiles) {
        this(600, 300, nDocks, nMobiles);
    }

}
