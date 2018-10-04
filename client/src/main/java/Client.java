import com.gigaspaces.app.persistent_event_processing.common.model.CrewMember;
import com.gigaspaces.app.persistent_event_processing.common.model.CrewMemberInfo;
import com.gigaspaces.app.persistent_event_processing.common.model.Flight;
import com.gigaspaces.app.persistent_event_processing.common.model.RandomUtils;
import com.j_spaces.core.client.SQLQuery;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;
import org.springframework.dao.DataAccessException;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static com.gigaspaces.app.persistent_event_processing.common.Constants.NUM_OF_FLIGHTS;
import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

public class Client {

    private static final GigaSpace gigaSpace = new GigaSpaceConfigurer(new SpaceProxyConfigurer("space").lookupGroups("EladPC")).gigaSpace();
    private static final long DELAY = 1000;
    private static final Logger logger = Logger.getLogger(Client.class.getName());

    public static void main(String[] args) throws InterruptedException {
//        setupLogger();
        log("Start Large Cluster Load Test");

        waitForSpaceToFillWithFlights(NUM_OF_FLIGHTS);
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> doQueries(), 0, DELAY, TimeUnit.MILLISECONDS);

        log("Finish Large Cluster Load Test");
    }

    private static void waitForSpaceToFillWithFlights(int expectedNumOfFlights) throws InterruptedException {
        boolean finish = false;

        log(MessageFormat.format("Wait for space to fill with {0} flights", expectedNumOfFlights));

        while (!finish) {
            int numOfFlights = gigaSpace.count(new Flight());
            log("Current num of flights: " + numOfFlights);
            if (numOfFlights == expectedNumOfFlights) {
                finish = true;
            } else {
                Thread.sleep(20000);
            }
        }
    }

    private static void doQueries() {

        try {
            for (int i = 0; i < 4; i++) {
                List<CrewMember> crewMembers = getAllCrewOnFlight(RandomUtils.nextInt() % NUM_OF_FLIGHTS);
                for (int j = 0; j < 2; j++) {
                    int crewMemberIndex = RandomUtils.nextInt() % crewMembers.size();
                    Integer crewMemberId = crewMembers.get(crewMemberIndex).getCrewMemberInfo().getId();
                    getCrewMemberInfo(crewMemberId);
                }
            }
        } catch (DataAccessException e) {
            log("Got DataAccessException while reading from space", e);
        } catch (TimeoutException e) {
            log("Got TimeOut while reading from space", e);
        }
    }

    private static List<CrewMember> getAllCrewOnFlight(Integer flightId) throws TimeoutException {
        log("Running Query on flight number: " + flightId);
        Flight flight = queryById(flightId, 1000, Flight.class);

        return flight.getCrewMembers();
    }

    private static CrewMemberInfo getCrewMemberInfo(Integer id) throws TimeoutException {
        log("Running Query on crew member with id: " + id);

        return queryById(id, 500, CrewMemberInfo.class);
    }

    private static <T> T queryById(int id, int timeout, Class<T> type) throws TimeoutException {
        String query = String.format("id = %d", id);
        T res = gigaSpace.read(new SQLQuery<>(type, query), timeout);

        if (res == null) {
            throw new TimeoutException();
        } else {
            return res;
        }
    }

    private static void log(String msg, Exception e) {
        logger.log(Level.SEVERE, msg);
        logger.log(Level.SEVERE, "Got Exception: ", e);
    }

    private static void log(String msg) {
        logger.log(Level.SEVERE, msg);
    }

    private static void setupLogger() {
        LOGGER.setLevel(Level.ALL);
        try {
            FileHandler fhandler = new FileHandler("/home/eladg/logs/" + createLogFile());
            SimpleFormatter sformatter = new SimpleFormatter();
            fhandler.setFormatter(sformatter);
            LOGGER.addHandler(fhandler);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SecurityException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private static String createLogFile() throws IOException {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        Date date = new Date();

        String filePath = dateFormat.format(date) + ".log";
        File file = new File(filePath);

        if (file.createNewFile())
        {
            System.out.println("File is created!");
        } else {
            System.out.println("File already exists.");
        }

        return filePath;
    }

}