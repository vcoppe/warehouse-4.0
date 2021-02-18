package warehouse;

public class Pallet {

    public static Pallet FREE = new Pallet(-1);
    public static Pallet RESERVED = new Pallet(-1);

    private final int type;

    public Pallet(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }
}
