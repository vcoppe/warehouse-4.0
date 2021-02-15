package brain;

import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;

public interface PalletPositionSelector {

    Position selectStartPosition(Pallet pallet, ArrayList<Position> positions);
    Position selectEndPosition(Pallet pallet, ArrayList<Position> positions);

}
