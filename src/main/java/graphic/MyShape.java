package graphic;

import javafx.animation.Animation;
import javafx.animation.PathTransition;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import util.Pair;
import warehouse.Position;

import java.util.LinkedList;

public abstract class MyShape {

    private static int SHAPE_ID = 0;
    protected final int id, x, y, width, height;
    protected Shape shape;

    public MyShape(int x, int y, int width, int height) {
        this.id = SHAPE_ID++;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getId() {
        return this.id;
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

    public MyAnimation getAnimation(LinkedList<Pair<Position,Double>> moves) {
        Path path = new Path();
        double duration = 0;

        for (Pair<Position,Double> move : moves) {
            Position position = move.first;
            duration += move.second;

            if (duration == 0) {
                path.getElements().add(new MoveTo(
                        position.getX() + 0.5 * this.width,
                        position.getY() + 0.5 * this.height
                ));
            } else {
                path.getElements().add(new LineTo(
                        position.getX() + 0.5 * this.width,
                        position.getY() + 0.5 * this.height
                ));
            }
        }

        PathTransition pathTransition = new PathTransition(
                Duration.seconds(duration),
                path,
                this.shape
        );

        return new MyAnimation(this, pathTransition);
    }

}
