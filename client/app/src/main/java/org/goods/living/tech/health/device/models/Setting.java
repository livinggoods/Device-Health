package org.goods.living.tech.health.device.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Setting extends BaseModel {

    @Id
    public long id;

    public boolean loglocationOffEvent;

    public String ussd0;

    public String ussd1;

}