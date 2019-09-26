package main;

import com.gigaspaces.common.model.CrewMember;
import com.gigaspaces.common.model.Flight;
import com.j_spaces.core.client.SQLQuery;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

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

import static com.gigaspaces.common.Constants.NUM_OF_CREW_MEMBERS;
import static com.gigaspaces.common.Constants.NUM_OF_FLIGHTS_TO_WRITE;

public class Benchmark implements InitializingBean, DisposableBean {

    @GigaSpaceContext
    private GigaSpace gigaSpace;
    private static final Logger logger = Logger.getLogger(Benchmark.class.getName());
    private static int runningNum = 0;
    private ScheduledExecutorService queriesService;
    private ScheduledExecutorService printSummeryService;
    private OperationsTimeTaker opTimeRecorder;
    private Summery summery;

    public void run() {
        opTimeRecorder = new OperationsTimeTaker();
        summery = new Summery();
        setupLogger();
        log("Start Benchmark");
        try {
            //waitForSpaceToFillWithFlights(NUM_OF_FLIGHTS_TO_WRITE);
            waitForSpaceToFillWithCrewMembers(NUM_OF_CREW_MEMBERS);
            initPrintSummeryExecuter();
            initDoQueriesExecutor();
        } catch (InterruptedException e) {
            log("--------------------------------------------------------------------");
            log("Benchmark failed: ", e);
            log("--------------------------------------------------------------------");
            queriesService.shutdown();
        }
    }

    private void initDoQueriesExecutor() throws InterruptedException {
        queriesService = Executors.newScheduledThreadPool(1);
        queriesService.scheduleAtFixedRate(this::doQueries, 0, 1, TimeUnit.SECONDS);
        queriesService.scheduleAtFixedRate(this::doBurstQuery, 0, 1, TimeUnit.MINUTES);
        queriesService.awaitTermination(30, TimeUnit.DAYS);
    }

    private void initPrintSummeryExecuter() throws InterruptedException {
        printSummeryService = Executors.newScheduledThreadPool(1);
        Runnable task = () -> log(summery.intermediateSummery());
        printSummeryService.scheduleAtFixedRate(task, 0, 30, TimeUnit.SECONDS);
    }

    /*private void waitForSpaceToFillWithFlights(int expectedNumOfFlights) throws InterruptedException {
        boolean finish = false;

        log(MessageFormat.format("Wait for space to fill with {0} flights", expectedNumOfFlights));

        while (!finish) {
            int numOfFlights = gigaSpace.count(new Flight());
            log("Current num of flights: " + numOfFlights);
            if (numOfFlights >= expectedNumOfFlights) {
                finish = true;
            } else {
                log("Waiting for space to fill up with flights");
                Thread.sleep(20000);
            }
        }
    }*/

    private void waitForSpaceToFillWithCrewMembers(int expectedNumOfCrewMembers) throws InterruptedException {
        boolean finish = false;

        log(MessageFormat.format("Wait for space to fill with {0} crew members", expectedNumOfCrewMembers));

        while (!finish) {
            int numOfCrewMembers = gigaSpace.count(new CrewMember());
            log("++++++++++++++Current num of crew members: ++++++++++++++++++++++++" + numOfCrewMembers);
            if (numOfCrewMembers >= expectedNumOfCrewMembers) {
                finish = true;
            } else {
                log("Waiting for space to fill up with CrewMembers");
                Thread.sleep(20000);
            }
        }
    }

    private void doQueries() {
        try {
            CrewMember[] member = gigaSpace.readMultiple(new SQLQuery<>(CrewMember.class, null), 100);
            //log("+++++++++++++the id of the crew member is : " + member[0].getId() + "++++++++++++++++");
        } catch (Exception e) {
            log("Got exception: ", e);
            summery.reportException(e);
        }

        runningNum++;
    }

    private void doBurstQuery() {
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < (1000 * 20)) {
            getAllCrewMembers();
            log("a second in dobrustQuery has passed");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log("Got exception: ", e);
                summery.reportException(e);
            }
        }
    }

    private void getAllCrewMembers() {
        try {
            CrewMember[] allMembers = queryAll(CrewMember.class);
            log("+++++++++++++number of all crew members: " + allMembers.length + "+++++++++++++");
        } catch (Exception e) {
            log("Got exception: ", e);
            summery.reportException(e);
        }
    }


 /*   private void doQueries() {
        int maxFlightId = gigaSpace.count(new Flight());

        try {
            int flightId = runningNum % maxFlightId;
            List<CrewMember> crewMembers = runGetAllCrewOnFlightQuery(flightId);
            for (CrewMember crewMember : crewMembers) {
                runGetCrewMemberQuery(crewMember.getId());
            }
        } catch (Exception e) {
            log("Got exception: ", e);
            summery.reportException(e);
        }

        runningNum++;
    }*/

    private List<CrewMember> runGetAllCrewOnFlightQuery(Integer flightId) throws TimeoutException {
        Flight flight = queryById(flightId, 1000, Flight.class);

        return flight.getCrewMembers();
    }

    private CrewMember runGetCrewMemberQuery(Integer id) throws TimeoutException {
        return queryById(id, 500, CrewMember.class);
    }

    private <T> T queryById(int id, int timeout, Class<T> type) throws TimeoutException {
        String query = String.format("id = %d", id);
        summery.incTotalQueries();
        opTimeRecorder.reportOperationStart();
        T res = gigaSpace.read(new SQLQuery<>(type, query), timeout);

        if (res == null) {
            throw new TimeoutException();
        } else {
            long queryDurationNano = opTimeRecorder.reportOperationEnd();
            summery.reportQueryDuration(queryDurationNano);
        }

        return res;
    }

    private <T> T[] queryAll(Class <T> type) throws TimeoutException {
        //String query = String.format("id = %d", id);
        summery.incTotalQueries();
        opTimeRecorder.reportOperationStart();
        T[] res = gigaSpace.readMultiple(new SQLQuery<>(type, null), 200);

        if (res == null) {
            throw new TimeoutException();
        } else {
            long queryDurationNano = opTimeRecorder.reportOperationEnd();
            summery.reportQueryDuration(queryDurationNano);
        }

        return res;
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
        log("Benchmark End");
        printSummeryService.shutdown();
        printSummeryService.awaitTermination(10, TimeUnit.SECONDS);
        queriesService.shutdown();
        queriesService.awaitTermination(10, TimeUnit.SECONDS);
        log("Printing final summery ...");
        log(summery.finalSummery());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(this::run).start();
    }

}