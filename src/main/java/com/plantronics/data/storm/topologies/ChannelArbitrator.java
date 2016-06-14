package com.plantronics.data.storm.topologies;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Creates a number of channels for Pub Nub based on the number of requested channels.
 * Created by mramakrishnan on 6/8/16.
 */
public class ChannelArbitrator implements Serializable {
    private final LinkedBlockingQueue<String> pubChannelBlockingQueue= new LinkedBlockingQueue<String>();
    private final LinkedBlockingQueue<String> subChannelBlockingQueue= new LinkedBlockingQueue<String>();
    private static final String PUB_CHANNEL_PREFIX= "pubdemo";
    private static final String SUB_CHANNEL_PREFIX= "subdemo";
    private static final Logger LOG = Logger.getLogger(ChannelArbitrator.class);
    /**
     * Constuctor initializes a given number of publish and subscribe channels
     * @param puBCount
     * @param subCount
     */
    public ChannelArbitrator(int puBCount, int subCount){
        for(int i=1; i <= puBCount ; i++){
            String channelName=PUB_CHANNEL_PREFIX+(i);
            pubChannelBlockingQueue.add(channelName);
            LOG.info("Added publish channel to arbitrator:"+ channelName);
        }
        for (int i=1; i <= subCount ; i++){
            String channelName=SUB_CHANNEL_PREFIX+(i);
            subChannelBlockingQueue.add(channelName);
            LOG.info("Added sub channel to arbitrator:"+ channelName);
        }
    }


    public String getPubChannel() throws NoSuchElementException {
        return pubChannelBlockingQueue.remove();
    }

    public String getSubChannel() throws NoSuchElementException{
        return subChannelBlockingQueue.remove();
    }
}
