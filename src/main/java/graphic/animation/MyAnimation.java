package graphic.animation;

import graphic.shape.MyShape;
import javafx.animation.Animation;
import javafx.util.Duration;

public class MyAnimation {

    private final MyShape shape;
    private final Animation animation;
    private Duration progress;
    private double start;

    public MyAnimation(MyShape shape, Animation animation) {
        this.shape = shape;
        this.animation = animation;
        this.progress = Duration.ZERO;
        this.start = -1;
    }

    public MyShape getShape() {
        return this.shape;
    }

    public Animation getAnimation() {
        return this.animation;
    }

    public void play() {
        if (this.progress.lessThan(this.animation.getTotalDuration())) {
            this.animation.playFrom(this.progress);
        }
    }

    public void play(double from) {
        if (this.start == -1) {
            this.start = from;
        }

        if (Duration.seconds(from-this.start).lessThan(this.animation.getTotalDuration())) {
            this.animation.playFrom(Duration.seconds(from-this.start));
        }
    }

    public void pause() {
        this.progress = this.animation.currentTimeProperty().get();
        this.animation.stop();
    }

}
