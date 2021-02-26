package observer;

import agent.Mobile;
import graphic.MobileShape;
import graphic.ShapeHandler;
import util.Pair;
import warehouse.Configuration;
import warehouse.Position;

import java.util.HashMap;
import java.util.LinkedList;

public class MobileObserver implements Observer<Mobile> {

    private final Configuration configuration;
    private final ShapeHandler shapeHandler;
    private final HashMap<Integer,MobileShape> shapes;

    public MobileObserver(Configuration configuration, ShapeHandler shapeHandler) {
        this.configuration = configuration;
        this.shapeHandler = shapeHandler;
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
        this.shapeHandler.add(shape);
        return shape;
    }

    @Override
    public void update(Mobile mobile) {
        MobileShape mobileShape = this.shapes.get(mobile.getId());
        if (mobileShape == null) {
            mobileShape = this.add(mobile);
        }

        // TODO ask warehouse the real path

        LinkedList<Pair<Position,Double>> moves = new LinkedList<>();
        moves.add(new Pair<>(
                mobile.getPosition(),
                5.0/*(double) this.configuration.warehouse.getDistance(
                        mobileShape.getPosition(),
                        mobile.getPosition()
                )*/)
        );

        mobileShape.move(moves);
    }

}
