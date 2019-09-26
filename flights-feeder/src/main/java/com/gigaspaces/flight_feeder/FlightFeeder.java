package com.gigaspaces.flight_feeder;

import com.gigaspaces.common.model.CrewMember;
import com.gigaspaces.common.model.Flight;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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
public class FlightFeeder implements InitializingBean, DisposableBean {

    Logger log = Logger.getLogger(this.getClass().getName());

    @GigaSpaceContext
    private GigaSpace gigaSpace;

    // This is the place to write static data into the space
    public void afterPropertiesSet() throws Exception {
        //new Thread(this::populateSpaceWithFlights).start();
    }

    private void populateSpaceWithFlights() {
        log.info("Start populating space with " + NUM_OF_FLIGHTS_TO_WRITE + " flights");
        int numOfFlightsInSpace = gigaSpace.count(new Flight());
        List<CrewMember> crewMembers = getCrewMembersFromSpace();
        crewMembers.sort(Comparator.comparing(CrewMember::getId));
        List<List<CrewMember>> crewMembersBuckets = createCrewMembersBuckets(crewMembers);
        int maxFlightId = numOfFlightsInSpace + NUM_OF_FLIGHTS_TO_WRITE;
        int numOfCrewMembers = crewMembers.size();
        int numOfBuckets = numOfCrewMembers / NUM_OF_CREW_MEMBERS_IN_FLIGHT;

        for (int id = numOfFlightsInSpace; id < maxFlightId; id++) {
            Flight flight = new Flight(id);
            List<CrewMember> crewMembersToPutInFlight = crewMembersBuckets.get(id % numOfBuckets);
            flight.setCrewMembers(crewMembersToPutInFlight);
            gigaSpace.write(flight);
        }

        log.info("Finish populating space with flights");
    }

    private List<CrewMember> getCrewMembersFromSpace() {
        CrewMember[] readCrewMembersArray = gigaSpace.readMultiple(new CrewMember());
        List<CrewMember> crewMembers = new ArrayList<CrewMember>(Arrays.asList(readCrewMembersArray));

        return crewMembers;
    }

    private List<List<CrewMember>> createCrewMembersBuckets(List<CrewMember> crewMembers) {
        int numOfCrewMembers = crewMembers.size();
        int numOfBuckets = numOfCrewMembers / NUM_OF_CREW_MEMBERS_IN_FLIGHT;
        List<List<CrewMember>> buckets = new ArrayList<>(numOfBuckets);
        int bucketStartIdx = 0;

        for (int bucketNum = 0; bucketNum < numOfBuckets; bucketNum++) {
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
}
