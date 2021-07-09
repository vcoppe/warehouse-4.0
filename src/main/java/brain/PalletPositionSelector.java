package brain;

import agent.Stock;
import util.Vector3D;
import warehouse.Pallet;

public interface PalletPositionSelector {

    Vector3D selectStartPosition(Pallet pallet, Vector3D endPosition, Stock stock);

    Vector3D selectEndPosition(Pallet pallet, Vector3D startPosition, Stock stock);

}
