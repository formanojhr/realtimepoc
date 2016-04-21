ADD JAR mongo-hadoop-hive-1.4.2.jar;
ADD JAR mongo-hadoop-core-1.4.2.jar;
ADD JAR mongodb-driver-3.2.1.jar;
ADD JAR mongo-java-driver-3.2.0.jar;
CREATE EXTERNAL TABLE call_events
(
id STRING,
sessionId STRING,
direction STRING,
source STRING,
type STRING,
lineType STRING,
pluginId STRING,
answeredInSoftphone BOOLEAN,
relatedDeviceEvent STRING,
docVersion STRING,
name STRING,
eventTime DATE,
tenant STRUCT<id: STRING, cname:STRING>,
userId  STRING,
device STRUCT<id: STRING, pid: STRING>,
productName STRING,
manufacturedId BOOLEAN,
clientInstanceId STRING,
callId STRING
)
STORED BY 'com.mongodb.hadoop.hive.MongoStorageHandler'
WITH SERDEPROPERTIES('mongo.columns.mapping'='{"id":"_id","sessionId":"sessionId","direction":"direction","type":"type", "lineType":"lineType","source":"source","pluginId": "pluginId","answeredInSoftphone":"answeredInSoftphone","relatedDeviceEvent":"relatedDeviceEvent","docVersion":"docVersion","type":"type","name":"name","eventTime":"eventTime","tenant":"tenant","userId":"userId","device":"device","productName":"productName",
"manufacturedId":"manufacturedId"}') TBLPROPERTIES('mongo.uri'='mongodb://localhost:27017/local.client38callevents');







_id,sessionId,"direction":"direction","type":"type", "lineType":"lineType","source":"source","pluginId": "pluginId","answeredInSoftphone":"answeredInSoftphone","relatedDeviceEvent":"relatedDeviceEvent","docVersion":"docVersion","type":"type","name":"name","eventTime":"eventTime","tenant":"tenant","userId":"userId","device":"device","productName":"productName",
"manufacturedId":"manufacturedId"}'