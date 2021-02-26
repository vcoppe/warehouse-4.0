package observer;

import agent.Truck;
import graphic.ShapeHandler;
import graphic.TruckShape;
import util.Pair;
import warehouse.Configuration;
import warehouse.Position;

import java.util.HashMap;
import java.util.LinkedList;

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

        if (truck.getPosition() == null) { // truck is done
            this.remove(truck);
        } else if (!truckShape.getPosition().equals(truck.getPosition())) {
            LinkedList<Pair<Position,Double>> moves = new LinkedList<>();
            moves.add(new Pair<>(truck.getPosition(), 5.0));
            truckShape.move(moves);
        }
    }

}
