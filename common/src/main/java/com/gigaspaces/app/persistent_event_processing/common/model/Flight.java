package com.gigaspaces.app.persistent_event_processing.common.model;

import com.gigaspaces.annotation.pojo.SpaceId;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Flight {
    private Integer number;
    private List<CrewMember> crewMembers;

    public Flight() {}

    private Flight(Integer number, List<CrewMember> crewMembers) {
        this.number = number;
        this.crewMembers = crewMembers;
    }

    public Flight(Integer flightNum) {
        number = flightNum;
    }

    public static Flight createFlight() {
        int num = RandomUtils.nextInt();
        return new Flight(num, createCrewMembers(num));
    }

    private static List<CrewMember> createCrewMembers(int numOfCrewMembers) {
        List<CrewMember> crewMembers = new ArrayList<CrewMember>(numOfCrewMembers);

        for (int i = 0; i < numOfCrewMembers; i++) {
            crewMembers.add(CrewMember.createCrewMember());
        }

        return crewMembers;
    }

    @SpaceId
    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public List<CrewMember> getCrewMembers() {
        return crewMembers;
    }

    public void setCrewMembers(List<CrewMember> crewMembers) {
        this.crewMembers = crewMembers;
    }
}
