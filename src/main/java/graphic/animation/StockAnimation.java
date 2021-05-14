package graphic.animation;

import agent.Stock;
import graphic.shape.PalletShape;
import javafx.scene.Group;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;

import java.util.HashMap;

public class StockAnimation {

    private final Configuration configuration;
    private final HashMap<Position, PalletShape> shapes;
    private final Group group;
    private int level;

    public StockAnimation(Configuration configuration) {
        this.configuration = configuration;
        this.shapes = new HashMap<>();
        this.group = new Group();
        this.level = 0;
    }

    public void remove(Position position, PalletShape shape) {
        this.shapes.remove(position);
        this.group.getChildren().remove(shape.getShape());
    }

    public PalletShape add(Position position, Pallet pallet) {
        PalletShape shape = new PalletShape(
                position.getX(),
                position.getY(),
                this.configuration.palletSize,
                pallet.getType()
        );
        this.shapes.put(position, shape);
        this.group.getChildren().add(shape.getShape());
        return shape;
    }

    public Group getGroup() {
        return this.group;
    }

    public void setLevel(int level) {
        this.level = level;
        this.update(this.configuration.stock);
    }

    public void update(Stock stock) {
        // add new slots/pallets
        for (Position position : stock.getAllPositions()) {
            Pallet pallet = stock.get(position);
            PalletShape palletShape = this.shapes.get(position);

            if (position.getZ() == this.level * this.configuration.palletSize) {

                if (palletShape == null) {
                    palletShape = this.add(position, pallet);
                }

                palletShape.setType(pallet.getType());
            } else if (palletShape != null) {
                this.remove(position, palletShape);
            }
        }

    }

}