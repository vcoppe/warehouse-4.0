package warehouse;

import agent.*;
import brain.*;
import pathfinding.*;
import simulation.Simulation;
import util.Vector3D;

import java.util.ArrayList;

public class Configuration {

    public final Simulation simulation;
    public final Controller controller;
    public final Warehouse warehouse;
    public final Stock stock;
    public final ArrayList<ProductionLine> productionLines;
    public final ArrayList<Dock> docks;
    public final ArrayList<Lift> lifts;
    public final ArrayList<Mobile> mobiles;

    public final ProductClustering productClustering;
    public final ClusterLocationAssignment clusterLocationAssignment;
    public final MobileMissionSelector mobileMissionSelector;
    public final PalletPositionSelector palletPositionSelector;
    public final TruckDockSelector truckDockSelector;

    public static final int palletSize = 10;
    public static int dockWidth = 3 * palletSize;
    public static int truckWidth = dockWidth;
    public static int truckDepth = 3 * truckWidth;

    public Configuration(int width, int depth, int height) {
        this.simulation = new Simulation();

        this.warehouse = this.createWarehouse(width, depth, height);
        this.stock = new Stock(this.warehouse.getGraph());

        this.productionLines = new ArrayList<>();
        this.docks = new ArrayList<>();
        this.lifts = new ArrayList<>();
        this.mobiles = new ArrayList<>();

        this.productClustering = new DedicatedStorage();
        this.clusterLocationAssignment = new SLAP();
        this.mobileMissionSelector = new MobileMissionMatchingSelector(this.warehouse);
        //this.mobileMissionSelector = new MobileMissionAnticipationMatchingSelector(this.warehouse);
        this.palletPositionSelector = new ClosestPositionSelector(this.warehouse);
        this.truckDockSelector = new NaiveSelector();

        this.controller = new Controller(this, this.mobileMissionSelector, this.truckDockSelector, this.palletPositionSelector);
    }

    public Configuration() {
        this(300, 200, 20);

        int width = this.warehouse.getWidth(), depth = this.warehouse.getDepth();

        int productionLineX = width - 70, productionLineY = 20, productionLineWidth = 50, productionLineDepth = 100;

        ArrayList<Vector3D> productionLineStartBuffer = new ArrayList<>();
        ArrayList<Vector3D> productionLineEndBuffer = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            productionLineStartBuffer.add(new Vector3D(productionLineX - (i / 2 + 1) * palletSize, productionLineY + (1 + 2 * (i % 2)) * palletSize));
            productionLineEndBuffer.add(new Vector3D(productionLineX + (1 + 2 * (i % 2)) * palletSize, productionLineY + productionLineDepth + i / 2 * palletSize));
        }

        this.addProductionLine(productionLineX, productionLineY, productionLineX + productionLineWidth, productionLineY + productionLineDepth, 10, productionLineStartBuffer, productionLineEndBuffer);

        this.addStockSection(20, 20, 120, 120, 20, true);

