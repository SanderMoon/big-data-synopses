package utils;

public class HashUtils {
    private static  final long[] xTable = new long[]{771063151,304531243,987285709,206316127,675647179,401884421,625658063,447950609,170948101,280817413,941392223,652717477,723389551,382484807,941666489,167320717};
    private static  final long[] yTable = new long[]{478873543,670627889,412760749,100177417,717982259,537675499,598629319,844743307,261840259,922457623,481529747,178751813,724412201,350188859,210036313,259025029};
    private static final long[] zTable = new long[]{894701341,730040203,328326451,611332219,605816261,103514167,441766361,440074757,686235967,582230321,875251277,179995187,159577993,381230909,164630569,443256257};

    public static HashFunction[] getHashFunctions(int nr) {
        final var hashFunctions = new HashFunction[nr];
        for (int i = 0; i < nr ; i++) {
            hashFunctions[i] = new HashFunction(xTable[i], yTable[i], zTable[i]);
        }
        return hashFunctions;
    }

}
