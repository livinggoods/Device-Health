/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.goods.living.tech.health.device.jpa.dao;

import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * @author kevinkorir
 */
public class ChvActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String chvUuid;
    private String activityId;
    private String activityType;
    private Double latitude;
    private Double longitude;
    private String clientName;
    private Date reportedDate;
    private JSONObject medicCoordinates;

    public JSONObject getMedicCoordinates() {
        return medicCoordinates;
    }

    public void setMedicCoordinates(JSONObject medicCoordinates) {
        this.medicCoordinates = medicCoordinates;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Date getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(Date reportedDate) {
        this.reportedDate = reportedDate;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getChvUuid() {
        return chvUuid;
    }

    public void setChvUuid(String chvUuid) {
        this.chvUuid = chvUuid;
    }

}
