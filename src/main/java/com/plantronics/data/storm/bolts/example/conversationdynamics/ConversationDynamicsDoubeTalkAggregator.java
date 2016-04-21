package com.plantronics.data.storm.bolts.example.conversationdynamics;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mramakrishnan on 4/12/16.
 */
public class ConversationDynamicsDoubeTalkAggregator extends BaseBasicBolt {
    Integer id;
    String name;
    Map<String, Integer> counters;
    /**
     * At the end of the spout (when the cluster is shutdown
     * We will show the word counters
     */
    @Override
    public void cleanup() {
        System.out.println("-- Word Counter ["+name+"-"+id+"] --");
        for(Map.Entry<String, Integer> entry : counters.entrySet()){
            System.out.println(entry.getKey()+": "+entry.getValue());
        }
    }

    /**
     * On create
     */
    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        this.counters = new HashMap<String, Integer>();
        this.name = context.getThisComponentId();
        this.id = context.getThisTaskId();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {}


    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
//        String str = input.getString(0);
//        /**
//         * If the word dosn't exist in the map we will create
//         * this, if not We will add 1
//         */
//        if(!counters.containsKey(str)){
//            counters.put(str, 1);
//        }else{
//            Integer c = counters.get(str) + 1;
//            counters.put(str, c);
//        }
    }

}