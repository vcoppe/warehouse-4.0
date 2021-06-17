package graphic.animation;

import agent.Controller;
import agent.Truck;
import graphic.shape.CompoundShape;
import graphic.shape.PalletShape;
import graphic.shape.TruckShape;
import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import observer.Observer;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TruckAnimation implements Observer<Controller> {

    private final Configuration configuration;
    private final HashMap<Integer, Truck> trucks;
    private final HashMap<Integer, CompoundShape> shapes;
    private final HashMap<Integer, HashMap<Position, PalletShape>> palletShapes;
    private Group group;

    public TruckAnimation(Configuration configuration) {
        this.configuration = configuration;
        this.trucks = new HashMap<>();
        this.shapes = new HashMap<>();
        this.palletShapes = new HashMap<>();
        this.group = new Group();

        this.configuration.controller.attach(this);
    }

    public CompoundShape add(Truck truck) {
        this.trucks.put(truck.getId(), truck);

        TruckShape truckShape = new TruckShape(
                0,
                0,
                this.configuration.truckWidth,
                this.configuration.truckDepth
        );
        CompoundShape shape = new CompoundShape(truckShape, truck.getPosition().getX(), truck.getPosition().getY());

        this.shapes.put(truck.getId(), shape);
        this.palletShapes.put(truck.getId(), new HashMap<>());

        for (Map.Entry<Position, Pallet> entry : truck.getCurrentLoad().entrySet()) {
            Position position = entry.getKey();
            Pallet pallet = entry.getValue();
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
        this.palletShapes.get(truck.getId()).put(position, shape);

        return shape;
    }

    public void remove(Truck truck) {
        CompoundShape shape = this.shapes.get(truck.getId());
        this.group.getChildren().remove(shape.getShape());
        this.trucks.remove(truck.getId());
        this.shapes.remove(truck.getId());
        this.palletShapes.remove(truck.getId());
    }

    public List<Animation> getAnimations(double time, double delta) {
        LinkedList<Truck> doneTrucks = new LinkedList<>();
        for (Truck truck : this.trucks.values()) {
            if (truck.left()) {
                doneTrucks.add(truck);
            }
        }
        for (Truck truck : doneTrucks) {
            this.remove(truck);
        }

        LinkedList<Animation> animations = new LinkedList<>();
        for (Truck truck : this.trucks.values()) {
            Animation animation = this.getAnimation(truck, time, delta);
            if (animation != null) {
                animations.add(animation);
            }
        }
        return animations;
    }

    public Animation getAnimation(Truck truck, double time, double delta) {
        CompoundShape shape = this.shapes.get(truck.getId());

        if (!truck.getPosition().equals(truck.getTargetPosition())) { // travelling to dock
            Position [] positions = {
                    truck.getPosition(),
                    new Position(truck.getPosition().getX(), Math.min(2 * this.configuration.warehouse.getDepth() - this.configuration.truckDepth, this.configuration.warehouse.getDepth() + this.configuration.truckDepth)),
                    new Position(truck.getTargetPosition().getX(), Math.min(2 * this.configuration.warehouse.getDepth() - this.configuration.truckDepth, this.configuration.warehouse.getDepth() + this.configuration.truckDepth)),
                    truck.getTargetPosition()
            };

            Path path = new Path();

            Position lastPosition = positions[0];
            double positionTime = truck.getCalledTime();
            for (Position position : positions) {
                if (positionTime > time + delta) {
                    break;
                }

                double moveTime = lastPosition.manhattanDistance2D(position) / Truck.getSpeed();

                if (time < positionTime + moveTime) {
                    Position moveStart = lastPosition, moveEnd = position;
                    if (time > positionTime) { // move start
                        double alpha = (positionTime + moveTime - time) / moveTime;
                        moveStart = new Position(
                                (int) (alpha * lastPosition.getX() + (1 - alpha) * position.getX()),
                                (int) (alpha * lastPosition.getY() + (1 - alpha) * position.getY()),
                                (int) (alpha * lastPosition.getZ() + (1 - alpha) * position.getZ())
                        );
                    }

                    if (time + delta < positionTime + moveTime) { // move end
                        double alpha = (positionTime + moveTime - (time + delta)) / (moveTime);
                        moveEnd = new Position(
                                (int) (alpha * lastPosition.getX() + (1 - alpha) * position.getX()),
                                (int) (alpha * lastPosition.getY() + (1 - alpha) * position.getY()),
                                (int) (alpha * lastPosition.getZ() + (1 - alpha) * position.getZ())
                        );
                    }

                    if (path.getElements().size() == 0) {
                        path.getElements().add(new MoveTo(
                            moveStart.getX() + 0.5 * shape.getWidth(),
                            moveStart.getY() + 0.5 * shape.getHeight()
                        ));
                    }

                    path.getElements().add(new LineTo(
                        moveEnd.getX() + 0.5 * shape.getWidth(),
                        moveEnd.getY() + 0.5 * shape.getHeight()
                    ));
                }

                positionTime += moveTime;
                lastPosition = position;
            }

            PathTransition pathTransition = new PathTransition(Duration.seconds(delta), path, shape.getShape());
            pathTransition.interpolatorProperty().setValue(Interpolator.LINEAR);
            return pathTransition;
        } else {
            // update current load
            for (PalletShape palletShape : this.palletShapes.get(truck.getId()).values()) {
                palletShape.setEmptyTruck();
            }

            for (Map.Entry<Position, Pallet> entry : truck.getCurrentLoad().entrySet()) {
                Position position = entry.getKey();
                Pallet pallet = entry.getValue();

                PalletShape palletShape = this.palletShapes.get(truck.getId()).get(position);
                if (palletShape != null) {
                    palletShape.setType(pallet.getType());
                } else {
                    this.add(truck, position, pallet);
                }
            }
        }

        return null;
    }

    @Override
    public void update(Controller controller) {
        for (Truck truck : controller.getTrucks()) {
            if (!this.trucks.containsKey(truck.getId())) {
                this.add(truck);
            }
        }
    }
}
