package brain;

import java.util.HashSet;

public abstract class ProductClustering {

    public static int getNumberOfClusters(int nProducts, int[] productCluster) {
        HashSet<Integer> clusters = new HashSet<>();
        for (int i = 0; i < nProducts; i++) {
            clusters.add(productCluster[i]);
        }
        return clusters.size();
    }

    public static int[] getClusterSpace(int nProducts, int nClusters, int[] productCluster, int[] productSpace) {
        int[] clusterSpace = new int[nClusters];
        for (int i = 0; i < nProducts; i++) {
            clusterSpace[productCluster[i]] += productSpace[i];
        }
        return clusterSpace;
    }

    public static double[] getClusterThroughput(int nProducts, int nClusters, int[] productCluster, double[] productThroughput) {
        double[] clusterThroughput = new double[nClusters];
        for (int i = 0; i < nProducts; i++) {
            clusterThroughput[productCluster[i]] += productThroughput[i];
        }
        return clusterThroughput;
    }

    public static double[][] getClusterAffinity(int nProducts, int nClusters, int[] productCluster, double[][] productAffinity) {
        double[][] clusterAffinity = new double[nClusters][nClusters];

        // TODO

        return clusterAffinity;
    }

    public abstract int[] cluster(int nProducts, int[] productSpace, double[] productThroughput, double[][] productAffinity);

}
