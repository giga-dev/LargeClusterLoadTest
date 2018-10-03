package com.gigaspaces.app.persistent_event_processing.common.model;

public class CrewMember {
    private CrewMemberInfo crewMemberInfo;
    private Integer sequenceId;

    public CrewMember() {
    }

    private CrewMember(CrewMemberInfo crewMemberInfo, Integer sequenceId) {
        this.crewMemberInfo = crewMemberInfo;
        this.sequenceId = sequenceId;
    }

    public static CrewMember createCrewMember() {
        return new CrewMember(CrewMemberInfo.createCrewMemberInfo(), RandomUtils.nextInt());
    }

    public CrewMemberInfo getCrewMemberInfo() {
        return crewMemberInfo;
    }

    public Integer getSequenceId() {
        return sequenceId;
    }

    public void setCrewMemberInfo(CrewMemberInfo crewMemberInfo) {
        this.crewMemberInfo = crewMemberInfo;
    }

    public void setSequenceId(Integer sequenceId) {
        this.sequenceId = sequenceId;
    }
}
