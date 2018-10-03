import com.gigaspaces.app.persistent_event_processing.common.model.CrewMember;
import com.gigaspaces.app.persistent_event_processing.common.model.CrewMemberInfo;
import com.gigaspaces.app.persistent_event_processing.common.model.Flight;
import com.gigaspaces.app.persistent_event_processing.common.model.RandomUtils;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

public class Client {

    private static final GigaSpace gigaSpace = new GigaSpaceConfigurer(new SpaceProxyConfigurer("space").lookupGroups("EladPC")).gigaSpace();
    private static long defaultDelay = 1000;

    public static void main(String[] args) {
        System.out.println("Start Large Cluster Load Test");

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(() -> doQueries(), defaultDelay, defaultDelay, TimeUnit.MILLISECONDS);


        System.out.println("Finish Large Cluster Load Test");
    }

    private static void doQueries() {

        for (int i = 0; i < 4; i++) {
            List<CrewMember> crewMembers = getAllCrewOnFlight(RandomUtils.nextInt());
            for (int j = 0; j < 2; j++) {
                for (CrewMember crewMember : crewMembers) {
                    getCrewMemberInfo(crewMember.getCrewMemberInfo().getId());
                }
            }
        }
    }

    private static void getCrewMemberInfo(Integer id) {
        CrewMemberInfo template = new CrewMemberInfo();

        template.setId(id);
        gigaSpace.read(template, 500);
    }

    private static List<CrewMember> getAllCrewOnFlight(Integer flightId) {
        Flight flight = gigaSpace.read(new Flight(flightId));
        List<CrewMember> crewMembers = flight.getCrewMembers();

        return crewMembers;
    }
}