package graphic;

import event.ProductionInitEvent;
import event.TruckGeneratorEvent;
import graphic.dashboard.AnimationDashboard;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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

        Event event = new TruckGeneratorEvent(configuration.simulation, 0, configuration);
        configuration.simulation.enqueueEvent(event);

        ArrayList<Pair<Pallet, Integer>> in = new ArrayList<>();
        ArrayList<Pair<Pallet, Integer>> out = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            in.add(new Pair<>(new Pallet(i), 1));
            out.add(new Pair<>(new Pallet(3 + i), 1));
        }
        Production production = new Production(in, out, 30, 1, 500);
        Event event2 = new ProductionInitEvent(configuration.simulation, 200, configuration.controller, production);
        configuration.simulation.enqueueEvent(event2);

        // create all shapes and observers
        AnimationDashboard animationDashboard = new AnimationDashboard(configuration);

        EventHandler callback = e -> {
            if (simulation.hasNextEvent()) {
                double currentTime = simulation.nextEvent().getTime();
                simulation.run(currentTime);
                double delta = simulation.nextEvent().getTime() - currentTime;
                animationDashboard.playAnimations(currentTime, delta);
            }
        };

        animationDashboard.setCallback(callback);

        // bottom pane
        Pane bottomPane = new HBox();

        Button play = new Button("Play");
        AtomicBoolean playing = new AtomicBoolean(false);
        EventHandler playHandler = e -> {
            if (playing.get()) {
                animationDashboard.pauseAnimation();
                playing.set(false);
                play.setText("Play");
            } else {
                animationDashboard.resumeAnimation();
                if (animationDashboard.isAutoplay()) {
                    playing.set(true);
                    play.setText("Pause");
                }
            }
        };
        play.setOnMouseClicked(playHandler);

        Button decreaseRate = new Button("/2");
        decreaseRate.setOnMouseClicked(e -> animationDashboard.setRate(animationDashboard.getRate() / 2));

        Button increaseRate = new Button("x2");
        increaseRate.setOnMouseClicked(e -> animationDashboard.setRate(animationDashboard.getRate() * 2));

        CheckBox autoPlay = new CheckBox("Play automatically");
        autoPlay.setSelected(true);
        autoPlay.setOnMouseClicked(e -> animationDashboard.setAutoplay(autoPlay.isSelected()));

        bottomPane.getChildren().addAll(play, decreaseRate, increaseRate, autoPlay);

        BorderPane pane = new BorderPane();
        pane.setTop(new Group());
        pane.setBottom(bottomPane);
        pane.setLeft(new Group());
        pane.setRight(new Group());
        pane.setCenter(animationDashboard.getPane());

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();

    }

}
