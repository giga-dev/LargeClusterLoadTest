package com.gigaspaces.app.persistent_event_processing.processor;

import com.gigaspaces.app.persistent_event_processing.common.Data;
import org.openspaces.events.adapter.SpaceDataEvent;
import java.util.logging.Logger;

/**
 * The processor simulates work done no un-processed Data object. The processData accepts a Data
 * object, simulate work by sleeping, and then sets the processed flag to true and returns the
 * processed Data.
 */
public class Processor {

    Logger log = Logger.getLogger(this.getClass().getName());

//        @SpaceDataEvent
    public Data processData(Data data) {
        log.info(" ------ PROCESSED : " + data);
        return data;
    }

}
