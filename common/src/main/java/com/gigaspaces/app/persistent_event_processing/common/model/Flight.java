package com.gigaspaces.app.persistent_event_processing.common.model;

import com.gigaspaces.annotation.pojo.SpaceId;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

public class Flight {
    private transient static final int MAX_CREW_MEMBERS = 20;
    private Integer id;
    private List<CrewMember> crewMembers;

    public Flight() {}

    private Flight(Integer flightNumber, List<CrewMember> crewMembers) {
        this.id = flightNumber;
        this.crewMembers = crewMembers;
    }

    public Flight(Integer flightNum) {
        id = flightNum;
    }

    public static Flight createFlight(int flightNum) {
        int numOfCrewMembers = flightNum % MAX_CREW_MEMBERS;

        return new Flight(flightNum, createCrewMembers(numOfCrewMembers));
    }

    private static List<CrewMember> createCrewMembers(int numOfCrewMembers) {
        List<CrewMember> crewMembers = new ArrayList<CrewMember>(numOfCrewMembers);

        for (int i = 0; i < numOfCrewMembers; i++) {
            crewMembers.add(CrewMember.createCrewMember());
        }

        return crewMembers;
    }

    @Id
    @SpaceId
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<CrewMember> getCrewMembers() {
        return crewMembers;
    }

    public void setCrewMembers(List<CrewMember> crewMembers) {
        this.crewMembers = crewMembers;
    }
}
