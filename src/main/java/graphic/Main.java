package graphic;

import event.ProductionInitEvent;
import event.TruckGeneratorEvent;
import graphic.dashboard.AnimationDashboard;
import graphic.dashboard.KPIDashboard;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import simulation.Event;
import simulation.Simulation;
import util.Pair;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;
import warehouse.Production;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    private final Configuration configuration;

    public Main() {
        super();
        this.configuration = new Configuration(10, 5);

        // disable JavaFX logging
        Logger logger = Logger.getLogger("javafx");
        logger.setLevel(Level.OFF);

        // create scenario
        Random random = new Random(0);

        for (int i = 0; i < 30 * 30; i++) {
            if (random.nextInt(100) < 70) {
                Pallet pallet = new Pallet(random.nextInt(10));
                this.configuration.stock.add(
                        new Position(
                                (2 * (i / 30) + (i / 30) % 2) * this.configuration.palletSize,
                                (i % 30) * this.configuration.palletSize
                        ),
                        pallet
                );
            }
        }

        Event event = new TruckGeneratorEvent(this.configuration.simulation, 0, this.configuration);
        this.configuration.simulation.enqueueEvent(event);
    }

    @Override
    public void start(Stage stage) throws Exception {

        AnimationDashboard animationDashboard = new AnimationDashboard(this.configuration);
        KPIDashboard kpiDashboard = new KPIDashboard(this.configuration);

        BorderPane pane = new BorderPane();
        pane.setTop(new Group());
        pane.setBottom(new Group());
        pane.setLeft(new Group());
        pane.setRight(kpiDashboard.getPane());
        pane.setCenter(animationDashboard.getPane());

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();

    }

}
