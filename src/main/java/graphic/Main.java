package graphic;

import event.ProductionGeneratorEvent;
import event.TruckGeneratorEvent;
import graphic.dashboard.AnimationDashboard;
import graphic.dashboard.KPIDashboard;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import util.Vector3D;
import warehouse.Configuration;
import warehouse.Scenario;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    private final Configuration configuration;

    public Main() {
        super();

        this.configuration = new Configuration(600, 300, 40);

        int width = this.configuration.warehouse.getWidth(), depth = this.configuration.warehouse.getDepth();

        int productionLineX = width - 70, productionLineY = 20, productionLineWidth = 50, productionLineDepth = 100;

        ArrayList<Vector3D> productionLineStartBuffer = new ArrayList<>();
        ArrayList<Vector3D> productionLineEndBuffer = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            productionLineStartBuffer.add(new Vector3D(productionLineX - 2 * this.configuration.palletSize + i / 5 * this.configuration.palletSize, productionLineY + (i % 5) * this.configuration.palletSize));
            productionLineEndBuffer.add(new Vector3D(productionLineX + (i % 5) * this.configuration.palletSize, productionLineY + productionLineDepth + i / 5 * this.configuration.palletSize));
        }

        this.configuration.addProductionLine(productionLineX, productionLineY, productionLineX + productionLineWidth, productionLineY + productionLineDepth, 10, productionLineStartBuffer, productionLineEndBuffer);

        this.configuration.addStockSection(20, 20, 120, 120, 20, true);
        this.configuration.addStockSection(20, 150, 120, 250, 20, true);

        this.configuration.addAutoStockSection(150, 20, 270, 200, 40, false, false, false, true, true);

        this.configuration.addStockSection(300, 20, 450, 160, 20, false);

        for (int i = 0; i < 5; i++) this.configuration.addOutdoorDock(i * this.configuration.dockWidth, depth);
        for (int i = 0; i < 3; i++)
            this.configuration.addIndoorDock(300 + i * this.configuration.dockWidth * 2, depth - this.configuration.truckDepth);
        for (int i = 0; i < 5; i++)
            this.configuration.addMobile(new Vector3D(this.configuration.dockWidth * i, depth - this.configuration.palletSize));

        // disable JavaFX logging
        Logger logger = Logger.getLogger("javafx");
        logger.setLevel(Level.OFF);

        // create scenario
        Scenario scenario = new Scenario(this.configuration, 15);
        scenario.createZones();
        scenario.initStock();

        this.configuration.simulation.enqueueEvent(new TruckGeneratorEvent(this.configuration.simulation, 0, this.configuration, scenario));
        this.configuration.simulation.enqueueEvent(new ProductionGeneratorEvent(this.configuration.simulation, 30, this.configuration, scenario, this.configuration.productionLines.get(0)));
    }

    @Override
    public void start(Stage stage) throws Exception {
        AnimationDashboard animation = new AnimationDashboard(this.configuration);
        KPIDashboard kpiDashboard = new KPIDashboard(this.configuration);

        BorderPane pane = new BorderPane();
        pane.setCenter(animation.getPane());
        pane.setRight(kpiDashboard.getPane());

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }
}
