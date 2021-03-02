package graphic;

import agent.Dock;
import agent.Mobile;
import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.util.Duration;
import observer.ControllerObserver;
import observer.MobileObserver;
import observer.StockObserver;
import observer.TruckObserver;
import warehouse.Configuration;

import java.util.HashMap;
import java.util.LinkedList;

public class ShapeHandler {

    private final LinkedList<MyShape> shapes;
    private final Group group;
    private final HashMap<Integer,MyAnimation> animations;
    private EventHandler callback;

    public ShapeHandler(Configuration configuration) {
        // TODO compute scaling and apply to all

        this.shapes = new LinkedList<>();
        this.group = new Group();
        this.animations = new HashMap<>();

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

    public void add(MyShape shape) {
        this.shapes.add(shape);
        this.group.getChildren().add(shape.getShape());
    }

    public void remove(MyShape shape) {
        this.shapes.remove(shape);
        this.group.getChildren().remove(shape.getShape());
    }

    public void add(MyAnimation animation) {
        this.animations.put(animation.getShape().getId(), animation);

        animation.getAnimation().setOnFinished((event) -> {
            //this.remove(animation);
            System.out.println("animation finished");
        });
    }

    public void remove(MyAnimation animation) {
        if (this.animations.get(animation.getShape().getId()) == animation) {
            this.animations.remove(animation.getShape().getId());
        }
    }

    public LinkedList<MyShape> getShapes() {
        return this.shapes;
    }

    public Group getGroup() {
        return this.group;
    }

    private void playAnimations(double start) {
        LinkedList<MyAnimation> toRemove = new LinkedList<>();
        for (MyAnimation animation : this.animations.values()) {
            if (animation.done(start)) {
                toRemove.add(animation);
            }
        }
        for (MyAnimation animation : toRemove) {
            this.remove(animation);
        }
        for (MyAnimation animation : this.animations.values()) {
            animation.play(start);
        }
    }

    private void pauseAnimations() {
        for (MyAnimation animation : this.animations.values()) {
            animation.pause();
        }
    }

    public void playAnimations(double start, double delta) {
        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(delta));
        pauseTransition.setOnFinished((event) -> {
            this.pauseAnimations();
            this.callback.handle(event);
        });
        pauseTransition.play();
        this.playAnimations(start);
    }

    public void setCallback(EventHandler handler) {
        this.callback = handler;
    }

}
