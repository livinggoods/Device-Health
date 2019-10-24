/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.goods.living.tech.health.device.jpa.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;

/**
 *
 * @author bensonbundi
 */
@Entity
@Table(name = "users",  uniqueConstraints = { @UniqueConstraint(columnNames = { "chv_id" }) })
@XmlRootElement
@NamedQueries({ @NamedQuery(name = "Users.findAll", query = "SELECT u FROM Users u"),
		@NamedQuery(name = "Users.findById", query = "SELECT u FROM Users u WHERE u.id = :id"),
		@NamedQuery(name = "Users.findByChvId", query = "SELECT u FROM Users u WHERE u.chvId = :chvId"),
		@NamedQuery(name = "Users.findByPhone", query = "SELECT u FROM Users u WHERE u.phone = :phone"),
		@NamedQuery(name = "Users.findByAndroidId", query = "SELECT u FROM Users u WHERE u.androidId = :androidId"),
		@NamedQuery(name = "Users.findByCreatedAt", query = "SELECT u FROM Users u WHERE u.createdAt = :createdAt"),
		@NamedQuery(name = "Users.findByUpdatedAt", query = "SELECT u FROM Users u WHERE u.updatedAt = :updatedAt"),
		@NamedQuery(name = "Users.findByVersionCode", query = "SELECT u FROM Users u WHERE u.versionCode = :versionCode"),
		@NamedQuery(name = "Users.findByVersionName", query = "SELECT u FROM Users u WHERE u.versionName = :versionName"),
		@NamedQuery(name = "Users.findByUsername", query = "SELECT u FROM Users u WHERE u.username = :username"),
		@NamedQuery(name = "Users.findByPassword", query = "SELECT u FROM Users u WHERE u.password = :password"),
		@NamedQuery(name = "Users.findByRecordedAt", query = "SELECT u FROM Users u WHERE u.recordedAt = :recordedAt"),
		@NamedQuery(name = "Users.findByDeviceTime", query = "SELECT u FROM Users u WHERE u.deviceTime = :deviceTime"),
		@NamedQuery(name = "Users.findByName", query = "SELECT u FROM Users u WHERE u.name = :name"),
		@NamedQuery(name = "Users.findByBranch", query = "SELECT u FROM Users u WHERE u.branch = :branch"),
		@NamedQuery(name = "Users.findByCountry", query = "SELECT u FROM Users u WHERE u.country = :country"),
		@NamedQuery(name = "Users.findByFcmToken", query = "SELECT u FROM Users u WHERE u.fcmToken = :fcmToken"),
		@NamedQuery(name = "Users.findByUserNameAndAndroidId", query = "SELECT u FROM Users u WHERE u.username = :username AND u.androidId = :androidId"),
		@NamedQuery(name = "Users.findByUsernameLike", query = "SELECT u FROM Users u WHERE u.username like :username") })
@TypeDef(name = "jsonb-node", typeClass = JsonNodeBinaryType.class)
public class Users implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "id", nullable = false)
	private Long id;
	@Column(name = "chv_id", length = 128)
	private String chvId;
	@Column(name = "phone", length = 64)
	private String phone;
	@Column(name = "android_id", nullable = true, length = 64)
	private String androidId;

	@Basic(optional = false)
	@Column(name = "created_at", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	@Column(name = "updated_at")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;
	@Basic(optional = false)
	@Column(name = "version_code", nullable = false)
	private int versionCode;
	@Column(name = "version_name", length = 128)
	private String versionName;
	@Basic(optional = false)
	@Column(name = "username", nullable = false, length = 128)
	private String username;
	@Column(name = "password", length = 128)
	private String password;
	@Column(name = "recorded_at")
	@Temporal(TemporalType.TIMESTAMP)
	private Date recordedAt;
	@Column(name = "device_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date deviceTime;
	@Column(name = "name", length = 128)
	private String name;
	@Column(name = "branch", length = 128)
	private String branch;
	@Column(name = "country", length = 8)
	private String country;

	@Column(name = "fcm_token", length = 512)
	private String fcmToken;

	@Type(type = "jsonb-node")
	@Column(name = "device_info", columnDefinition = "jsonb")

	// org.codehaus.jackson.node.ObjectNode cannot be cast to
	// com.fasterxml.jackson.databind.JsonNode
	private com.fasterxml.jackson.databind.JsonNode deviceInfo;
	@Type(type = "jsonb-node")
	@Column(name = "setting", columnDefinition = "jsonb")
	private com.fasterxml.jackson.databind.JsonNode setting;
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
	private Collection<Stats> statsCollection;
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
	private Collection<DataBalance> dataBalanceCollection;

	@Column(name = "supervisor")
	private Boolean supervisor;

	public Boolean getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(Boolean supervisor) {
		this.supervisor = supervisor;
	}

	public Users() {
	}

	public Users(Long id) {
		this.id = id;
	}

	public Users(Long id, String androidId, Date createdAt, int versionCode, String username) {
		this.id = id;
		this.androidId = androidId;
		this.createdAt = createdAt;
		this.versionCode = versionCode;
		this.username = username;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getChvId() {
		return chvId;
	}

	public void setChvId(String chvId) {
		this.chvId = chvId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAndroidId() {
		return androidId;
	}

	public void setAndroidId(String androidId) {
		this.androidId = androidId;
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

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getRecordedAt() {
		return recordedAt;
	}

	public void setRecordedAt(Date recordedAt) {
		this.recordedAt = recordedAt;
	}

	public Date getDeviceTime() {
		return deviceTime;
	}

	public void setDeviceTime(Date deviceTime) {
		this.deviceTime = deviceTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getFcmToken() {
		return fcmToken;
	}

	public void setFcmToken(String fcmToken) {
		this.fcmToken = fcmToken;
	}

	public com.fasterxml.jackson.databind.JsonNode getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(com.fasterxml.jackson.databind.JsonNode deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	public com.fasterxml.jackson.databind.JsonNode getSetting() {
		return setting;
	}

	public void setSetting(com.fasterxml.jackson.databind.JsonNode setting) {
		this.setting = setting;
	}

	@XmlTransient
	public Collection<Stats> getStatsCollection() {
		return statsCollection;
	}

	public void setStatsCollection(Collection<Stats> statsCollection) {
		this.statsCollection = statsCollection;
	}

	@XmlTransient
	public Collection<DataBalance> getDataBalanceCollection() {
		return dataBalanceCollection;
	}

	public void setDataBalanceCollection(Collection<DataBalance> collection) {
		this.dataBalanceCollection = collection;
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
		if (!(object instanceof Users)) {
			return false;
		}
		Users other = (Users) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "org.goods.living.tech.health.device.jpa.dao.Users[ id=" + id + " ]";
	}

}
