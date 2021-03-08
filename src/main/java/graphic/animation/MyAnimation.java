package graphic.animation;

import graphic.shape.MyShape;
import javafx.animation.Animation;
import javafx.util.Duration;

public class MyAnimation {

    private final MyShape shape;
    private final Animation animation;
    private Double start;

    public MyAnimation(MyShape shape, Animation animation) {
        this.shape = shape;
        this.animation = animation;
        this.start = null;
    }

    public MyShape getShape() {
        return this.shape;
    }

    public Animation getAnimation() {
        return this.animation;
    }

    public void play() {
        if (this.animation.currentTimeProperty().get().lessThan(this.animation.getTotalDuration())) {
            this.animation.play();
        }
    }

    public void play(double time) {
        if (this.start == null) {
            this.start = time;
        }
        if (this.animation.currentTimeProperty().get().lessThan(this.animation.getTotalDuration())) {
            this.animation.playFrom(Duration.seconds(time - this.start));
        }
    }

    public void pause() {
        this.animation.pause();
    }

}
