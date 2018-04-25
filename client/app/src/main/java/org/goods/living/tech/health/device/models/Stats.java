package org.goods.living.tech.health.device.models;

import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Stats {

    @Id
    public long id;
    public long userId; // ToOne target ID property
    public Long userMasterId;
    //  public ToOne<User> user;

    public double latitude;
    public double longitude;
    public double accuracy;
    public String provider;
    public Date time;

    public Date createdAt;
    // Date updatedAt;
}
