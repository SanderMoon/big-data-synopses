package utils;

public class HashFunction {
    private final long x;
    private final long y;
    private final long z;

    public HashFunction(long x, long y, long z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public long hash(int value, int lengthMatrix){

        return (((x * value) + y) % z) % lengthMatrix;
    }
}
