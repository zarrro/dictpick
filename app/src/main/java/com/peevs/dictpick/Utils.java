package com.peevs.dictpick;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by zarrro on 4.9.2015 г..
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    /**
     * Generates array with size n, with unique (no duplicated numbers) in the range 0 to rangeSize.
     *
     * @param n         - how many random numbers to be generated, if n > rangeSize rangeSize
     *                  numbers will be returned
     * @param rangeSize - range of the random numbers generated
     * @param rand      - java.util.Random instance to be used
     * @return - array with unique random numbers in the range from 0 to rangeSize
     */
    public static int[] generateUniqueRandomNumbers(int n, int rangeSize, Random rand) {
        if (rand == null)
            throw new IllegalArgumentException();

        if (n > rangeSize) {
            // you can't generate more numbers than the rangeSize
            n = rangeSize;
        }


        int[] generated = new int[rangeSize];
        int[] result = new int[n];

        final int MARK_AS_GENARATED = 1;
        Arrays.fill(generated, 0);

        for (int i = 0; i < n; i++) {
            int randNumber = rand.nextInt(rangeSize);
            // if the candidate number has been already generated, shift to the
            // next free
            int j = 0, candidate = 0;
            for (; j < generated.length
                    && generated[(candidate = (randNumber + j) %
                    generated.length)] == MARK_AS_GENARATED; j++);
            generated[candidate] = MARK_AS_GENARATED;
            result[i] = candidate;
        }
        return result;
    }

}
