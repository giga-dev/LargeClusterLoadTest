package com.gigaspaces.common.model;

import com.gigaspaces.annotation.pojo.SpaceId;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

import static com.gigaspaces.common.Constants.NUM_OF_CREW_MEMBERS_IN_FLIGHT;

public class Flight {
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

    public static List<CrewMember> createCrewMembers(int numOfCrewMembers) {
        List<CrewMember> crewMembers = new ArrayList<CrewMember>(numOfCrewMembers);

        for (int i = 0; i < numOfCrewMembers; i++) {
            crewMembers.add(CrewMember.createCrewMember(i));
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