        for (int i = 0; i < 5; i++) this.addOutdoorDock(i * dockWidth, depth);
        for (int i = 0; i < 5; i++) this.addMobile(new Vector3D(dockWidth * i, depth - palletSize));
    }

    private Warehouse createWarehouse(int width, int depth, int height) {
        Warehouse warehouse = new Warehouse(width, depth, height);

        for (int x = 0; x < width; x += palletSize) {
            for (int y = 0; y < depth; y += palletSize) {
                warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x + palletSize, y, 0));
                warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x - palletSize, y, 0));
                warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y + palletSize, 0));
                warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y - palletSize, 0));
            }
        }

        return warehouse;
    }

    private void removeGraphEdges(int x1, int y1, int x2, int y2) {
        for (int x=x1; x<x2; x++) {
            for (int y=y1; y<y2; y++) {
                this.warehouse.removeEdge(new Vector3D(x, y, 0), new Vector3D(x + palletSize, y, 0));
                this.warehouse.removeEdge(new Vector3D(x, y, 0), new Vector3D(x - palletSize, y, 0));
                this.warehouse.removeEdge(new Vector3D(x, y, 0), new Vector3D(x, y + palletSize, 0));
                this.warehouse.removeEdge(new Vector3D(x, y, 0), new Vector3D(x, y - palletSize, 0));
            }
        }
    }

    public void addStockSection(int x1, int y1, int x2, int y2, int height, boolean vertical) {
        this.removeGraphEdges(x1, y1, x2, y2);

        boolean startWall = (vertical && x1 == 0) || (!vertical && y1 == 0);

        if (vertical) {
            for (int x = x1; x < x2; x += palletSize) {
                int typeX = ((x - x1) / palletSize) % 4;
                if ((startWall && (typeX == 0 || typeX == 3)) || (!startWall && (typeX == 0 || typeX == 1))) {
                    this.warehouse.removeEdge(new Vector3D(x, y1 - palletSize, 0), new Vector3D(x, y1, 0));
                    this.warehouse.removeEdge(new Vector3D(x, y2, 0), new Vector3D(x, y2 - palletSize, 0));
                }
            }
        } else {
            for (int y = y1; y < y2; y += palletSize) {
                int typeY = ((y - y1) / palletSize) % 4;
                if ((startWall && (typeY == 0 || typeY == 3)) || (!startWall && (typeY == 0 || typeY == 1))) {
                    this.warehouse.removeEdge(new Vector3D(x1 - palletSize, y, 0), new Vector3D(x1, y, 0));
                    this.warehouse.removeEdge(new Vector3D(x2, y, 0), new Vector3D(x2 - palletSize, y, 0));
                }
            }
        }

        for (int x = x1; x < x2; x += palletSize) {
            int typeX = ((x - x1) / palletSize) % 4;
            for (int y = y1; y < y2; y += palletSize) {
                int typeY = ((y - y1) / palletSize) % 4;
                if (vertical) {
                    if (startWall) {
                        if (typeX == 0 || typeX == 3) {
                            if (typeX == 0)
                                this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x + palletSize, y, 0));
                            else if (typeX == 3)
                                this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x - palletSize, y, 0));
                            for (int z = 0; z < height; z += palletSize) {
                                this.stock.addStockPosition(new Vector3D(x, y, z));
                                this.warehouse.addEdge(new Vector3D(x, y, z), new Vector3D(x, y, z - palletSize));
                                if (z + palletSize < height)
                                    this.warehouse.addEdge(new Vector3D(x, y, z), new Vector3D(x, y, z + palletSize));
                            }
                        } else {
                            this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x + palletSize, y, 0));
                            this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x - palletSize, y, 0));
                            this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y + palletSize, 0));
                            this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y - palletSize, 0));
                        }
                    } else {
                        if (typeX == 0 || typeX == 1) {
                            if (typeX == 0)
                                this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x - palletSize, y, 0));
                            else if (typeX == 1)
                                this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x + palletSize, y, 0));
                            for (int z = 0; z < height; z += palletSize) {
                                this.stock.addStockPosition(new Vector3D(x, y, z));
                                this.warehouse.addEdge(new Vector3D(x, y, z), new Vector3D(x, y, z - palletSize));
                                if (z + palletSize < height)
                                    this.warehouse.addEdge(new Vector3D(x, y, z), new Vector3D(x, y, z + palletSize));
                            }
                        } else {
                            this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x + palletSize, y, 0));
                            this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x - palletSize, y, 0));
                            this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y + palletSize, 0));
                            this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y - palletSize, 0));
                        }
                    }
                } else {
                    if (startWall) {
                        if (typeY == 0 || typeY == 3) {
                            if (typeY == 0)
                                this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y + palletSize, 0));
                            else if (typeY == 3)
                                this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y - palletSize, 0));
                            for (int z = 0; z < height; z += palletSize) {
                                this.stock.addStockPosition(new Vector3D(x, y, z));
                                this.warehouse.addEdge(new Vector3D(x, y, z), new Vector3D(x, y, z - palletSize));
                                if (z + palletSize < height)
                                    this.warehouse.addEdge(new Vector3D(x, y, z), new Vector3D(x, y, z + palletSize));
                            }
                        } else {
                            this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y + palletSize, 0));
                            this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y - palletSize, 0));
                            this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x + palletSize, y, 0));
                            this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x - palletSize, y, 0));
                        }
                    } else {
                        if (typeY == 0 || typeY == 1) {
                            if (typeY == 0)
                                this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y - palletSize, 0));
                            else if (typeY == 1)
                                this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y + palletSize, 0));
                            for (int z = 0; z < height; z += palletSize) {
                                this.stock.addStockPosition(new Vector3D(x, y, z));
                                this.warehouse.addEdge(new Vector3D(x, y, z), new Vector3D(x, y, z - palletSize));
                                if (z + palletSize < height)
                                    this.warehouse.addEdge(new Vector3D(x, y, z), new Vector3D(x, y, z + palletSize));
                            }
                        } else {
                            this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y + palletSize, 0));
                            this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y - palletSize, 0));
                            this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x + palletSize, y, 0));
                            this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x - palletSize, y, 0));
                        }
                    }
                }
            }
        }

        this.controller.closestOpenPositionFinder.updatePositions();
    }

    public void addAutoStockSection(int x1, int y1, int x2, int y2, int height, boolean vertical, boolean liftNW, boolean liftNE, boolean liftSW, boolean liftSE) {
        this.removeGraphEdges(x1, y1, x2, y2);

        int xStart = (liftNW || liftSW) ? x1 + palletSize : x1;
        int xEnd = (liftNE || liftSE) ? x2 - palletSize : x2;
        int yStart = (liftNW || liftNE) ? y1 + palletSize : y1;
        int yEnd = (liftSW || liftSE) ? y2 - palletSize : y2;

        for (int x = xStart; x < xEnd; x += palletSize) {
            for (int y = yStart; y < yEnd; y += palletSize) {
                for (int z = 0; z < height; z += palletSize) {
                    this.stock.addStockPosition(new Vector3D(x, y, z));
                }
            }
        }

        ArrayList<Edge> edges = new ArrayList<>();

        for (int x = x1; x < x2; x += palletSize) {
            if (vertical) {
                if (x >= xStart && x < xEnd) {
                    edges.add(this.warehouse.addEdge(new Vector3D(x, y1, 0), new Vector3D(x, y1 - palletSize, 0)));
                    edges.add(this.warehouse.addEdge(new Vector3D(x, y2 - palletSize, 0), new Vector3D(x, y2, 0)));
                    edges.add(this.warehouse.getEdge(new Vector3D(x, y1 - palletSize, 0), new Vector3D(x, y1, 0)));
                    edges.add(this.warehouse.getEdge(new Vector3D(x, y2, 0), new Vector3D(x, y2 - palletSize, 0)));
                } else if (x < xStart) { // remove edges (horizontal ones)
                    this.warehouse.removeEdge(new Vector3D(x - palletSize, y1, 0), new Vector3D(x, y1, 0));
                    this.warehouse.removeEdge(new Vector3D(x - palletSize, y2 - palletSize, 0), new Vector3D(x, y2 - palletSize, 0));
                } else if (x >= xEnd) {
                    this.warehouse.removeEdge(new Vector3D(x + palletSize, y1, 0), new Vector3D(x, y1, 0));
                    this.warehouse.removeEdge(new Vector3D(x + palletSize, y2 - palletSize, 0), new Vector3D(x, y2 - palletSize, 0));
                }
                for (int z = 0; z < height; z += palletSize) {
                    if (x >= xStart && x < xEnd) {
                        edges.add(this.warehouse.addEdge(new Vector3D(x, y1, z), new Vector3D(x, y1 + palletSize, z)));
                        edges.add(this.warehouse.addEdge(new Vector3D(x, y2 - palletSize, z), new Vector3D(x, y2 - 2 * palletSize, z)));
                    }
                    if (z > 0) {
                        if (x + palletSize < x2) {
                            if (y1 < yStart)
                                this.warehouse.addEdge(new Vector3D(x, y1, z), new Vector3D(x + palletSize, y1, z));
                            if (y2 > yEnd)
                                this.warehouse.addEdge(new Vector3D(x, y2 - palletSize, z), new Vector3D(x + palletSize, y2 - palletSize, z));
                        }
                        if (x - palletSize >= x1) {
                            if (y1 < yStart)
                                this.warehouse.addEdge(new Vector3D(x, y1, z), new Vector3D(x - palletSize, y1, z));
                            if (y2 > yEnd)
                                this.warehouse.addEdge(new Vector3D(x, y2 - palletSize, z), new Vector3D(x - palletSize, y2 - palletSize, z));
                        }
                    }
                }
            } else {
                this.warehouse.removeEdge(new Vector3D(x, y1 - palletSize, 0), new Vector3D(x, y1, 0));
                this.warehouse.removeEdge(new Vector3D(x, y2, 0), new Vector3D(x, y2 - palletSize, 0));
            }
        }

        for (int y = y1; y < y2; y += palletSize) {
            if (vertical) {
                this.warehouse.removeEdge(new Vector3D(x1 - palletSize, y, 0), new Vector3D(x1, y, 0));
                this.warehouse.removeEdge(new Vector3D(x2, y, 0), new Vector3D(x2 - palletSize, y, 0));
            } else {
                if (y >= yStart && y < yEnd) {
                    edges.add(this.warehouse.addEdge(new Vector3D(x1, y, 0), new Vector3D(x1 - palletSize, y, 0)));
                    edges.add(this.warehouse.addEdge(new Vector3D(x2 - palletSize, y, 0), new Vector3D(x2, y, 0)));
                    edges.add(this.warehouse.getEdge(new Vector3D(x1 - palletSize, y, 0), new Vector3D(x1, y, 0)));
                    edges.add(this.warehouse.getEdge(new Vector3D(x2, y, 0), new Vector3D(x2 - palletSize, y, 0)));
                } else if (y < yStart) { // remove edges (vertical ones)
                    this.warehouse.removeEdge(new Vector3D(x1, y - palletSize, 0), new Vector3D(x1, y, 0));
                    this.warehouse.removeEdge(new Vector3D(x2 - palletSize, y - palletSize, 0), new Vector3D(x2 - palletSize, y, 0));
                } else if (y >= yEnd) {
                    this.warehouse.removeEdge(new Vector3D(x1, y + palletSize, 0), new Vector3D(x1, y, 0));
                    this.warehouse.removeEdge(new Vector3D(x2 - palletSize, y + palletSize, 0), new Vector3D(x2 - palletSize, y, 0));
                }
                for (int z = 0; z < height; z += palletSize) {
                    if (y >= yStart && y < yEnd) {
                        edges.add(this.warehouse.addEdge(new Vector3D(x1, y, z), new Vector3D(x1 + palletSize, y, z)));
                        edges.add(this.warehouse.addEdge(new Vector3D(x2 - palletSize, y, z), new Vector3D(x2 - 2 * palletSize, y, z)));
                    }
                    if (z > 0) {
                        if (y + palletSize < y2) {
                            if (x1 < xStart)
                                this.warehouse.addEdge(new Vector3D(x1, y, z), new Vector3D(x1, y + palletSize, z));
                            if (x2 > xEnd)
                                this.warehouse.addEdge(new Vector3D(x2 - palletSize, y, z), new Vector3D(x2 - palletSize, y + palletSize, z));
                        }
                        if (y - palletSize >= y1) {
                            if (x1 < xStart)
                                this.warehouse.addEdge(new Vector3D(x1, y, z), new Vector3D(x1, y - palletSize, z));
                            if (x2 > xEnd)
                                this.warehouse.addEdge(new Vector3D(x2 - palletSize, y, z), new Vector3D(x2 - palletSize, y - palletSize, z));
                        }
                    }
                }
            }
        }

        // lift vertical edges + exits
        if (liftNW) {
            this.lifts.add(new Lift(new Vector3D(x1, y1, 0), height));
            if (vertical) {
                this.warehouse.addEdge(new Vector3D(x1, y1, 0), new Vector3D(x1, y1 + palletSize, 0));
                this.warehouse.addEdge(new Vector3D(x1, y1 + palletSize, 0), new Vector3D(x1 - palletSize, y1 + palletSize, 0));
            } else {
                this.warehouse.addEdge(new Vector3D(x1, y1, 0), new Vector3D(x1 + palletSize, y1, 0));
                this.warehouse.addEdge(new Vector3D(x1 + palletSize, y1, 0), new Vector3D(x1 + palletSize, y1 - palletSize, 0));
            }
            Vector3D[] positions = new Vector3D[height / palletSize];
            for (int z = 0; z < height; z += palletSize) {
                positions[z / palletSize] = new Vector3D(x1, y1, z);
                this.warehouse.addEdge(new Vector3D(x1, y1, z), new Vector3D(x1, y1, z - palletSize));
                if (z + palletSize < height) {
                    this.warehouse.addEdge(new Vector3D(x1, y1, z), new Vector3D(x1, y1, z + palletSize));
                }
            }
            this.controller.getPathFinder().addGraphConstraint(new LiftConstraint(this.controller.getPathFinder().getReservationTable(), positions));
        }
        if (liftNE) {
            this.lifts.add(new Lift(new Vector3D(x2 - palletSize, y1, 0), height));
            if (vertical) {
                this.warehouse.addEdge(new Vector3D(x2 - palletSize, y1, 0), new Vector3D(x2 - palletSize, y1 + palletSize, 0));
                this.warehouse.addEdge(new Vector3D(x2 - palletSize, y1 + palletSize, 0), new Vector3D(x2, y1 + palletSize, 0));
            } else {
                this.warehouse.addEdge(new Vector3D(x2 - palletSize, y1, 0), new Vector3D(x2 - 2 * palletSize, y1, 0));
                this.warehouse.addEdge(new Vector3D(x2 - 2 * palletSize, y1, 0), new Vector3D(x2 - 2 * palletSize, y1 - palletSize, 0));
            }
            Vector3D[] positions = new Vector3D[height / palletSize];
            for (int z = 0; z < height; z += palletSize) {
                positions[z / palletSize] = new Vector3D(x2 - palletSize, y1, z);
                this.warehouse.addEdge(new Vector3D(x2 - palletSize, y1, z), new Vector3D(x2 - palletSize, y1, z - palletSize));
                if (z + palletSize < height) {
                    this.warehouse.addEdge(new Vector3D(x2 - palletSize, y1, z), new Vector3D(x2 - palletSize, y1, z + palletSize));
                }
            }
            this.controller.getPathFinder().addGraphConstraint(new LiftConstraint(this.controller.getPathFinder().getReservationTable(), positions));
        }
        if (liftSW) {
            this.lifts.add(new Lift(new Vector3D(x1, y2 - palletSize, 0), height));
            if (vertical) {
                this.warehouse.addEdge(new Vector3D(x1, y2 - palletSize, 0), new Vector3D(x1, y2 - 2 * palletSize, 0));
                this.warehouse.addEdge(new Vector3D(x1, y2 - 2 * palletSize, 0), new Vector3D(x1 - palletSize, y2 - 2 * palletSize, 0));
            } else {
                this.warehouse.addEdge(new Vector3D(x1, y2 - palletSize, 0), new Vector3D(x1 + palletSize, y2 - palletSize, 0));
                this.warehouse.addEdge(new Vector3D(x1 + palletSize, y2 - palletSize, 0), new Vector3D(x1 + palletSize, y2, 0));
            }
            Vector3D[] positions = new Vector3D[height / palletSize];
            for (int z = 0; z < height; z += palletSize) {
                positions[z / palletSize] = new Vector3D(x1, y2 - palletSize, z);
                this.warehouse.addEdge(new Vector3D(x1, y2 - palletSize, z), new Vector3D(x1, y2 - palletSize, z - palletSize));
                if (z + palletSize < height) {
                    this.warehouse.addEdge(new Vector3D(x1, y2 - palletSize, z), new Vector3D(x1, y2 - palletSize, z + palletSize));
                }
            }
            this.controller.getPathFinder().addGraphConstraint(new LiftConstraint(this.controller.getPathFinder().getReservationTable(), positions));
        }
        if (liftSE) {
            this.lifts.add(new Lift(new Vector3D(x2 - palletSize, y2 - palletSize, 0), height));
            if (vertical) {
                this.warehouse.addEdge(new Vector3D(x2 - palletSize, y2 - palletSize, 0), new Vector3D(x2 - palletSize, y2 - 2 * palletSize, 0));
                this.warehouse.addEdge(new Vector3D(x2 - palletSize, y2 - 2 * palletSize, 0), new Vector3D(x2, y2 - 2 * palletSize, 0));
            } else {
                this.warehouse.addEdge(new Vector3D(x2 - palletSize, y2 - palletSize, 0), new Vector3D(x2 - 2 * palletSize, y2 - palletSize, 0));
                this.warehouse.addEdge(new Vector3D(x2 - 2 * palletSize, y2 - palletSize, 0), new Vector3D(x2 - 2 * palletSize, y2, 0));
            }
            Vector3D[] positions = new Vector3D[height / palletSize];
            for (int z = 0; z < height; z += palletSize) {
                positions[z / palletSize] = new Vector3D(x2 - palletSize, y2 - palletSize, z);
                this.warehouse.addEdge(new Vector3D(x2 - palletSize, y2 - palletSize, z), new Vector3D(x2 - palletSize, y2 - palletSize, z - palletSize));
                if (z + palletSize < height) {
                    this.warehouse.addEdge(new Vector3D(x2 - palletSize, y2 - palletSize, z), new Vector3D(x2 - palletSize, y2 - palletSize, z + palletSize));
                }
            }
            this.controller.getPathFinder().addGraphConstraint(new LiftConstraint(this.controller.getPathFinder().getReservationTable(), positions));
        }

        for (int x = x1; x < x2; x += palletSize) {
            for (int y = y1; y < y2; y += palletSize) {
                for (int z = 0; z < height; z += palletSize) {
                    if (z > 0 && (x < xStart || x >= xEnd || y < yStart || y >= yEnd)) continue;
                    if (vertical) {
                        if (y + palletSize < y2)
                            edges.add(this.warehouse.addEdge(new Vector3D(x, y, z), new Vector3D(x, y + palletSize, z)));
                        if (y - palletSize >= y1)
                            edges.add(this.warehouse.addEdge(new Vector3D(x, y, z), new Vector3D(x, y - palletSize, z)));
                    } else {
                        if (x + palletSize < x2)
                            edges.add(this.warehouse.addEdge(new Vector3D(x, y, z), new Vector3D(x + palletSize, y, z)));
                        if (x - palletSize >= x1)
                            edges.add(this.warehouse.addEdge(new Vector3D(x, y, z), new Vector3D(x - palletSize, y, z)));
                    }
                }
            }
        }

        for (Edge edge : edges) {
            edge.addCrossCondition(new StockEdgeCondition(this.stock, edge));
        }

        this.controller.closestOpenPositionFinder.updatePositions();
    }

    public void addOutdoorDock(int x1, int y1) {
        Dock dock = new Dock(new Vector3D(x1, y1), Truck.Type.BACK);
        this.docks.add(dock);

        int depth = this.warehouse.getDepth();
        int x2 = x1 + truckWidth, y2 = y1 + truckDepth;

        ArrayList<Edge> edges = new ArrayList<>();

        for (int x = x1; x < x2; x += palletSize) {
            edges.add(this.warehouse.addEdge(new Vector3D(x, depth - palletSize, 0), new Vector3D(x, depth, 0), true));
        }

        Vector3D[] positions = new Vector3D[(truckWidth / palletSize) * (truckDepth / palletSize)];
        int cnt = 0;
        for (int x = x1; x < x2; x += palletSize) {
            for (int y = y1; y < y2; y += palletSize) {
                if (x + palletSize < x2)
                    edges.add(this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x + palletSize, y, 0), true));
                if (x - palletSize >= x1)
                    edges.add(this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x - palletSize, y, 0), true));
                if (y + palletSize < y2)
                    edges.add(this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y + palletSize, 0), true));
                edges.add(this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y - palletSize, 0), true));
                positions[cnt++] = new Vector3D(x, y, 0);
            }
        }
        this.controller.getPathFinder().addGraphConstraint(new ZoneConstraint(this.controller.getPathFinder().getReservationTable(), positions));

        for (Edge edge : edges) {
            edge.addCrossCondition(new DockEdgeCondition(dock, dockWidth, truckDepth, edge));
        }

        this.controller.closestOpenPositionFinder.updatePositions();
    }

    public void addIndoorDock(int x1, int y1) {
        Dock dock = new Dock(new Vector3D(x1, y1), Truck.Type.SIDES);
        this.docks.add(dock);
        int x2 = x1 + truckWidth, y2 = y1 + truckDepth;

        this.removeGraphEdges(x1, y1, x2, y2);

        for (int x = x1; x < x2; x += palletSize) { // remove access from top and bottom
            this.warehouse.removeEdge(new Vector3D(x, y1 - palletSize, 0), new Vector3D(x, y1, 0));
            this.warehouse.removeEdge(new Vector3D(x, y1, 0), new Vector3D(x, y1 - palletSize, 0));
            this.warehouse.removeEdge(new Vector3D(x, y2, 0), new Vector3D(x, y2 - palletSize, 0));
            this.warehouse.removeEdge(new Vector3D(x, y2 - palletSize, 0), new Vector3D(x, y2, 0));
        }

        ArrayList<Edge> edges = new ArrayList<>();
        Vector3D[] positions = new Vector3D[(truckWidth / palletSize) * (truckDepth / palletSize)];
        int cnt = 0;

        for (int x = x1; x < x2; x += palletSize) {
            for (int y = y1; y < y2; y += palletSize) {
                edges.add(this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x - palletSize, y, 0)));
                edges.add(this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x + palletSize, y, 0)));
                if (y - palletSize >= y1)
                    edges.add(this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y - palletSize, 0)));
                if (y + palletSize < y2)
                    edges.add(this.warehouse.addEdge(new Vector3D(x, y, 0), new Vector3D(x, y + palletSize, 0)));
                positions[cnt++] = new Vector3D(x, y, 0);
            }
        }
        this.controller.getPathFinder().addGraphConstraint(new ZoneConstraint(this.controller.getPathFinder().getReservationTable(), positions));

        for (int y = y1; y < y2; y += palletSize) {
            edges.add(this.warehouse.getEdge(new Vector3D(x1 - palletSize, y, 0), new Vector3D(x1, y, 0)));
            edges.add(this.warehouse.getEdge(new Vector3D(x2, y, 0), new Vector3D(x2 - palletSize, y, 0)));
        }

        for (Edge edge : edges) {
            edge.addCrossCondition(new DockEdgeCondition(dock, dockWidth, truckDepth, edge));
        }

        this.controller.closestOpenPositionFinder.updatePositions();
    }

    public void addProductionLine(int x1, int y1, int x2, int y2, int capacity, ArrayList<Vector3D> startBuffer, ArrayList<Vector3D> endBuffer) {
        this.removeGraphEdges(x1, y1, x2, y2);

        for (int x = x1; x < x2; x += palletSize) {
            this.warehouse.removeEdge(new Vector3D(x, y1 - palletSize, 0), new Vector3D(x, y1, 0));
            this.warehouse.removeEdge(new Vector3D(x, y2, 0), new Vector3D(x, y2 - palletSize, 0));
        }
        for (int y = y1; y < y2; y += palletSize) {
            this.warehouse.removeEdge(new Vector3D(x1 - palletSize, y, 0), new Vector3D(x1, y, 0));
            this.warehouse.removeEdge(new Vector3D(x2, y, 0), new Vector3D(x2 - palletSize, y, 0));
        }

        for (Vector3D position : startBuffer) {
            this.stock.addBufferPosition(position);

            for (Edge reverseEdge : this.warehouse.getGraph().getReverseEdges(position)) {
                Edge edge = this.warehouse.getGraph().getEdge(reverseEdge.to(), position);
                if (edge != null) edge.addCrossCondition(new StockEdgeCondition(this.stock, edge));
            }
        }

        for (Vector3D position : endBuffer) {
            this.stock.addBufferPosition(position);

            for (Edge reverseEdge : this.warehouse.getGraph().getReverseEdges(position)) {
                Edge edge = this.warehouse.getGraph().getEdge(reverseEdge.to(), position);
                if (edge != null) edge.addCrossCondition(new StockEdgeCondition(this.stock, edge));
            }
        }

        this.productionLines.add(new ProductionLine(this.stock, new Vector3D(x1, y1), x2 - x1, y2 - y1, capacity, startBuffer, endBuffer));

        this.controller.closestOpenPositionFinder.updatePositions();
    }

    public void addMobile(Vector3D position) {
        Mobile mobile = new Mobile(position);
        this.mobiles.add(mobile);
        this.controller.add(mobile);
    }

}
