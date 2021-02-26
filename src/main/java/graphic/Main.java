package graphic;

import agent.Truck;
import brain.NaiveSelector;
import event.ProductionInitEvent;
import event.TruckArriveEvent;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
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

public class Main extends Application {

    public double WIDTH, HEIGHT, TIMESTEP;
    private double delta = 0;

    @Override
    public void start(Stage stage) throws Exception {
        NaiveSelector selector = new NaiveSelector();
        Configuration configuration = new Configuration(10, 5, selector, selector, selector);
        Simulation simulation = configuration.simulation;

        Random random = new Random(0);

        ArrayList<Pallet> pallets = new ArrayList<>();
        for (int i=0; i<30*26; i++) {
            if (random.nextInt(100) < 50) {
                Pallet pallet = new Pallet(random.nextInt(10));
                configuration.stock.add(
                        new Position(
                                (2 * (i / 30) + (i / 30) % 2) * configuration.palletSize,
                                (i % 30) * configuration.palletSize
                        ),
                        pallet
                );
                if (random.nextInt(100) < 2) pallets.add(pallet);
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

        Truck truck = new Truck(new Position(0, configuration.warehouse.depth + 110), pallets, new ArrayList<>());
        Event event = new TruckArriveEvent(configuration.simulation, 0, configuration.controller, truck);
        configuration.simulation.enqueueEvent(event);ArrayList<Pair<Pallet,Integer>> in = new ArrayList<>();

        ArrayList<Pair<Pallet,Integer>> out = new ArrayList<>();
        for (int i=0; i<3; i++) {
            in.add(new Pair<>(new Pallet(i), 1));
            out.add(new Pair<>(new Pallet(3 + i), 1));
        }
        Production production = new Production(in, out, 10, 1, 1000);
        Event event2 = new ProductionInitEvent(configuration.simulation, 300, configuration.controller, production);
        configuration.simulation.enqueueEvent(event2);

        ShapeHandler shapeHandler = new ShapeHandler(configuration);

        Button button = new Button("Forward");
        button.setOnMouseClicked(mouseEvent -> {
            double previousTime = simulation.getCurrentTime();
            if (simulation.hasNextEvent()) {
                double currentTime = simulation.nextEvent().getTime();
                this.delta = currentTime - previousTime;
                simulation.run(currentTime);
                shapeHandler.playAnimations();
            }
        });

        BorderPane pane = new BorderPane();
        pane.setTop(button);
        pane.setBottom(new Text("Bottom"));
        pane.setLeft(new Text("Left"));
        pane.setRight(new Text("Right"));
        pane.setCenter(shapeHandler.getGroup());

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();

        // TODO keep list of all animations (in each shape ?) and run for delta time

    }

}
