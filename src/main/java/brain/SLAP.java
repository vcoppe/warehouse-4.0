package brain;

import gurobi.GRBException;
import warehouse.Scenario;

public class SLAP {

    private final Scenario scenario;
    private int[] assignment;

    public SLAP(Scenario scenario) {
        this.scenario = scenario;
    }

    public int[] getAssignment() {
        return this.assignment;
    }

    public void solve() {
        try {
            SLAPMIP model = new SLAPMIP(this.scenario.nTypes, this.scenario.nIOPoints, this.scenario.nSlots, this.scenario.slotCapacity, this.scenario.nPalletsOfType, this.scenario.throughput, this.scenario.freq, this.scenario.dist);
            model.solve();

            this.assignment = model.getSolution();
        } catch (GRBException e) {
            e.printStackTrace();
            this.assignment = null;
        }

    }

    /*public void solve2() {
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
    }*/

    public void solve4() {
        try {
            SLAPCG model = new SLAPCG(this.scenario.nTypes, this.scenario.nIOPoints, this.scenario.nSlots, this.scenario.nPalletsOfType, this.scenario.throughput, this.scenario.slotCapacity, this.scenario.freq, this.scenario.dist);
            model.solve();

            this.assignment = model.getSolution();
        } catch (GRBException e) {
            e.printStackTrace();
            this.assignment = null;
        }

    }

}
