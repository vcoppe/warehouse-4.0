package graphic.dashboard;

import agent.Controller;
import agent.Truck;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import observer.Observer;
import warehouse.Configuration;

import java.util.HashMap;

public class KPIDashboard implements Observer<Controller> {

    private final Pane pane;
    private int countTrucks;
    private final Series<Number,Number> series;
    private final HashMap<Integer,Truck> trucks;
    private final TruckObserver truckObserver;

    public KPIDashboard(Configuration configuration) {
        this.pane = new VBox();
        this.countTrucks = 0;
        this.trucks = new HashMap<>();
        this.truckObserver = new TruckObserver(this);
        this.series = new Series<>();

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("simulation time");
        yAxis.setLabel("waiting time");

        LineChart<Number,Number> lineChart = new LineChart<>(xAxis,yAxis);
        lineChart.setTitle("Evolution of the waiting time of trucks");
        lineChart.setLegendVisible(false);
        lineChart.getData().add(this.series);

        this.pane.getChildren().add(lineChart);
        configuration.controller.attach(this);
    }

    public Pane getPane() {
        return this.pane;
    }

    public void addDataPoint(double departureTime, double waitingTime) {
        this.countTrucks++;
        this.series.getData().add(new Data<>(departureTime, waitingTime));
    }

    private void add(Truck truck) {
        this.trucks.put(truck.getId(), truck);
        truck.attach(this.truckObserver);
    }

    private void remove(Truck truck) {
        this.trucks.remove(truck.getId());
    }

    @Override
    public void update(Controller controller) {
        for (Truck truck : controller.getTrucks()) {
            if (!this.trucks.containsKey(truck.getId())) {
                this.add(truck);
            }
        }
    }

    static class TruckObserver implements Observer<Truck> {

        private final KPIDashboard kpiDashboard;

        public TruckObserver(KPIDashboard kpiDashboard) {
            this.kpiDashboard = kpiDashboard;
        }

        @Override
        public void update(Truck truck) {
            if (truck.left()) {
                this.kpiDashboard.addDataPoint(truck.getDepartureTime(), truck.getWaitingTime());
                this.kpiDashboard.remove(truck);
            }
        }
    }
}