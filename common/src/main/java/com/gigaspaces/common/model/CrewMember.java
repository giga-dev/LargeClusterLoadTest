package com.gigaspaces.common.model;

import com.gigaspaces.annotation.pojo.SpaceId;

import java.io.Serializable;

public class CrewMember implements Serializable {
    private Integer id;
    private String name;
    private Integer age;
    private Integer numOfFlights;
    private Integer salary;
    private CommunicationInfo communicationInfo;

    public CrewMember() {
    }

    private CrewMember(Integer id, String name, Integer age, Integer numOfFlights, Integer salary, CommunicationInfo communicationInfo) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.numOfFlights = numOfFlights;
        this.salary = salary;
        this.communicationInfo = communicationInfo;
    }

    public static CrewMember createCrewMember(int id) {
        return new CrewMember(id, RandomUtils.nextString(), RandomUtils.nextInt(),
                RandomUtils.nextInt(), RandomUtils.nextInt(), CommunicationInfo.createCommunicationInfo());
    }

    @SpaceId
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getNumOfFlights() {
        return numOfFlights;
    }

    public void setNumOfFlights(Integer numOfFlights) {
        this.numOfFlights = numOfFlights;
    }

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    public CommunicationInfo getCommunicationInfo() {
        return communicationInfo;
    }

    public void setCommunicationInfo(CommunicationInfo communicationInfo) {
        this.communicationInfo = communicationInfo;
    }
}
