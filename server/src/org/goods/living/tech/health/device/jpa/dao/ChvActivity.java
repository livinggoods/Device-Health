/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.goods.living.tech.health.device.jpa.dao;

import org.codehaus.jackson.JsonNode;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.HashMap;

/**
 * @author kevinkorir
 */
public class ChvActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String uuid;
    private HashMap<String,String> coordinates;
    private String activityType;
    private double timestamp;
    private String contactPerson;
    private double reportedDate;

    public HashMap<String, String> getCoordinates() {
        return coordinates;
    }

    //location coordinates

    public void setCoordinates(HashMap<String, String> coordinates) {
        this.coordinates = coordinates;
    }

    public double getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(double reportedDate) {
        this.reportedDate = reportedDate;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
