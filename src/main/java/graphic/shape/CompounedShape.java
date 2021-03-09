package graphic.shape;

import javafx.scene.Group;
import javafx.scene.Node;

import java.util.ArrayList;

public class CompounedShape extends MyShape {

    private final ArrayList<MyShape> shapes;
    private final Group group;

    public CompounedShape() {
        super(0, 0, 0, 0);
        this.shapes = new ArrayList<>();
        this.group = new Group();
    }

    public CompounedShape(MyShape shape) {
        this();
        this.add(shape);
    }

    public void add(MyShape shape) {
        if (this.shapes.size() == 0) {
            this.x = shape.x;
            this.y = shape.y;
            this.width = shape.width;
            this.height = shape.height;
        } else {
            this.width = Math.max(
                    this.width,
                    Math.max(
                            shape.width + shape.x - this.x,
                            this.width + this.x - shape.x
                    )
            );
            this.height = Math.max(
                    this.height,
                    Math.max(
                            shape.height + shape.y - this.y,
                            this.height + this.y - shape.y
                    )
            );
            this.x = Math.min(this.x, shape.x);
            this.y = Math.min(this.y, shape.y);
        }
        this.shapes.add(shape);
        this.group.getChildren().add(shape.getShape());
    }

    public void remove(MyShape shape) {
        this.shapes.remove(shape);
        this.group.getChildren().remove(shape.getShape());
    }

    @Override
    public Node getShape() {
        return this.group;
    }
}
