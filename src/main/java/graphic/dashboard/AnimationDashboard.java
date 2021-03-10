package graphic.dashboard;

import agent.Dock;
import agent.Mobile;
import graphic.animation.MyAnimation;
import graphic.shape.*;
import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
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

public class AnimationDashboard {

    private final Pane pane;
    private final Group group;
    private final HashMap<Integer, MyAnimation> animations;
    private EventHandler callback;
    private PauseTransition pauseTransition;
    private double rate;
    private boolean autoplay;

    public AnimationDashboard(Configuration configuration) {
        this.group = new Group();
        this.pane = new Pane(this.group);
        this.animations = new HashMap<>();
        this.rate = 1;
        this.autoplay = true;

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
        WarehouseShape warehouseShape = new WarehouseShape(0, 0, configuration.warehouse.getWidth(), configuration.warehouse.getDepth());
        ProductionLineShape productionLineShape = new ProductionLineShape(
                configuration.productionLine.getPosition().getX(),
                configuration.productionLine.getPosition().getY(),
                configuration.productionLine.getWidth(),
                configuration.productionLine.getDepth());

        this.add(siteShape.getShape());
        this.add(warehouseShape.getShape());
        this.add(productionLineShape.getShape());

        for (Dock dock : configuration.docks) {
            DockShape dockShape = new DockShape(
                    dock.getPosition().getX(),
                    dock.getPosition().getY(),
                    configuration.dockWidth
            );
            this.add(dockShape.getShape());
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

        this.add(truckObserver.getGroup());
        this.add(stockObserver.getGroup());
        this.add(mobileObserver.getGroup());
    }

    public void add(Node node) {
        this.group.getChildren().add(node);
    }

    public void remove(Node node) {
        this.group.getChildren().remove(node);
    }

    public void add(MyAnimation animation) {
        this.animations.put(animation.getShape().getId(), animation);
    }

    public Pane getPane() {
        return this.pane;
    }

    private void playAnimations(double start) {
        for (MyAnimation animation : this.animations.values()) {
            animation.getAnimation().setRate(this.rate);
            animation.play(start);
        }
    }

    private void pauseAnimations() {
        for (MyAnimation animation : this.animations.values()) {
            animation.pause();
        }
    }

    public void playAnimations(double start, double delta) {
        if (this.pauseTransition != null) {
            this.resumeAnimation();
        } else {
            this.pauseTransition = new PauseTransition(Duration.seconds(delta));
            this.pauseTransition.setRate(this.rate);
            this.pauseTransition.setOnFinished((event) -> {
                this.pauseAnimations();
                this.pauseTransition = null;
                if (this.autoplay) {
                    this.callback.handle(event);
                }
            });
            this.pauseTransition.play();
            this.playAnimations(start);
        }
    }

    public void pauseAnimation() {
        this.pauseAnimations();
        if (this.pauseTransition != null) {
            this.pauseTransition.pause();
        }
    }

    public void resumeAnimation() {
        if (this.pauseTransition != null) {
            for (MyAnimation animation : this.animations.values()) {
                animation.play();
            }
            this.pauseTransition.play();
        } else {
            this.callback.handle(null);
        }
    }

    public void setCallback(EventHandler handler) {
        this.callback = handler;
    }

    public double getRate() {
        return this.rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
        if (this.pauseTransition != null) {
            this.pauseTransition.setRate(rate);
        }
        for (MyAnimation animation : this.animations.values()) {
            animation.getAnimation().setRate(rate);
        }
    }

    public boolean isAutoplay() {
        return this.autoplay;
    }

    public void setAutoplay(boolean autoplay) {
        this.autoplay = autoplay;
    }
}
