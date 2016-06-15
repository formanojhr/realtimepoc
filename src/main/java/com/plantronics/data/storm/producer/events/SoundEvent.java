package com.plantronics.data.storm.producer.events;

/**
 * Created by bthorington on 12/2/15.
 */
public class SoundEvent {
    private long farEndDuration=0;
    private long nearEndDuration=0;
    private long overTalkDuration=0;
    private long noTalkDuration=0;
    private long farEndMaxDb=0;
    private long nearEndMaxDb=0;

    public SoundEvent() {
    }

    public SoundEvent(long farEndDuration, long nearEndDuration, long overTalkDuration, long noTalkDuration, long farEndMaxDb, long nearEndMaxDb) {
        this.farEndDuration = farEndDuration;
        this.nearEndDuration = nearEndDuration;
        this.overTalkDuration = overTalkDuration;
        this.noTalkDuration = noTalkDuration;
        this.farEndMaxDb = farEndMaxDb;
        this.nearEndMaxDb = nearEndMaxDb;
    }

    public long getFarEndDuration() {
        return farEndDuration;
    }

    public void setFarEndDuration(long farEndDuration) {
        this.farEndDuration = farEndDuration;
    }

    public long getNearEndDuration() {
        return nearEndDuration;
    }

    public void setNearEndDuration(long nearEndDuration) {
        this.nearEndDuration = nearEndDuration;
    }

    public long getOverTalkDuration() {
        return overTalkDuration;
    }

    public void setOverTalkDuration(long overTalkDuration) {
        this.overTalkDuration = overTalkDuration;
    }

    public long getNoTalkDuration() {
        return noTalkDuration;
    }

    public void setNoTalkDuration(long noTalkDuration) {
        this.noTalkDuration = noTalkDuration;
    }

    public long getFarEndMaxDb() {
        return farEndMaxDb;
    }

    public void setFarEndMaxDb(long farEndMaxDb) {
        this.farEndMaxDb = farEndMaxDb;
    }

    public long getNearEndMaxDb() {
        return nearEndMaxDb;
    }

    public void setNearEndMaxDb(long nearEndMaxDb) {
        this.nearEndMaxDb = nearEndMaxDb;
    }
}
