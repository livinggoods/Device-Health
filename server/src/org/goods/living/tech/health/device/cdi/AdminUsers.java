/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.goods.living.tech.health.device.cdi;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author kevinkorir
 */
@Entity
@Table(name = "admin_users")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AdminUsers.findAll", query = "SELECT a FROM AdminUsers a")
    , @NamedQuery(name = "AdminUsers.findById", query = "SELECT a FROM AdminUsers a WHERE a.id = :id")
    , @NamedQuery(name = "AdminUsers.findByName", query = "SELECT a FROM AdminUsers a WHERE a.name = :name")
    , @NamedQuery(name = "AdminUsers.findByEmail", query = "SELECT a FROM AdminUsers a WHERE a.email = :email")
    , @NamedQuery(name = "AdminUsers.findByPassword", query = "SELECT a FROM AdminUsers a WHERE a.password = :password")
    , @NamedQuery(name = "AdminUsers.findByForgotToken", query = "SELECT a FROM AdminUsers a WHERE a.forgotToken = :forgotToken")
    , @NamedQuery(name = "AdminUsers.findByCreatedAt", query = "SELECT a FROM AdminUsers a WHERE a.createdAt = :createdAt")
    , @NamedQuery(name = "AdminUsers.findByUpdatedAt", query = "SELECT a FROM AdminUsers a WHERE a.updatedAt = :updatedAt")})
public class AdminUsers implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Size(max = 128)
    @Column(name = "name")
    private String name;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Size(max = 128)
    @Column(name = "email")
    private String email;
    @Size(max = 256)
    @Column(name = "password")
    private String password;
    @Size(max = 120)
    @Column(name = "forgot_token")
    private String forgotToken;
    @Basic(optional = false)
    @NotNull
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    public AdminUsers() {
    }

    public AdminUsers(Long id) {
        this.id = id;
    }

    public AdminUsers(Long id, Date createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getForgotToken() {
        return forgotToken;
    }

    public void setForgotToken(String forgotToken) {
        this.forgotToken = forgotToken;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AdminUsers)) {
            return false;
        }
        AdminUsers other = (AdminUsers) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.goods.living.tech.health.device.cdi.AdminUsers[ id=" + id + " ]";
    }
    
}
