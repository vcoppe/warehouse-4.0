package observer;

import agent.Stock;
import graphic.PalletShape;
import javafx.scene.Group;
import javafx.scene.shape.Shape;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;

import java.util.HashMap;

public class StockObserver implements Observer<Stock> {

    private final Configuration configuration;
    private final Group group;
    private final HashMap<Integer, PalletShape> shapes;

    public StockObserver(Configuration configuration, Group group) {
        this.configuration = configuration;
        this.group = group;
        this.shapes = new HashMap<>();
    }

    public PalletShape add(Position position, Pallet pallet) {
        PalletShape shape = new PalletShape(
                position.getX(),
                position.getY(),
                this.configuration.palletSize,
                pallet.getType()
        );
        this.shapes.put(this.configuration.stock.toInt(position), shape);
        this.group.getChildren().add(shape.getShape());
        return shape;
    }

    @Override
    public void update(Stock stock) {
        // add new slots/pallets
        for (Position position : stock.getAllPositions()) {
            Pallet pallet = stock.get(position);

            PalletShape palletShape = this.shapes.get(stock.toInt(position));
            if (palletShape == null) {
                palletShape = this.add(position, pallet);
            }

            palletShape.setPosition(position);
            palletShape.setType(pallet.getType());
        }

    }

}
