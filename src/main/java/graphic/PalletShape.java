package graphic;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import warehouse.Pallet;

public class PalletShape extends BaseShape {

    private static final Color[] colors = {
            Color.GREEN,
            Color.BLUE,
            Color.RED,
            Color.YELLOW,
            Color.ORANGE,
            Color.PINK
    };

    public PalletShape(int x, int y, int  width, int type) {
        super(x, y);
        this.shape = new Rectangle(x, y,width, width);
        this.setType(type);
    }

    public void setType(int type) {
        if (type == Pallet.FREE.getType()) {
            this.shape.setFill(Color.WHITE);
            this.shape.setStroke(Color.GREY);
        } else {
            this.shape.setFill(colors[type % colors.length]);
            this.shape.setStroke(Color.BLACK);
        }
    }

}
