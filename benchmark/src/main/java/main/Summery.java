package main;

import java.util.Hashtable;
import java.util.Map;

public class Summery {
    private long minTime;
    private long maxTime;
    private long sumTime;
    private long totalQueries;
    private long successfulQueries;
    private Map<String, Integer> thrownExceptions; // <e.simpleName, numOfThrownExceptions>

    public Summery() {
        this.thrownExceptions = new Hashtable<String, Integer>();
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

    public long getTotalQueries() {
        return totalQueries;
    }

    public long getSuccessfulQueries() {
        return successfulQueries;
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

    @Override
    public String toString() {
        long failedQueries = totalQueries - successfulQueries;

        return new StringBuilder("").append('\n').append('\n')
                .append("-------------Summery Start-------------").append('\n')
                .append("Total num of queries: " + totalQueries).append('\n')
                .append("Successful queries: " + successfulQueries).append('\n')
                .append("Failed queries: " + failedQueries).append('\n')
                .append("Avg query time in nanoseconds: " + avgQueryTime()).append('\n')
                .append("Min query time in nanoseconds: " + minTime).append('\n')
                .append("Max query time in nanoseconds: " + maxTime).append('\n')
                .append(formatThrownExceptions())
                .append("-------------Summery end-------------").append('\n')
                .toString();
    }
}
