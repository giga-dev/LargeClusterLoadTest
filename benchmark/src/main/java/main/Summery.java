package main;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Summery {
    private long minTime = Long.MAX_VALUE;
    private long maxTime;
    private long sumTime;
    private long totalQueries;
    private long successfulQueries;
    private Map<String, Integer> thrownExceptions; // <e.simpleName, numOfThrownExceptions>

    private long setMinTime = Long.MAX_VALUE;
    private long setMaxTime;
    private long setSumTime;
    private long totalSetOfQueries;
    private long successfulSetsOfQueries;

    public Summery() {
        this.thrownExceptions = new Hashtable<String, Integer>();
    }

    private String formatThrownExceptions() {
        StringBuilder stringBuilder = new StringBuilder("Thrown exceptions by type: ").append('\n');

        thrownExceptions.forEach(
                (name, amount) -> stringBuilder.append(name + " : " + amount).append('\n')
        );

        return stringBuilder.toString();
    }

    public void incNumOfQueries() {
        totalQueries++;
    }

    public void incNumOfSetOfQueries() {
        totalSetOfQueries++;
    }



    public <E extends Exception> void reportException(E e) {
        String simpleName = e.getClass().getSimpleName();
        Integer currentAmountOfThrownExceptions = thrownExceptions.get(simpleName);
        Integer newAmountOfThrownExceptions;

        if (currentAmountOfThrownExceptions != null) {
            newAmountOfThrownExceptions = currentAmountOfThrownExceptions + 1;
        } else {
            newAmountOfThrownExceptions = 1;
        }

        thrownExceptions.put(simpleName, newAmountOfThrownExceptions);
    }

    public void reportSuccessfulQuery(long queryTimeInNanoSeconds) {
        successfulQueries++;
        sumTime += queryTimeInNanoSeconds;

        if (queryTimeInNanoSeconds < minTime) {
            minTime = queryTimeInNanoSeconds;
        }

        if (queryTimeInNanoSeconds > maxTime) {
            maxTime = queryTimeInNanoSeconds;
        }

    }

    public void reportSuccessfulSetOfQueries(long timeNanoSeconds) {
        successfulSetsOfQueries++;
        setSumTime += timeNanoSeconds;

        if (timeNanoSeconds < setMinTime) {
            setMinTime = timeNanoSeconds;
        }

        if (timeNanoSeconds > setMaxTime) {
            setMaxTime = timeNanoSeconds;
        }
    }

    private long avgQueryTime() {
        long avg;

        if (successfulQueries != 0) {
            avg = sumTime / successfulQueries;
        } else {
            avg = 0;
        }

        return avg;
    }

    private long avgSetOfQueriesTime() {
        long avg;

        if (successfulSetsOfQueries != 0) {
            avg = setSumTime / successfulSetsOfQueries;
        } else {
            avg = 0;
        }

        return avg;
    }

    private String formatNanoTime(String initMsg, long timeInNano) {
        return MessageFormat.format("{0}: {1} nano seconds ({2} ms)", initMsg, timeInNano, TimeUnit.NANOSECONDS.toMillis(timeInNano));
    }

    @Override
    public String toString() {
        long failedQueries = totalQueries - successfulQueries;
        long failedSetOfQueries = totalSetOfQueries - successfulSetsOfQueries;

        return new StringBuilder("").append("\n\n")
                .append("-------------Summery Start-------------").append('\n')
                .append("Single queries details: \n").append('\n')
                .append("Total num of queries: " + totalQueries).append('\n')
                .append("Successful single queries: " + successfulQueries).append('\n')
                .append("Failed queries: " + failedQueries).append('\n')
                .append(formatNanoTime("avg single query time: ", avgQueryTime())).append('\n')
                .append(formatNanoTime("min single query time: ", minTime)).append('\n')
                .append(formatNanoTime("max single query time: ", maxTime)).append("\n\n")
                .append("Set of queries details: ").append('\n')
                .append("Total num of sets of queries: " + totalSetOfQueries).append('\n')
                .append("Successful sets of queries: " + successfulSetsOfQueries).append('\n')
                .append("Failed queries: " + failedSetOfQueries).append('\n')
                .append(formatNanoTime("avg set of queries time: ", avgSetOfQueriesTime())).append('\n')
                .append(formatNanoTime("min set of queries time: ", setMinTime)).append('\n')
                .append(formatNanoTime("max set of queries time: ", setMaxTime)).append('\n')
                .append(formatThrownExceptions())
                .append("-------------Summery end-------------").append('\n')
                .toString();
    }

}
