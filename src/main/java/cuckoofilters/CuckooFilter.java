package cuckoofilters;

import utils.HashFunction;
import utils.HashUtils;

import java.util.Arrays;

/**
 * A cuckoo filter is a probabilistic data structure that can be used to test whether an element is a member of a set.
 * I have made some adjustments to the signature hashing described in the slides.
 * In the slides we used a very simple hash function, but it would be better to base the signature size on epsilon (FPR)
 * The signature size is usually (in the literature) calculated in the number of bits, as can be seen in the constructor.
 * So instead of using the signature size as the maximum integer of the signature itself, we need to encode an integer into the bits we calculated.
 * We can use the standard Java hashCode function to do hash the value and then take the X (signature size) least-significant bits of this hash to get the signature.
 * This signature will then be larger than the filter size, meaning the second has (after the XOR) will be larger than the filter size.
 * This is why we need to use the modulo operator to get a value that fits in the filter.
 * This works well and the FPR is lower than the specified value, which is positive.
 */
public class CuckooFilter {

    private final int MAX_RETRIES = 500;

    // in a real-life scenario you would probably want to use something that wraps closer around your signature, e.g. short or byte.
    private final int[][] cuckooFilter;

    private final int signatureSize;

    private final HashFunction primaryHashFunction;


    /**
     * Creates a new Cuckoo filter
     *
     * @param falsePositiveRate the false positive rate of the filter
     * @param n                 an estimation of the number of distinct values to be added to the filter
     */
    public CuckooFilter(double falsePositiveRate, int n) {
        //chosen arbitrarily
        final var bucketSize = 4;
        final var targetLoadFactor = 0.95;
        this.signatureSize = (int) Math.ceil((Math.log(1 / falsePositiveRate) / Math.log(2)) + (Math.log(2 * bucketSize) / Math.log(2)));

        final var nrOfBuckets = Math.ceil(n / (targetLoadFactor * bucketSize));
        System.out.println("nr of buckets: " + nrOfBuckets);
        System.out.println("bucket size: " + bucketSize);
        System.out.println("signature size: " + signatureSize);
        final var hashFunctions = HashUtils.getHashFunctions(2);
        this.primaryHashFunction = hashFunctions[1];

        this.cuckooFilter = new int[(int) nrOfBuckets][(int) bucketSize];
        int unusedValue = -1;
        for (int i = 0; i < nrOfBuckets; i++) {
            Arrays.fill(this.cuckooFilter[i], unusedValue);
        }
    }

    /**
     * Inserts a new value into the cuckoo filter
     *
     * @param item    the value to insert
     * @param retries the number of retries, fail after MAX_RETRIES
     * @return true if the value was inserted, false otherwise
     */
    public boolean insert(int item, int retries) {
        if (retries >= MAX_RETRIES) {
            return false;
        }
        final var signature = getSignature(item);
        final var primaryHashedValue = (int) primaryHashFunction.hash(item, cuckooFilter.length);
        //first try inserting normally
        if (insertIntoBucket(signature, primaryHashedValue)) {
            return true;
        }
        final var secondaryHashedValue = secondHashFunction(signature, primaryHashedValue);
        if (insertIntoBucket(signature, secondaryHashedValue)) {
            return true;
        }
        //if both are full, evict a random element from one of the buckets and reinsert
        final var randomBucket = Math.random() < 0.5 ? primaryHashedValue : secondaryHashedValue;
        final var randomIndex = (int) (Math.random() * cuckooFilter[randomBucket].length);
        final var evictedSignature = cuckooFilter[randomBucket][randomIndex];
        cuckooFilter[randomBucket][randomIndex] = signature;
        return insert(evictedSignature, retries + 1);
    }

