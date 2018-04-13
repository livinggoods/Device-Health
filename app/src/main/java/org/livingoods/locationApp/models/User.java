package org.livingoods.locationApp.models;

import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class User {

    @Id
    public long id;
    public Long userId;
    public String phoneNumber;



    Date createdAt;
    Date updatedAt;

}