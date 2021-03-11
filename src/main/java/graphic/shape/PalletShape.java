package graphic.shape;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import warehouse.Pallet;

public class PalletShape extends MyShape {

    private static final Color[] colors = {
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.MAGENTA,
            Color.DARKORANGE,
            Color.CYAN,
            Color.SIENNA,
            Color.DARKRED,
            Color.SPRINGGREEN,
            Color.DEEPSKYBLUE,
            Color.DARKORCHID,
            Color.TEAL,
            Color.PALEVIOLETRED
    };

    public PalletShape(int x, int y, int  width, int type) {
        super(x, y, width, width);
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

    public void setEmptyTruck() {
        this.shape.setFill(Color.DARKGREY);
        this.shape.setStroke(Color.DARKGREY);
    }

    public void setEmptyMobile() {
        this.shape.setFill(Color.BLACK);
        this.shape.setStroke(Color.BLACK);
    }

}
