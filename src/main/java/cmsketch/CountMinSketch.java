package cmsketch;

import utils.HashFunction;
import utils.HashUtils;

import java.util.Arrays;

public class CountMinSketch {

    private final HashFunction[] hashFunctions;

    private final int[][] sketch;

    private final int m;

    /**
     * Creates a Count-Min Sketch with the given error and confidence.
     * error: the probability that the count of a value is less than the actual count
     * confidence: the probability to have a significant error
     * @param epsilon the error
     * @param delta the confidence
     */
    public CountMinSketch(double epsilon, double delta){
        int m = (int) Math.ceil(Math.E / epsilon);
        int k = (int) Math.ceil(Math.log(1 / delta));
        this.hashFunctions = HashUtils.getHashFunctions(k);
        this.sketch = new int[k][m];
        this.m = m;
        for (int i = 0; i < k; i++) {
            Arrays.fill(sketch[i], 0);
        }
    }

    /**
     * Adds a value to the sketch.
     * For each hash function, the corresponding cell is incremented by 1.
     * @param value the value to add to the sketch
     */
    public void add(int value){
        for (int i = 0; i < hashFunctions.length; i++) {
            sketch[i][(int) hashFunctions[i].hash(value, m)] += 1;
        }
    }

    /**
     * Returns the count of a value by taking the minimum of the corresponding cells.
     * @param value the value to count
     * @return the count of the value
     */
    public int count(int value){
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < hashFunctions.length; i++) {
            int count = sketch[i][(int) hashFunctions[i].hash(value, m)];
            if (count < min) {
                min = count;
            }
        }
        return min;
    }

    /**
     * Removes a value from the sketch.
     * @param value the value to remove from the sketch
     */
    public void remove(int value){
        for (int i = 0; i < hashFunctions.length; i++) {
            final var count = sketch[i][(int) hashFunctions[i].hash(value, m)];
            if (count > 0) {
                sketch[i][(int) hashFunctions[i].hash(value, m)] -= 1;
            }
        }
    }

}
