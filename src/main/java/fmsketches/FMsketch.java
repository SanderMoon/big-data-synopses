package fmsketches;

import utils.HashFunction;
import utils.HashUtils;

public class FMsketch {

    final boolean[][] sketch;

    final HashFunction[] hashFunctions;

    final double scalingFactor = 1.3;

    public FMsketch(double epsilon, double delta){
        // usually we caluldate the number of hash functions like this:
        // final var numberOfHashFunctions = (int) (1/ Math.pow(epsilon, 2) * Math.log(1/delta));
        // I dont have enough prime numbers for this, so i'm just going to hardcode it for this example
        // usually you would use many many hash functions
        final var numberOfHashFunctions = 10;
        this.hashFunctions = HashUtils.getHashFunctions(numberOfHashFunctions);
        this.sketch = new boolean[numberOfHashFunctions][32];

    }

    /**
     * Adds a value to the sketch.
     * @param value the value to add to the sketch
     */
    public void add(int value){
        for (int i = 0; i < hashFunctions.length; i++) {
            int hash = (int) hashFunctions[i].hash(value, Integer.MAX_VALUE);
            // count trailing zeros of hash to get the index of the sketch
            int trailingZeros = Integer.numberOfTrailingZeros(hash);
            sketch[i][trailingZeros] = true;
        }
    }

    /**
     * Returns the count of a value by taking the average of the corresponding cells.
     * @return the estimated count of the value
     */
    public int countDistinct(){
        int count = 0;
        for (int i = 0; i < hashFunctions.length; i++) {
            for (int j = 0; j < sketch[i].length; j++) {
                if (sketch[i][j]) {
                    count++;
                }
            }
        }
        final var average = count / (double) hashFunctions.length;
        return (int) (Math.pow(2, average) * scalingFactor);

    }


}
