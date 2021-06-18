package warehouse;

import agent.*;
import brain.*;
import simulation.Simulation;

import java.util.ArrayList;

public class Configuration {

    public final Simulation simulation;
    public final Controller controller;
    public final Warehouse warehouse;
    public final Stock stock;
    public final ProductionLine productionLine;
    public final ArrayList<Dock> docks;
    public final ArrayList<Lift> lifts;
    public final ArrayList<Mobile> mobiles;

    public final MobileMissionSelector mobileMissionSelector;
    public final PalletPositionSelector palletPositionSelector;
    public final TruckDockSelector truckDockSelector;

    public int palletSize = 10;
    public int dockWidth = 3 * palletSize;
    public int truckWidth = dockWidth;
    public int truckDepth = 3 * truckWidth;

    public Configuration(int width, int depth, int height, int nDocks, int nMobiles) {
        this.simulation = new Simulation();

        this.warehouse = this.createWarehouse(width, depth, height);
        this.stock = new Stock();

        this.docks = new ArrayList<>();
        this.lifts = new ArrayList<>();
        this.mobiles = new ArrayList<>();

        int productionLineX = width - 70, productionLineY = 20, productionLineWidth = 50, productionLineDepth = 100;

        ArrayList<Position> productionLineStartBuffer = new ArrayList<>();
        ArrayList<Position> productionLineEndBuffer = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            productionLineStartBuffer.add(new Position(productionLineX - 2 * this.palletSize + i / 5 * this.palletSize, productionLineY + (i % 5) * this.palletSize));
            productionLineEndBuffer.add(new Position(productionLineX + (i % 5) * this.palletSize, productionLineY + productionLineDepth + i / 5 * this.palletSize));
        }

        this.productionLine = this.addProductionLine(productionLineX, productionLineY, productionLineX + productionLineWidth, productionLineY + productionLineDepth, 10, productionLineStartBuffer, productionLineEndBuffer);

        this.addStockSection(20, 20, 120, 120, 20, true);
        this.addStockSection(20, 150, 120, 250, 20, true);

        this.addAutoStockSection(150, 20, 270, 200, 40, true, false, false, true, true);

        this.addStockSection(300, 20, 460, 160, 20, false);

        for (int i = 0; i < nDocks - 2; i++) this.addOutdoorDock(i * this.dockWidth, depth);
        for (int i = 0; i < 2; i++) this.addIndoorDock(300 + i * this.dockWidth * 2, depth - truckDepth);
        for (int i = 0; i < nMobiles; i++)
            this.mobiles.add(new Mobile(new Position(dockWidth * i, depth - this.palletSize)));

        this.mobileMissionSelector = new MobileMissionMatchingSelector(this.warehouse);
        this.palletPositionSelector = new ClosestPositionSelector(this.warehouse);
        this.truckDockSelector = new NaiveSelector();

