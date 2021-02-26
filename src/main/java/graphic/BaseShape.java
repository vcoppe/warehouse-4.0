package graphic;

import javafx.animation.Animation;
import javafx.animation.PathTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import util.Pair;
import warehouse.Position;

import java.util.LinkedList;

public abstract class BaseShape {

    protected final int x, y, width, height;
    protected final LinkedList<Animation> animations;
    protected Shape shape;

    public BaseShape(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.animations = new LinkedList<>();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Shape getShape() {
        return this.shape;
    }

    public Position getPosition() {
        return new Position(this.x + (int) this.shape.getTranslateX(), this.y + (int) this.shape.getTranslateY());
    }

    public LinkedList<Animation> getAnimations() {
        return this.animations;
    }

    public void move(LinkedList<Pair<Position,Double>> moves) {

        Path path = new Path();
        path.getElements().add(new MoveTo(
                this.getPosition().getX() + 0.5 * this.width,
                this.getPosition().getY() + 0.5 * this.height
        ));

        double duration = 0;

        for (Pair<Position,Double> move : moves) {
            Position position = move.first;
            duration += move.second;

            /* temporary */
            //this.shape.setTranslateX(position.getX() - this.x);
            //this.shape.setTranslateY(position.getY() - this.y);
            /* --------- */

            path.getElements().add(new LineTo(
                    position.getX() + 0.5 * this.width,
                    position.getY() + 0.5 * this.height
            ));
        }

        PathTransition pathTransition = new PathTransition(
                Duration.seconds(duration),
                path,
                this.shape
        );

        pathTransition.setOnFinished((event) -> {
            this.animations.remove(pathTransition);
            System.out.println("animation finished");
        });

        this.animations.add(pathTransition);
    }

}
