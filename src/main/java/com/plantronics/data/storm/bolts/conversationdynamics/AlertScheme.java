package com.plantronics.data.storm.bolts.conversationdynamics;

/**
 * Created by twang on 4/26/16.
 */


import java.util.Date;
import java.util.List;
import com.plantronics.data.storm.common.Constants;
import org.apache.hadoop.hive.ql.log.PerfLogger;
import org.apache.log4j.Logger;
import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONObject;

public class AlertScheme implements Scheme {

    private static final long serialVersionUID = -2990121166902741545L;
    private static final Logger logger = Logger.getLogger(AlertScheme.class);
    private static String TIME_WINDOW_SIZE_DEFAULT = "1000";
    private static monitoring.internal.PerfLogger perfLogger;
    private static String KAFKA_SPOUT_COUNT_METRIC="topology.kafkaalertaverage.spout.count";

    public AlertScheme(monitoring.internal.PerfLogger perfLogger) {
       this.perfLogger=perfLogger;
    }

    @Override
    public List<Object> deserialize(byte[] bytes) {
        try {
            synchronized (this) {
                if (perfLogger == null) {
                    perfLogger = new monitoring.internal.PerfLogger();
                    perfLogger.init();
                }
                else if(perfLogger.getPerfLoggerInstance()==null){
                    perfLogger.init();
                }
            }
            String deviceid = "";
            String msgDatetime = "";
            String cdDuration = "";
            String cdTimePeriod = "";
            String msgISOtime = "";

            perfLogger.getPerfLoggerInstance().count(KAFKA_SPOUT_COUNT_METRIC, 1);

            Long originTime=new Date().getTime();//start time for topology
            try {
                String msgStr = new String(bytes, "UTF-8");
                logger.info("Received JSON FORMAT Msg: " + msgStr);
                JSONObject obj = new JSONObject(msgStr);
                deviceid = obj.getString("deviceId");
                msgDatetime = "***";
                msgISOtime = convertISOTimeToMillis(obj.get(Constants.JSONFieldNames.TIME_STAMP).toString());
                logger.debug("msgISOTime converted: " + msgISOtime);
                cdDuration = obj.get(Constants.JSONFieldNames.DOUBLE_TALK_DURATION).toString();
                cdTimePeriod=obj.get(Constants.JSONFieldNames.TIME_PERIOD).toString();
                if(cdTimePeriod.isEmpty()){
                    cdTimePeriod=AlertScheme.TIME_WINDOW_SIZE_DEFAULT;//set to default
                    logger.warn("Event didn't have time period. Setting period to default value :"+TIME_WINDOW_SIZE_DEFAULT +"(ms)");
                }
            } catch (Exception e) {
                logger.error("Exception while de serializing scheme ", e);
            }
            return new Values(deviceid, msgDatetime, msgISOtime, cdTimePeriod, cdDuration,originTime);

        } catch (Exception e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }

    }

    /**
     * Converts {ISODateTimeFormat} to {@link java.util.Date} and then to long
     *
     * @param isoTime
     * @return
     */
    private String convertISOTimeToMillis(String isoTime) {
        DateTime dtTime = ISODateTimeFormat.dateTime().parseDateTime(isoTime);
        return (Long.toString(dtTime.toDate().getTime()));
    }

    @Override
    public Fields getOutputFields() {
        return new Fields("ID", "msgDatetime", "msgUnixtime", "dyDuration", "cdDuration",Constants.JSONFieldNames.STORM_ORIGIN_TIME);

    }

    private String cleanup(String str) {
        if (str != null) {
            return str.trim().replace("\n", "").replace("\t", "");
        } else {
            return str;
        }
    }

}
