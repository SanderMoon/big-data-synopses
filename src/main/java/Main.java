import bloomfilters.BloomFilter;
import bloomfilters.CountingBloomFilter;
import cmsketch.CMRangeSketch;
import cmsketch.CountMinSketch;
import cuckoofilters.CuckooFilter;
import exponentialhistograms.ExponentialHistogram;
import utils.DyadicIntervalCalculator;

import java.util.Arrays;
import java.util.List;

import static utils.Utils.*;

public class Main {

    public static void main(String[] args) {
        //testExponentialHistograms();
        //testBloomFilter();
        //testCountingBloomFilter();
        //testCuckooFilter();
        //testCountMinSketch();
        //testDyadicIntervalCalculator();
        testCMsketchRangeQueries();

    }

    public static void testCMsketchRangeQueries(){
        final var epsilon = 0.001;
        final var delta = 0.01;
        final var domain = new int[]{1, 10000};
        final var cmRangeSketch = new CMRangeSketch(epsilon, delta, domain);
        // generate uniform integer data so we can easily test ranges. (just an example)
        final var arrivals = new int[10000];
        for (int i = 0; i < arrivals.length; i++) {
            arrivals[i] = i + 1;
        }
        for (int arrival : arrivals) {
            cmRangeSketch.updateSketches(arrival);
        }
        // get first 500 arrivals
        final var range = new int[]{1, 500};
        System.out.println("Range: " + Arrays.toString(range));
        System.out.println("Actual count: " + Arrays.stream(arrivals).filter(i -> i >= range[0] && i <= range[1]).count());
        System.out.println("Estimated count: " + cmRangeSketch.count(range));
    }

    public static void testDyadicIntervalCalculator(){
        final var range = new int[]{2, 452};
        final var intervals = DyadicIntervalCalculator.calculateDyadicIntervalsOverRange(range);
        // print ranges
        for (int[] interval : intervals) {
            System.out.println(Arrays.toString(interval));
        }

        final var domain = new int[]{1, 1000};
        final var domainIntervals = DyadicIntervalCalculator.calculateDyadicIntervalsOverDomain(domain);
        // print ranges
        for (List<int[]> interval : domainIntervals) {
            for (int[] ints : interval) {
                System.out.print(Arrays.toString(ints) + " ");
            }
            System.out.println();
        }


    }

    private static void testCountMinSketch(){
        int[] arrivals = generateRandomGaussians(10000, 500, 50);
        final var epsilon = 0.01;
        final var delta = 0.01;
        final var countMinSketch = new CountMinSketch(epsilon, delta);
        for (int arrival : arrivals) {
            countMinSketch.add(arrival);
        }
        int randomValue = arrivals[(int) (Math.random() * arrivals.length)];
        System.out.println("Random value: " + randomValue);
        System.out.println("Estimated Count of random value: " + countMinSketch.count(randomValue));

        // check actual value
        int actualValue = (int) Arrays.stream(arrivals).filter(i -> i == randomValue).count();
        System.out.println("Actual count of random value: " + actualValue);

    }

    private static void testCuckooFilter() {
        final var falsePositiveRate = 0.001;
        int mean = 500;
        int stdDev = 50;
        // generate random data from a gaussian distribution (just an example)
        int[] arrivals = generateRandomGaussians(10000, 500, 50);
        int estimationOfDistinctValues = estimateNumberOfDistinctValuesFromGaussian(mean, stdDev);
        // or use int actualNumberOfDistinctValues = (int) Arrays.stream(arrivals).distinct().count();

        final var cuckooFilter = new CuckooFilter(falsePositiveRate, estimationOfDistinctValues);
        // add values
        for (int arrival : arrivals) {
            boolean success = cuckooFilter.insert(arrival);
            if (!success) {
                System.out.println("Could not insert value: " + arrival);
            }
        }
        // get a random value to test with
        int randomValue = arrivals[(int) (Math.random() * arrivals.length)];
        System.out.println("Random value: " + randomValue);
        System.out.println("Random value is in the filter: " + cuckooFilter.contains(randomValue));
        // delete the random value
        cuckooFilter.delete(randomValue);
        System.out.println("Random value is not in the filter: " + cuckooFilter.contains(randomValue));

        // add the random value again
        cuckooFilter.insert(randomValue);

        //check FPR
        int[] testValues = generateRandomArray(10000, 1000, 10000);
        int falsePositives = 0;
        for (int testValue : testValues) {
            if (cuckooFilter.contains(testValue)) {
                falsePositives++;
            }
        }
        System.out.println("False positive rate: " + (double) falsePositives / testValues.length);
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
        // or use int actualNumberOfDistinctValues = (int) Arrays.stream(arrivals).distinct().count();

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
