package com.feasycom.feasyblue.Bean;

import java.io.Serializable;
import java.util.HashMap;

public class BaseEvent implements Serializable {
    public static final int CUSTOMIZE_COMMAND_COUNT_CHANGE=0;
    public static final int COMPLETE_COUNT_CHANGE=1;
    private int eventId;
    private Object param;
    public BaseEvent(int evenid) {
        this.eventId=evenid;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public Object getParam() {
        return param;
    }

    public void setParam(Object param) {
        this.param = param;
    }
}
