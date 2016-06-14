package com.plantronics.data.storm.bolts.example.conversationdynamics;

/**
 * Created by twang on 4/29/16.
 */


import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import java.util.Map;

import com.plantronics.data.storm.common.Constants;
import org.apache.log4j.Logger;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import org.json.JSONObject;

public class AlertPubNub extends BaseRichBolt {

    private static final long serialVersionUID = 2946379346389650318L;
    private static final Logger LOG = Logger.getLogger(AlertLogBoltPubNub.class);
    private OutputCollector collector;

    private Pubnub pubnub;

    public void prepare(Map map, TopologyContext tc, OutputCollector collector) {
        //no output.
        this.collector = collector; //This collector is used later in the execute method
        pubnub= new Pubnub(Constants.PUBNUB_PUB_KEY, Constants.PUBNUB_SUB_KEY, false);
        LOG.info("Initialized Pub Nub client in Alert Pub Nub with pub key: "+
                Constants.PUBNUB_PUB_KEY + "and sub key: "+ Constants.PUBNUB_SUB_KEY);
        LOG.info("Messages for Pub Nub will be published to channel:  " + Constants.PUBNUB_PUB_CHANNEL);
    }

    public void sendDataToPubNub(String ID, String msgUnixtime, String simulateAve) {
        //Send data back to pubnub
        StringBuilder sb = new StringBuilder();

        sb.append("{");

        sb.append("\"type\":\"");
        //"{\"health\":1}"
        sb.append("AveValue");
        sb.append("\",");

        sb.append("\"deviceId\":\"");
        sb.append(ID);
        sb.append("\",");

        sb.append("\"msgUnixtime\":\"");
        sb.append(msgUnixtime);
        sb.append("\",");

        sb.append("\"health\":\"");//actual run-time average
        sb.append(simulateAve);
        sb.append("\"");

        sb.append("}");

//        pubnub = new Pubnub(Constants.PUBNUB_PUB_KEY, Constants.PUBNUB_SUB_KEY, false);
        JSONObject jsonObject = new JSONObject(sb.toString());

        pubnub.publish(Constants.PUBNUB_PUB_CHANNEL, jsonObject, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                LOG.info("**");
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                LOG.error("Pub Nub Publish error on channel  "+ channel +"  "+ error.getErrorString());
            }});

//        Thread.yield();

    }
    public void execute(Tuple tuple) {

        try {
            System.out.println("At the beginning, get tuple into LogBolt: " + tuple);

            String ID= tuple.getStringByField("ID");
            String msgDatetime = tuple.getStringByField("msgDatetime");
            String msgUnixtime = tuple.getStringByField("msgUnixtime");
            String dyDuration = tuple.getStringByField("dyDuration");
            String cdDuration = tuple.getStringByField("cdDuration");

            String simulateAve = String.valueOf((Double.parseDouble(cdDuration) + 3.0) / 2);

            LOG.info("Result of the LogBolt is as follows ....");
            LOG.info(ID  + "," +
                    msgDatetime  + "," +
                    msgUnixtime    + "," +
                    dyDuration   + "," +
                    cdDuration   + ".");

            sendDataToPubNub(ID, msgUnixtime, simulateAve);

            collector.ack(tuple);


        } catch (Exception ex) {
            LOG.info("Log processing Error: " + ex);
        }
    }

/*

    Callback callback = new Callback() {
        public void successCallback(String channel, Object response) {
            System.out.print(".");
        }
        public void errorCallback(String channel, PubnubError error) {
            System.out.println(error.toString());
        }
    };


*/



    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("ID","msgDatetime","msgUnixtime","dyDuration","cdDuration"));

    }

}
