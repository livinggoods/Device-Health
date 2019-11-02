package org.goods.living.tech.health.device.jpa.dao;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
/**
 *
 * @author ernestmurimi
 */
@Entity
@Table(name = "chw")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Chw.findAll", query = "SELECT c FROM Chw c")
    , @NamedQuery(name = "Chw.findByUsername", query = "SELECT c FROM Chw c WHERE c.username = :username")
    , @NamedQuery(name = "Chw.findByContactId", query = "SELECT c FROM Chw c WHERE c.contactId = :contactId")
    , @NamedQuery(name = "Chw.findByChwName", query = "SELECT c FROM Chw c WHERE c.chwName = :chwName")
    , @NamedQuery(name = "Chw.findByBranchName", query = "SELECT c FROM Chw c WHERE c.branchName = :branchName")
    , @NamedQuery(name = "Chw.findByChwPhone", query = "SELECT c FROM Chw c WHERE c.chwPhone = :chwPhone")
    , @NamedQuery(name = "Chw.findByCountry", query = "SELECT c FROM Chw c WHERE c.country = :country")
    , @NamedQuery(name = "Chw.findBySupervisorName", query = "SELECT c FROM Chw c WHERE c.supervisorName = :supervisorName")
    , @NamedQuery(name = "Chw.findById", query = "SELECT c FROM Chw c WHERE c.id = :id")})
@TypeDef(name = "jsonb-node", typeClass = JsonNodeBinaryType.class)
public class Chw implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "username")
    private String username;
    @Basic(optional = false)
    @Column(name = "contact_id")
    private String contactId;
    @Column(name = "chw_name")
    private String chwName;
    @Column(name = "branch_name")
    private String branchName;
    @Column(name = "chw_phone")
    private String chwPhone;
    @Column(name = "country")
    private String country;
    @Column(name = "supervisor_name")
    private String supervisorName;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    
    public Chw() {
    	
    }

    public Chw(Long id) {
        this.id = id;
    }

    public Chw(Long id, String contactId) {
        this.id = id;
        this.contactId = contactId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getChwName() {
        return chwName;
    }

    public void setChwName(String chwName) {
        this.chwName = chwName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getChwPhone() {
        return chwPhone;
    }

    public void setChwPhone(String chwPhone) {
        this.chwPhone = chwPhone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        if (!(object instanceof Chw)) {
            return false;
        }
        Chw other = (Chw) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.goods.living.tech.health.device.jpa.dao.Chw[ id=" + id + " ]";
    }
    
}