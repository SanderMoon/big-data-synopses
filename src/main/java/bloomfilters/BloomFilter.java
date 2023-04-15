package bloomfilters;

import utils.HashFunction;
import utils.HashUtils;

/**
 * Bloom filters are used for containment queries.
 * Instead of storing the actual distinct values, we store a smaller sized array where we encode the presence of a value.
 * The encoding is done with the use of a set of hash functions.
 * The hashes of the values are used as indices to set the bits in the array to true.
 * When querying, we can check if the bits are set to true for all the hashes of the value.
 * If one is set to false, the value is definitely not in the filter.
 * It could be that the value was not actually seen, but because of collisions, all the bits are still set to true.
 * This is why bloom filters have a false positive rate and allow a trade off between space complexity and accuracy.
 * The more bits we use, the less false positives we get.
 * Similarly, the more hash functions we use the smaller the probability of false positives given a large enough array.
 */
public class BloomFilter {
    private final boolean[] bloomFilter;
    private final HashFunction[] hashFunctions;

    /**
     * Creates a new bloom filter.
     * @param size the size of the bloom filter
     * @param nrHashFunctions the number of hash functions to use
     */
    public BloomFilter(int size, int nrHashFunctions) {
        this.bloomFilter = new boolean[size];
        this.hashFunctions = new HashUtils().getHashFunctions(nrHashFunctions);
    }

    /**
     * Adds a value to the bloom filter.
     * Hashes the value for each hash function and set the corresponding bit to 1.
     * @param value the value to add to the filter
     */
    public void add(int value) {
        for (HashFunction hashFunction : hashFunctions) {
            bloomFilter[(int) hashFunction.hash(value, bloomFilter.length)] = true;
        }
    }

    /**
     * Checks if the bloom filter contains a value.
     * @param value the value to check
     * @return true if the value is in the filter, false otherwise (FPs possible)
     */
    public boolean contains(int value) {
        for (HashFunction hashFunction : hashFunctions) {
            if (!bloomFilter[(int) hashFunction.hash(value, bloomFilter.length)]) {
                return false;
            }
        }
        return true;
    }
}
