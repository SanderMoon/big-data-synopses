package bloomfilters;

import utils.HashFunction;
import utils.HashUtils;

/**
 * similar to a bloom filter but uses counts in order to support deletion
 */
public class CountingBloomFilter {

    private final short[] countingBloomFilter;
    private final HashFunction[] hashFunctions;

    /**
     * Creates a new bloom filter.
     * Formulas for k and m can be found in the slides and a proof can be found here:
     * https://people.eecs.berkeley.edu/~daw/teaching/cs170-s03/Notes/lecture10.pdf
     * @param falsePositiveRate the false positive rate of the filter
     *                          (the probability that a query returns true for a value that was not added to the filter)
     * @param n an estimation of the number of distinct values to be added to the filter
     */
    public CountingBloomFilter(double falsePositiveRate, int n) {
        // size of the bloom filter
        int m = (int) Math.ceil(n * Math.log(falsePositiveRate) / Math.log(0.6185));
        // number of hash functions
        int k = (int) Math.ceil(Math.log(2) * (m / n));

        this.countingBloomFilter = new short[m];
        this.hashFunctions = HashUtils.getHashFunctions(k);
    }

    /**
     * Adds a value to the bloom filter.
     * Hashes the value for each hash function and set the corresponding bit to 1.
     * @param value the value to add to the filter
     */
    public void add(int value) {
        for (HashFunction hashFunction : hashFunctions) {
            countingBloomFilter[(int) hashFunction.hash(value, countingBloomFilter.length)] += 1;
        }
    }

    /**
     * Checks if the bloom filter contains a value.
     * @param value the value to check
     * @return true if the value is in the filter, false otherwise (FPs possible)
     */
    public boolean contains(int value) {
        for (HashFunction hashFunction : hashFunctions) {
            if (countingBloomFilter[(int) hashFunction.hash(value, countingBloomFilter.length)] == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes a value from the bloom filter.
     * Hashes the value for each hash function and set the corresponding bit to 0.
     * @param value the value to remove from the filter
     */
    public void remove(int value) {
        for (HashFunction hashFunction : hashFunctions) {
            countingBloomFilter[(int) hashFunction.hash(value, countingBloomFilter.length)] -= 1;
        }
    }
}
