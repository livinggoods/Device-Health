package org.goods.living.tech.health.device.models;

import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class User {

    @Id
    public long id;
    public Long masterId;
    public String chpId;
    public String phoneNumber;
    public String androidId;
    public long updateInterval = 60000; // seconds in millis 1000=1
    public boolean syncEnabled;
    public Date lastSync;


    public Date createdAt;
    public Date updatedAt;

}