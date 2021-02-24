package observer;

import agent.Mobile;
import graphic.MobileShape;
import javafx.scene.Group;
import javafx.scene.shape.Shape;
import warehouse.Configuration;

import java.util.HashMap;

public class MobileObserver implements Observer<Mobile> {

    private final Configuration configuration;
    private final Group group;
    private final HashMap<Integer,MobileShape> shapes;

    public MobileObserver(Configuration configuration, Group group) {
        this.configuration = configuration;
        this.group = group;
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
        this.group.getChildren().add(shape.getShape());
        return shape;
    }

    @Override
    public void update(Mobile mobile) {
        MobileShape mobileShape = this.shapes.get(mobile.getId());
        if (mobileShape == null) {
            mobileShape = this.add(mobile);
        }

        mobileShape.setPosition(mobile.getPosition());
    }

}
