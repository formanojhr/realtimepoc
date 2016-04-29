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
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;


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



    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        _pubnub = new Pubnub(Constants.PUBNUB_PUB_KEY, Constants.PUBNUB_SUB_KEY, false);

        queue = new LinkedBlockingQueue<String>(1000);
        this.collector = collector;

        try {
            LOG.info("PubNub subscription starting...in PUB CHANNEL :" +  Constants.PUBNUB_SUB_CHANNEL);
            LOG.info("PubNub pub key: " + Constants.PUBNUB_PUB_KEY);
            LOG.info("PubNub sub key: " + Constants.PUBNUB_SUB_KEY);
            _pubnub.subscribe(new String[]{Constants.PUBNUB_SUB_CHANNEL}, new Callback() {
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
                JSONObject obj = new JSONObject(ret);
                String ID = obj.get("deviceId").toString();
                String msgDatetime = "***";
                String msgUnixtime = obj.get("eventTime").toString();
                String dyDuration = obj.get("timePeriod").toString();
                String cdDuration = obj.get("overTalkDuration").toString();
                collector.emit(new Values(ID, msgDatetime, msgUnixtime, dyDuration, cdDuration));
            }
            catch (Exception e){
                LOG.error("Error deserializing Json:   ", e);
            }
        }
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
        declarer.declare(new Fields("ID","msgDatetime","msgUnixtime","dyDuration","cdDuration"));
        //declarer.declare(this._scheme.getOutputFields());
    }
}