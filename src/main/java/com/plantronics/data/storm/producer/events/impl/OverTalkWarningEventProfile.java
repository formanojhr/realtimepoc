package com.plantronics.data.storm.producer.events.impl;



import com.plantronics.data.storm.producer.events.SoundEvent;
import com.plantronics.data.storm.producer.events.SoundEventProfile;

import java.util.Random;

/**
 * Created by bthorington on 12/2/15.
 */
public class OverTalkWarningEventProfile implements SoundEventProfile {

    private Random random = new Random();

    @Override
    public SoundEvent generateSoundEvent(long timeBetweenEvents) throws Exception {

        int farEndPercent = random.nextInt((10 - 6) + 1) + 5; // range [5-10]%
        int nearEndPercent = random.nextInt((10 - 6) + 1) + 5; // range [5-10]%
        int overTalkPercent = random.nextInt((75 - 60) + 1) + 60; // range [60-75]%
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
