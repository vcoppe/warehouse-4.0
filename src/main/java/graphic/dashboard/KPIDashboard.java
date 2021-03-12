package graphic.dashboard;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import observer.ControllerObserver;
import observer.TruckObserver;
import observer.TruckWaitingTimeObserver;
import warehouse.Configuration;

public class KPIDashboard {

    private final Pane pane;
    private int countTrucks;
    private final Series<Number,Number> series;

    public KPIDashboard(Configuration configuration) {
        this.pane = new VBox();
        this.countTrucks = 0;
        this.series = new Series<>();

        TruckWaitingTimeObserver truckObserver = new TruckWaitingTimeObserver(this);
        ControllerObserver controllerObserver = new ControllerObserver(truckObserver);
        configuration.controller.attach(controllerObserver);

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

}