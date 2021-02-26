package graphic;

import agent.Dock;
import agent.Mobile;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.scene.Group;
import observer.ControllerObserver;
import observer.MobileObserver;
import observer.StockObserver;
import observer.TruckObserver;
import warehouse.Configuration;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class ShapeHandler {

    private final LinkedList<BaseShape> shapes;
    private final Group group;

    public ShapeHandler(Configuration configuration) {
        // TODO compute scaling and apply to all

        this.shapes = new LinkedList<>();
        this.group = new Group();

        SiteShape siteShape = new SiteShape(configuration.warehouse.width, 3 * configuration.warehouse.depth / 2);
        this.add(siteShape);

        WarehouseShape warehouseShape = new WarehouseShape(0, 0, configuration.warehouse.width, configuration.warehouse.depth);
        this.add(warehouseShape);

        ProductionLineShape productionLineShape = new ProductionLineShape(
                configuration.productionLine.getPosition().getX(),
                configuration.productionLine.getPosition().getY(),
                configuration.productionLine.getWidth(),
                configuration.productionLine.getDepth());
        this.add(productionLineShape);

        for (Dock dock : configuration.docks) {
            DockShape dockShape = new DockShape(
                    dock.getPosition().getX(),
                    dock.getPosition().getY(),
                    configuration.dockWidth
            );
            this.add(dockShape);
        }

        MobileObserver mobileObserver = new MobileObserver(configuration, this);
        for (Mobile mobile : configuration.mobiles) {
            mobileObserver.add(mobile);
        }

        StockObserver stockObserver = new StockObserver(configuration, this);
        configuration.stock.attach(stockObserver);
        stockObserver.update(configuration.stock);

        TruckObserver truckObserver = new TruckObserver(configuration, this);
        ControllerObserver controllerObserver = new ControllerObserver(truckObserver);
        configuration.controller.attach(controllerObserver);

    }

    public void add(BaseShape shape) {
        this.shapes.add(shape);
        this.group.getChildren().add(shape.getShape());
    }

    public void remove(BaseShape shape) {
        this.shapes.remove(shape);
        this.group.getChildren().remove(shape.getShape());
    }

    public LinkedList<BaseShape> getShapes() {
        return this.shapes;
    }

    public Group getGroup() {
        return this.group;
    }

    // TODO handle animations, play all at the same time using ParallelAnimation
    public void playAnimations() {
        ParallelTransition parallelAnimation = new ParallelTransition();

        for (BaseShape shape : this.shapes) {
            for (Animation animation : shape.getAnimations()) {
                parallelAnimation.getChildren().add(animation);
            }
        }

        parallelAnimation.play();
    }

}
