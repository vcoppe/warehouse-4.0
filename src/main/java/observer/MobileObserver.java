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
import warehouse.Configuration;
import warehouse.Position;

import java.util.HashMap;
import java.util.List;

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

        List<Position> positions = this.configuration.warehouse.getPath(
                mobile.getPosition(),
                mobile.getTargetPosition()
        );

        Path path = new Path();
        boolean first = true;
        for (Position position : positions) {
            if (first) {
                path.getElements().add(new MoveTo(
                        position.getX() + 0.5 * mobileShape.getWidth(),
                        position.getY() + 0.5 * mobileShape.getHeight()
                ));
                first = false;
            } else {
                path.getElements().add(new LineTo(
                        position.getX() + 0.5 * mobileShape.getWidth(),
                        position.getY() + 0.5 * mobileShape.getHeight()
                ));
            }
        }

        PathTransition pathTransition = new PathTransition(
                Duration.seconds(this.configuration.warehouse.getTravelTime(
                        mobile.getPosition(),
                        mobile.getTargetPosition(),
                        mobile
                )),
                path,
                mobileShape.getShape()
        );

        this.shapeHandler.add(new MyAnimation(mobileShape, pathTransition));
    }

}