    /**
     * Inserts a new value into the cuckoo filter (overloaded, so we don't have to give retries the firs time)
     *
     * @param item the value to insert
     * @return true if the value was inserted, false otherwise
     */
    public boolean insert(int item) {
        final var signature = getSignature(item);
        final var primaryHashedValue = (int) primaryHashFunction.hash(item, cuckooFilter.length);
        //first try inserting normally
        if (insertIntoBucket(signature, primaryHashedValue)) {
            return true;
        }
        final var secondaryHashedValue = secondHashFunction(signature, primaryHashedValue);
        if (insertIntoBucket(signature, secondaryHashedValue)) {
            return true;
        }
        //if both are full, evict a random element from one of the buckets and reinsert
        final var randomBucket = Math.random() < 0.5 ? primaryHashedValue : secondaryHashedValue;
        final var randomIndex = (int) (Math.random() * cuckooFilter[randomBucket].length);
        final var evictedSignature = cuckooFilter[randomBucket][randomIndex];
        cuckooFilter[randomBucket][randomIndex] = signature;
        return insert(evictedSignature, 1);
    }

    /**
     * Actually inserts the signature into the bucket
     *
     * @param signature   the signature to insert
     * @param bucketIndex the index of the bucket to insert into
     * @return true if the signature was inserted, false otherwise
     */
    private boolean insertIntoBucket(int signature, int bucketIndex) {
        for (int i = 0; i < cuckooFilter[bucketIndex].length; i++) {
            if (cuckooFilter[bucketIndex][i] == -1) {
                cuckooFilter[bucketIndex][i] = signature;
                return true;
            }
            // if the signature is already in the bucket, we don't need to insert it again
            if (cuckooFilter[bucketIndex][i] == signature) {
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes an item from the cuckoo filter
     *
     * @param item the item to delete
     * @return true if the item was deleted, false otherwise
     */
    public boolean delete(int item) {
        final var signature = getSignature(item);
        final var primaryHashedValue = (int) primaryHashFunction.hash(item, cuckooFilter.length);
        if (deleteFromBucket(signature, primaryHashedValue)) {
            return true;
        }
        final var secondaryHashedValue = secondHashFunction(signature, primaryHashedValue);
        return deleteFromBucket(signature, secondaryHashedValue);
    }

    /**
     * Deletes a signature from a bucket
     *
     * @param signature   the signature to delete
     * @param bucketIndex the index of the bucket to delete from
     * @return true if the signature was deleted, false otherwise
     */
    private boolean deleteFromBucket(int signature, int bucketIndex) {
        for (int i = 0; i < cuckooFilter[bucketIndex].length; i++) {
            if (cuckooFilter[bucketIndex][i] == signature) {
                cuckooFilter[bucketIndex][i] = -1;
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the cuckoo filter contains an item
     *
     * @param item the item to check
     * @return true if the item is in the filter, false otherwise
     */
    public boolean contains(int item) {
        final var signature = getSignature(item);
        final var primaryHashedValue = (int) primaryHashFunction.hash(item, cuckooFilter.length);
        if (containsInBucket(signature, primaryHashedValue) && containsInBucket(signature, secondHashFunction(signature, primaryHashedValue))) {
            return true;
        }
        final var secondaryHashedValue = secondHashFunction(signature, primaryHashedValue);
        return containsInBucket(signature, secondaryHashedValue);

    }

    /**
     * Checks if a bucket contains a signature
     *
     * @param signature   the signature to check
     * @param bucketIndex the index of the bucket to check
     * @return true if the bucket contains the signature, false otherwise
     */
    private boolean containsInBucket(int signature, int bucketIndex) {
        for (int i = 0; i < cuckooFilter[bucketIndex].length; i++) {
            if (cuckooFilter[bucketIndex][i] == signature) {
                return true;
            }
        }
        return false;
    }

    /**
     * The second hash function, which XORs the signature with the primary hash
     *
     * @param signature          the signature to XOR
     * @param primaryHashedValue the primary hash
     * @return the result of the XOR
     */
    private int secondHashFunction(int signature, int primaryHashedValue) {
        return (primaryHashedValue ^ signature) % cuckooFilter.length;
    }

    /**
     * Customized hash function for the singature
     * @param value the value to hash
     * @return the signature
     */
    private int getSignature(Integer value) {
        final var hashcode = value.hashCode();
        return hashcode & ((1 << signatureSize) - 1);
    }
}
