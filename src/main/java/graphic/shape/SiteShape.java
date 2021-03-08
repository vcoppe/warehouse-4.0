package graphic.shape;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SiteShape extends MyShape {

    public SiteShape(int width, int height) {
        super(0, 0, width, height);
        this.shape = new Rectangle(0, 0, width, height);
        this.shape.setFill(Color.GREY);
        this.shape.setStroke(Color.GREY);
    }

}