package com.gigaspaces.app.persistent_event_processing.common.model;

import java.io.Serializable;

public class CommunicationInfo implements Serializable {

    private CommunicationInfo(Integer homePhone, Integer celPhone) {
        this.homePhone = homePhone;
        this.celPhone = celPhone;
    }

    private Integer homePhone;
    private Integer celPhone;

    public CommunicationInfo() {
    }

    public static CommunicationInfo createCommunicationInfo() {
        return new CommunicationInfo(RandomUtils.nextInt(), RandomUtils.nextInt());
    }

    public Integer getHomePhone() {
        return homePhone;
    }

    public Integer getCelPhone() {
        return celPhone;
    }

    public void setHomePhone(Integer homePhone) {
        this.homePhone = homePhone;
    }

    public void setCelPhone(Integer celPhone) {
        this.celPhone = celPhone;
    }
}
