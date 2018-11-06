package main;

import com.gigaspaces.common.model.CrewMember;
import com.gigaspaces.common.model.Flight;
import com.j_spaces.core.client.SQLQuery;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static com.gigaspaces.common.Constants.NUM_OF_FLIGHTS_TO_WRITE;

public class Benchmark implements InitializingBean, DisposableBean {

    //    @GigaSpaceContext
//    private GigaSpace gigaSpace;
    private static GigaSpace gigaSpace = new GigaSpaceConfigurer(new SpaceProxyConfigurer("space").lookupGroups("EladPC")).gigaSpace();
    private static final long DELAY = 1000;
    private static final Logger logger = Logger.getLogger(Benchmark.class.getName());
    private Summery summery;
    private static int runningNum = 0;
    private ScheduledExecutorService executorService;

    public static void main(String[] args) {
        new Benchmark().run();
    }

    public void run() {
        summery = new Summery();
        setupLogger();
        log("Start Large Cluster Load Test");
        try {
            waitForSpaceToFillWithFlights(NUM_OF_FLIGHTS_TO_WRITE);
            executorService = Executors.newScheduledThreadPool(1);
            executorService.scheduleAtFixedRate(this::doQueries, 0, DELAY, TimeUnit.MILLISECONDS);
            // Debug Start
            Thread.sleep(5000);
            executorService.shutdown();
            executorService.awaitTermination(30, TimeUnit.SECONDS);
            System.out.println(summery);
            // Debug End
//            executorService.awaitTermination(30, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            log("--------------------------------------------------------------------");
            log("Test failed: ", e);
            log("--------------------------------------------------------------------");
            executorService.shutdown();
        }
    }

    private void waitForSpaceToFillWithFlights(int expectedNumOfFlights) throws InterruptedException {
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

    private void doQueries() {
        int maxIndex = gigaSpace.count(new Flight());
        try {
            int flightId = runningNum % maxIndex;
            List<CrewMember> crewMembers = getAllCrewOnFlight(flightId);
            int crewMemberIndex = runningNum % crewMembers.size();
            Integer crewMemberId = crewMembers.get(crewMemberIndex).getId();
            getCrewMember(crewMemberId);
        } catch (DataAccessException e) {
            log("Got DataAccessException while reading from space", e);
        } catch (TimeoutException e) {
            log("Got TimeOut while reading from space", e);
        } catch (Exception e) {
            log("Got exception: ", e);
        }
        runningNum++;
    }

    private List<CrewMember> getAllCrewOnFlight(Integer flightId) throws TimeoutException {
        log("Running Query on flight number: " + flightId);
        Flight flight = queryById(flightId, 1000, Flight.class);

        return flight.getCrewMembers();
    }

    private CrewMember getCrewMember(Integer id) throws TimeoutException {
        log("Running Query on crew member with id: " + id);

        return queryById(id, 500, CrewMember.class);
    }

    private <T> T queryById(int id, int timeout, Class<T> type) throws TimeoutException {
        long start = System.currentTimeMillis();
        String query = String.format("id = %d", id);
        T res = gigaSpace.read(new SQLQuery<>(type, query), timeout);

        summery.incNumOfQueries();
        if (res == null) {
            throw new TimeoutException();
        } else {
            long end = System.currentTimeMillis();
            long queryTime = end - start;
            summery.updateOnSuccessfulQuery(queryTime);
            return res;
        }
    }

    private static void log(String msg, Exception e) {
        logger.log(Level.SEVERE, msg);
        logger.log(Level.SEVERE, "Got Exception: ", e);
    }

    private static void log(String msg) {
        logger.log(Level.INFO, msg);
    }

    private static void setupLogger() {
        logger.setLevel(Level.ALL);
        try {
            String logDirPath = System.getProperty("user.home") + "/logs";
            createLogDirectoryInUserHomeDir(logDirPath);
            String logFilePath = MessageFormat.format("{0}/{1}", logDirPath, generateLogFileNameByTimeStamp());
            FileHandler fhandler = new FileHandler(logFilePath);
            log("Log file created at: " + logFilePath);
            SimpleFormatter sformatter = new SimpleFormatter();
            fhandler.setFormatter(sformatter);
            logger.addHandler(fhandler);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private static String generateLogFileNameByTimeStamp() throws IOException {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
        Date date = new Date();

        String filePath = dateFormat.format(date) + ".log";

        return filePath;
    }

    private static boolean createLogDirectoryInUserHomeDir(String path) {
        boolean created = new File(path).mkdirs();

        if (created) {
            log("log directory created at: " + path);
        }

        return created;
    }

    @Override
    public void destroy() throws Exception {
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        log("Finish Large Cluster Load Test");
        log(summery.toString());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(this::run);
    }

}