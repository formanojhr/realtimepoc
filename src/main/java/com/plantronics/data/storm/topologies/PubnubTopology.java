package com.plantronics.data.storm.topologies;

/**
 * Created by twang on 4/28/16.
 */
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.MultiScheme;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;

import java.util.Properties;
import java.io.FileNotFoundException;
import java.io.IOException;

import backtype.storm.tuple.Fields;
import com.plantronics.data.storm.spouts.pubnub.PubnubSpout;
import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.ZkHosts;

import org.apache.log4j.Logger;

import com.plantronics.data.storm.bolts.example.conversationdynamics.AlertLogBolt;
import com.plantronics.data.storm.bolts.example.conversationdynamics.AlertHBaseBolt;
import com.plantronics.data.storm.bolts.example.conversationdynamics.AlertScheme;

public class PubnubTopology
{
    private static final String KAFKA_SPOUT_ID = "kafkaSpout";
    private static final String PUBNUB_SPOUT_ID = "PubnubSpout";
    private static final String HBASE_BOLT_ID = "hbaseBolt";
    private static final String LOG_TRUCK_BOLT_ID = "logBolt";

    protected Properties topologyConfig;
    private static final Logger LOG = Logger.getLogger(AlertTopology.class);

    public PubnubTopology(String systemPropertiesFile, String algorithmParametersFile) throws Exception
    {
        topologyConfig = new Properties();
        try {
            topologyConfig.load(ClassLoader.getSystemResourceAsStream(systemPropertiesFile));
            topologyConfig.load(ClassLoader.getSystemResourceAsStream(algorithmParametersFile));
        } catch (FileNotFoundException e) {
            LOG.error("Encountered FileNotFoundException while reading configuration properties: " + e.getMessage());
            throw e;
        } catch (IOException e) {
            LOG.error("Encountered IOException while reading configuration properties: " + e.getMessage());
            throw e;
        }
    }

    public void constructPubNubSpout(TopologyBuilder builder)
    {

        //MultiScheme scheme = new SchemeAsMultiScheme(new AlertScheme());
        //PubnubSpout pubNubSpout = new PubnubSpout(scheme);

        PubnubSpout pubNubSpout = new PubnubSpout();
        int spoutCount = Integer.valueOf(topologyConfig.getProperty("spout.thread.count"));
        builder.setSpout(PUBNUB_SPOUT_ID, pubNubSpout,spoutCount);
    }

    public void constructKafkaSpout(TopologyBuilder builder)
    {
        //Set up SpoutConfig information
        BrokerHosts hosts = new ZkHosts(topologyConfig.getProperty("kafka.zookeeper.host.port"));
        String topic = topologyConfig.getProperty("kafka.topic");
        String zkRoot = topologyConfig.getProperty("kafka.zkRoot");
        String consumerGroupId = "StormSpout";
        SpoutConfig spoutConfig = new SpoutConfig(hosts, topic, zkRoot, consumerGroupId);

        // Use class KafkaSpout to set up a new Kafka Spout.
        spoutConfig.scheme = new SchemeAsMultiScheme(new AlertScheme());
        KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);

        int spoutCount = Integer.valueOf(topologyConfig.getProperty("spout.thread.count"));
        builder.setSpout(KAFKA_SPOUT_ID, kafkaSpout,spoutCount);
    }

/*    public void constructHBaseBolt(TopologyBuilder builder)
    {
        AlertHBaseBolt hbaseBolt = new AlertHBaseBolt(topologyConfig);
        int HBaseBoltCount = Integer.valueOf(topologyConfig.getProperty("hbasebolt.thread.count"));
        builder.setBolt(HBASE_BOLT_ID, hbaseBolt, HBaseBoltCount).fieldsGrouping(KAFKA_SPOUT_ID, new Fields("ID"));
    }*/

    public void constructLogBolt(TopologyBuilder builder)
    {
        AlertLogBolt logBolt = new AlertLogBolt();
        int logBoltCount = Integer.valueOf(topologyConfig.getProperty("logbolt.thread.count"));
        //builder.setBolt(LOG_TRUCK_BOLT_ID, logBolt,logBoltCount).fieldsGrouping(KAFKA_SPOUT_ID, new Fields("ID"));
        builder.setBolt(LOG_TRUCK_BOLT_ID, logBolt,logBoltCount).fieldsGrouping(PUBNUB_SPOUT_ID, new Fields("ID"));
    }

    private void buildAndSubmit() throws Exception {
        try {
            TopologyBuilder builder = new TopologyBuilder();
            //Add spout
            //constructKafkaSpout(builder);
            constructPubNubSpout(builder);
            //Add bolt
            //constructHBaseBolt(builder);
            constructLogBolt(builder);

            //Configure parameters and submit topology
            Config conf = new Config();
            conf.setDebug(false);

            //Integer topologyWorkers = Integer.valueOf(topologyConfig.getProperty("storm.topology.workers"));
            //conf.put(Config.TOPOLOGY_WORKERS, topologyWorkers);

            LocalCluster cluster = new LocalCluster();

            //StormSubmitter.submitTopology("AlertTopology", conf, builder.createTopology());
            cluster.submitTopology(topologyConfig.getProperty("storm.topology.name"), conf, builder.createTopology());
            //StormSubmitter.submitTopology(topologyConfig.getProperty("storm.topology.name"), conf, builder.createTopology());

            Thread.sleep(1000000000);
            cluster.shutdown();

        } catch (Exception e) {
            String errMsg = "Error submiting Topology";
            LOG.error(errMsg + ": " + e.toString());
            throw new RuntimeException(errMsg, e);
        }

    }

    public static void main(String[] str) throws Exception
    {
        //String systemPropertiesFile = "alert_topology_setting.properties";
        String systemPropertiesFile = "alert_topology_setting_local.properties";
        String algorithmParametersFile = "alert_algorithms.properties";
        //Build topology
        PubnubTopology myTopology = new PubnubTopology(systemPropertiesFile, algorithmParametersFile);
        myTopology.buildAndSubmit();

    }

}