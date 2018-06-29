package org.goods.living.tech.health.device.jpa.dao;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "admin_users", schema = "events", catalog = "device_health_development")
public class AdminUsers {
    private long id;
    private String name;
    private String email;
    private String password;
    private String forgotToken;
    private Serializable createdAt;
    private Serializable updatedAt;

    @Id
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Basic
    @Column(name = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Basic
    @Column(name = "forgot_token")
    public String getForgotToken() {
        return forgotToken;
    }

    public void setForgotToken(String forgotToken) {
        this.forgotToken = forgotToken;
    }

    @Basic
    @Column(name = "created_at")
    public Serializable getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Serializable createdAt) {
        this.createdAt = createdAt;
    }

    @Basic
    @Column(name = "updated_at")
    public Serializable getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Serializable updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdminUsers that = (AdminUsers) o;

        if (id != that.id) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (forgotToken != null ? !forgotToken.equals(that.forgotToken) : that.forgotToken != null) return false;
        if (createdAt != null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;
        if (updatedAt != null ? !updatedAt.equals(that.updatedAt) : that.updatedAt != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (forgotToken != null ? forgotToken.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        return result;
    }
}