        this.controller = new Controller(this, this.mobileMissionSelector, this.truckDockSelector, this.palletPositionSelector);
    }

    public Configuration(int nDocks, int nMobiles) {
        this(600, 300, 40, nDocks, nMobiles);
    }

    private Warehouse createWarehouse(int width, int depth, int height) {
        Warehouse warehouse = new Warehouse(width, depth, height);

        for (int x=0; x<width; x++) {
            for (int y=0; y<depth; y++) {
                warehouse.addEdge(new Position(x, y, 0), new Position(x+this.palletSize, y, 0));
                warehouse.addEdge(new Position(x, y, 0), new Position(x-this.palletSize, y, 0));
                warehouse.addEdge(new Position(x, y, 0), new Position(x, y+this.palletSize, 0));
                warehouse.addEdge(new Position(x, y, 0), new Position(x, y-this.palletSize, 0));
            }
        }

        return warehouse;
    }

    private void removeGraphEdges(int x1, int y1, int x2, int y2) {
        for (int x=x1; x<x2; x++) {
            for (int y=y1; y<y2; y++) {
                this.warehouse.removeEdge(new Position(x, y, 0), new Position(x+this.palletSize, y, 0));
                this.warehouse.removeEdge(new Position(x, y, 0), new Position(x-this.palletSize, y, 0));
                this.warehouse.removeEdge(new Position(x, y, 0), new Position(x, y+this.palletSize, 0));
                this.warehouse.removeEdge(new Position(x, y, 0), new Position(x, y-this.palletSize, 0));
            }
        }
    }

    public void addStockSection(int x1, int y1, int x2, int y2, int height, boolean vertical) {
        this.removeGraphEdges(x1, y1, x2, y2);

        boolean startWall = (vertical && x1 == 0) || (!vertical && y1 == 0);

        if (vertical) {
            for (int x=x1; x<x2; x += this.palletSize) {
                int typeX = ((x - x1) / this.palletSize) % 4;
                if ((startWall && (typeX == 0 || typeX == 3)) || (!startWall && (typeX == 0 || typeX == 1))) {
                    this.warehouse.removeEdge(new Position(x, y1-this.palletSize, 0), new Position(x, y1, 0));
                    this.warehouse.removeEdge(new Position(x, y2, 0), new Position(x, y2-this.palletSize, 0));
                }
            }
        } else {
            for (int y=y1; y<y2; y += this.palletSize) {
                int typeY = ((y - y1) / this.palletSize) % 4;
                if ((startWall && (typeY == 0 || typeY == 3)) || (!startWall && (typeY == 0 || typeY == 1))) {
                    this.warehouse.removeEdge(new Position(x1-this.palletSize, y, 0), new Position(x1, y, 0));
                    this.warehouse.removeEdge(new Position(x2, y, 0), new Position(x2-this.palletSize, y, 0));
                }
            }
        }

        for (int x=x1; x<x2; x += this.palletSize) {
            int typeX = ((x - x1) / this.palletSize) % 4;
            for (int y=y1; y<y2; y += this.palletSize) {
                int typeY = ((y - y1) / this.palletSize) % 4;
                if (vertical) {
                    if (startWall) {
                        if (typeX == 0 || typeX == 3) {
                            if (typeX == 0) this.warehouse.addEdge(new Position(x, y, 0), new Position(x+this.palletSize, y, 0));
                            else if (typeX == 3) this.warehouse.addEdge(new Position(x, y, 0), new Position(x-this.palletSize, y, 0));
                            for (int z=0; z<height; z+=this.palletSize) {
                                this.stock.addStockPosition(new Position(x, y, z));
                                this.warehouse.addEdge(new Position(x, y, z), new Position(x, y, z-this.palletSize));
                                if (z+this.palletSize < height) this.warehouse.addEdge(new Position(x, y, z), new Position(x, y, z+this.palletSize));
                            }
                        } else {
                            this.warehouse.addEdge(new Position(x, y, 0), new Position(x+this.palletSize, y, 0));
                            this.warehouse.addEdge(new Position(x, y, 0), new Position(x-this.palletSize, y, 0));
                            if (typeX == 1) this.warehouse.addEdge(new Position(x, y, 0), new Position(x, y+this.palletSize, 0));
                            else if (typeX == 2) this.warehouse.addEdge(new Position(x, y, 0), new Position(x, y-this.palletSize, 0));
                        }
                    } else {
                        if (typeX == 0 || typeX == 1) {
                            if (typeX == 0) this.warehouse.addEdge(new Position(x, y, 0), new Position(x-this.palletSize, y, 0));
                            else if (typeX == 1) this.warehouse.addEdge(new Position(x, y, 0), new Position(x+this.palletSize, y, 0));
                            for (int z=0; z<height; z+=this.palletSize) {
                                this.stock.addStockPosition(new Position(x, y, z));
                                this.warehouse.addEdge(new Position(x, y, z), new Position(x, y, z-this.palletSize));
                                if (z+this.palletSize < height) this.warehouse.addEdge(new Position(x, y, z), new Position(x, y, z+this.palletSize));
                            }
                        } else {
                            this.warehouse.addEdge(new Position(x, y, 0), new Position(x+this.palletSize, y, 0));
                            this.warehouse.addEdge(new Position(x, y, 0), new Position(x-this.palletSize, y, 0));
                            if (typeX == 2) this.warehouse.addEdge(new Position(x, y, 0), new Position(x, y+this.palletSize, 0));
                            else if (typeX == 3) this.warehouse.addEdge(new Position(x, y, 0), new Position(x, y-this.palletSize, 0));
                        }
                    }
                } else {
                    if (startWall) {
                        if (typeY == 0 || typeY == 3) {
                            if (typeY == 0) this.warehouse.addEdge(new Position(x, y, 0), new Position(x, y+this.palletSize, 0));
                            else if (typeY == 3) this.warehouse.addEdge(new Position(x, y, 0), new Position(x, y-this.palletSize, 0));
                            for (int z=0; z<height; z+=this.palletSize) {
                                this.stock.addStockPosition(new Position(x, y, z));
                                this.warehouse.addEdge(new Position(x, y, z), new Position(x, y, z-this.palletSize));
                                if (z+this.palletSize < height) this.warehouse.addEdge(new Position(x, y, z), new Position(x, y, z+this.palletSize));
                            }
                        } else {
                            this.warehouse.addEdge(new Position(x, y, 0), new Position(x, y+this.palletSize, 0));
                            this.warehouse.addEdge(new Position(x, y, 0), new Position(x, y-this.palletSize, 0));
                            if (typeY == 1) this.warehouse.addEdge(new Position(x, y, 0), new Position(x+this.palletSize, y, 0));
                            else if (typeY == 2) this.warehouse.addEdge(new Position(x, y, 0), new Position(x-this.palletSize, y, 0));
                        }
                    } else {
                        if (typeY == 0 || typeY == 1) {
                            if (typeY == 0) this.warehouse.addEdge(new Position(x, y, 0), new Position(x, y-this.palletSize, 0));
                            else if (typeY == 1) this.warehouse.addEdge(new Position(x, y, 0), new Position(x, y+this.palletSize, 0));
                            for (int z=0; z<height; z+=this.palletSize) {
                                this.stock.addStockPosition(new Position(x, y, z));
                                this.warehouse.addEdge(new Position(x, y, z), new Position(x, y, z-this.palletSize));
                                if (z+this.palletSize < height) this.warehouse.addEdge(new Position(x, y, z), new Position(x, y, z+this.palletSize));
                            }
                        } else {
                            this.warehouse.addEdge(new Position(x, y, 0), new Position(x, y+this.palletSize, 0));
                            this.warehouse.addEdge(new Position(x, y, 0), new Position(x, y - this.palletSize, 0));
                            if (typeY == 2)
                                this.warehouse.addEdge(new Position(x, y, 0), new Position(x + this.palletSize, y, 0));
                            else if (typeY == 3)
                                this.warehouse.addEdge(new Position(x, y, 0), new Position(x - this.palletSize, y, 0));
                        }
                    }
                }
            }
        }
    }

    public void addAutoStockSection(int x1, int y1, int x2, int y2, int height, boolean vertical, boolean liftNW, boolean liftNE, boolean liftSW, boolean liftSE) {
        this.removeGraphEdges(x1, y1, x2, y2);

        int xStart = (liftNW || liftSW) ? x1 + this.palletSize : x1;
        int xEnd = (liftNE || liftSE) ? x2 - this.palletSize : x2;
        int yStart = (liftNW || liftNE) ? y1 + this.palletSize : y1;
        int yEnd = (liftSW || liftSE) ? y2 - this.palletSize : y2;

        for (int x = xStart; x < xEnd; x += this.palletSize) {
            for (int y = yStart; y < yEnd; y += this.palletSize) {
                for (int z = 0; z < height; z += this.palletSize) {
                    this.stock.addStockPosition(new Position(x, y, z));
                }
            }
        }

        for (int x = x1; x < x2; x += this.palletSize) {
            if (vertical) {
                if (x >= xStart && x < xEnd) {
                    this.warehouse.addEdge(new Position(x, y1, 0), new Position(x, y1 - this.palletSize, 0));
                    this.warehouse.addEdge(new Position(x, y2 - this.palletSize, 0), new Position(x, y2, 0));
                } else if (x < xStart) { // remove edges (horizontal ones)
                    this.warehouse.removeEdge(new Position(x - this.palletSize, y1, 0), new Position(x, y1, 0));
                    this.warehouse.removeEdge(new Position(x - this.palletSize, y2 - this.palletSize, 0), new Position(x, y2 - this.palletSize, 0));
                } else if (x >= xEnd) {
                    this.warehouse.removeEdge(new Position(x + this.palletSize, y1, 0), new Position(x, y1, 0));
                    this.warehouse.removeEdge(new Position(x + this.palletSize, y2 - this.palletSize, 0), new Position(x, y2 - this.palletSize, 0));
                }
                for (int z = 0; z < height; z += this.palletSize) {
                    if (x >= xStart && x < xEnd) {
                        this.warehouse.addEdge(new Position(x, y1, z), new Position(x, y1 + this.palletSize, z));
                        this.warehouse.addEdge(new Position(x, y2 - this.palletSize, z), new Position(x, y2 - 2 * this.palletSize, z));
                    }
                    if (z > 0) {
                        if (x + this.palletSize < x2) {
                            if (y1 < yStart)
                                this.warehouse.addEdge(new Position(x, y1, z), new Position(x + this.palletSize, y1, z));
                            if (y2 > yEnd)
                                this.warehouse.addEdge(new Position(x, y2 - this.palletSize, z), new Position(x + this.palletSize, y2 - this.palletSize, z));
                        }
                        if (x - this.palletSize >= x1) {
                            if (y1 < yStart)
                                this.warehouse.addEdge(new Position(x, y1, z), new Position(x - this.palletSize, y1, z));
                            if (y2 > yEnd)
                                this.warehouse.addEdge(new Position(x, y2 - this.palletSize, z), new Position(x - this.palletSize, y2 - this.palletSize, z));
                        }
                    }
                }
            } else {
                this.warehouse.removeEdge(new Position(x, y1 - this.palletSize, 0), new Position(x, y1, 0));
                this.warehouse.removeEdge(new Position(x, y2, 0), new Position(x, y2 - this.palletSize, 0));
            }
        }

        for (int y = y1; y < y2; y += this.palletSize) {
            if (vertical) {
                this.warehouse.removeEdge(new Position(x1 - this.palletSize, y, 0), new Position(x1, y, 0));
                this.warehouse.removeEdge(new Position(x2, y, 0), new Position(x2 - this.palletSize, y, 0));
            } else {
                if (y >= yStart && y < yEnd) {
                    this.warehouse.addEdge(new Position(x1, y, 0), new Position(x1 - this.palletSize, y, 0));
                    this.warehouse.addEdge(new Position(x2 - this.palletSize, y, 0), new Position(x2, y, 0));
                } else if (y < yStart) { // remove edges (vertical ones)
                    this.warehouse.removeEdge(new Position(x1, y - this.palletSize, 0), new Position(x1, y, 0));
                    this.warehouse.removeEdge(new Position(x2 - this.palletSize, y - this.palletSize, 0), new Position(x2 - this.palletSize, y, 0));
                } else if (y >= yEnd) {
                    this.warehouse.removeEdge(new Position(x1, y + this.palletSize, 0), new Position(x1, y, 0));
                    this.warehouse.removeEdge(new Position(x2 - this.palletSize, y + this.palletSize, 0), new Position(x2 - this.palletSize, y, 0));
                }
                for (int z = 0; z < height; z += this.palletSize) {
                    if (y >= yStart && y < yEnd) {
                        this.warehouse.addEdge(new Position(x1, y, z), new Position(x1 + this.palletSize, y, z));
                        this.warehouse.addEdge(new Position(x2 - this.palletSize, y, z), new Position(x2 - 2 * this.palletSize, y, z));
                    }
                    if (z > 0) {
                        if (y + this.palletSize < y2) {
                            if (x1 < xStart)
                                this.warehouse.addEdge(new Position(x1, y, z), new Position(x1, y + this.palletSize, z));
                            if (x2 > xEnd)
                                this.warehouse.addEdge(new Position(x2 - this.palletSize, y, z), new Position(x2 - this.palletSize, y + this.palletSize, z));
                        }
                        if (y - this.palletSize >= y1) {
                            if (x1 < xStart)
                                this.warehouse.addEdge(new Position(x1, y, z), new Position(x1, y - this.palletSize, z));
                            if (x2 > xEnd)
                                this.warehouse.addEdge(new Position(x2 - this.palletSize, y, z), new Position(x2 - this.palletSize, y - this.palletSize, z));
                        }
                    }
                }
            }
        }

        // lift vertical edges + exits
        if (liftNW) {
            this.lifts.add(new Lift(new Position(x1, y1, 0), height));
            if (vertical) {
                this.warehouse.addEdge(new Position(x1, y1, 0), new Position(x1, y1 + this.palletSize, 0));
                this.warehouse.addEdge(new Position(x1, y1 + this.palletSize, 0), new Position(x1 - this.palletSize, y1 + this.palletSize, 0));
            } else {
                this.warehouse.addEdge(new Position(x1, y1, 0), new Position(x1 + this.palletSize, y1, 0));
                this.warehouse.addEdge(new Position(x1 + this.palletSize, y1, 0), new Position(x1 + this.palletSize, y1 - this.palletSize, 0));
            }
            for (int z = 0; z < height; z += this.palletSize) {
                this.warehouse.addEdge(new Position(x1, y1, z), new Position(x1, y1, z - this.palletSize));
                if (z + this.palletSize < height) {
                    this.warehouse.addEdge(new Position(x1, y1, z), new Position(x1, y1, z + this.palletSize));
                }
            }
        }
        if (liftNE) {
            this.lifts.add(new Lift(new Position(x2 - this.palletSize, y1, 0), height));
            if (vertical) {
                this.warehouse.addEdge(new Position(x2 - this.palletSize, y1, 0), new Position(x2 - this.palletSize, y1 + this.palletSize, 0));
                this.warehouse.addEdge(new Position(x2 - this.palletSize, y1 + this.palletSize, 0), new Position(x2, y1 + this.palletSize, 0));
            } else {
                this.warehouse.addEdge(new Position(x2 - this.palletSize, y1, 0), new Position(x2 - 2 * this.palletSize, y1, 0));
                this.warehouse.addEdge(new Position(x2 - 2 * this.palletSize, y1, 0), new Position(x2 - 2 * this.palletSize, y1 - this.palletSize, 0));
            }
            for (int z = 0; z < height; z += this.palletSize) {
                this.warehouse.addEdge(new Position(x2 - this.palletSize, y1, z), new Position(x2 - this.palletSize, y1, z - this.palletSize));
                if (z + this.palletSize < height) {
                    this.warehouse.addEdge(new Position(x2 - this.palletSize, y1, z), new Position(x2 - this.palletSize, y1, z + this.palletSize));
                }
            }
        }
        if (liftSW) {
            this.lifts.add(new Lift(new Position(x1, y2 - this.palletSize, 0), height));
            if (vertical) {
                this.warehouse.addEdge(new Position(x1, y2 - this.palletSize, 0), new Position(x1, y2 - 2 * this.palletSize, 0));
                this.warehouse.addEdge(new Position(x1, y2 - 2 * this.palletSize, 0), new Position(x1 - this.palletSize, y2 - 2 * this.palletSize, 0));
            } else {
                this.warehouse.addEdge(new Position(x1, y2 - this.palletSize, 0), new Position(x1 + this.palletSize, y2 - this.palletSize, 0));
                this.warehouse.addEdge(new Position(x1 + this.palletSize, y2 - this.palletSize, 0), new Position(x1 + this.palletSize, y2, 0));
            }
            for (int z = 0; z < height; z += this.palletSize) {
                this.warehouse.addEdge(new Position(x1, y2 - this.palletSize, z), new Position(x1, y2 - this.palletSize, z - this.palletSize));
                if (z + this.palletSize < height) {
                    this.warehouse.addEdge(new Position(x1, y2 - this.palletSize, z), new Position(x1, y2 - this.palletSize, z + this.palletSize));
                }
            }
        }
        if (liftSE) {
            this.lifts.add(new Lift(new Position(x2 - this.palletSize, y2 - this.palletSize, 0), height));
            if (vertical) {
                this.warehouse.addEdge(new Position(x2 - this.palletSize, y2 - this.palletSize, 0), new Position(x2 - this.palletSize, y2 - 2 * this.palletSize, 0));
                this.warehouse.addEdge(new Position(x2 - this.palletSize, y2 - 2 * this.palletSize, 0), new Position(x2, y2 - 2 * this.palletSize, 0));
            } else {
                this.warehouse.addEdge(new Position(x2 - this.palletSize, y2 - this.palletSize, 0), new Position(x2 - 2 * this.palletSize, y2 - this.palletSize, 0));
                this.warehouse.addEdge(new Position(x2 - 2 * this.palletSize, y2 - this.palletSize, 0), new Position(x2 - 2 * this.palletSize, y2, 0));
            }
            for (int z = 0; z < height; z += this.palletSize) {
                this.warehouse.addEdge(new Position(x2 - this.palletSize, y2 - this.palletSize, z), new Position(x2 - this.palletSize, y2 - this.palletSize, z - this.palletSize));
                if (z + this.palletSize < height) {
                    this.warehouse.addEdge(new Position(x2 - this.palletSize, y2 - this.palletSize, z), new Position(x2 - this.palletSize, y2 - this.palletSize, z + this.palletSize));
                }
            }
        }

        for (int x = x1; x < x2; x += this.palletSize) {
            for (int y = y1; y < y2; y += this.palletSize) {
                for (int z = 0; z < height; z += this.palletSize) {
                    if (z > 0 && (x < xStart || x >= xEnd || y < yStart || y >= yEnd)) continue;
                    if (vertical) {
                        if (y + this.palletSize < y2)
                            this.warehouse.addEdge(new Position(x, y, z), new Position(x, y + this.palletSize, z));
                        if (y - this.palletSize >= y1)
                            this.warehouse.addEdge(new Position(x, y, z), new Position(x, y - this.palletSize, z));
                    } else {
                        if (x + this.palletSize < x2)
                            this.warehouse.addEdge(new Position(x, y, z), new Position(x + this.palletSize, y, z));
                        if (x - this.palletSize >= x1)
                            this.warehouse.addEdge(new Position(x, y, z), new Position(x - this.palletSize, y, z));
                    }
                }
            }
        }
    }

    public void addOutdoorDock(int x1, int y1) {
        this.docks.add(new Dock(new Position(x1, y1)));

        int depth = this.warehouse.getDepth();
        int x2 = x1 + this.truckWidth, y2 = y1 + this.truckDepth;

        for (int x=x1; x<x2; x+= this.palletSize) {
            this.warehouse.addEdge(new Position(x, depth-this.palletSize, 0), new Position(x, depth, 0), true);
        }

        for (int x=x1; x<x2; x+= this.palletSize) {
            for (int y=y1; y<y2; y+= this.palletSize) {
                if (y+this.palletSize < y2) this.warehouse.addEdge(new Position(x, y, 0), new Position(x, y+this.palletSize, 0), true);
                this.warehouse.addEdge(new Position(x, y, 0), new Position(x, y-this.palletSize, 0), true);
                if (x+this.palletSize < x2) this.warehouse.addEdge(new Position(x, y, 0), new Position(x+this.palletSize, y, 0), true);
                if (x-this.palletSize >= x1) this.warehouse.addEdge(new Position(x, y, 0), new Position(x-this.palletSize, y, 0), true);
            }
        }
    }

    public void addIndoorDock(int x1, int y1) {
        this.docks.add(new Dock(new Position(x1, y1)));
        int x2 = x1 + this.truckWidth, y2 = y1 + this.truckDepth;

        // remove all edges and add condition to cross edges inside truck (mobile with mission of the given truck)

        for (int x=x1; x<x2; x += this.palletSize) {
            this.warehouse.removeEdge(new Position(x, y1-this.palletSize, 0), new Position(x, y1, 0));
            this.warehouse.removeEdge(new Position(x, y1, 0), new Position(x, y1-this.palletSize, 0));
            this.warehouse.removeEdge(new Position(x, y2, 0), new Position(x, y2-this.palletSize, 0));
            this.warehouse.removeEdge(new Position(x, y2-this.palletSize, 0), new Position(x, y2, 0));
        }
    }

    public ProductionLine addProductionLine(int x1, int y1, int x2, int y2, int capacity, ArrayList<Position> startBuffer, ArrayList<Position> endBuffer) {
        this.removeGraphEdges(x1, y1, x2, y2);

        for (int x=x1; x<x2; x += this.palletSize) {
            this.warehouse.removeEdge(new Position(x, y1-this.palletSize, 0), new Position(x, y1, 0));
            this.warehouse.removeEdge(new Position(x, y2, 0), new Position(x, y2-this.palletSize, 0));
        }
        for (int y=y1; y<y2; y += this.palletSize) {
            this.warehouse.removeEdge(new Position(x1-this.palletSize, y, 0), new Position(x1, y, 0));
            this.warehouse.removeEdge(new Position(x2, y, 0), new Position(x2-this.palletSize, y, 0));
        }

        for (Position position : startBuffer) {
            this.stock.addBufferPosition(position);
        }

        for (Position position : endBuffer) {
            this.stock.addBufferPosition(position);
        }

        return new ProductionLine(this.stock, new Position(x1, y1), x2-x1, y2-y1, capacity, startBuffer, endBuffer);
    }

}
