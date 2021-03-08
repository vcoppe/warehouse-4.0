package observer;

import agent.Truck;
import graphic.animation.MyAnimation;
import graphic.dashboard.AnimationDashboard;
import graphic.shape.TruckShape;
import javafx.animation.PathTransition;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import warehouse.Configuration;

import java.util.HashMap;

public class TruckObserver implements Observer<Truck> {

    private final Configuration configuration;
    private final AnimationDashboard animationDashboard;
    private final HashMap<Integer, TruckShape> shapes;

    public TruckObserver(Configuration configuration, AnimationDashboard animationDashboard) {
        this.configuration = configuration;
        this.animationDashboard = animationDashboard;
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
        this.animationDashboard.add(shape);
        return shape;
    }

    public void remove(Truck truck) {
        TruckShape shape = this.shapes.get(truck.getId());
        this.animationDashboard.remove(shape);
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
                    Duration.seconds(truck.getPosition().manhattanDistance2D(truck.getTargetPosition()) / truck.getSpeed()),
                    path,
                    truckShape.getShape()
            );

            this.animationDashboard.add(new MyAnimation(truckShape, pathTransition));
        }
    }

}
