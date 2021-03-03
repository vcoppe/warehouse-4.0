package graphic;

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
        this.animation.play();
    }

    public void play(double time) {
        if (this.start == null) {
            this.start = time;
        }
        this.animation.playFrom(Duration.seconds(time - this.start));
    }

    public void pause() {
        this.animation.pause();
    }

    public boolean done(double time) {
        if (this.start == null) {
            return false;
        }
        return Duration.seconds(time-this.start).greaterThanOrEqualTo(this.animation.getTotalDuration());
    }

}
