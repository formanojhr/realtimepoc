package com.plantronics.data.storm.bolts.conversationdynamics;

import java.util.*;


import backtype.storm.topology.base.BaseRichBolt;
import com.plantronics.data.storm.common.Constants;
import monitoring.internal.PerfLogger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;

import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

/**
 *  A bolt which listens to ingested tuples from {@link storm.kafka.KafkaSpout} and parses with {@link AlertScheme}
 *  and writes the results back to HBase.
 */


public class AlertLogKafkaBolt extends BaseRichBolt {

    private static final long serialVersionUID = 2946379346389650318L;

    private static final Logger LOG = Logger.getLogger(AlertBoltPubNub.class);

    // TABLES
    private static final String MESSAGES_TABLE_NAME = "received_messages_kafka";
    private static final String DEVICE_STATE_TABLE_NAME = "device_state_kafka";
    private static final String ALERT_RESULT_TABLE_NAME = "alert_result_kafka";

    // In the table received_messages, i.e. MESSAGES_TABLE_NAME
    // Its schema: ID_msgUnixTime, [ID, msgDatetime, msgUnixtime, signalValue]
    // column family name
    private static final byte[] CF_MESSAGES_TABLE = Bytes.toBytes("messages");
    // column name
    private static final byte[] COL_DEVICEID = Bytes.toBytes("ID");
    private static final byte[] COL_MSGDATETIME = Bytes.toBytes("msgDatetime");
    private static final byte[] COL_MSGUNIXTIME = Bytes.toBytes("msgUnixtime");
    private static final byte[] COL_DYDURATION = Bytes.toBytes("dyDuration");
    private static final byte[] COL_CDDURATION = Bytes.toBytes("cdDuration");

    // In the table device_state, i.e. DEVICE_STATE_TABLE_NAME
    // Its schema: ID, [ID, HistoryInfo]
    // column family name
    private static final byte[] CF_STATE_TABLE = Bytes.toBytes("state");
    // column name
    //COL_DEVICEID is the same with the one in messages table.
    private static final byte[] COL_HISTORYINFO = Bytes.toBytes("HistoryInfo");

    // In the table alert_result, i.e. ALERT_RESULT_TABLE_NAME
    // Its schema: ID_msgUnixTime, [ID, TIMEEND, CDDAVE]
    // Column family name
    private static final byte[] CF_ALERT_TABLE = Bytes.toBytes("alertSummary");
    // column name
    //COL_DEVICEID is the same with the one in messages table.
    private static final byte[] COL_TIMEEND = Bytes.toBytes("TIMEEND");
    private static final byte[] COL_CDDAVE = Bytes.toBytes("CDDAVE");

    private OutputCollector collector;
    private Connection connection;
    private Table messagesTable;
    private Table stateTable;
    private Table alertTable;

    private double AlertThreshold;
    private static long TIME_WINDOW_SIZE_DEFAULT = 1000;
    private PerfLogger perfLogger;
    private static String TOPOLOGY_LATENCY_METRIC="topology.kafkaalertaverage.totaltopology.latency";

    public AlertLogKafkaBolt(Properties topologyConfig, PerfLogger perfLogger) {
        this.AlertThreshold = Double.parseDouble(topologyConfig.getProperty("AlertThreshold"));
        this.perfLogger=perfLogger;
        LOG.info("Constructing Kafka Bolt....");
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        try {
            this.connection = ConnectionFactory.createConnection(HBaseConfiguration.create());
            this.messagesTable = connection.getTable(TableName.valueOf(MESSAGES_TABLE_NAME));
            this.stateTable = connection.getTable(TableName.valueOf(DEVICE_STATE_TABLE_NAME));
            this.alertTable = connection.getTable(TableName.valueOf(ALERT_RESULT_TABLE_NAME));
            LOG.info("Initialized kafka bolt..");
        } catch (Exception e) {
            String errMsg = "Error retrievinging connection and access to HBase Tables";
            LOG.error(errMsg + ": " + e.toString());
            throw new RuntimeException(errMsg, e);
        }

    }

