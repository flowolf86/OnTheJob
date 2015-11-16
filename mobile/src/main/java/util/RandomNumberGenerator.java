package util;

import java.util.Random;

/**
 * Created by Florian on 23.06.2015.
 */
public class RandomNumberGenerator {

    // Good enough for us
    public static long getRandomLong(){
        long range = Long.MAX_VALUE;
        Random r = new Random();
        return (long)(r.nextDouble()*range);
    }

}
