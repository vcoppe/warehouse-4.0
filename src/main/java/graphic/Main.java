package graphic;

import agent.Truck;
import brain.NaiveSelector;
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
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;

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
            Pallet pallet = new Pallet(random.nextInt(10));
            configuration.stock.add(
                    new Position(
                            (2*(i/30)+(i/30)%2)*configuration.palletSize,
                            (i%30)*configuration.palletSize
                    ),
                    pallet
            );
            if (random.nextInt(1000) < 50) pallets.add(pallet);
        }

        Truck truck = new Truck(new Position(0, configuration.warehouse.depth + 110), pallets, new ArrayList<>());
        Event event = new TruckArriveEvent(configuration.simulation, 0, configuration.controller, truck);
        configuration.simulation.enqueueEvent(event);

        Button button = new Button("Forward");
        button.setOnMouseClicked(mouseEvent -> {
            double previousTime = simulation.getCurrentTime();
            if (simulation.hasNextEvent()) {
                double currentTime = simulation.nextEvent().getTime();
                this.delta = currentTime - previousTime;
                simulation.run(currentTime);
            }
        });

        BorderPane pane = new BorderPane();
        pane.setTop(button);
        pane.setBottom(new Text("Bottom"));
        pane.setLeft(new Text("Left"));
        pane.setRight(new Text("Right"));
        pane.setCenter(ShapeCreator.getShapes(configuration));

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();

        // TODO keep list of all animations (in each shape ?) and run for delta time

    }

}
