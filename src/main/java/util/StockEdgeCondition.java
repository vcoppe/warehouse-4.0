package util;

import agent.Mobile;
import agent.Stock;
import graph.Edge;
import warehouse.Mission;
import warehouse.Pallet;

public class StockEdgeCondition extends Condition {

    Stock stock;
    Edge edge;

    public StockEdgeCondition(Stock stock, Edge edge) {
        this.stock = stock;
        this.edge = edge;
    }

    @Override
    public boolean satisfied(double time, Mobile mobile) {
        // empty robots can go below pallets

        Pallet pallet = this.stock.get(this.edge.to());
        if (pallet == null || pallet == Pallet.FREE) {
            return true;
        }

        Mission mission = mobile.getMission();
        if (mission == null) {
            return false;
        }

        return mission.getStartPosition().equals(this.edge.to());

    }
}
