package exponentialhistograms;

import lombok.Getter;

import java.util.*;

/**
 * An implementation of the Exponential Histograms algorithm.
 * Assumes integers as input arrivals from a stream.
 */
public class ExponentialHistogram {

    @Getter
    HashMap<Integer, Queue<Bucket>> buckets = new HashMap<>();
    private final int target;
    private final int maxBucketsForSize;
    private int totalArrivals = 0;

    /**
     * Creates a new ExponentialHistogram.
     * @param epsilon the error rate
     * @param target the target value that we should count.
     */
    public ExponentialHistogram(double epsilon, int target) {
        double k = 1 / epsilon;
        // set minimum number of buckets to 2 for each size, else we can't merge buckets
        this.maxBucketsForSize = Math.max((int) Math.ceil(k / 2) + 1, 2);
        this.target = target;
        buckets.put(1, new LinkedList<>());
    }

    /**
     * Adds an arrival to the histogram.
     * @param arrival the arrival to add
     */
    public void addArrival(int arrival) {
        totalArrivals++;
        // only count certain values
        if (arrival != target) {
            return;
        }
        buckets.get(1).add(new Bucket(1, totalArrivals));
        reorderBuckets(1);
    }

    /**
     * Recursively Reorders the buckets if necessary.
     *
     * @param i the bucket size queue to reorder
     */
    private void reorderBuckets(int i) {
        // invariant 2
        if (buckets.get(i).size() > maxBucketsForSize) {
            Bucket firstBucket = buckets.get(i).poll();
            Bucket secondBucket = buckets.get(i).poll();
            // we just assume neither are null, as minimum number of buckets are 2 per size.
            Bucket combinedBucket = firstBucket.combineBucket(secondBucket);
            i *= 2;
            if (!buckets.containsKey(i)) {
                buckets.put(i, new LinkedList<>());
            }
            buckets.get(i).add(combinedBucket);
            reorderBuckets(i);
        }
    }

    /**
     * Returns an estimation of the number of arrivals with the target value in the last givenWindow
     * @param givenWindow the window to look at
     * @return the estimation
     */

    public int getCountEstimation(int givenWindow) {
        int count = 0;
        // get all keys as list
        List<Integer> keys = new ArrayList<>(buckets.keySet());
        // sort keys, so we can start from the bottom.
        Collections.sort(keys);
        // loop over all bucket sizes
        for (Integer key : keys) {
            Queue<Bucket> bucketsEntry = buckets.get(key);
            List<Bucket> bucketList = new ArrayList<>(bucketsEntry);
            // loop over all buckets of this size in reverse order
            for (int j = bucketList.size() - 1; j >= 0; j--) {
                Bucket bucket = bucketList.get(j);
                // check if the bucket is still in the window
                if (bucket.getFirstArrival() < totalArrivals - givenWindow) {
                    count += bucket.getCount() / 2;
                    return count;
                } else {
                    count += bucket.getCount();
                }
            }
        }
        return count;
    }


}
