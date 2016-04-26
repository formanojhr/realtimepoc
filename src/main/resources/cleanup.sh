#!/bin/bash

#Step 1: clean up hbase

echo '** Drop HBase tables if exists...'
echo "disable 'received_messages'" | hbase shell
echo "drop 'received_messages'" | hbase shell

echo "disable 'device_state'" | hbase shell
echo "drop 'device_state'" | hbase shell

echo "disable 'alert_result'" | hbase shell
echo "drop 'alert_result'" | hbase shell


echo '** Creating HBase tables...'
echo '** Creating received_messages table...'
echo "create 'received_messages', {NAME => 'messages', VERSIONS => 3}" | hbase shell

echo '** Creating device_state table...'
echo "create 'device_state', {NAME => 'state', VERSIONS => 3}" | hbase shell

echo '** Creating alert_result table...'
echo "create 'alert_result', {NAME => 'alertSummary', VERSIONS => 3}" | hbase shell


#Step 2: clean up log
echo '** Cleaning up storm logs...'
rm -rf /var/log/storm/AlertTopology*

##############################################

#echo '** Creating driver_dangerous_events table...'
#echo "create 'driver_dangerous_events', {NAME=> 'events', VERSIONS=>3}" | hbase shell
#echo '** Creating driver_dangerous_events_count table...'
#echo "create 'driver_dangerous_events_count', {NAME=> 'counters', VERSIONS=>3}" | hbase shell
#echo '** Creating driver_events table...'
#echo "create 'driver_events', {NAME=> 'allevents', VERSIONS=>3}" | hbase shell
