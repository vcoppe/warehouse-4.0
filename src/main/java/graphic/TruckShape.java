package graphic;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class TruckShape extends BaseShape {

    public TruckShape(int x, int y, int width) {
        super(x, y, width, width * 3);
        this.shape = new Rectangle(x, y, width, this.height);
        this.shape.setFill(Color.BLACK);
    }

}