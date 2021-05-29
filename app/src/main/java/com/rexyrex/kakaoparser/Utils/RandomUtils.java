package com.rexyrex.kakaoparser.Utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
    public static int getRandomInt(int startInclusive, int endExclusive){
        long seed = System.nanoTime();
        Random rand = new Random();
        rand.setSeed(seed);
        int randomNum = rand.nextInt(((endExclusive-1) - startInclusive) + 1) + startInclusive;
        return randomNum;
    }
}