    @Override
    public void execute(Tuple tuple) {

        try {
            String deviceid = tuple.getStringByField("ID");
            String msgDatetime = tuple.getStringByField("msgDatetime");
            Long msgUnixtime = Long.parseLong(tuple.getStringByField("msgUnixtime"));
            String dyString=tuple.getStringByField("dyDuration");
            Long dyDuration;
            if(!dyString.isEmpty()) {
                dyDuration = Long.parseLong(dyString);
                LOG.info("dyDuration: "+dyDuration);
            }
            else{
                dyDuration = TIME_WINDOW_SIZE_DEFAULT;
                LOG.info("dyDuration: "+dyDuration);
            }
            Double cdDuration = Double.parseDouble(tuple.getStringByField("cdDuration"));
            Long stormOriginTime =  tuple.getLongByField("stormOriginTime");


            // Step1: First, we store the received row of data (from KafkaSpout) into HBase table.
            //loadAllMessagesToHBase(deviceid, msgDatetime, msgUnixtime, dyDuration, cdDuration);

            // Step 2: At the last step, we store device's current state HBase table.
            loadStateInfoToHBase(deviceid, msgDatetime, msgUnixtime, dyDuration, cdDuration);

            collector.ack(tuple);
            //Push end to end latency for Storm Topology
            if(perfLogger==null){
                perfLogger=new PerfLogger();
                perfLogger.init();
            }
            else if(perfLogger.getPerfLoggerInstance()==null){
                perfLogger.init();
            }
            perfLogger.getPerfLoggerInstance().recordExecutionTime(TOPOLOGY_LATENCY_METRIC,new Date().getTime() - stormOriginTime);
            LOG.info("Latency(ms):"+ (new Date().getTime() - stormOriginTime));

        } catch (Exception e) {
            String errMsg = "Error parsing input data.";
            LOG.error(errMsg + ": " + e.toString());
            throw new RuntimeException(errMsg, e);
        }

    }

    private void loadAllMessagesToHBase(String deviceid, String msgDatetime,
                                        Long msgUnixtime, Double dyDuration, Double cdDuration) {
        try {
            String rowKey = deviceid + '_' + String.valueOf(msgUnixtime);
            Put put = constructRowLoadMsg(rowKey, deviceid, msgDatetime, String.valueOf(msgUnixtime),
                    String.valueOf(dyDuration), String.valueOf(cdDuration));
            this.messagesTable.put(put);
        } catch (Exception e) {
            String errMsg = "Error inserting event into HBase table[" + MESSAGES_TABLE_NAME + "]";
            LOG.error(errMsg + ": " + e.toString());
            throw new RuntimeException(errMsg, e);
        }
    }

