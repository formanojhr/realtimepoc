package com.plantronics.data.storm.spouts.pubnub;

import backtype.storm.Config;
import backtype.storm.spout.MultiScheme;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import com.plantronics.data.storm.common.Constants;
import com.plantronics.data.storm.topologies.ChannelArbitrator;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import monitoring.internal.PerfLogger;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * This is the PUBNUB Spout. This works as the input data stream for this Topology
 */

@SuppressWarnings({"rawtypes", "serial"})
public class PubnubSpout extends BaseRichSpout {

    Pubnub _pubnub;
    private SpoutOutputCollector collector;
    private LinkedBlockingQueue<String> queue;
    MultiScheme _scheme;
    private static final Logger LOG = Logger.getLogger(PubnubSpout.class);
    private ChannelArbitrator channelArbitrator;
    private String channelName;
    private static PerfLogger perfLogger;
    private static String PUBNUBSPOUT_COUNT_METRIC="topology.pubnub.spout.channel.%s.exec.count";

    public PubnubSpout(ChannelArbitrator channelArbitrator) {
        this.channelArbitrator=channelArbitrator;
    }

    public PubnubSpout(ChannelArbitrator channelArbitrator, PerfLogger perfLogger) {
        this.channelArbitrator=channelArbitrator;
        PubnubSpout.perfLogger=perfLogger;
        this.channelName=channelArbitrator.getSubChannel();
        LOG.info("Constructing spout with channel name: "+ this.channelName);
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        _pubnub = new Pubnub(Constants.PUBNUB_PUB_KEY, Constants.PUBNUB_SUB_KEY, false);

        queue = new LinkedBlockingQueue<String>(1000);
        this.collector = collector;

        try {
//            String subChannel=channelArbitrator.getSubChannel();
//            this.channelName =subChannel;


            LOG.info("PubNub subscription starting...in PUB CHANNEL :" +  this.channelName);
            LOG.info("PubNub pub key: " + Constants.PUBNUB_PUB_KEY);
            LOG.info("PubNub sub key: " + Constants.PUBNUB_SUB_KEY);
//            _pubnub.subscribe(new String[]{Constants.PUBNUB_SUB_CHANNEL}, new Callback() {
            _pubnub.subscribe(new String[]{this.channelName}, new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    LOG.info("Successfully getting messages from channel:  " +channel);
                    queue.offer(message.toString());
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    LOG.error("Error getting a response from channel:" + channel + "with error"+ error.getErrorString());
                }
            });
        } catch (PubnubException e) {
            LOG.error("Pub Nub Exception:",e);
        }
    }

    @Override
    public void nextTuple() {
        String ret = queue.poll();
        if (ret == null) {
            Utils.sleep(50);
        } else {
            try {
                perfLogger.getPerfLoggerInstance().count(String.format(PUBNUBSPOUT_COUNT_METRIC,this.channelName),1);
                JSONObject obj = new JSONObject(ret);
                String ID = obj.get("deviceId").toString();
                String msgDatetime = "***";
                String msgISOtime = convertISOTimeToMillis(obj.get(Constants.JSONFieldNames.TIME_STAMP).toString());
                String originTime = obj.get(Constants.JSONFieldNames.ORIGIN_TIME).toString();
                String dyDuration = obj.get("timePeriod").toString();//TODO remove this.
//                String cdDuration = obj.get("overTalkDuration").toString();
                String cdDuration = obj.get(Constants.JSONFieldNames.DOUBLE_TALK_DURATION).toString();
                collector.emit(new Values(ID, msgISOtime, dyDuration, cdDuration, originTime, new Date().getTime()));
            }
            catch (Exception e){
                LOG.error("Error deserializing Json:   ", e);
            }
        }
    }

    /**
     * Converts {ISODateTimeFormat} to {@link java.util.Date} and then to long
     * @param isoTime
     * @return
     */
    private String convertISOTimeToMillis(String isoTime) {
        DateTime dtTime= ISODateTimeFormat.dateTime().parseDateTime(isoTime);
        return (Long.toString(dtTime.toDate().getTime()));
    }


    /**
     * Helper to deserialize conversation dynamic json aka"{\"NearTalkDuration\":0,\"FarTalkDuration\":0,\"DoubleTalkDuration\":0,\"Timestamp\":\"2016-04-29T11:58:24.7459935-07:00\"}"
     * @param ret
     */
    private void jsonSerializeCDTuple(String ret) {
    }

    @Override
    public void close() {
        _pubnub.unsubscribe(Constants.PUBNUB_SUB_CHANNEL);
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Config ret = new Config();
        ret.setMaxTaskParallelism(1);
        return ret;
    }

    @Override
    public void ack(Object id) {
    }

    @Override
    public void fail(Object id) {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("ID","msgUnixtime","dyDuration","cdDuration",Constants.JSONFieldNames.ORIGIN_TIME, "stormOriginTime"));
        //declarer.declare(this._scheme.getOutputFields());
    }
}