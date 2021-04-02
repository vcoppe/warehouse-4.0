package observer;

import agent.Mobile;
import graphic.animation.MyAnimation;
import graphic.dashboard.AnimationDashboard;
import graphic.shape.CompoundShape;
import graphic.shape.MobileShape;
import graphic.shape.PalletShape;
import javafx.animation.PathTransition;
import javafx.animation.SequentialTransition;
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
        } else if (mobile.getPosition().equals(mission.getStartPosition())) {
            if (palletShape == null) {
                palletShape = new PalletShape(1, 1, this.configuration.palletSize - 2, mission.getPallet().getType());
                mobileShape.add(palletShape);
                this.palletShapes.put(mobile.getId(), palletShape);
            } else {
                palletShape.setType(mission.getPallet().getType());
            }
        }

        List<Pair<Position, Double>> path = mobile.getPath();

        if (path != null) {
            SequentialTransition transition = new SequentialTransition();
            Position lastPosition = null;
            double lastTime = 0;
            boolean first = true;
            for (Pair<Position, Double> pair : path) {
                Position position = pair.first;
                double time = pair.second;

                if (first) {
                    first = false;
                } else {
                    Path pathShape = new Path();
                    pathShape.getElements().add(new MoveTo(
                            lastPosition.getX() + 0.5 * mobileShape.getWidth(),
                            lastPosition.getY() + 0.5 * mobileShape.getHeight()
                    ));
                    pathShape.getElements().add(new LineTo(
                            position.getX() + 0.5 * mobileShape.getWidth(),
                            position.getY() + 0.5 * mobileShape.getHeight()
                    ));
                    PathTransition pathTransition = new PathTransition(Duration.seconds(time - lastTime), pathShape, mobileShape.getShape());
                    transition.getChildren().add(pathTransition);
                }

                lastPosition = position;
                lastTime = time;
            }

            this.animationDashboard.add(new MyAnimation(mobileShape, transition));
        }
    }

}
