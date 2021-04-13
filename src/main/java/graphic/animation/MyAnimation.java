package graphic.animation;

import graphic.shape.MyShape;
import javafx.animation.Animation;
import javafx.util.Duration;

public class MyAnimation {

    private final MyShape shape;
    private final Animation animation;
    private Duration progress;

    public MyAnimation(MyShape shape, Animation animation) {
        this.shape = shape;
        this.animation = animation;
        this.progress = Duration.ZERO;
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

    public void pause() {
        this.progress = this.animation.currentTimeProperty().get();
        this.animation.stop();
    }

}
