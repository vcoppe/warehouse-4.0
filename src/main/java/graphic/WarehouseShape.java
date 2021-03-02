package graphic;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class WarehouseShape extends MyShape {

    public WarehouseShape(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.shape = new Rectangle(x, y, width, height);
        this.shape.setFill(Color.LIGHTGREY);
        this.shape.setStroke(Color.GREY);
    }

}
