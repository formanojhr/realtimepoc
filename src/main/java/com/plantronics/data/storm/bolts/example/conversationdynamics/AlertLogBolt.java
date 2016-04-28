package com.plantronics.data.storm.bolts.example.conversationdynamics;

/**
 * Created by twang on 4/22/16.
 */

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class AlertLogBolt extends BaseRichBolt
{

    private static final long serialVersionUID = 2946379346389650318L;
    private static final Logger LOG = Logger.getLogger(AlertLogBolt.class);
    private OutputCollector collector;

    public void prepare(Map map, TopologyContext tc, OutputCollector collector)
    {
        //no output.
        this.collector = collector; //This collector is used later in the execute method
    }

    public void execute(Tuple tuple)
    {

        try {
            System.out.println("At the beginning, get tuple into LogBolt: " + tuple);

            String ID= tuple.getStringByField("ID");
            String msgDatetime = tuple.getStringByField("msgDatetime");
            String msgUnixtime = tuple.getStringByField("msgUnixtime");
            String dyDuration = tuple.getStringByField("dyDuration");
            String cdDuration = tuple.getStringByField("cdDuration");
/*

            Fields fields = tuple.getFields();
            String msgStr = new String((byte[]) tuple.getValueByField(fields.get(0)), "UTF-8");
            System.out.println("Received JSON FORMAT Msg: " + msgStr);

            JSONObject obj = new JSONObject(msgStr);
            String ID= obj.getString("deviceId");
            String msgDatetime = "***";
            String msgUnixtime = obj.getString("eventTime");
            String dyDuration = obj.getString("timePeriod");
            String cdDuration = obj.getString("overTalkDuration");

*/

            LOG.info("Result of the LogBolt is as follows ....");
            LOG.info(ID  + "," +
                    msgDatetime  + "," +
                    msgUnixtime    + "," +
                    dyDuration   + "," +
                    cdDuration   + ".");

            collector.ack(tuple);
        } catch (Exception ex) {
            LOG.info("Log processing Error: " + ex);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("ID","msgDatetime","msgUnixtime","dyDuration","cdDuration"));

    }

    private String cleanup(String str)
    {
        if (str != null)
        {
            return str.trim().replace("\n", "").replace("\t", "");
        }
        else
        {
            return str;
        }

    }
}