    private void loadStateInfoToHBase(String deviceid, String msgDatetime,
                                      Long msgUnixtime, Long dyDuration, Double cdDuration) {
        try {
            String rowKey = deviceid;
            List<String> currentDeviceState = new ArrayList<String>();

            //StateTable
            //ID, [ID, History]
            //Step 1: getDeviceState from HBase Table.
            currentDeviceState = getDeviceState(deviceid);
            String HistoryInfo = currentDeviceState.get(1);

            //Initialize the state value
            String[] HistoryInfoArray = HistoryInfo.split(",");

            String T1 = HistoryInfoArray[0];
            String CDDT1 = HistoryInfoArray[1];
            String T2 = HistoryInfoArray[2];
            String CDDT2 = HistoryInfoArray[3];
            String T3 = HistoryInfoArray[4];
            String CDDT3 = HistoryInfoArray[5];

            //Initialize CDDAVE result value
            int CDDAVE = 0;
            long TIMEEND = 0L;
            boolean insertFlag = false;

            if (T1.equals("null") && T2.equals("null") && T3.equals("null")) {
                T1 = String.valueOf(msgUnixtime);
                CDDT1 = String.valueOf(cdDuration);

                //Calculate CDDAVE
                CDDAVE = (int) (cdDuration / dyDuration * 100 / 10);
                TIMEEND = msgUnixtime;
                insertFlag = true;

            } else if (!T1.equals("null") && T2.equals("null") && T3.equals("null")) {
                LOG.info("!T1.equals(\"null\") && T2.equals(\"null\") && T3.equals(\"null\")");
                T2 = String.valueOf(msgUnixtime);
                CDDT2 = String.valueOf(cdDuration);

                //Calculate CDDAVE
                CDDAVE = ((((int) ((Double.parseDouble(CDDT1) + cdDuration)) / 2) * 100 / dyDuration.intValue()) / 10);
                TIMEEND = msgUnixtime;
                insertFlag = true;

            } else if (!T1.equals("null") && !T2.equals("null") && T3.equals("null")) {
                LOG.debug("!T1.equals(\"null\") && !T2.equals(\"null\") && T3.equals(\"null\")");
                T3 = String.valueOf(msgUnixtime);
                CDDT3 = String.valueOf(cdDuration);

                //Calculate CDDAVE
                CDDAVE = ((((int) ((Double.parseDouble(CDDT1) + Double.parseDouble(CDDT2) + cdDuration)) / 3) * 100 / dyDuration.intValue()) / 10);
                TIMEEND = msgUnixtime;
                insertFlag = true;

            } else if (!T1.equals("null") && !T2.equals("null") && !T3.equals("null")) {
                LOG.debug("!T1.equals(\"null\") && !T2.equals(\"null\") && !T3.equals(\"null\")");
                //Move T2, T3 to T1, T2. Then put the new value to T3
                T1 = T2;
                CDDT1 = CDDT2;
                T2 = T3;
                CDDT2 = CDDT3;
                T3 = String.valueOf(msgUnixtime);
                CDDT3 = String.valueOf(cdDuration);

                //Calculate CDDAVE
                CDDAVE = ((((int) ((Double.parseDouble(CDDT1) + Double.parseDouble(CDDT2) + cdDuration)) / 3) * 100 / dyDuration.intValue()) / 10);//DTpercentage = (DTtotal / totalDuration) * 100 return (int) DTpercentage / 10;
                TIMEEND = msgUnixtime;
                insertFlag = true;

            }
            if (insertFlag) {
                LOG.debug("Entered insert alert table section");
                //Step 2: update the DeviceState Table.
                Put put = constructRowInsertStatus(rowKey, deviceid, T1, CDDT1, T2, CDDT2, T3, CDDT3);
                this.stateTable.put(put);
                if (CDDAVE > this.AlertThreshold) {
                    //Step 4: insert the CDDAVE Result into table
                    String newRowKey = deviceid + '_' + String.valueOf(TIMEEND);
                    Put alertPut = constructRowInsertAlert(newRowKey, deviceid, CDDAVE, TIMEEND);
                    this.alertTable.put(alertPut);
                }
            }

        } catch (Exception e) {
            String errMsg = "Error inserting event into HBase table[" + DEVICE_STATE_TABLE_NAME + "]";
            LOG.error(errMsg + ": " + e.toString());
            throw new RuntimeException(errMsg, e);
        }
    }


//    public void sendDataToPubNub(String ID, String msgUnixtime, String simulateAve) {
//        //Send data back to pubnub
//        StringBuilder sb = new StringBuilder();
//
//        sb.append("{");
//
//        sb.append("\"type\":\"");
//        sb.append("HBaseAveValue");
//        sb.append("\",");
//
//        sb.append("\"deviceId\":\"");
//        sb.append(ID);
//        sb.append("\",");
//
//        sb.append("\"msgUnixtime\":\"");
//        sb.append(msgUnixtime);
//        sb.append("\",");
//
//        sb.append("\"" + Constants.JSONFieldNames.AVERAGE_HEALTH + "\":\"");
//        sb.append(simulateAve);
//        sb.append("\"");
//
//        sb.append("}");
//        LOG.info("HBase Bolt: converting average value to JSON.... ");
//        JSONObject jsonObject = new JSONObject(sb.toString());
//        LOG.info("HBase Bolt: writing to Pub NUb.... ");
//        pubnub.publish(sendChannel, jsonObject, new Callback() {
//            @Override
//            public void successCallback(String channel, Object message) {
//                LOG.info("HBase bolt: SUCCESSFUL callback");
//            }
//
//            @Override
//            public void errorCallback(String channel, PubnubError error) {
//                LOG.error("HBase bolt: Failed callback on channel: " + channel + "with error:  " + error.getErrorString());
//            }
//        });
//    }

