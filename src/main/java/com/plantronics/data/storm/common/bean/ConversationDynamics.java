package com.plantronics.data.storm.common.bean;

import com.google.common.primitives.UnsignedInteger;
import org.apache.storm.joda.time.*;
/**
 * Created by mramakrishnan on 4/12/16.
 */
public class ConversationDynamics {
    public ConversationDynamics(UnsignedInteger m_ntDuration, UnsignedInteger m_dtDuration, UnsignedInteger m_ftDuration) {
        this.m_ntDuration = m_ntDuration;
        this.m_dtDuration = m_dtDuration;
        this.m_ftDuration = m_ftDuration;
    }

    public UnsignedInteger getM_dtDuration() {
        return m_dtDuration;
    }

    public void setM_dtDuration(UnsignedInteger m_dtDuration) {
        this.m_dtDuration = m_dtDuration;
    }

    public UnsignedInteger getM_ftDuration() {
        return m_ftDuration;
    }

    public void setM_ftDuration(UnsignedInteger m_ftDuration) {
        this.m_ftDuration = m_ftDuration;
    }

    public UnsignedInteger getM_ntDuration() {
        return m_ntDuration;
    }

    public void setM_ntDuration(UnsignedInteger m_ntDuration) {
        this.m_ntDuration = m_ntDuration;
    }

    public DateTime getM_timeStamp() {
        return m_timeStamp;
    }

    public void setM_timeStamp(DateTime m_timeStamp) {
        this.m_timeStamp = m_timeStamp;
    }

    private UnsignedInteger m_dtDuration;
    private UnsignedInteger m_ftDuration;
    private UnsignedInteger m_ntDuration;
    private DateTime m_timeStamp;
}
