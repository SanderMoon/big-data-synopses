package exponentialhistograms;

import lombok.Data;

@Data
public class Bucket {

    private int count;
    private int firstArrival;

    /**
     * Creates a new bucket.
     * @param count the number of arrivals with the target value in the bucket
     * @param firstArrival the first arrival in the bucket
     */
    public Bucket(int count, int firstArrival){
        this.count = count;
        this.firstArrival = firstArrival;
    }

    /**
     * Combines two buckets into one.
     * @param bucket the bucket to combine with
     * @return the combined bucket
     */
    public Bucket combineBucket(Bucket bucket){
        this.count += bucket.count;
        this.firstArrival = Math.min(this.firstArrival, bucket.firstArrival);
        return this;
    }


}
