package graphic;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ProductionLineShape extends BaseShape {

    public ProductionLineShape(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.shape = new Rectangle(x, y, width, height);
        this.shape.setFill(Color.DARKGREY);
        this.shape.setStroke(Color.GREY);
    }

}
