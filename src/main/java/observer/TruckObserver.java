package observer;

import agent.Truck;
import graphic.animation.MyAnimation;
import graphic.dashboard.AnimationDashboard;
import graphic.shape.CompounedShape;
import graphic.shape.PalletShape;
import graphic.shape.TruckShape;
import javafx.animation.PathTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import util.Pair;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;

import java.util.HashMap;

public class TruckObserver implements Observer<Truck> {

    private final Configuration configuration;
    private final AnimationDashboard animationDashboard;
    private final HashMap<Integer, CompounedShape> shapes;
    private final HashMap<Integer, HashMap<Integer, PalletShape>> palletShapes;
    private Group group;

    public TruckObserver(Configuration configuration, AnimationDashboard animationDashboard) {
        this.configuration = configuration;
        this.animationDashboard = animationDashboard;
        this.shapes = new HashMap<>();
        this.palletShapes = new HashMap<>();
        this.group = new Group();
    }

    public CompounedShape add(Truck truck) {
        truck.attach(this);

        TruckShape truckShape = new TruckShape(
                0,
                0,
                this.configuration.dockWidth
        );
        CompounedShape shape = new CompounedShape(truckShape, truck.getPosition().getX(), truck.getPosition().getY());

        this.shapes.put(truck.getId(), shape);
        this.palletShapes.put(truck.getId(), new HashMap<>());

        for (Pair<Position, Pallet> pair : truck.getCurrentLoad()) {
            Position position = pair.first;
            Pallet pallet = pair.second;
            this.add(truck, position, pallet);
        }

        this.group.getChildren().add(shape.getShape());

        return shape;
    }

    public Group getGroup() {
        return this.group;
    }

    public PalletShape add(Truck truck, Position position, Pallet pallet) {
        PalletShape shape = new PalletShape(
                position.getX(),
                position.getY(),
                this.configuration.palletSize,
                pallet.getType()
        );

        this.shapes.get(truck.getId()).add(shape);
        this.palletShapes.get(truck.getId()).put(this.configuration.warehouse.toInt(position), shape);

        return shape;
    }

    public void remove(Truck truck) {
        CompounedShape shape = this.shapes.get(truck.getId());
        this.group.getChildren().remove(shape.getShape());
        this.shapes.remove(truck.getId());
        this.palletShapes.remove(truck.getId());
    }

    @Override
    public void update(Truck truck) {
        CompounedShape shape = this.shapes.get(truck.getId());
        if (shape == null) {
            this.add(truck);
        }

        if (truck.getTargetPosition() == null) { // truck is done
            this.remove(truck);
        } else if (!truck.getPosition().equals(truck.getTargetPosition())) {
            Path path = new Path();
            path.getElements().add(new MoveTo(
                    truck.getPosition().getX() + 0.5 * shape.getWidth(),
                    truck.getPosition().getY() + 0.5 * shape.getHeight()
            ));
            path.getElements().add(new LineTo(
                    truck.getTargetPosition().getX() + 0.5 * shape.getWidth(),
                    truck.getTargetPosition().getY() + 0.5 * shape.getHeight()
            ));

            PathTransition pathTransition = new PathTransition(
                    Duration.seconds(truck.getPosition().manhattanDistance2D(truck.getTargetPosition()) / truck.getSpeed()),
                    path,
                    shape.getShape()
            );

            this.animationDashboard.add(new MyAnimation(shape, pathTransition));
        } else {
            // update current load
            for (PalletShape palletShape : this.palletShapes.get(truck.getId()).values()) {
                palletShape.setBlack();
            }

            for (Pair<Position, Pallet> pair : truck.getCurrentLoad()) {
                Position position = pair.first;
                Pallet pallet = pair.second;

                PalletShape palletShape = this.palletShapes.get(truck.getId()).get(this.configuration.warehouse.toInt(position));
                if (palletShape != null) {
                    palletShape.setType(pallet.getType());
                } else {
                    this.add(truck, position, pallet);
                }
            }
        }

    }

}
