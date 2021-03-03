package graphic;

import javafx.scene.shape.Shape;

public abstract class MyShape {

    private static int SHAPE_ID = 0;
    protected final int id, x, y, width, height;
    protected Shape shape;

    public MyShape(int x, int y, int width, int height) {
        this.id = SHAPE_ID++;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getId() {
        return this.id;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Shape getShape() {
        return this.shape;
    }

}
