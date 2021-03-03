package graphic;

import agent.Dock;
import agent.Mobile;
import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
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
    private final Pane pane;
    private final Group group;
    private final HashMap<Integer,MyAnimation> animations;
    private EventHandler callback;

    public ShapeHandler(Configuration configuration) {
        this.shapes = new LinkedList<>();
        this.group = new Group();
        this.pane = new Pane(group);
        this.animations = new HashMap<>();

        int width = configuration.warehouse.getWidth();
        int height = 3 * configuration.warehouse.getDepth() / 2;
        double ratio = (double) width / height;

        double pixelWidth = Math.min(600.0, ratio * 600.0);
        double pixelHeight = Math.min(600.0, 600.0 / ratio);

        Scale scale = new Scale(
                pixelWidth / width,
                pixelHeight / height
        );
        this.pane.getTransforms().add(scale);
        this.pane.setPrefSize(pixelWidth, pixelHeight);

        SiteShape siteShape = new SiteShape(width, height);
        this.add(siteShape);

        WarehouseShape warehouseShape = new WarehouseShape(0, 0, configuration.warehouse.getWidth(), configuration.warehouse.getDepth());
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

        animation.getAnimation().setOnFinished((event) -> this.remove(animation));
    }

    public void remove(MyAnimation animation) {
        if (this.animations.get(animation.getShape().getId()) == animation) {
            this.animations.remove(animation.getShape().getId());
        }
    }

    public LinkedList<MyShape> getShapes() {
        return this.shapes;
    }

    public Pane getPane() {
        return this.pane;
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
