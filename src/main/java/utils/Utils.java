package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Utils {

    public static int[] generateRandomArray(int length, int minValue, int maxValue) {
        int[] array = new int[length];
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            array[i] = minValue + random.nextInt(maxValue - minValue + 1);
        }

        return array;
    }

    public static <K, V> String prettyPrintHashMap(HashMap<K, V> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (Map.Entry<K, V> entry : map.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append(",\n");
        }
        sb.append("}");
        return sb.toString();
    }


}
