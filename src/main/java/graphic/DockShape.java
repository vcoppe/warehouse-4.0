package graphic;

import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class DockShape extends BaseShape {

    public DockShape(int x, int y, int width) {
        super(x, y, width, 3 * width);
        MoveTo start1 = new MoveTo(x, y);
        LineTo line1 = new LineTo(x, y+this.height);
        MoveTo start2 = new MoveTo(x+width, y);
        LineTo line2 = new LineTo(x+width, y+this.height);
        this.shape = new Path(start1, line1, start2, line2);
        this.shape.setStroke(Color.WHITE);
    }

}
