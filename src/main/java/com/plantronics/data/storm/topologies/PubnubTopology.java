package com.plantronics.data.storm.topologies;

/**
 * Created by twang on 4/28/16.
 */
import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;

import java.util.Properties;
import java.io.FileNotFoundException;
import java.io.IOException;

import backtype.storm.tuple.Fields;
import com.plantronics.data.storm.bolts.conversationdynamics.AlertBoltPubNub;
import com.plantronics.data.storm.spouts.pubnub.PubnubSpout;

import monitoring.internal.PerfLogger;
import org.apache.log4j.Logger;

public class PubnubTopology
{
    private static final String KAFKA_SPOUT_ID = "kafkaSpout";
    private static final String PUBNUB_SPOUT_ID = "PubnubSpout";
    private static final String HBASE_BOLT_ID = "hbaseBolt";
    private static final String LOG_BOLT_ID = "logBolt";
    private static final String PUBNUB_BOLT_ID = "pubnubBolt";

    protected Properties topologyConfig;
    private static final Logger LOG = Logger.getLogger(AlertTopologyKafka.class);

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

    public void constructPubNubSpout(TopologyBuilder builder, ChannelArbitrator channelArbitrator, PerfLogger perfLogger)
    {

        //MultiScheme scheme = new SchemeAsMultiScheme(new AlertScheme());
        //PubnubSpout pubNubSpout = new PubnubSpout(scheme);


        PubnubSpout pubNubSpout = new PubnubSpout(channelArbitrator, perfLogger);
        int spoutCount = Integer.valueOf(topologyConfig.getProperty("spout.thread.count"));
        LOG.info("PubNubSpout thread count"+ spoutCount);
        builder.setSpout(PUBNUB_SPOUT_ID, pubNubSpout,spoutCount);
    }

    public void constructHBasePubNubBolt(TopologyBuilder builder, ChannelArbitrator channelArbitrator, PerfLogger perfLogger)
    {
        AlertBoltPubNub hbaseBolt = new AlertBoltPubNub(topologyConfig, channelArbitrator,perfLogger);
        int HBaseBoltCount = Integer.valueOf(topologyConfig.getProperty("hbasebolt.thread.count"));
        LOG.info("AlertBoltPubNub thread count"+ HBaseBoltCount);
        builder.setBolt(HBASE_BOLT_ID, hbaseBolt, HBaseBoltCount).fieldsGrouping(PUBNUB_SPOUT_ID, new Fields("ID"));
    }

//    public void constructLogBolt(TopologyBuilder builder)
//    {
//        AlertLogBoltPubNub logBolt = new AlertLogBoltPubNub();
//        int logBoltCount = Integer.valueOf(topologyConfig.getProperty("logbolt.thread.count"));
//        //builder.setBolt(LOG_TRUCK_BOLT_ID, logBolt,logBoltCount).fieldsGrouping(KAFKA_SPOUT_ID, new Fields("ID"));
//        builder.setBolt(LOG_BOLT_ID, logBolt,logBoltCount).fieldsGrouping(PUBNUB_SPOUT_ID, new Fields("ID"));
//    }
//
//    public void constructPubNubBolt(TopologyBuilder builder)
//    {
//        AlertPubNub PubNubBolt = new AlertPubNub();
//        int pubnubBoltCount = Integer.valueOf(topologyConfig.getProperty("pubnubbolt.thread.count"));
//        builder.setBolt(PUBNUB_BOLT_ID, PubNubBolt,pubnubBoltCount).fieldsGrouping(PUBNUB_SPOUT_ID, new Fields("ID"));
//    }

    /**
     * Build and submit a topology with spout {@link PubnubSpout} and bolt {@link AlertBoltPubNub}; creates a running average
     * topology consuming and publishing result tuples from and to PubNub.
     * @throws Exception
     */
    private void buildAndSubmit(PerfLogger perfLogger) throws Exception {
        try {
            TopologyBuilder builder = new TopologyBuilder();
            ChannelArbitrator channelArbitrator= new ChannelArbitrator(8,8);
            //Add spout
            //constructKafkaSpout(builder);
            constructPubNubSpout(builder, channelArbitrator, perfLogger);
            //Add bolt
            constructHBasePubNubBolt(builder, channelArbitrator, perfLogger);
            //constructLogBolt(builder);
            //constructPubNubBolt(builder);

            //Configure parameters and submit topology
            Config conf = new Config();
            conf.setDebug(false);

            Integer topologyWorkers = Integer.valueOf(topologyConfig.getProperty("storm.topology.workers"));
            conf.put(Config.TOPOLOGY_WORKERS, topologyWorkers);

            //LocalCluster cluster = new LocalCluster();
            StormSubmitter.submitTopology("AlertTopologyPubNub", conf, builder.createTopology());

        } catch (Exception e) {
            String errMsg = "Error submiting Topology";
            LOG.error(errMsg + ": " + e.toString());
            throw new RuntimeException(errMsg, e);
        }

    }

    public static void main(String[] str) throws Exception {
        //Init statsD initialization
        PerfLogger perfLogger = new PerfLogger();
        perfLogger.init();
        //TODO change main to take parameters for topology configuration
        String systemPropertiesFile = "alert_topology_setting.properties";
        //String systemPropertiesFile = "alert_topology_setting_local.properties";
        String algorithmParametersFile = "alert_algorithms.properties";
        //Build topology
        PubnubTopology myTopology = new PubnubTopology(systemPropertiesFile, algorithmParametersFile);
        myTopology.buildAndSubmit(perfLogger);
    }
}