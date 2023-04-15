import bloomfilters.BloomFilter;
import bloomfilters.CountingBloomFilter;
import exponentialhistograms.ExponentialHistogram;

import java.util.Arrays;

import static utils.Utils.*;

public class Main {

    public static void main(String[] args) {
        //testExponentialHistograms();
        //testBloomFilter();
        testCountingBloomFilter();
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
        final var falsePositiveRate = 0.01;
        int mean = 500;
        int stdDev = 50;
        // generate random data from a gaussian distribution (just an example)
        int[] arrivals = generateRandomGaussians(10000, 500, 50);

        // estimate the number of distinct values (definitely not accurate)
        // We just need some estimation to calculate k and m
        int estimationOfDistinctValues = estimateNumberOfDistinctValuesFromGaussian(mean, stdDev);
        //randomly insert testing values into the arrivals
        arrivals[arrivals.length/2] = 666666;
        final var bloomFilter = new BloomFilter(falsePositiveRate, estimationOfDistinctValues);
        for (int arrival : arrivals) {
            bloomFilter.add(arrival);
        }
        System.out.println("1001 is in the filter: " + bloomFilter.contains(666666));
        System.out.println("1002 is not in the filter: " + bloomFilter.contains(777777));

        // calculate the actual false positive rate with fictive numbers outside of our distribution
        int falsePositives = 0;
        for (int i = 5000; i < 15000; i++) {
            if (bloomFilter.contains(i)) {
                falsePositives++;
            }
        }
        // FPR might be inaccurate, as we don't know the actual number of distinct values, estimation might be off
        System.out.println("False positive rate: " + (double) falsePositives / 10000);
    }

    public static void testCountingBloomFilter(){
        final var falsePositiveRate = 0.01;
        int mean = 500;
        int stdDev = 50;
        // generate random data from a gaussian distribution (just an example)
        int[] arrivals = generateRandomGaussians(10000, 500, 50);

        // estimate the number of distinct values (definitely not accurate)
        // We just need some estimation to calculate k and m
        int estimationOfDistinctValues = estimateNumberOfDistinctValuesFromGaussian(mean, stdDev);
        //randomly insert testing values into the arrivals
        final var countingBloomFilter = new CountingBloomFilter(falsePositiveRate, estimationOfDistinctValues);

        // test 1: add and remove a value
        countingBloomFilter.add(666666);
        System.out.println("666666 is in the filter: " + countingBloomFilter.contains(666666));
        countingBloomFilter.remove(666666);
        System.out.println("666666 is not in the filter: " + countingBloomFilter.contains(666666));

        // test 2: add many values and calculate FPR
        for (int arrival : arrivals) {
            countingBloomFilter.add(arrival);
        }

        int falsePositives = 0;
        for (int i = 5000; i < 15000; i++) {
            if (countingBloomFilter.contains(i)) {
                falsePositives++;
            }
        }
        // FPR might be inaccurate, as we don't know the actual number of distinct values, estimation might be off
        System.out.println("False positive rate: " + (double) falsePositives / 10000);
    }
}
