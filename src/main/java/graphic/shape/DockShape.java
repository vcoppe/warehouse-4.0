package graphic.shape;

import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;

public class DockShape extends CompoundShape {

    public DockShape(int x, int y, int width, int height) {
        super();
        this.add(new DockLinesShape(x, y, width, height));
        this.add(new DockGroundShape(x, y, width, height));
    }

    public class DockLinesShape extends MyShape {
        public DockLinesShape(int x, int y, int width, int height) {
            super(x, y, width, height);
            MoveTo start1 = new MoveTo(x, y);
            LineTo line1 = new LineTo(x, y+this.height);
            MoveTo start2 = new MoveTo(x+width, y);
            LineTo line2 = new LineTo(x+width, y+this.height);
            this.shape = new Path(start1, line1, start2, line2);
            this.shape.setStroke(Color.WHITE);
        }
    }

    public class DockGroundShape extends MyShape {
        public DockGroundShape(int x, int y, int width, int height) {
            super(x, y, width, height);
            this.shape = new Rectangle(x, y, width, height);
            this.shape.setFill(Color.GREY);
        }
    }

}
