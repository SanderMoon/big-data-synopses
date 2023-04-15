import bloomfilters.BloomFilter;
import exponentialhistograms.ExponentialHistogram;

import java.util.Arrays;

import static utils.Utils.generateRandomArray;
import static utils.Utils.prettyPrintHashMap;

public class Main {

    public static void main(String[] args) {
        //testExponentialHistograms();
        testBloomFilter();
    }

    /**
     * Tests the ExponentialHistograms algorithms.
     */
    public static void testExponentialHistograms(){
        final var window = 20000;
        final var histogram = new ExponentialHistogram(0.5, 1);
        // generate 100 million (BIG DATA) arrivals of integers
        int[] arrivals = generateRandomArray(100_000_000, 0, 5);
        for (int arrival : arrivals) {
            histogram.addArrival(arrival);
        }
        int[] lastXarrivals = Arrays.copyOfRange(arrivals, arrivals.length - window, arrivals.length);
        System.out.println("Actual number of 1s: " + Arrays.stream(lastXarrivals).filter(i -> i == 1).count());
        System.out.println("Estimated number of 1s: " + histogram.getCountEstimation(window));
        System.out.println("buckets: " + prettyPrintHashMap(histogram.getBuckets()));
    }

    public static void testBloomFilter(){
        int[] arrivals = generateRandomArray(1000, 0, 1000);
        //randomly insert testing values into the arrivals
        arrivals[arrivals.length/2] = 1001;
        final var bloomFilter = new BloomFilter(100, 10);
        for (int arrival : arrivals) {
            bloomFilter.add(arrival);
        }
        System.out.println("1001 is in the filter: " + bloomFilter.contains(1001));
        System.out.println("1002 is not in the filter: " + bloomFilter.contains(1002));
    }
}
