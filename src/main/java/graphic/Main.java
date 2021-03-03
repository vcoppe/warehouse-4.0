package graphic;

import brain.NaiveSelector;
import event.TruckGeneratorEvent;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simulation.Event;
import simulation.Simulation;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    private double rate = 10;

    @Override
    public void start(Stage stage) throws Exception {
        // disable JavaFX logging
        Logger logger = Logger.getLogger("javafx");
        logger.setLevel(Level.OFF);

        // create scenario
        NaiveSelector selector = new NaiveSelector();
        Configuration configuration = new Configuration(10, 5, selector, selector, selector);
        Simulation simulation = configuration.simulation;

        Random random = new Random(0);

        for (int i = 0; i < 30 * 26; i++) {
            if (random.nextInt(100) < 70) {
                Pallet pallet = new Pallet(random.nextInt(10));
                configuration.stock.add(
                        new Position(
                                (2 * (i / 30) + (i / 30) % 2) * configuration.palletSize,
                                (i % 30) * configuration.palletSize
                        ),
                        pallet
                );
            } else {
                configuration.stock.add(
                        new Position(
                                (2 * (i / 30) + (i / 30) % 2) * configuration.palletSize,
                                (i % 30) * configuration.palletSize
                        ),
                        Pallet.FREE
                );
            }
        }

        Event event = new TruckGeneratorEvent(configuration.simulation, 0, configuration.warehouse, configuration.controller);
        configuration.simulation.enqueueEvent(event);

        // create all shapes and observers
        ShapeHandler shapeHandler = new ShapeHandler(configuration);

        // run simulation
        EventHandler handler = e -> {
            if (simulation.hasNextEvent()) {
                double currentTime = simulation.nextEvent().getTime();
                simulation.run(currentTime);
                double delta = simulation.nextEvent().getTime() - currentTime;
                shapeHandler.playAnimations(currentTime, delta, rate);
            }
        };

        Button button = new Button("Forward");
        button.setOnMouseClicked(handler);
        shapeHandler.setCallback(handler);

        BorderPane pane = new BorderPane();
        pane.setTop(button);
        pane.setBottom(new Text("Bottom"));
        pane.setLeft(new Text("Left"));
        pane.setRight(new Text("Right"));
        pane.setCenter(shapeHandler.getPane());

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();

    }

}