    private Put constructRowInsertStatus(String rowKey, String deviceid, String T1, String CDDT1,
                                         String T2, String CDDT2, String T3, String CDDT3) {
        // Construct Row into HBase
        // Step 1: construct rowKey with its time stamp overwritten
        Put put = new Put(Bytes.toBytes(rowKey));
        // Step 2: add columns into the specific key.
        String HistoryInfo = T1 + "," + CDDT1 + "," + T2 + "," + CDDT2 + "," + T3 + "," + CDDT3;
//        System.out.println("In constructRowInsertStatus, HistoryInfo is " + HistoryInfo);
        LOG.info("In constructRowInsertStatus, HistoryInfo is " + HistoryInfo);
        put.addColumn(CF_STATE_TABLE, COL_DEVICEID, Bytes.toBytes(deviceid));
        put.addColumn(CF_STATE_TABLE, COL_HISTORYINFO, Bytes.toBytes(HistoryInfo));
        return put;
    }

    private Put constructRowInsertAlert(String rowKey, String deviceid, double CDDAVE, long TIMEEND) {
        // Construct Row into HBase
        // Step 1: construct rowKey with its time stamp overwritten
        Put put = new Put(Bytes.toBytes(rowKey));
        // Step 2: add columns into the specific key.
        put.addColumn(CF_ALERT_TABLE, COL_DEVICEID, Bytes.toBytes(deviceid));
        put.addColumn(CF_ALERT_TABLE, COL_CDDAVE, Bytes.toBytes(String.valueOf(CDDAVE)));
        put.addColumn(CF_ALERT_TABLE, COL_TIMEEND, Bytes.toBytes(String.valueOf(TIMEEND)));
        return put;
    }

    public static Configuration constructConfiguration() {
        Configuration config = HBaseConfiguration.create();
        return config;
    }


    private Put constructRowLoadMsg(String rowKey, String deviceid, String msgDatetime, String msgUnixtime, String dyDuration, String cdDuration) {
        // Construct Row into HBase
        // Step 1: construct rowKey with its time stamp overwritten
        Put put = new Put(Bytes.toBytes(rowKey));
        // Step 2: add columns into the specific key.
        put.addColumn(CF_MESSAGES_TABLE, COL_DEVICEID, Bytes.toBytes(deviceid));
        put.addColumn(CF_MESSAGES_TABLE, COL_MSGDATETIME, Bytes.toBytes(msgDatetime));
        put.addColumn(CF_MESSAGES_TABLE, COL_MSGUNIXTIME, Bytes.toBytes(msgUnixtime));
        put.addColumn(CF_MESSAGES_TABLE, COL_DYDURATION, Bytes.toBytes(dyDuration));
        put.addColumn(CF_MESSAGES_TABLE, COL_CDDURATION, Bytes.toBytes(cdDuration));

        return put;
    }


    @Override
    public void cleanup() {
        try {
            messagesTable.close();
            stateTable.close();
            alertTable.close();
            connection.close();
        } catch (Exception e) {
            String errMsg = "Error closing connections";
            LOG.error(errMsg + ": " + e.toString());
            throw new RuntimeException(errMsg, e);
        }
    }

    private List<String> getDeviceState(String deviceid) {
        // Key : value  deviceID: <T1....Tn>
        List<String> DeviceState = new ArrayList<String>(Arrays.asList("null", "null,null,null,null,null,null"));

        try {
            byte[] device = Bytes.toBytes(deviceid);
            Get get = new Get(device);
            Result result = stateTable.get(get);

            if (result != null) {
                byte[] ID = result.getValue(CF_STATE_TABLE, COL_DEVICEID);
                if (ID != null) {
                    DeviceState.set(0, Bytes.toString(ID));
                }
                byte[] HistoryInfo = result.getValue(CF_STATE_TABLE, COL_HISTORYINFO);
                if (HistoryInfo != null) {
                    DeviceState.set(1, Bytes.toString(HistoryInfo));
                }
            }
            return DeviceState;

        } catch (Exception e) {
            String errMsg = "Error getting device state";
            LOG.error(errMsg + ": " + e.toString());
            throw new RuntimeException(errMsg, e);
        }
    }

    @Override
    /*
	 * declare field names
	 */
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("ID", "msgDatetime", "msgUnixtime", "dyDuration", "cdDuration"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

}


