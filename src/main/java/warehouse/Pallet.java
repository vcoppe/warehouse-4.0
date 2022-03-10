package warehouse;

public class Pallet {

    public static Pallet FREE = new Pallet(-1);

    private static int PALLET_ID = 0;
    private final int id, product, supplier;
    private final double weight, expirationTime;

    public Pallet(int product, int supplier, double weight, double expirationTime) {
        this.id = PALLET_ID++;
        this.product = product;
        this.supplier = supplier;
        this.weight = weight;
        this.expirationTime = expirationTime;
    }

    public Pallet(int product) {
        this(product, 0, 1, Double.MAX_VALUE);
    }

    public int getId() {
        return this.id;
    }

    public int getProduct() {
        return this.product;
    }

    public int getSupplier() {
        return this.supplier;
    }

    public double getWeight() {
        return this.weight;
    }

    public double getExpirationTime() {
        return this.expirationTime;
    }

}
