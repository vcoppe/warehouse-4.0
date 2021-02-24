package graphic;

import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import warehouse.Position;

public abstract class BaseShape {

    protected final int x, y;
    protected Shape shape;

    public BaseShape(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Shape getShape() {
        return this.shape;
    }

    public void setPosition(Position position) {
        shape.setTranslateX(position.getX() - x);
        shape.setTranslateY(position.getY() - y);
    }

    // create methods to change shape properties
    // create methods for animations

}
