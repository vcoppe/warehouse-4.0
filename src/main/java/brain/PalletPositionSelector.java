package brain;

import util.Vector3D;
import warehouse.Pallet;

import java.util.ArrayList;

public interface PalletPositionSelector {

    Vector3D selectStartPosition(Pallet pallet, Vector3D endPosition, ArrayList<Vector3D> positions);

    Vector3D selectEndPosition(Pallet pallet, Vector3D startPosition, ArrayList<Vector3D> positions);

}
