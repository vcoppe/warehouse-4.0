package observer;

import agent.Mobile;
import graphic.animation.MyAnimation;
import graphic.dashboard.AnimationDashboard;
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
import java.util.List;

public class MobileObserver implements Observer<Mobile> {

    private final Configuration configuration;
    private final AnimationDashboard animationDashboard;
    private final HashMap<Integer, CompoundShape> shapes;
    private final HashMap<Integer, PalletShape> palletShapes;
    private final Group group;

    public MobileObserver(Configuration configuration, AnimationDashboard animationDashboard) {
        this.configuration = configuration;
        this.animationDashboard = animationDashboard;
        this.shapes = new HashMap<>();
        this.palletShapes = new HashMap<>();
        this.group = new Group();

        for (Mobile mobile : configuration.mobiles) {
            this.add(mobile);
        }
    }

    public CompoundShape add(Mobile mobile) {
        mobile.attach(this);
        CompoundShape shape = new CompoundShape(
                new MobileShape(0, 0, this.configuration.palletSize),
                mobile.getPosition().getX(),
                mobile.getPosition().getY()
        );
        this.shapes.put(mobile.getId(), shape);
        this.group.getChildren().add(shape.getShape());
        return shape;
    }

    public Group getGroup() {
        return this.group;
    }

    @Override
    public void update(Mobile mobile) {
        CompoundShape mobileShape = this.shapes.get(mobile.getId());
        if (mobileShape == null) {
            mobileShape = this.add(mobile);
        }

        Mission mission = mobile.getMission();
        PalletShape palletShape = this.palletShapes.get(mobile.getId());
        if (mission == null) {
            palletShape.setEmptyMobile();
        } else {
            if (palletShape == null) {
                palletShape = new PalletShape(1, 1, this.configuration.palletSize - 2, 0);
                mobileShape.add(palletShape);
                this.palletShapes.put(mobile.getId(), palletShape);
            }

            if (mobile.getPosition().equals(mission.getStartPosition()) && mission.getPallet() != null) {
                palletShape.setType(mission.getPallet().getType());
            } else {
                palletShape.setEmptyMobile();
            }
        }

        List<Pair<Position, Double>> path = mobile.getPath();

        if (path != null) {
            SequentialTransition transition = new SequentialTransition();
            transition.interpolatorProperty().setValue(Interpolator.LINEAR);

            Path currentPath = new Path();

            Position currentPosition = mobile.getCurrentPosition();
            currentPath.getElements().add(new MoveTo(
                    currentPosition.getX() + 0.5 * mobileShape.getWidth(),
                    currentPosition.getY() + 0.5 * mobileShape.getHeight()
            ));

            Position lastPosition = currentPosition;
            double cumulTime = 0, lastTime = mobile.getPathTime();

            for (Pair<Position, Double> pair : path) {
                Position position = pair.first;
                double time = pair.second;

                if (time <= lastTime) {
                    continue;
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
                    transition.getChildren().add(new PauseTransition(Duration.seconds(time - lastTime)));
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

                    cumulTime += time - lastTime;
                }

                lastPosition = position;
                lastTime = time;
            }

            if (currentPath != null) {
                PathTransition pathTransition = new PathTransition(Duration.seconds(cumulTime), currentPath, mobileShape.getShape());
                pathTransition.interpolatorProperty().setValue(Interpolator.LINEAR);
                transition.getChildren().add(pathTransition);
            }

            this.animationDashboard.add(new MyAnimation(mobileShape, transition));
        }
    }

}
