package graphic.dashboard;

import agent.Controller;
import agent.Truck;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import observer.Observer;
import warehouse.Configuration;

import java.util.HashMap;
import java.util.LinkedList;

public class KPIDashboard implements Observer<Controller> {

    private final Pane pane;
    private int countTrucks;
    private final Series<Number,Number> series;
    private final HashMap<Integer,Truck> trucks;

    public KPIDashboard(Configuration configuration) {
        this.pane = new VBox();
        this.countTrucks = 0;
        this.trucks = new HashMap<>();
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
    }

    public Pane getPane() {
        return this.pane;
    }

    public void addDataPoint(double departureTime, double waitingTime) {
        this.countTrucks++;
        this.series.getData().add(new Data<>(departureTime, waitingTime));
    }

    @Override
    public void update(Controller controller) {
        for (Truck truck : controller.getTrucks()) {
            if (!this.trucks.containsKey(truck.getId())) {
                this.trucks.put(truck.getId(), truck);
            }
        }


        LinkedList<Truck> doneTrucks = new LinkedList<>();
        for (Truck truck : this.trucks.values()) {
            if (truck.left()) {
                doneTrucks.add(truck);
                this.addDataPoint(truck.getDepartureTime(), truck.getWaitingTime());
            }
        }

        for (Truck truck : doneTrucks) {
            this.trucks.remove(truck.getId());
        }
    }
}