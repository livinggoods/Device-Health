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

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;

/**
 *
 * @author bensonbundi
 */
@Entity
@Table(name = "data_balance", schema = "events")
@XmlRootElement
@NamedQueries({ @NamedQuery(name = "DataBalance.findAll", query = "SELECT d FROM DataBalance d"),
		@NamedQuery(name = "DataBalance.findById", query = "SELECT d FROM DataBalance d WHERE d.id = :id"),
		@NamedQuery(name = "DataBalance.findByBalance", query = "SELECT d FROM DataBalance d WHERE d.balance = :balance"),
		@NamedQuery(name = "DataBalance.findByBalanceMessage", query = "SELECT d FROM DataBalance d WHERE d.balanceMessage = :balanceMessage"),
		@NamedQuery(name = "DataBalance.findByMessage", query = "SELECT d FROM DataBalance d WHERE d.message = :message"),
		@NamedQuery(name = "DataBalance.findByRecordedAt", query = "SELECT d FROM DataBalance d WHERE d.recordedAt = :recordedAt"),
		@NamedQuery(name = "DataBalance.findByCreatedAt", query = "SELECT d FROM DataBalance d WHERE d.createdAt = :createdAt"),
		@NamedQuery(name = "DataBalance.findByUpdatedAt", query = "SELECT d FROM DataBalance d WHERE d.updatedAt = :updatedAt") })
@TypeDef(name = "jsonb-node", typeClass = JsonNodeBinaryType.class)
public class DataBalance implements Serializable {

	@Type(type = "jsonb-node")
	@Column(name = "info", columnDefinition = "jsonb")
	private com.fasterxml.jackson.databind.JsonNode info;

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "id")
	private Long id;
	// @Max(value=?) @Min(value=?)//if you know range of your decimal fields
	// consider using these annotations to enforce field validation
	@Column(name = "balance")
	private Double balance;
	@Column(name = "balance_message")
	private String balanceMessage;
	@Column(name = "message")
	private String message;
	@Column(name = "sim")
	private Integer sim;
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
	private Users userId;

	public DataBalance() {
	}

	public DataBalance(Long id) {
		this.id = id;
	}

	public DataBalance(Long id, Date createdAt) {
		this.id = id;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public String getBalanceMessage() {
		return balanceMessage;
	}

	public void setBalanceMessage(String balanceMessage) {
		this.balanceMessage = balanceMessage;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getSim() {
		return sim;
	}

	public void setSim(Integer sim) {
		this.sim = sim;
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

	public Users getUserId() {
		return userId;
	}

	public void setUserId(Users userId) {
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
		if (!(object instanceof DataBalance)) {
			return false;
		}
		DataBalance other = (DataBalance) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "org.goods.living.tech.health.device.jpa.dao.DataBalance[ id=" + id + " ]";
	}

	public com.fasterxml.jackson.databind.JsonNode getInfo() {
		return info;
	}

	public void setInfo(com.fasterxml.jackson.databind.JsonNode info) {
		this.info = info;
	}

}
