package observer;

import agent.Mobile;
import agent.Truck;
import graphic.MobileShape;
import graphic.TruckShape;
import javafx.scene.Group;
import javafx.scene.shape.Shape;
import warehouse.Configuration;

import java.util.HashMap;

public class TruckObserver implements Observer<Truck> {

    private final Configuration configuration;
    private final Group group;
    private final HashMap<Integer, TruckShape> shapes;

    public TruckObserver(Configuration configuration, Group group) {
        this.configuration = configuration;
        this.group = group;
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
        this.group.getChildren().add(shape.getShape());
        return shape;
    }

    public void remove(Truck truck) {
        TruckShape shape = this.shapes.get(truck.getId());
        this.group.getChildren().remove(shape.getShape());
        this.shapes.remove(truck.getId());
    }

    @Override
    public void update(Truck truck) {
        TruckShape truckShape = this.shapes.get(truck.getId());
        if (truckShape == null) {
            truckShape = this.add(truck);
        }

        if (truck.getPosition() == null) { // truck is done
            this.remove(truck);
        } else {
            truckShape.setPosition(truck.getPosition());
        }
    }

}
