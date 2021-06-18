package graphic.dashboard;

import agent.Dock;
import agent.Lift;
import graphic.animation.MobileAnimation;
import graphic.animation.StockAnimation;
import graphic.animation.TruckAnimation;
import graphic.shape.*;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import warehouse.Configuration;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class AnimationDashboard {

    private final Configuration configuration;
    private final Pane pane;
    private final Group group;
    private final LinkedList<Animation> animations;
    private double rate;
    private boolean autoplay;
    private Animation animation;

    private final StockAnimation stockAnimation;
    private final TruckAnimation truckAnimation;
    private final MobileAnimation mobileAnimation;

    public AnimationDashboard(Configuration configuration) {
        this.configuration = configuration;
        this.rate = 8;
        this.autoplay = true;
        this.group = new Group();
        this.animations = new LinkedList<>();
        this.animation = null;

        int width = configuration.warehouse.getWidth();
        int height = 2 * configuration.warehouse.getDepth();

        SiteShape siteShape = new SiteShape(width, height);
        WarehouseShape warehouseShape = new WarehouseShape(0, 0, configuration.warehouse.getWidth(), configuration.warehouse.getDepth());
        ProductionLineShape productionLineShape = new ProductionLineShape(
                configuration.productionLine.getPosition().getX(),
                configuration.productionLine.getPosition().getY(),
                configuration.productionLine.getWidth(),
                configuration.productionLine.getDepth());

        this.add(siteShape);
        this.add(warehouseShape);
        this.add(productionLineShape);

        for (Dock dock : configuration.docks) {
            DockShape dockShape = new DockShape(
                    dock.getPosition().getX(),
                    dock.getPosition().getY(),
                    configuration.dockWidth,
                    configuration.truckDepth
            );
            this.add(dockShape);
        }

        for (Lift lift : configuration.lifts) {
            LiftShape liftShape = new LiftShape(
                    lift.getPosition().getX(),
                    lift.getPosition().getY(),
                    configuration.palletSize,
                    configuration.palletSize
            );
            this.add(liftShape);
        }

        this.stockAnimation = new StockAnimation(this.configuration);
        this.truckAnimation = new TruckAnimation(this.configuration);
        this.mobileAnimation = new MobileAnimation(this.configuration);

        this.configuration.stock.attach(this.stockAnimation);

        this.add(this.stockAnimation.getGroup());
        this.add(this.truckAnimation.getGroup());
        this.add(this.mobileAnimation.getGroup());

        Button play = new Button("Play");
        AtomicBoolean playing = new AtomicBoolean(false);
        play.setOnMouseClicked(e -> {
            if (playing.get()) {
                this.pause();
                playing.set(false);
                play.setText("Play");
            } else {
                this.play();
                playing.set(true);
                play.setText("Pause");
            }
        });

        Button decreaseRate = new Button("/2");
        decreaseRate.setOnMouseClicked(e -> this.setRate(this.getRate() / 2));

        Button increaseRate = new Button("x2");
        increaseRate.setOnMouseClicked(e -> this.setRate(this.getRate() * 2));

        CheckBox autoPlay = new CheckBox("Play automatically");
        autoPlay.setSelected(true);
        autoPlay.setOnMouseClicked(e -> this.setAutoplay(autoPlay.isSelected()));

        Text levelText = new Text("Level");
        Spinner<Integer> spinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, this.configuration.warehouse.getHeight()/this.configuration.palletSize - 1, 0));
        spinner.valueProperty().addListener((o,v,v2) -> {
            this.stockAnimation.setLevel(v2);
        });

        Pane animationPane = new Pane(this.group);
        Pane toolbar = new HBox(play, decreaseRate, increaseRate, autoPlay, levelText, spinner);

        this.pane = new VBox(animationPane, toolbar);

        double ratio = (double) width / height;

        double pixelWidth = Math.min(600.0, ratio * 600.0);
        double pixelHeight = Math.min(600.0, 600.0 / ratio);

        Scale scale = new Scale(
                pixelWidth / width,
                pixelHeight / height
        );
        animationPane.getTransforms().add(scale);
        animationPane.setPrefSize(pixelWidth, pixelHeight);
    }

    public Pane getPane() {
        return this.pane;
    }

    public void add(MyShape shape) {
        this.add(shape.getShape());
    }

    public void remove(MyShape shape) {
        this.remove(shape.getShape());
    }

    public void add(Node node) {
        this.group.getChildren().add(node);
    }

    public void remove(Node node) {
        this.group.getChildren().remove(node);
    }

    private void play() {
        if (this.animation == null) {
            if (this.configuration.simulation.hasNextEvent()) {
                double currentTime = this.configuration.simulation.nextEvent().getTime();
                this.configuration.simulation.run(currentTime);
                double delta = this.configuration.simulation.nextEvent().getTime() - currentTime;
                this.createAnimations(currentTime, delta);

                if (this.animations.isEmpty()) {
                    this.animation = new PauseTransition(Duration.seconds(delta));
                } else {
                    ParallelTransition transition = new ParallelTransition();
                    transition.getChildren().addAll(this.animations);
                    this.animation = transition;
                }

                this.animation.setRate(this.rate);
                this.animation.play();

                this.animation.setOnFinished((event) -> {
                    this.animation = null;
                    this.animations.clear();
                    if (this.autoplay) {
                        this.play();
                    }
                });
            }
        } else {
            this.animation.play();
        }
    }

    private void pause() {
        if (this.animation != null) {
            this.animation.pause();
        }
    }

    public double getRate() {
        return this.rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
        if (this.animation != null) {
            this.animation.setRate(this.rate);
        }
    }

    public boolean isAutoplay() {
        return this.autoplay;
    }

    public void setAutoplay(boolean autoplay) {
        this.autoplay = autoplay;
    }

    private void createAnimations(double time, double delta) {
        this.animations.addAll(this.mobileAnimation.getAnimations(time, delta));
        this.animations.addAll(this.truckAnimation.getAnimations(time, delta));
    }

}
