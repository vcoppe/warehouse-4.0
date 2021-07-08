package graphic;

import event.TruckGeneratorEvent;
import graphic.dashboard.AnimationDashboard;
import graphic.dashboard.KPIDashboard;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import simulation.Event;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    private final Configuration configuration;

    public Main() {
        super();

        this.configuration = new Configuration(600, 300, 40);

        int width = this.configuration.warehouse.getWidth(), depth = this.configuration.warehouse.getDepth();

        int productionLineX = width - 70, productionLineY = 20, productionLineWidth = 50, productionLineDepth = 100;

        ArrayList<Position> productionLineStartBuffer = new ArrayList<>();
        ArrayList<Position> productionLineEndBuffer = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            productionLineStartBuffer.add(new Position(productionLineX - 2 * this.configuration.palletSize + i / 5 * this.configuration.palletSize, productionLineY + (i % 5) * this.configuration.palletSize));
            productionLineEndBuffer.add(new Position(productionLineX + (i % 5) * this.configuration.palletSize, productionLineY + productionLineDepth + i / 5 * this.configuration.palletSize));
        }

        this.configuration.addProductionLine(productionLineX, productionLineY, productionLineX + productionLineWidth, productionLineY + productionLineDepth, 10, productionLineStartBuffer, productionLineEndBuffer);

        this.configuration.addStockSection(20, 20, 120, 120, 20, true);
        this.configuration.addStockSection(20, 150, 120, 250, 20, true);

        this.configuration.addAutoStockSection(150, 20, 270, 200, 40, true, false, false, true, true);

        this.configuration.addStockSection(300, 20, 460, 160, 20, false);

        for (int i = 0; i < 5; i++) this.configuration.addOutdoorDock(i * this.configuration.dockWidth, depth);
        for (int i = 0; i < 2; i++)
            this.configuration.addIndoorDock(300 + i * this.configuration.dockWidth * 2, depth - this.configuration.truckDepth);
        for (int i = 0; i < 5; i++)
            this.configuration.addMobile(new Position(this.configuration.dockWidth * i, depth - this.configuration.palletSize));

        // disable JavaFX logging
        Logger logger = Logger.getLogger("javafx");
        logger.setLevel(Level.OFF);

        // create scenario
        Random random = new Random(0);

        for (Position position : this.configuration.stock.getStockPositions()) {
            if (random.nextInt(100) < 70) {
                Pallet pallet = new Pallet(random.nextInt(10));
                this.configuration.stock.add(
                        position,
                        pallet
                );
            }
        }

        Event event = new TruckGeneratorEvent(this.configuration.simulation, 0, this.configuration);
        this.configuration.simulation.enqueueEvent(event);


        /*this.configuration.addStockSection(10, 10, 230, 50, 10, true);
        this.configuration.addOutdoorDock(0, this.configuration.warehouse.getDepth());
        this.configuration.addOutdoorDock(100, this.configuration.warehouse.getDepth());
        this.configuration.addOutdoorDock(200, this.configuration.warehouse.getDepth());

        SLAP slap = new SLAP(this.configuration);
        slap.solve4();*/
    }

    @Override
    public void start(Stage stage) throws Exception {
        AnimationDashboard animation = new AnimationDashboard(this.configuration);
        KPIDashboard kpiDashboard = new KPIDashboard(this.configuration);

        // test
        /*Configuration configuration = new Configuration(this.configuration.warehouse.getWidth(), this.configuration.warehouse.getDepth(), this.configuration.warehouse.getHeight());
        configuration.addStockSection(10, 10, 230, 50, 10, true);
        configuration.addOutdoorDock(0, this.configuration.warehouse.getDepth());
        configuration.addOutdoorDock(100, this.configuration.warehouse.getDepth());
        configuration.addOutdoorDock(200, this.configuration.warehouse.getDepth());

        SLAP slap = new SLAP(configuration);
        slap.solve();

        AnimationDashboard animation2 = new AnimationDashboard(configuration);*/

        BorderPane pane = new BorderPane();
        pane.setCenter(animation.getPane());
        //pane.setLeft(animation2.getPane());
        pane.setRight(kpiDashboard.getPane());

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }
}
