package com.plantronics.data.storm.producer.events;

/**
 * Created by bthorington on 12/2/15.
 */
public interface SoundEventProfile {

    public SoundEvent generateSoundEvent(long timeBetweenEvents) throws Exception;

}
