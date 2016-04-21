CREATE EXTERNAL TABLE call_events
(
id STRING,
sessionId STRING,
direction STRING,
lineType STRING,
source STRING,
pluginId STRING,
relatedDeviceEvent STRING,
docVersion STRING,
type STRING,
name STRING,
eventTime DATE,
tenant_id STRING,
tenant_cname STRING,
userId  STRING,
device_id STRING,
device_pid STRING,
device_productName STRING,
manufacturedId BOOLEAN,
clientInstanceId STRING,
callId STRING,
duration INT,
answeredInSoftphone BOOLEAN
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
WITH SERDEPROPERTIES (
   "separatorChar" = ",") STORED AS TEXTFILE;


LOAD DATA INPATH '/callevents.csv' OVERWRITE INTO TABLE call_events;




_id,sessionId,"direction":"direction","type":"type", "lineType":"lineType","source":"source","pluginId": "pluginId","answeredInSoftphone":"answeredInSoftphone","relatedDeviceEvent":"relatedDeviceEvent","docVersion":"docVersion","type":"type","name":"name","eventTime":"eventTime","tenant":"tenant","userId":"userId","device":"device","productName":"productName",
"manufacturedId":"manufacturedId"}'