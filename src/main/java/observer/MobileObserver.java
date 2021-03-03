package observer;

import agent.Mobile;
import graphic.MobileShape;
import graphic.MyAnimation;
import graphic.ShapeHandler;
import javafx.animation.PathTransition;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import util.Pair;
import warehouse.Configuration;
import warehouse.Position;

import java.util.HashMap;
import java.util.LinkedList;

public class MobileObserver implements Observer<Mobile> {

    private final Configuration configuration;
    private final ShapeHandler shapeHandler;
    private final HashMap<Integer,MobileShape> shapes;

    public MobileObserver(Configuration configuration, ShapeHandler shapeHandler) {
        this.configuration = configuration;
        this.shapeHandler = shapeHandler;
        this.shapes = new HashMap<>();
    }

    public MobileShape add(Mobile mobile) {
        mobile.attach(this);
        MobileShape shape = new MobileShape(
                mobile.getPosition().getX(),
                mobile.getPosition().getY(),
                this.configuration.palletSize
        );
        this.shapes.put(mobile.getId(), shape);
        this.shapeHandler.add(shape);
        return shape;
    }

    @Override
    public void update(Mobile mobile) {
        MobileShape mobileShape = this.shapes.get(mobile.getId());
        if (mobileShape == null) {
            mobileShape = this.add(mobile);
        }

        // TODO ask warehouse the real path

        LinkedList<Pair<Position, Double>> moves = new LinkedList<>();
        moves.add(new Pair<>(mobile.getPosition(), 0.0));
        moves.add(new Pair<>(
                mobile.getTargetPosition(),
                this.configuration.warehouse.getDistance(
                        mobile.getPosition(),
                        mobile.getTargetPosition()
                ))
        );

        Path path = new Path();
        path.getElements().add(new MoveTo(
                mobile.getPosition().getX() + 0.5 * mobileShape.getWidth(),
                mobile.getPosition().getY() + 0.5 * mobileShape.getHeight()
        ));
        path.getElements().add(new LineTo(
                mobile.getTargetPosition().getX() + 0.5 * mobileShape.getWidth(),
                mobile.getTargetPosition().getY() + 0.5 * mobileShape.getHeight()
        ));
        //path.getTransforms().add(this.shapeHandler.getScale());

        PathTransition pathTransition = new PathTransition(
                Duration.seconds(this.configuration.warehouse.getDistance(
                        mobile.getPosition(),
                        mobile.getTargetPosition()
                )),
                path,
                mobileShape.getShape()
        );

        this.shapeHandler.add(new MyAnimation(mobileShape, pathTransition));
    }

}
