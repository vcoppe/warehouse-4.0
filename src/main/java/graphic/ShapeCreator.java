package graphic;

import agent.Dock;
import agent.Mobile;
import javafx.scene.Group;
import observer.ControllerObserver;
import observer.MobileObserver;
import observer.StockObserver;
import observer.TruckObserver;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;

public class ShapeCreator {

    public static Group getShapes(Configuration configuration) {
        // TODO compute scaling and apply to all
        // TODO create observers and bind them

        Group group = new Group();

        SiteShape siteShape = new SiteShape(configuration.warehouse.width, 3 * configuration.warehouse.depth / 2);
        WarehouseShape warehouseShape = new WarehouseShape(0, 0, configuration.warehouse.width, configuration.warehouse.depth);
        ProductionLineShape productionLineShape = new ProductionLineShape(
                configuration.productionLine.getPosition().getX(),
                configuration.productionLine.getPosition().getY(),
                configuration.productionLine.getWidth(),
                configuration.productionLine.getDepth());
        group.getChildren().addAll(siteShape.getShape(), warehouseShape.getShape(), productionLineShape.getShape());

        for (Dock dock : configuration.docks) {
            DockShape dockShape = new DockShape(
                    dock.getPosition().getX(),
                    dock.getPosition().getY(),
                    configuration.dockWidth
            );
            group.getChildren().add(dockShape.getShape());
        }

        MobileObserver mobileObserver = new MobileObserver(configuration, group);
        for (Mobile mobile : configuration.mobiles) {
            mobileObserver.add(mobile);
        }

        StockObserver stockObserver = new StockObserver(configuration, group);
        configuration.stock.attach(stockObserver);
        stockObserver.update(configuration.stock);

        TruckObserver truckObserver = new TruckObserver(configuration, group);
        ControllerObserver controllerObserver = new ControllerObserver(truckObserver);
        configuration.controller.attach(controllerObserver);

        return group;
    }

}
