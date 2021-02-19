package warehouse;

public class Pallet {

    public static Pallet FREE = new Pallet(-1);
    public static Pallet RESERVED = new Pallet(-2);

    private static int PALLET_ID = 0;
    private final int id, type;

    public Pallet(int type) {
        this.id = PALLET_ID++;
        this.type = type;
    }

    public int getId() {
        return this.id;
    }

    public int getType() {
        return this.type;
    }
}
