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

    @GigaSpaceContext
    private GigaSpace gigaSpace;

    // This is the place to write static data into the space
    public void afterPropertiesSet() throws Exception {
        new Thread(this::populateSpaceWithFlights).start();
    }

    private void populateSpaceWithFlights() {
        int flightNum = gigaSpace.count(new Flight());

        log.info("Start populating space with flights");
        for (int i = 0; i < NUM_OF_FLIGHTS; i++) {
            Flight flight = Flight.createFlight(flightNum++);
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
