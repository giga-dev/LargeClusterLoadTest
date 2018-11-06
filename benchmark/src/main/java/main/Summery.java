package main;

public class Summery {
    private long minTime;
    private long maxTime;
    private long sumTime;
    private long totalQueries;
    private long successfulQueries;

    private long avgQueryTime() {
        return sumTime / successfulQueries;
    }

    public long getTotalQueries() {
        return totalQueries;
    }

    public long getSuccessfulQueries() {
        return successfulQueries;
    }

    public void updateOnSuccessfulQuery(long queryTime) {
        successfulQueries++;

        if (queryTime < minTime) {
            minTime = queryTime;
        }

        if (queryTime > maxTime) {
            maxTime = queryTime;
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("-------------Summery Start-------------").append('\n')
                .append("Total num of queries: " + totalQueries).append('\n')
                .append("Successful queries: " + successfulQueries).append('\n')
                .append("Avg query time in milliseconds: " + avgQueryTime()).append('\n')
                .append("Min query time: " + minTime).append('\n')
                .append("Max query time: " + maxTime).append('\n')
                .append("-------------Summery end-------------").append('\n')
                .toString();
    }

    public void incNumOfQueries() {
        totalQueries++;
    }
}
