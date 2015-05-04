package com.cardshifter.api;

/**
 * Created by Simon on 4/24/2015.
 */
public class ArrayUtil {

    public static int[] copyOf(int[] array) {
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    public static String[] copyOf(String[] array) {
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

}
