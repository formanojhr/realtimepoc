package com.plantronics.data.storm.hbasetest;

/**
 * Created by twang on 4/22/16.
 */
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.util.Bytes;

public class ConnectHBase {

    public static void main(String[] args) throws Exception{

        Configuration config = HBaseConfiguration.create();

        Connection connection = ConnectionFactory.createConnection(config);

        Table table = connection.getTable(TableName.valueOf("iemployee"));

        System.out.println("Table is " + Bytes.toString(table.getName().getName()));


        // Instantiating Get class
        Get g = new Get(Bytes.toBytes("1"));

        // Reading the data
        Result result = table.get(g);

        // Reading values from Result class object
        byte [] value = result.getValue(Bytes.toBytes("insurance"),Bytes.toBytes("dental"));

        byte [] value1 = result.getValue(Bytes.toBytes("insurance"),Bytes.toBytes("health"));

        // Printing the values
        String name = Bytes.toString(value);
        String city = Bytes.toString(value1);

        System.out.println("name: " + name + " city: " + city);

        table.close();

        connection.close();
    }
}