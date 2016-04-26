package com.plantronics.data.storm.bolts.example.conversationdynamics;

/**
 * Created by twang on 4/26/16.
 */

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class AlertScheme implements Scheme {

    private static final long serialVersionUID = -2990121166902741545L;
    private static final Logger logger = Logger.getLogger(AlertScheme.class);

    @Override
    public List<Object> deserialize(byte[] bytes) {
        try {
            String msgStr = new String(bytes, "UTF-8");
            String[] msgStrArray = msgStr.split(",");

            String deviceid= cleanup(msgStrArray[0]);
            String msgDatetime = cleanup(msgStrArray[1]);
            String msgUnixtime = cleanup(msgStrArray[2]);
            String dyDuration = cleanup(msgStrArray[3]);
            String cdDuration = cleanup(msgStrArray[4]);

            return new Values(deviceid, msgDatetime, msgUnixtime, dyDuration, cdDuration);

        } catch (UnsupportedEncodingException e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }

    }

    @Override
    public Fields getOutputFields() {
        return new Fields("ID","msgDatetime","msgUnixtime","dyDuration","cdDuration");

    }

    private String cleanup(String str) {
        if (str != null) {
            return str.trim().replace("\n", "").replace("\t", "");
        } else {
            return str;
        }
    }
}