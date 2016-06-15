package com.plantronics.data.storm.topologies;

/**
 * A running average topology which wires a {@link storm.kafka.KafkaSpout} and a
 * {@link com.plantronics.data.storm.bolts.conversationdynamics.AlertLogKafkaBolt}. This will write average
 * results to
 */
import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;

import java.util.Properties;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

import backtype.storm.tuple.Fields;

import monitoring.Metric;
import monitoring.PeriodicMetricCollector;
import monitoring.internal.JVMStats;
import monitoring.internal.KafkaStats;
import monitoring.internal.PerfLogger;
import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.ZkHosts;

import org.apache.log4j.Logger;

import com.plantronics.data.storm.bolts.conversationdynamics.AlertLogKafkaBolt;

import com.plantronics.data.storm.bolts.conversationdynamics.AlertScheme;

public class AlertTopologyKafka
{
    private static final String KAFKA_SPOUT_ID = "kafkaSpout";
    private static final String HBASE_BOLT_ID = "hbaseKafkaBolt";
    private static final String LOG_TRUCK_BOLT_ID = "logBolt";

    protected Properties topologyConfig;
    private static final Logger LOG = Logger.getLogger(AlertTopologyKafka.class);
    private final PerfLogger perfLogger;

    public AlertTopologyKafka(String systemPropertiesFile, String algorithmParametersFile) throws Exception
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

        perfLogger=new PerfLogger();
        perfLogger.init();
        LOG.info("Starting AlertTopologyKafka....");
    }

    public void constructKafkaSpout(TopologyBuilder builder)

    {
        LOG.info("Constucting Kafka Spout....");
        //Set up SpoutConfig information
        BrokerHosts hosts = new ZkHosts(topologyConfig.getProperty("kafka.zookeeper.host.port"));
        String topic = topologyConfig.getProperty("kafka.topic");
        String zkRoot = topologyConfig.getProperty("kafka.zkRoot");
        String consumerGroupId = "KafkaSpout";
        SpoutConfig spoutConfig = new SpoutConfig(hosts, topic, zkRoot, consumerGroupId);

        // Use class KafkaSpout to set up a new Kafka Spout.
        KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);

        spoutConfig.scheme = new SchemeAsMultiScheme(new AlertScheme(perfLogger));

        int spoutCount = Integer.valueOf(topologyConfig.getProperty("spout.thread.count"));
        LOG.info("Setting Kafka Spout count: "+spoutCount);
        builder.setSpout(KAFKA_SPOUT_ID, kafkaSpout,spoutCount);
    }

    public void constructHBaseBolt(TopologyBuilder builder)
    {
        LOG.info("Hbase Bolt....");
        AlertLogKafkaBolt hbaseBolt = new AlertLogKafkaBolt(topologyConfig, perfLogger);
        int HBaseBoltCount = Integer.valueOf(topologyConfig.getProperty("hbasebolt.thread.count"));
        LOG.info("Setting Hbase Bolt count: "+HBaseBoltCount);
        builder.setBolt(HBASE_BOLT_ID, hbaseBolt, HBaseBoltCount).fieldsGrouping(KAFKA_SPOUT_ID, new Fields("ID"));

    }

    private void buildAndSubmit() throws Exception
    {
        try {
            TopologyBuilder builder = new TopologyBuilder();
            PerfLogger perfLogger= new PerfLogger();
            perfLogger.init();
            PeriodicMetricCollector periodicMetricCollector= new PeriodicMetricCollector();

            CopyOnWriteArraySet<Metric> metrics = new CopyOnWriteArraySet<Metric>();
            metrics.add(new KafkaStats());
            metrics.add(new JVMStats());
            periodicMetricCollector.setMetrics(metrics);

            periodicMetricCollector.start();

            //Add spout
            constructKafkaSpout(builder);
            //Add bolt
            constructHBaseBolt(builder);
//            constructLogBolt(builder);

            //Configure parameters and submit topology
            Config conf = new Config();
            conf.setDebug(false);

            Integer topologyWorkers = Integer.valueOf(topologyConfig.getProperty("storm.topology.workers"));
            conf.put(Config.TOPOLOGY_WORKERS, topologyWorkers);

            //LocalCluster cluster = new LocalCluster();

            StormSubmitter.submitTopology("AlertTopologyKafka", conf, builder.createTopology());


        } catch (Exception e) {
            String errMsg = "Error submiting Topology";
            LOG.error(errMsg + ": " + e.toString());
            throw new RuntimeException(errMsg, e);
        }

    }

    public static void main(String[] str) throws Exception
    {
        String systemPropertiesFile = "alert_topology_setting.properties";
        String algorithmParametersFile = "alert_algorithms.properties";
        //Build topology
        AlertTopologyKafka myTopology = new AlertTopologyKafka(systemPropertiesFile, algorithmParametersFile);
        myTopology.buildAndSubmit();
    }
}