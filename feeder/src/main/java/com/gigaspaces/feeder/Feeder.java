package com.gigaspaces.feeder;

import com.gigaspaces.common.Data;

import com.gigaspaces.common.model.CrewMember;
import com.gigaspaces.common.model.Flight;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.SpaceInterruptedException;
import org.openspaces.core.context.GigaSpaceContext;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import static com.gigaspaces.common.Constants.*;

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
        log.info("Start populating space with " + NUM_OF_FLIGHTS_TO_WRITE + " flights");
        int totalFlights = gigaSpace.count(new Flight());
        List<CrewMember> crewMembers = createCrewMembers(NUM_OF_CREW_MEMBERS);
        crewMembers.forEach(crewMember -> gigaSpace.write(crewMember));
        List<List<CrewMember>> crewMembersShuffle = shuffleCrewMembers(crewMembers);
        for (int flightNum = totalFlights; flightNum < NUM_OF_FLIGHTS_TO_WRITE + totalFlights; flightNum++) {
            Flight flight = new Flight(flightNum);
            List<CrewMember> crewMembersToPutInFlight = crewMembersShuffle.get(flightNum % NUM_OF_CREW_MEMBERS_IN_SHUFFLE);
            flight.setCrewMembers(crewMembersToPutInFlight);
            gigaSpace.write(flight);
        }
        log.info("Finish populating space with flights");
    }

    private List<CrewMember> createCrewMembers(int numOfCrewMembers) {
        List<CrewMember> crewMembers = new ArrayList<>(numOfCrewMembers);

        for (int id = 0; id < numOfCrewMembers; id++) {
            crewMembers.add(CrewMember.createCrewMember(id));
        }

        return crewMembers;
    }

    private List<List<CrewMember>> shuffleCrewMembers(List<CrewMember> crewMembers) {
        List<List<CrewMember>> shuffleList = new ArrayList<>(NUM_OF_CREW_MEMBERS_IN_SHUFFLE);
        int idx = 0;
        for (int i = 0; i < NUM_OF_CREW_MEMBERS_IN_SHUFFLE; i++) {
            List<CrewMember> createdList = new ArrayList<>(NUM_OF_CREW_MEMBERS_IN_FLIGHT);
            for (int j = 0; j < NUM_OF_CREW_MEMBERS_IN_FLIGHT; j++) {
                CrewMember crewMember = crewMembers.get(idx++);
                createdList.add(crewMember);
            }
            shuffleList.add(createdList);
        }

        return shuffleList;
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

    public long getNumberOfTypes() {
        return numberOfTypes;
    }

    public void setNumberOfTypes(long numberOfTypes) {
        this.numberOfTypes = numberOfTypes;
    }
}
