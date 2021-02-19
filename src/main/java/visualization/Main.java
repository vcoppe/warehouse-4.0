package visualization;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    public double WIDTH, HEIGHT, TIMESTEP;

    //public

    @Override
    public void start(Stage stage) throws Exception {
        this.WIDTH = stage.getWidth();
        this.HEIGHT = stage.getHeight();

        stage.widthProperty().addListener((observable, oldValue, newValue) -> this.WIDTH = newValue.doubleValue());
        stage.heightProperty().addListener((observable, oldValue, newValue) -> this.HEIGHT = newValue.doubleValue());

        Rectangle rectangle = new Rectangle(10, 10, 10, 10);
        Rectangle rectangle2 = new Rectangle(10, 30, 10, 10);
        Rectangle rectangle3 = new Rectangle(10, 50, 10, 10);

        Group group = new Group(rectangle, rectangle2, rectangle3);

        Scene scene = new Scene(group,600, 300);

        stage.setScene(scene);

        stage.show();

        {
            Timeline timeline = new Timeline();
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.setAutoReverse(true);
            KeyValue kv = new KeyValue(rectangle.xProperty(), 300);
            KeyFrame kf = new KeyFrame(Duration.millis(1000), kv);
            timeline.getKeyFrames().add(kf);
            timeline.play();
        }

        {
            Timeline timeline = new Timeline();
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.setAutoReverse(true);
            KeyValue kv = new KeyValue(rectangle2.rotateProperty(), 90);
            KeyFrame kf = new KeyFrame(Duration.millis(1000), kv);
            timeline.getKeyFrames().add(kf);
            timeline.play();
        }

        {
            RotateTransition rt = new RotateTransition(Duration.millis(3000), rectangle3);
            rt.setByAngle(180);
            rt.setCycleCount(Timeline.INDEFINITE);
            rt.setAutoReverse(true);

            rt.play();
        }

    }

}
