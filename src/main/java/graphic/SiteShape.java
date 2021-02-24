package graphic;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SiteShape extends BaseShape {

    public SiteShape(int width, int height) {
        super(0, 0);
        this.shape = new Rectangle(0, 0, width, height);
        this.shape.setFill(Color.GREY);
    }

}