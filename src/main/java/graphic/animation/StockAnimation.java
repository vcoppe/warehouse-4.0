package graphic.animation;

import agent.Stock;
import graphic.shape.PalletShape;
import javafx.scene.Group;
import observer.Observer;
import util.Vector3D;
import warehouse.Configuration;
import warehouse.Pallet;

import java.util.HashMap;

public class StockAnimation implements Observer<Stock> {

    private final Configuration configuration;
    private final HashMap<Vector3D, PalletShape> shapes;
    private final Group group;
    private int level;

    public StockAnimation(Configuration configuration) {
        this.configuration = configuration;
        this.shapes = new HashMap<>();
        this.group = new Group();
        this.level = 0;
    }

    public void remove(Vector3D position, PalletShape shape) {
        this.shapes.remove(position);
        this.group.getChildren().remove(shape.getShape());
    }

    public PalletShape add(Vector3D position, Pallet pallet) {
        PalletShape shape = new PalletShape(
                position.getX(),
                position.getY(),
                Configuration.palletSize,
                pallet.getProduct()
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

    @Override
    public void update(Stock stock) {
        // add new slots/pallets
        for (Vector3D position : stock.getAllPositions()) {
            Pallet pallet = stock.get(position);
            PalletShape palletShape = this.shapes.get(position);

            if (position.getZ() == this.level * Configuration.palletSize) {

                if (palletShape == null) {
                    palletShape = this.add(position, pallet);
                }

                palletShape.setType(pallet.getProduct());
            } else if (palletShape != null) {
                this.remove(position, palletShape);
            }
        }

    }
}
