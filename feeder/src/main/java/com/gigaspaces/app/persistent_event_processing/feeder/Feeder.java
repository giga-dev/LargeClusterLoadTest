package com.gigaspaces.app.persistent_event_processing.feeder;

import com.gigaspaces.app.persistent_event_processing.common.Data;

import com.gigaspaces.app.persistent_event_processing.common.model.CrewMember;
import com.gigaspaces.app.persistent_event_processing.common.model.Flight;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.SpaceInterruptedException;
import org.openspaces.core.context.GigaSpaceContext;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.gigaspaces.app.persistent_event_processing.common.Constants.NUM_OF_FLIGHTS;

/**
 * A feeder bean starts a scheduled task that writes a new Data objects to the space (in an
 * unprocessed state).
 *
 * <p>The space is injected into this bean using OpenSpaces support for @GigaSpaceContext
 * annotation.
 *
 * <p>The scheduling uses the java.util.concurrent Scheduled Executor Service. It is started and
 * stopped based on Spring lifecycle events.
 */
public class Feeder implements InitializingBean, DisposableBean {

    Logger log = Logger.getLogger(this.getClass().getName());

    private ScheduledExecutorService executorService;

    private ScheduledFuture<?> sf;

    private long numberOfTypes = 10;

    private long defaultDelay = 1000;

    private FeederTask feederTask;

    @GigaSpaceContext
    private GigaSpace gigaSpace;

    /**
     * Sets the number of types that will be used to set {@link org.openspaces.example.data.common.Data#setType(Long)}.
     *
     * <p>The type is used as the routing index for partitioned space. This will affect the
     * distribution of Data objects over a partitioned space.
     */
    public void setNumberOfTypes(long numberOfTypes) {
        this.numberOfTypes = numberOfTypes;
    }

    public void setDefaultDelay(long defaultDelay) {
        this.defaultDelay = defaultDelay;
    }

    // This is the place to write static data into the space
    public void afterPropertiesSet() throws Exception {
        populateSpaceWithFlights();
//        executorService = Executors.newScheduledThreadPool(1);
//        feederTask = new FeederTask();
//        sf = executorService.scheduleAtFixedRate(feederTask, defaultDelay, defaultDelay,TimeUnit.MILLISECONDS);
    }

    private void populateSpaceWithFlights() {
        log.info("Start populating space with flights");

        for (int flightNum = 0; flightNum < NUM_OF_FLIGHTS; flightNum++) {
            Flight flight = Flight.createFlight(flightNum);
            gigaSpace.write(flight);
            for (CrewMember crewMember : flight.getCrewMembers()) {
                gigaSpace.write(crewMember.getCrewMemberInfo());
            }
        }
    }

    public void destroy() throws Exception {
        sf.cancel(false);
        sf = null;
        executorService.shutdown();
    }

    public long getFeedCount() {
        return feederTask.getCounter();
    }


    public class FeederTask implements Runnable {

        private long counter = 1;

        public void run() {
            try {
                long time = System.currentTimeMillis();
                Data data = new Data((counter++ % numberOfTypes), "FEEDER " + Long.toString(time));
                gigaSpace.write(data);
                log.info("--- FEEDER WROTE " + data);
            } catch (SpaceInterruptedException e) {
                // ignore, we are being shutdown
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public long getCounter() {
            return counter;
        }
    }


}
