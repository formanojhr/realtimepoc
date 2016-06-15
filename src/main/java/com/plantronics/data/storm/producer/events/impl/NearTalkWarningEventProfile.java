package com.plantronics.data.storm.producer.events.impl;



import com.plantronics.data.storm.producer.events.SoundEvent;
import com.plantronics.data.storm.producer.events.SoundEventProfile;

import java.util.Random;

/**
 * Created by bthorington on 12/2/15.
 */
public class NearTalkWarningEventProfile implements SoundEventProfile {

    private Random random = new Random();

    @Override
    public SoundEvent generateSoundEvent(long timeBetweenEvents) throws Exception {

        int farEndPercent = random.nextInt(15); // range [0-10]%
        int nearEndPercent = random.nextInt((80 - 75) + 1) + 75; // range [75-80]%
        int overTalkPercent = random.nextInt(10); // range [0-10]%
        double farEndMaxDb = random.nextInt((30 - 10) + 1) + 20; // range [20-30]db
        double nearEndMaxDb = random.nextInt((30 - 10) + 1) + 20; // range [20-30]db

        double farEndDuration = timeBetweenEvents * (farEndPercent / 100.0);
        double nearEndDuration = timeBetweenEvents * (nearEndPercent / 100.0);
        double overTalkDuration = timeBetweenEvents * (overTalkPercent / 100.0);
        double noTalkDuration = timeBetweenEvents - farEndDuration - nearEndDuration - overTalkDuration;

        return new SoundEvent((long)farEndDuration,
                (long)nearEndDuration,
                (long)overTalkDuration,
                (long)noTalkDuration,
                (long)farEndMaxDb,
                (long)nearEndMaxDb);

    }
}
