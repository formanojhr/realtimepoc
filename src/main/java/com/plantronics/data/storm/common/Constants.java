package com.plantronics.data.storm.common;

/**
 * Created by mramakrishnan on 4/12/16.
 */
public final class Constants {
    public static String PUBNUB_PUB_KEY="pub-c-d1314104-910c-46c1-9e58-32d7504b9a01";
    public static String PUBNUB_SUB_KEY="sub-c-cf5b662a-0da9-11e6-996b-0619f8945a4f";
    public static String PUBNUB_SUB_CHANNEL="demo";
    public static String PUBNUB_PUB_CHANNEL="demopub";
    public static final String timePeriodInMS = "1000";
    public static final class JSONFieldNames {
        public static final String NEAR_TALK_DURATION = "NearTalkDuration";
        public static final String FAR_TALK_DURATION = "FarTalkDuration";
        public static final String DOUBLE_TALK_DURATION = "DoubleTalkDuration";
        public static final String AVERAGE_HEALTH = "health";
        public static final String TIME_STAMP="Timestamp";
        public static final String TIME_PERIOD="timePeriod";
        public static final String ORIGIN_TIME="OriginTime";
        public static final String STORM_ORIGIN_TIME="stormOriginTime";
    }

}
