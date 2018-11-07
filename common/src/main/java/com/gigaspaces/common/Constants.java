package com.gigaspaces.common;

public class Constants {
    public static final int NUM_OF_FLIGHTS_TO_WRITE = 1000;
    public static final int NUM_OF_PARTITIONS = 72;
    public static final int NUM_OF_CREW_MEMBERS= 100;
    public static final int NUM_OF_CREW_MEMBERS_IN_FLIGHT = 10;
    public static final int NUM_OF_CREW_MEMBERS_BUCKETS = NUM_OF_CREW_MEMBERS / NUM_OF_CREW_MEMBERS_IN_FLIGHT;
}