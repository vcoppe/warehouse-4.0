package graphic;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MobileShape extends MyShape {

    public MobileShape(int x, int y, int width) {
        super(x, y, width, width);
        this.shape = new Rectangle(x, y, width, width);
        this.shape.setFill(Color.BLACK);
        this.shape.setStroke(Color.BLACK);
    }

}
