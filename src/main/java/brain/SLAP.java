package brain;

import graph.HungarianAlgorithm;
import graph.MinCostMaxFlow;
import gurobi.GRBException;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class SLAP {

    private final Random random = new Random(0);

    public final Configuration configuration;

    private final int nSlots, nTypes, nIOPoints;
    private final ArrayList<Position> slots;
    private final int[] nPalletsOfType, slotCapacity;

    private final double[][] dist, freq;
    private final double[] throughput;

    public SLAP(Configuration configuration) {
        this.configuration = configuration;
        int nDocks = this.configuration.docks.size();
        int nProductionLines = this.configuration.productionLines.size();

        this.slots = this.configuration.stock.getStockPositions();
        this.nSlots = this.slots.size();
        this.slotCapacity = new int[this.nSlots];
        Arrays.fill(this.slotCapacity, 1);

        this.nIOPoints = nDocks + nProductionLines * 2; // all docks + production lines (in + out)

        this.dist = new double[this.nSlots][this.nIOPoints];
        for (int i=0; i<this.nSlots; i++) {
            Position p1 = this.slots.get(i);
            for (int j=0; j<this.nIOPoints; j++) {
                Position p2;
                if (j < nDocks) {
                    p2 = this.configuration.docks.get(j).getPosition();
                } else if (j % 2 == 0) {
                    p2 = this.configuration.productionLines.get((j - nDocks) / 2).getStartBuffer().get(0);
                } else {
                    p2 = this.configuration.productionLines.get((j - nDocks) / 2).getEndBuffer().get(0);
                }
                this.dist[i][j] = this.configuration.warehouse.getDistance(p1, p2);
            }
        }

        this.nTypes = 5;
        this.nPalletsOfType = new int[this.nTypes];
        this.freq = new double[this.nTypes][this.nIOPoints];
        this.throughput = new double[this.nTypes];

        int totalPallets = 0;
        for (int i=0; i<this.nTypes; i++) {
            this.nPalletsOfType[i] = 1 + random.nextInt(this.nSlots - totalPallets - (this.nTypes - 1 - i));
            totalPallets += this.nPalletsOfType[i];
            this.throughput[i] = 10 * random.nextDouble() * this.nPalletsOfType[i];

            double totalFreq = 0;
            for (int j=0; j<this.nIOPoints; j++) {
                this.freq[i][j] = random.nextInt(1000);
                totalFreq += this.freq[i][j];
            }
            for (int j=0; j<this.nIOPoints; j++) {
                this.freq[i][j] /= totalFreq;
            }
        }
    }

    public void solve() {
        try {
            SLAPMIP model = new SLAPMIP(this.nTypes, this.nIOPoints, this.nSlots, this.slotCapacity, this.nPalletsOfType, this.throughput, this.freq, this.dist);
            model.solve();

            int[] assignment = model.getSolution();
            for (int i=0; i<this.nSlots; i++) {
                this.configuration.stock.add(this.slots.get(i), new Pallet(assignment[i]));
            }

        } catch (GRBException e) {
            e.printStackTrace();
        }

    }

    public void solve2() {
        int[] type = new int[this.nSlots];
        double[][] cost = new double[this.nSlots][this.nSlots];

        for (int k=0; k<this.nSlots; k++) {
            int z = 0;
            for (int i=0; i<this.nTypes; i++) {
                double weightedDistance = 0;
                for (int j = 0; j < this.nIOPoints; j++) {
                    weightedDistance += this.freq[i][j] * this.dist[k][j];
                }
                for (int l=0; l<this.nPalletsOfType[i]; l++) {
                    cost[k][z] = weightedDistance * this.throughput[i] / this.nPalletsOfType[i];
                    type[z] = i;
                    z++;
                }
            }
        }

        long start = System.currentTimeMillis();

        HungarianAlgorithm alg = new HungarianAlgorithm(cost);
        int[] assignment = alg.execute();

        System.out.println("time elapsed: " + (System.currentTimeMillis() - start) / 1000);

        for (int i=0; i<this.nSlots; i++) {
            this.configuration.stock.add(this.slots.get(i), new Pallet(type[assignment[i]]));
        }
    }

    public void solve3() {
        int[][] capacity = new int[this.nTypes + this.nSlots + 2][this.nTypes + this.nSlots + 2];
        double[][] cost = new double[this.nTypes + this.nSlots + 2][this.nTypes + this.nSlots + 2];

        for (int i=0; i<this.nTypes; i++) {
            cost[this.nTypes + this.nSlots][i] = 0;
            capacity[this.nTypes + this.nSlots][i] = this.nPalletsOfType[i];
            for (int k=0; k<this.nSlots; k++) {
                double weightedDistance = 0;
                for (int j = 0; j < this.nIOPoints; j++) {
                    weightedDistance += this.freq[i][j] * this.dist[k][j];
                }
                cost[i][this.nTypes + k] = weightedDistance * this.throughput[i] / this.nPalletsOfType[i];
                capacity[i][this.nTypes + k] = 1;
            }
        }
        for (int k=0; k<this.nSlots; k++) {
            cost[this.nTypes + k][this.nTypes + this.nSlots + 1] = 0;
            capacity[this.nTypes + k][this.nTypes + this.nSlots + 1] = 1;
        }

        long start = System.currentTimeMillis();

        MinCostMaxFlow alg = new MinCostMaxFlow();
        alg.getMaxFlow(capacity, cost, this.nTypes + this.nSlots, this.nTypes + this.nSlots + 1);

        System.out.println("time elapsed: " + (System.currentTimeMillis() - start) / 1000);

        for (int i=0; i<this.nTypes; i++) {
            for (int k = 0; k < this.nSlots; k++) {
                if (alg.flow[i][this.nTypes+k] > 0) {
                    this.configuration.stock.add(this.slots.get(k), new Pallet(i));
                }
            }
        }
    }

    public void solve4() {
        try {
            SLAPCG model = new SLAPCG(this.nTypes, this.nIOPoints, this.nSlots, this.nPalletsOfType, this.throughput, this.slotCapacity, this.freq, this.dist);
            model.solve();

            int[] assignment = model.getSolution();
            for (int i=0; i<this.nSlots; i++) {
                this.configuration.stock.add(this.slots.get(i), new Pallet(assignment[i]));
            }

        } catch (GRBException e) {
            e.printStackTrace();
        }

    }

}
