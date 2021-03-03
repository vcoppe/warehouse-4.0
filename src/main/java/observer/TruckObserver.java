package observer;

import agent.Truck;
import graphic.MyAnimation;
import graphic.ShapeHandler;
import graphic.TruckShape;
import javafx.animation.PathTransition;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import warehouse.Configuration;

import java.util.HashMap;

public class TruckObserver implements Observer<Truck> {

    private final Configuration configuration;
    private final ShapeHandler shapeHandler;
    private final HashMap<Integer, TruckShape> shapes;

    public TruckObserver(Configuration configuration, ShapeHandler shapeHandler) {
        this.configuration = configuration;
        this.shapeHandler = shapeHandler;
        this.shapes = new HashMap<>();
    }

    public TruckShape add(Truck truck) {
        truck.attach(this);
        TruckShape shape = new TruckShape(
                truck.getPosition().getX(),
                truck.getPosition().getY(),
                configuration.dockWidth
        );
        this.shapes.put(truck.getId(), shape);
        this.shapeHandler.add(shape);
        return shape;
    }

    public void remove(Truck truck) {
        TruckShape shape = this.shapes.get(truck.getId());
        this.shapeHandler.remove(shape);
        this.shapes.remove(truck.getId());
    }

    @Override
    public void update(Truck truck) {
        TruckShape truckShape = this.shapes.get(truck.getId());
        if (truckShape == null) {
            truckShape = this.add(truck);
        }

        if (truck.getTargetPosition() == null) { // truck is done
            this.remove(truck);
        } else if (!truck.getPosition().equals(truck.getTargetPosition())) {
            Path path = new Path();
            path.getElements().add(new MoveTo(
                    truck.getPosition().getX() + 0.5 * truckShape.getWidth(),
                    truck.getPosition().getY() + 0.5 * truckShape.getHeight()
            ));
            path.getElements().add(new LineTo(
                    truck.getTargetPosition().getX() + 0.5 * truckShape.getWidth(),
                    truck.getTargetPosition().getY() + 0.5 * truckShape.getHeight()
            ));

            PathTransition pathTransition = new PathTransition(
                    Duration.seconds(this.configuration.warehouse.getDistance(
                            truck.getPosition(),
                            truck.getTargetPosition()
                    )),
                    path,
                    truckShape.getShape()
            );

            this.shapeHandler.add(new MyAnimation(truckShape, pathTransition));
        }
    }

}
