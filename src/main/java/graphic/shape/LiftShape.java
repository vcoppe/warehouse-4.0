package graphic.shape;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class LiftShape extends CompoundShape {

    public LiftShape(int x, int y, int width, int height) {
        super();
        this.add(new LiftRectangleShape(x, y, width, height));

        int radius = 2;
        this.add(new LiftCircleShape(x, y, radius));
        this.add(new LiftCircleShape(x + width, y, radius));
        this.add(new LiftCircleShape(x, y + height, radius));
        this.add(new LiftCircleShape(x + width, y + height, radius));
    }

    public class LiftCircleShape extends MyShape {

        public LiftCircleShape(int x, int y, int radius) {
            super(x, y, radius, radius);
            this.shape = new Circle(x, y, width);
            this.shape.setFill(Color.BLACK);
        }

    }

    public class LiftRectangleShape extends MyShape {

        public LiftRectangleShape(int x, int y, int width, int height) {
            super(x, y, width, height);
            this.shape = new Rectangle(x, y, width, height);
            this.shape.setFill(Color.DIMGREY);
            this.shape.setStroke(Color.BLACK);
        }
    }

}
