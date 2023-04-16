package utils;

import java.util.ArrayList;
import java.util.List;

public class DyadicIntervalCalculator {

    /**
     * Calculates the dyadic intervals for a given range.
     * this formula is used: [𝑥2^𝑦 +1,  (𝑥+1) 2^𝑦 ]
     *
     * @param range an array of length 2 containing the lower and upper bound of the range
     * @return a list of dyadic intervals
     */
    public static List<int[]> calculateDyadicIntervalsOverRange(int[] range) {
        List<int[]> minimalDyadicIntervals = new ArrayList<>();
        if (range.length != 2) {
            throw new IllegalArgumentException("Range array must have exactly 2 elements.");
        }
        int lower = range[0];
        int upper = range[1];
        while (lower <= upper) {
            int y = (int) (Math.log(lower) / Math.log(2));
            int x = (int) Math.floor((double) (lower - 1) / Math.pow(2, y));
            int start = Math.max(x * (int) Math.pow(2, y) + 1, lower);
            int end = (x + 1) * (int) Math.pow(2, y);
            if (end > upper) {
                int newY = (int) (Math.log(upper - lower + 1) / Math.log(2));
                int newX = (int) Math.floor((double) (upper - (int) Math.pow(2, newY)) / Math.pow(2, newY));
                start = newX * (int) Math.pow(2, newY) + 1;
                end = (newX + 1) * (int) Math.pow(2, newY);
                if (end > upper) {
                    end = upper;
                }
            }
            minimalDyadicIntervals.add(new int[]{start, end});
            lower = end + 1;
        }
        return minimalDyadicIntervals;
    }

    /**
     * Calculates the dyadic intervals for a given domain.
     *
     * @param range an array of length 2 containing the lower and upper bound of the domain
     * @return a list of dyadic intervals
     */
    public static List<List<int[]>> calculateDyadicIntervalsOverDomain(int[] range) {
        List<List<int[]>> dyadicIntervals = new ArrayList<>();
        if (range.length != 2) {
            throw new IllegalArgumentException("Range array must have exactly 2 elements.");
        }
        int lower = range[0];
        int upper = range[1];
        for (int y = 0; (1 << y) <= (upper - lower + 1); y++) {
            List<int[]> dyadicIntervalsForY = new ArrayList<>();
            for (int x = (lower - 1) >> y; (x << y) <= upper; x++) {
                int start = x * (1 << y) + 1;
                int end = (x + 1) * (1 << y);
                if (end > upper) {
                    break;
                }
                if (start < lower) {
                    continue;
                }
                dyadicIntervalsForY.add(new int[]{start, end});
                // Check if the start of the next interval is within the domain
                int nextStart = (x + 1) * (1 << y) + 1;
                if (nextStart > upper) {
                    break;
                }
            }
            dyadicIntervals.add(dyadicIntervalsForY);
        }
        return dyadicIntervals;
    }


    public static int getPowerOfTwoFromInterval(int[] interval) {
        if (interval.length != 2) {
            throw new IllegalArgumentException("Interval array must have exactly 2 elements.");
        }
        int lower = interval[0];
        int upper = interval[1];
        int difference = upper - lower + 1;
        return (int) (Math.log(difference) / Math.log(2));
    }
}
