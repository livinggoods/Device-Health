/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.goods.living.tech.health.device.jpa.dao;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author bensonbundi
 */
@Entity
@Table(name = "stats",  schema = "events")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Stat.findAll", query = "SELECT s FROM Stat s")
    , @NamedQuery(name = "Stat.findById", query = "SELECT s FROM Stat s WHERE s.id = :id")
    , @NamedQuery(name = "Stat.findByLatitude", query = "SELECT s FROM Stat s WHERE s.latitude = :latitude")
    , @NamedQuery(name = "Stat.findByLongitude", query = "SELECT s FROM Stat s WHERE s.longitude = :longitude")
    , @NamedQuery(name = "Stat.findByAccuracy", query = "SELECT s FROM Stat s WHERE s.accuracy = :accuracy")
    , @NamedQuery(name = "Stat.findByProvider", query = "SELECT s FROM Stat s WHERE s.provider = :provider")
    , @NamedQuery(name = "Stat.findByRecordedAt", query = "SELECT s FROM Stat s WHERE s.recordedAt = :recordedAt")
    , @NamedQuery(name = "Stat.findByCreatedAt", query = "SELECT s FROM Stat s WHERE s.createdAt = :createdAt")
    , @NamedQuery(name = "Stat.findByUpdatedAt", query = "SELECT s FROM Stat s WHERE s.updatedAt = :updatedAt")})
public class Stat implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Long id;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private String provider;
    @Column(name = "recorded_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date recordedAt;
    @Basic(optional = false)
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private User userId;

    public Stat() {
    }

    public Stat(Long id) {
        this.id = id;
    }

    public Stat(Long id, Date createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Date getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Date recordedAt) {
        this.recordedAt = recordedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Stat)) {
            return false;
        }
        Stat other = (Stat) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.goods.living.tech.health.device.jpa.dao.Stat[ id=" + id + " ]";
    }
    
}
