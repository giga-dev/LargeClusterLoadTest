package com.gigaspaces.common.model;

import com.gigaspaces.annotation.pojo.SpaceId;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import java.util.List;


public class Flight {
    private Integer id;
    private List<CrewMember> crewMembers;

    public Flight() {}

    public Flight(Integer flightNum) {
        id = flightNum;
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
