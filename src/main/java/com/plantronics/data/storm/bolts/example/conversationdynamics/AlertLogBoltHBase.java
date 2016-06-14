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
import org.apache.log4j.Logger;

import java.util.Map;

public class AlertLogBoltHBase extends BaseRichBolt
{

    private static final long serialVersionUID = 2946379346389650318L;
    private static final Logger LOG = Logger.getLogger(AlertLogBoltHBase.class);
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
