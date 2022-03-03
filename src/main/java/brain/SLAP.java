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

}
