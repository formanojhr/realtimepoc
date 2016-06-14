
####################################
<Introduction>
The core logic of AlertHBaseBolt is implemented in function loadStateInfoHBase(). This function
gets message tuple from PubNub spout, update the device history table in HBase and set moving-average
result back to PubNub.

<Implemetation details of loadStateInfoHBase()>
Input: Field values of message tuple from PubNub spout (String deviceid, String msgDatetime, Long msgUnixtime,
Double dyDuration, Double cdDuration)

Initialization: Define 6 local variables (T1, CDDT1, T2, CDDT2, T3, CDDT3) to save the volume information at
the previous three steps.
Then initialize their values by reading the device history table (device_state table) in hbase.
And also, we use CDDAVE to save the moving-average result and use TIMEEND to save its related timestamp.

Update device history table and calculate moving-value result:
if (T1, T2 and T3 are all null) //This means we just get the first message for this device, its history table is empty
T1 = current msg timestamp;
CDDT1 = cdDuration;
Set the value of CDDAVE and TIMEEND with information in T1;
else if (T1 is not null, but T2 and T3 are both null) //This means we only have one history message for this device.
T2 = current msg timestamp;
CDDT2 = cdDuration;
Set the value of CDDAVE and TIMEEND with information in T1 and T2;
else if (T1 and T2 are not null, but T3 is null) //This means we only have two history messages for this device.
T3 = current msg timestamp;
CDDT3 = cdDuration;
Set the value of CDDAVE and TIMEEND with information in T1, T2 and T3;
else if (None of T1, T2 and T3 are null) //We have more than three history messages for this device
Update the information in T1, T2 and T3.
Set the value of CDDAVE and TIMEEND with updated information in T1, T2 and T3;
else
Do nothing;

Save the updated device history information to HBase table:
constructRowInsertStatus(rowKey, deviceid, T1, CDDT1, T2, CDDT2, T3, CDDT3);
Send the moving-average result CDDAVE and TIMEEND back to PubNub:
sendDataToPubNub(deviceid, String.valueOf(TIMEEND), String.valueOf(CDDAVE));
####################################