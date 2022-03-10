package brain;

import util.Pair;

import java.util.ArrayList;
import java.util.Comparator;

public class SharedStorage extends ProductClustering {

    private final static double[] proportion = {0.5, 0.3, 0.2};

    @Override
    public int[] cluster(int nProducts, int[] productSpace, double[] productThroughput, double[][] productAffinity) {
        int totalSpace = 0;

        ArrayList<Pair<Double, Integer>> order = new ArrayList<>();
        for (int i = 0; i < nProducts; i++) {
            order.add(new Pair<>(-productThroughput[i] / productSpace[i], i));
            totalSpace += productSpace[i];
        }
        order.sort(Comparator.comparing(p -> -p.first));

        int cluster = 0, clusterSize = 0;
        int[] clusters = new int[nProducts];
        for (Pair<Double, Integer> pair : order) {
            clusters[pair.second] = cluster;
            clusterSize += productSpace[pair.second];
            if (clusterSize > proportion[cluster] * totalSpace) {
                cluster++;
                clusterSize = 0;
            }
        }

        return clusters;
    }

}
