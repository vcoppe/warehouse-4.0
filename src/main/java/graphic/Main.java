package graphic;

import brain.SLAP;
import event.TruckGeneratorEvent;
import graphic.dashboard.KPIDashboard;
import graphic.dashboard.AnimationDashboard;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import simulation.Event;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    private final Configuration configuration;

    public Main() {
        super();
        //this.configuration = new Configuration(10, 5);

        // disable JavaFX logging
        Logger logger = Logger.getLogger("javafx");
        logger.setLevel(Level.OFF);

        // create scenario
        Random random = new Random(0);

        /*for (int i = 0; i < this.configuration.nAisles * this.configuration.nSlotsPerAisle; i++) {
            if (random.nextInt(100) < 70) {
                Pallet pallet = new Pallet(random.nextInt(10));
                this.configuration.stock.add(
                        new Position(
                                (2 * (i / this.configuration.nSlotsPerAisle) + (i / this.configuration.nSlotsPerAisle) % 2) * this.configuration.palletSize,
                                (i % this.configuration.nSlotsPerAisle) * this.configuration.palletSize
                        ),
                        pallet
                );
            }
        }

        Event event = new TruckGeneratorEvent(this.configuration.simulation, 0, this.configuration);
        this.configuration.simulation.enqueueEvent(event);*/

        // test SLAP
        this.configuration = new Configuration(800, 500, 5, 0);
    }

    @Override
    public void start(Stage stage) throws Exception {

        SLAP slap = new SLAP(this.configuration);
        slap.solve();
        AnimationDashboard animation = new AnimationDashboard(this.configuration);
        //KPIDashboard kpiDashboard = new KPIDashboard(this.configuration);

        BorderPane pane = new BorderPane();
        pane.setCenter(animation.getPane());

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();

    }
}
