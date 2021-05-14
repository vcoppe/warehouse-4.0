package graphic.animation;

import agent.Mobile;
import graphic.shape.CompoundShape;
import graphic.shape.MobileShape;
import graphic.shape.PalletShape;
import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import util.Pair;
import warehouse.Configuration;
import warehouse.Mission;
import warehouse.Position;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MobileAnimation {

    private final Configuration configuration;
    private final HashMap<Integer, CompoundShape> shapes;
    private final HashMap<Integer, PalletShape> palletShapes;
    private final Group group;

    public MobileAnimation(Configuration configuration) {
        this.configuration = configuration;
        this.shapes = new HashMap<>();
        this.palletShapes = new HashMap<>();
        this.group = new Group();

        for (Mobile mobile : configuration.mobiles) {
            this.add(mobile);
        }
    }

    public CompoundShape add(Mobile mobile) {
        CompoundShape shape = new CompoundShape(
                new MobileShape(0, 0, this.configuration.palletSize),
                mobile.getPosition().getX(),
                mobile.getPosition().getY()
        );
        this.shapes.put(mobile.getId(), shape);
        this.group.getChildren().add(shape.getShape());


        PalletShape palletShape = new PalletShape(1, 1, this.configuration.palletSize - 2, 0);
        palletShape.setEmptyMobile();
        shape.add(palletShape);
        this.palletShapes.put(mobile.getId(), palletShape);

        return shape;
    }

    public Group getGroup() {
        return this.group;
    }

    public List<Animation> getAnimations(double time, double delta) {
        LinkedList<Animation> animations = new LinkedList<>();
        for (Mobile mobile : this.configuration.mobiles) {
            Animation animation = this.getAnimation(mobile, time, delta);
            if (animation != null) {
                animations.add(animation);
            }
        }
        return animations;
    }

    public Animation getAnimation(Mobile mobile, double time, double delta) {
        CompoundShape mobileShape = this.shapes.get(mobile.getId());
        PalletShape palletShape = this.palletShapes.get(mobile.getId());

        Mission mission = mobile.getMission();
        if (mission == null) {
            palletShape.setEmptyMobile();
        } else {
            if (mobile.getPosition().equals(mission.getStartPosition()) && mission.getPallet() != null) {
                palletShape.setType(mission.getPallet().getType());
            } else {
                palletShape.setEmptyMobile();
            }
        }

        List<Pair<Position, Double>> path = mobile.getPath();

        if (path == null) {
            return null;
        }

        SequentialTransition transition = new SequentialTransition();
        transition.interpolatorProperty().setValue(Interpolator.LINEAR);

        Path currentPath = new Path();

        Position currentPosition = mobile.getPositionAt(time);
        currentPath.getElements().add(new MoveTo(
                currentPosition.getX() + 0.5 * mobileShape.getWidth(),
                currentPosition.getY() + 0.5 * mobileShape.getHeight()
        ));

        Position lastPosition = currentPosition;
        double cumulTime = 0, lastTime = time;
        boolean end = false;

        for (Pair<Position, Double> pair : path) {
            Position position = pair.first;
            double positionTime = pair.second;

            if (positionTime <= lastTime) {
                continue;
            }

            if (positionTime >= time + delta) {
                positionTime = time + delta;
                position = mobile.getPositionAt(positionTime);
                end = true;
            }

            if (position.equals(lastPosition)) {
                if (currentPath != null) {
                    if (currentPath.getElements().size() > 1) {
                        PathTransition pathTransition = new PathTransition(Duration.seconds(cumulTime), currentPath, mobileShape.getShape());
                        pathTransition.interpolatorProperty().setValue(Interpolator.LINEAR);
                        transition.getChildren().add(pathTransition);
                    }
                    cumulTime = 0;
                    currentPath = null;
                }
                transition.getChildren().add(new PauseTransition(Duration.seconds(positionTime - lastTime)));
            } else {
                if (currentPath == null) {
                    currentPath = new Path();

                    currentPath.getElements().add(new MoveTo(
                            lastPosition.getX() + 0.5 * mobileShape.getWidth(),
                            lastPosition.getY() + 0.5 * mobileShape.getHeight()
                    ));
                }

                currentPath.getElements().add(new LineTo(
                        position.getX() + 0.5 * mobileShape.getWidth(),
                        position.getY() + 0.5 * mobileShape.getHeight()
                ));

                cumulTime += positionTime - lastTime;
            }

            if (end) {
                break;
            }

            lastPosition = position;
            lastTime = positionTime;
        }

        if (currentPath != null) {
            PathTransition pathTransition = new PathTransition(Duration.seconds(cumulTime), currentPath, mobileShape.getShape());
            pathTransition.interpolatorProperty().setValue(Interpolator.LINEAR);
            transition.getChildren().add(pathTransition);
        }

        return transition;
    }

}
