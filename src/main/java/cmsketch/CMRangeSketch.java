package cmsketch;

import utils.DyadicIntervalCalculator;
import utils.HashFunction;
import utils.HashUtils;

import java.util.Arrays;

public class CMRangeSketch {

    private final CountMinSketch[] sketches;

    public CMRangeSketch(double epsilon, double delta, int[] domain) {
        int numberOfSketches = DyadicIntervalCalculator.getPowerOfTwoFromInterval(domain);
        this.sketches = new CountMinSketch[numberOfSketches];
        for (int i = 0; i < numberOfSketches; i++) {
            final var cmSketch = new CountMinSketch(epsilon, delta);
            sketches[i] = cmSketch;
        }
    }

    public void updateSketches(int value) {
        for (int level = 0; level < sketches.length; level++) {
            // Calculate the range for the current level
            int range = 1 << level;
            int start = ((value - 1) / range) * range + 1;
            int end = start + range - 1;

            // Update the corresponding CM sketch for this level with the range
            int[] currentRange = {start, end};
            addRange(currentRange, level);
        }
    }

    public int count(int[] range){
        final var intervals = DyadicIntervalCalculator.calculateDyadicIntervalsOverRange(range);
        int count = 0;
        for (int[] interval : intervals) {
            int level = DyadicIntervalCalculator.getPowerOfTwoFromInterval(interval);
            count += getRangeCount(interval, level);
        }
        return count;
    }

    public void addRange(int[] currentRange, int level) {
        final var hashedRange = Arrays.hashCode(currentRange);
        sketches[level].add(hashedRange);
    }

    public int getRangeCount(int[] range, int level){
        final var hashedRange = Arrays.hashCode(range);
        return sketches[level].count(hashedRange);
    }


}
