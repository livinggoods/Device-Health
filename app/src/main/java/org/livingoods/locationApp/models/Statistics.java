package org.livingoods.locationApp.models;

import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

@Entity
public class Statistics {

    @Id public long id;
    public long userId; // ToOne target ID property
    public ToOne<User> user;

    Long latitude;
    Long longitude;

    Date createdAt;
    Date updatedAt;
}
