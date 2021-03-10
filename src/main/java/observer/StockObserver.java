package observer;

import agent.Stock;
import graphic.shape.PalletShape;
import graphic.dashboard.AnimationDashboard;
import javafx.scene.Group;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;

import java.util.HashMap;

public class StockObserver implements Observer<Stock> {

    private final Configuration configuration;
    private final AnimationDashboard animationDashboard;
    private final HashMap<Integer, PalletShape> shapes;
    private final Group group;

    public StockObserver(Configuration configuration, AnimationDashboard animationDashboard) {
        this.configuration = configuration;
        this.animationDashboard = animationDashboard;
        this.shapes = new HashMap<>();
        this.group = new Group();
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

    public Group getGroup() {
        return this.group;
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

            palletShape.setType(pallet.getType());
        }

    }

}
