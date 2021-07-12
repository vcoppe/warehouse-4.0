package warehouse;

import brain.SLAP;
import util.Vector3D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Scenario {

    // generates distribution of pallet types
    // (later) distribution evolves over time

    private static final Random random = new Random(0);
    public final int nSlots, nTypes, nIOPoints, nDocks;
    public final ArrayList<Vector3D> slots;
    public final int[] nPalletsOfType, slotCapacity;
    public final double[][] dist, freq;
    public final double[] throughput, dockThroughput, productionLineInThroughput, productionLineOutThroughput;
    private final Configuration configuration;

    public Scenario(Configuration configuration, int nTypes) {
        this.configuration = configuration;
        this.nTypes = nTypes;

        this.nDocks = this.configuration.docks.size();
        int nProductionLines = this.configuration.productionLines.size();

        this.slots = this.configuration.stock.getStockPositions();
        this.nSlots = this.slots.size();
        this.slotCapacity = new int[this.nSlots];
        Arrays.fill(this.slotCapacity, 1);

        this.nIOPoints = nDocks + nProductionLines * 2; // all docks + production lines (in + out)

        this.dist = new double[this.nSlots][this.nIOPoints];
        for (int i = 0; i < this.nSlots; i++) {
            Vector3D p1 = this.slots.get(i);
            for (int j = 0; j < this.nIOPoints; j++) {
                Vector3D p2;
                if (j < nDocks) {
                    p2 = this.configuration.docks.get(j).getPosition();
                } else if (j % 2 == 0) {
                    p2 = this.configuration.productionLines.get((j - nDocks) / 2).getStartBuffer().get(0); // could take average of all buffer positions
                } else {
                    p2 = this.configuration.productionLines.get((j - nDocks) / 2).getEndBuffer().get(0); // could take average of all buffer positions
                }
                this.dist[i][j] = this.configuration.warehouse.getDistance(p1, p2).norm();
            }
        }

        this.nPalletsOfType = new int[this.nTypes];
        this.freq = new double[this.nTypes][this.nIOPoints];
        this.throughput = new double[this.nTypes];

        int totalPallets = 0;
        for (int i = 0; i < this.nTypes; i++) {
            this.nPalletsOfType[i] = 1 + random.nextInt(this.nSlots - totalPallets - (this.nTypes - 1 - i));
            totalPallets += this.nPalletsOfType[i];
            this.throughput[i] = 10 * random.nextDouble() * this.nPalletsOfType[i];

            double totalFreq = 0;
            for (int j = 0; j < this.nIOPoints; j++) {
                this.freq[i][j] = random.nextInt(1000);
                totalFreq += this.freq[i][j];
            }
            for (int j = 0; j < this.nIOPoints; j++) {
                this.freq[i][j] /= totalFreq;
            }
        }


        this.dockThroughput = new double[this.nTypes];
        this.productionLineInThroughput = new double[this.nTypes];
        this.productionLineOutThroughput = new double[this.nTypes];
        for (int i = 0; i < this.nTypes; i++) {
            for (int j = 0; j < this.nIOPoints; j++) {
                if (j < nDocks) {
                    this.dockThroughput[i] += this.throughput[i] * this.freq[i][j];
                } else if (j % 2 == 0) { // start
                    this.productionLineInThroughput[i] += this.throughput[i] * this.freq[i][j];
                } else { // end
                    this.productionLineOutThroughput[i] += this.throughput[i] * this.freq[i][j];
                }
            }
        }
    }

    public static int pickFromDistribution(double[] dist) {
        double total = 0;
        double[] normDist = new double[dist.length];
        for (int i = 0; i < dist.length; i++) {
            total += dist[i];
        }
        for (int i = 0; i < normDist.length; i++) {
            normDist[i] = dist[i] / total;
        }

        total = 0;
        double number = random.nextDouble();
        for (int i = 0; i < normDist.length; i++) {
            total += normDist[i];
            if (number <= total) {
                return i;
            }
        }

        return -1;
    }

    public void createZones() {
        SLAP slap = new SLAP(this);
        slap.solve();

        int[] assignment = slap.getAssignment();
        ArrayList<Vector3D>[] typePositions = new ArrayList[this.nTypes];
        for (int i = 0; i < this.nTypes; i++) {
            typePositions[i] = new ArrayList<>();
        }

        for (int i = 0; i < this.nSlots; i++) {
            if (assignment[i] != -1) {
                typePositions[assignment[i]].add(this.slots.get(i));
            }
        }

        for (int i = 0; i < this.nTypes; i++) {
            int finalI = i;
            this.configuration.stock.filter.add(new Rule(1, true, pallet -> pallet.getType() == finalI, typePositions[i]));
        }
    }

    public void initStock() {
        for (int i = 0; i < this.nTypes; i++) {
            int nPallets = this.nPalletsOfType[i] / 2 + random.nextInt(1 + this.nPalletsOfType[i] / 2);
            for (int j = 0; j < nPallets; j++) {
                Pallet pallet = new Pallet(i);
                int dock = this.nDocks;
                while (dock >= this.nDocks) {
                    dock = pickFromDistribution(this.freq[i]);
                }
                Vector3D position = this.configuration.palletPositionSelector.selectEndPosition(
                        pallet,
                        this.configuration.docks.get(dock).getPosition(),
                        this.configuration.stock
                );
                this.configuration.stock.add(position, pallet);
            }
        }
    }

}
