package observer;

import agent.Truck;
import graphic.dashboard.KPIDashboard;

import java.util.HashSet;

public class TruckWaitingTimeObserver implements Observer<Truck> {

    private final KPIDashboard kpiDashboard;
    private final HashSet<Integer> truckIds;

    public TruckWaitingTimeObserver(KPIDashboard kpiDashboard) {
        this.kpiDashboard = kpiDashboard;
        this.truckIds = new HashSet<>();
    }

    @Override
    public void update(Truck truck) {
        if (!this.truckIds.contains(truck.getId())) {
            this.truckIds.add(truck.getId());
            truck.attach(this);
        }

        if (truck.left()) {
            this.kpiDashboard.addDataPoint(truck.getDepartureTime(), truck.getWaitingTime());
        }
    }

}
