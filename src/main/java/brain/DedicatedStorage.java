package brain;

public class DedicatedStorage extends ProductClustering {

    @Override
    public int[] cluster(int nProducts, int[] productSpace, double[] productThroughput, double[][] productAffinity) {
        int[] clusters = new int[nProducts];
        for (int i = 0; i < nProducts; i++) {
            clusters[i] = i;
        }
        return clusters;
    }

}
