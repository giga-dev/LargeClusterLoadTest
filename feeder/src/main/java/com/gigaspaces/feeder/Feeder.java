package com.gigaspaces.feeder;

import com.gigaspaces.common.model.CrewMember;
import com.gigaspaces.common.model.Flight;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private ScheduledFuture<?> sf;

    private long numberOfTypes = 10;

    @GigaSpaceContext
    private GigaSpace gigaSpace;

    // This is the place to write static data into the space
    public void afterPropertiesSet() throws Exception {
        new Thread(this::populateSpace).start();
    }

    private void populateSpace() {
        log.info("Start populating space with " + NUM_OF_FLIGHTS_TO_WRITE + " flights");
        int totalFlights = gigaSpace.count(new Flight());
        List<CrewMember> crewMembers = populateWithCrewMembersIfNeeded(); // Make sure the there is NUM_OF_CREW_MEMBERS in the space
        populateWithFlights(totalFlights, crewMembers);
        log.info("Finish populating space with flights");
    }

    private List<CrewMember> populateWithCrewMembersIfNeeded() {
        CrewMember[] readCrewMembersArray = gigaSpace.readMultiple(new CrewMember());
        List<CrewMember> crewMembers = new ArrayList<CrewMember>(Arrays.asList(readCrewMembersArray));
        int numOfCrewMembersInSpace = crewMembers.size();

        if (numOfCrewMembersInSpace < NUM_OF_CREW_MEMBERS) {

            for (int id = numOfCrewMembersInSpace; id < NUM_OF_CREW_MEMBERS; id++) {
                crewMembers.add(CrewMember.createCrewMember(id));
            }

            gigaSpace.writeMultiple(crewMembers.toArray());
        }

        return crewMembers;
    }

    private void populateWithFlights(int totalFlights, List<CrewMember> crewMembers) {
        List<List<CrewMember>> crewMembersBuckets = createCrewMembersBuckets(crewMembers);
        for (int flightNum = totalFlights; flightNum < NUM_OF_FLIGHTS_TO_WRITE + totalFlights; flightNum++) {
            Flight flight = new Flight(flightNum);
            List<CrewMember> crewMembersToPutInFlight = crewMembersBuckets.get(flightNum % NUM_OF_CREW_MEMBERS_BUCKETS);
            flight.setCrewMembers(crewMembersToPutInFlight);
            gigaSpace.write(flight);
        }
    }

    private List<List<CrewMember>> createCrewMembersBuckets(List<CrewMember> crewMembers) {
        List<List<CrewMember>> buckets = new ArrayList<>(NUM_OF_CREW_MEMBERS_BUCKETS);
        int bucketStartIdx = 0;

        for (int bucketNum = 0; bucketNum < NUM_OF_CREW_MEMBERS_BUCKETS; bucketNum++) {
            int bucketEndIdx = bucketStartIdx + NUM_OF_CREW_MEMBERS_IN_FLIGHT;
            List<CrewMember> list = new ArrayList<>(NUM_OF_CREW_MEMBERS_IN_FLIGHT);
            crewMembers.subList(bucketStartIdx, bucketEndIdx).forEach(crewMember -> list.add(crewMember));
            buckets.add(bucketNum, list);
            bucketStartIdx = bucketEndIdx;
        }

        return buckets;
    }

    public void destroy() throws Exception {
    }

    public long getNumberOfTypes() {
        return numberOfTypes;
    }

    public void setNumberOfTypes(long numberOfTypes) {
        this.numberOfTypes = numberOfTypes;
    }
}
