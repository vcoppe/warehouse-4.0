package brain;

import agent.Stock;
import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;

public interface PalletPositionSelector {

    Position selectStartPosition(Pallet pallet, Position endPosition, Stock stock);

    Position selectEndPosition(Pallet pallet, Position startPosition, Stock stock);

}
