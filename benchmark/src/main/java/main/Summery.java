package main;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Map;

import static java.text.MessageFormat.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class Summery {
    private static final long MAX_EXPECTED_QUERY_DURATION_NANO = MILLISECONDS.toNanos(500);
    private String name;
    private long queryMinTimeNano = Long.MAX_VALUE;
    private long queryMaxTimeNano;
    private long totalQueriesTime;
    private long totalQueries;
    private long successQueries;
    private Map<String, Integer> thrownExceptions; // <e.simpleName, numOfThrownExceptions>
    private int numOfExceptionalQueries;
    private int totalExceptionalQueriesTime;

    public Summery() {
        this.name = "Summary";
        this.thrownExceptions = new Hashtable<String, Integer>();
    }

    public void incTotalQueries() {
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

    public void reportQueryDuration(long duration) {
        successQueries++;
        totalQueriesTime += duration;

        if (MAX_EXPECTED_QUERY_DURATION_NANO < duration) {
            this.numOfExceptionalQueries++;
            this.totalExceptionalQueriesTime += duration;
        }

        if (duration < queryMinTimeNano) {
            queryMinTimeNano = duration;
        }

        if (duration > queryMaxTimeNano) {
            queryMaxTimeNano = duration;
        }

    }

    private long avgQueryTime() {
        long avg;

        if (successQueries != 0) {
            avg = totalQueriesTime / successQueries;
        } else {
            avg = 0;
        }

        return avg;
    }

    private String formatQueriesDuration() {
        long avg = avgQueryTime();
        long min = queryMinTimeNano == Long.MAX_VALUE ? 0 : queryMinTimeNano;

        StringBuilder sb = new StringBuilder()
                .append(format("min query time: {0} ({1} ns)\n", formatNanoTime(min), min))
                .append(format("max query time: {0}\n", formatNanoTime(queryMaxTimeNano)))
                .append(format("avg query time: {0} ({1} ns)\n", formatNanoTime(avg), avg));

        return sb.toString();
    }

    private String formatThrownExceptions() {
        StringBuilder stringBuilder = new StringBuilder("Thrown exceptions by type: ").append('\n');

        thrownExceptions.forEach(
                (name, amount) -> stringBuilder.append(name + " : " + amount).append('\n')
        );

        return stringBuilder.toString();
    }

    private String formatExceptionalQueries() {
        long avgQueryTime;

        if (numOfExceptionalQueries != 0) {
            avgQueryTime = totalExceptionalQueriesTime / numOfExceptionalQueries;
        } else {
            avgQueryTime = 0;
        }

        StringBuilder sb = new StringBuilder("")
                .append(MessageFormat.format("num of exceptional queries: {0}\n", numOfExceptionalQueries))
                .append(MessageFormat.format("avg exceptional queries time: {0}\n", avgQueryTime));

        return sb.toString();
    }

    public String intermediateSummery() {
        return new StringBuilder("")
                .append("\n\n-------------Intermediate Summary Start-------------").append('\n')
                .append(format("{0} info:\n", name))
                .append(format("Total num of queries: {0}\n", totalQueries))
                .append(formatQueriesDuration())
                .append(formatExceptionalQueries())
                .append(formatThrownExceptions())
                .append("-------------Intermediate Summary End-------------").append('\n')
                .toString();
    }

    public String finalSummery() {
        long failedQueries = totalQueries - successQueries;

        return new StringBuilder("")
                .append("\n\n-------------Final Summary Start-------------").append('\n')
                .append(format("{0} info:\n", name))
                .append(format("Total num of queries: {0}\n", totalQueries))
                .append(format("Successful queries: {0}\n", successQueries))
                .append(format("Failed queries: {0}\n", failedQueries))
                .append(formatQueriesDuration())
                .append(formatThrownExceptions())
                .append(formatExceptionalQueries())
                .append("-------------Final Summary end-------------").append('\n')
                .toString();
    }

    private String formatNanoTime(long nanoTime) {
        return format("{0} ms", NANOSECONDS.toMillis(nanoTime));
    }

}
