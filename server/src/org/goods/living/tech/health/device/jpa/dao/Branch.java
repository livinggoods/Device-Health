package org.goods.living.tech.health.device.jpa.dao;

import java.io.Serializable;

/**
 * @author kevinkorir
 */
public class Branch implements Serializable {

    private static final long serialVersionUID = 1L;
    private String uuid;
    private String name;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
