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

    public PubnubSpout() {

    }

    public PubnubSpout(MultiScheme scheme) {
        this._scheme = scheme;
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        _pubnub = new Pubnub(Constants.PUBNUB_PUB_KEY, Constants.PUBNUB_SUB_KEY, false);

        queue = new LinkedBlockingQueue<String>(1000);
        this.collector = collector;

        try {
            _pubnub.subscribe(new String[]{Constants.PUBNUB_SUB_CHANNEL}, new Callback() {
                @Override
                public void successCallback(String channel, Object message) {

                    queue.offer(message.toString());
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                }
            });
        } catch (PubnubException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nextTuple() {
        String ret = queue.poll();
        if (ret == null) {
            Utils.sleep(50);
        } else {
            JSONObject obj = new JSONObject(ret);
            String ID= obj.getString("deviceId");
            String msgDatetime = "***";
            String msgUnixtime = obj.getString("eventTime");
            String dyDuration = obj.getString("timePeriod");
            String cdDuration = obj.getString("overTalkDuration");
            collector.emit(new Values(ID, msgDatetime, msgUnixtime, dyDuration, cdDuration));
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