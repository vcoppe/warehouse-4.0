package graphic;

import event.TruckGeneratorEvent;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simulation.Event;
import simulation.Simulation;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // disable JavaFX logging
        Logger logger = Logger.getLogger("javafx");
        logger.setLevel(Level.OFF);

        // create scenario
        Configuration configuration = new Configuration(10, 5);
        Simulation simulation = configuration.simulation;

        Random random = new Random(0);

        for (int i = 0; i < 30 * 30; i++) {
            if (random.nextInt(100) < 70) {
                Pallet pallet = new Pallet(random.nextInt(10));
                configuration.stock.add(
                        new Position(
                                (2 * (i / 30) + (i / 30) % 2) * configuration.palletSize,
                                (i % 30) * configuration.palletSize
                        ),
                        pallet
                );
            }
        }

        Event event = new TruckGeneratorEvent(configuration.simulation, 0, configuration.warehouse, configuration.controller);
        configuration.simulation.enqueueEvent(event);

        // create all shapes and observers
        ShapeHandler shapeHandler = new ShapeHandler(configuration);

        EventHandler callback = e -> {
            if (simulation.hasNextEvent()) {
                double currentTime = simulation.nextEvent().getTime();
                simulation.run(currentTime);
                double delta = simulation.nextEvent().getTime() - currentTime;
                shapeHandler.playAnimations(currentTime, delta);
            }
        };

        shapeHandler.setCallback(callback);

        // bottom pane
        Pane bottomPane = new HBox();

        Button play = new Button("Play");
        AtomicBoolean playing = new AtomicBoolean(false);
        EventHandler playHandler = e -> {
            if (playing.get()) {
                shapeHandler.pauseAnimation();
                playing.set(false);
                play.setText("Play");
            } else {
                shapeHandler.resumeAnimation();
                if (shapeHandler.isAutoplay()) {
                    playing.set(true);
                    play.setText("Pause");
                }
            }
        };
        play.setOnMouseClicked(playHandler);

        Button decreaseRate = new Button("/2");
        decreaseRate.setOnMouseClicked(e -> shapeHandler.setRate(shapeHandler.getRate() / 2));

        Button increaseRate = new Button("x2");
        increaseRate.setOnMouseClicked(e -> shapeHandler.setRate(shapeHandler.getRate() * 2));

        CheckBox autoPlay = new CheckBox("Play automatically");
        autoPlay.setSelected(true);
        autoPlay.setOnMouseClicked(e -> shapeHandler.setAutoplay(autoPlay.isSelected()));

        bottomPane.getChildren().addAll(play, decreaseRate, increaseRate, autoPlay);

        BorderPane pane = new BorderPane();
        pane.setTop(new Text("Top"));
        pane.setBottom(bottomPane);
        pane.setLeft(new Text("Left"));
        pane.setRight(new Text("Right"));
        pane.setCenter(shapeHandler.getPane());

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();

    }

}
