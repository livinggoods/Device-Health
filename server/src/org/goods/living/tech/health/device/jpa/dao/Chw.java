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
 * @author Chebet
 */
@Entity
@Table(name = "chw", uniqueConstraints = { @UniqueConstraint(columnNames = { "contact_id" }) })
@XmlRootElement
@NamedQueries({ @NamedQuery(name = "Chw.findAll", query = "SELECT u FROM Chw u"),
		@NamedQuery(name = "Chw.findByUsername", query = "SELECT u FROM Chw u WHERE u.username = :username"),
		@NamedQuery(name = "Chw.findByContactId", query = "SELECT u FROM Chw u WHERE u.contactId = :contactId"),
		@NamedQuery(name = "Chw.findByChwName", query = "SELECT u FROM Chw u WHERE u.chwName = :chwName"),
		@NamedQuery(name = "Chw.findByBranchName", query = "SELECT u FROM Chw u WHERE u.branchName = :branchName"),
		@NamedQuery(name = "Chw.findByChwPhone", query = "SELECT u FROM Chw u WHERE u.chwPhone = :chwPhone"),
		@NamedQuery(name = "Chw.findByCountry", query = "SELECT u FROM Chw u WHERE u.country = :country"),
        @NamedQuery(name = "Chw.findBySupervisorName", query = "SELECT u FROM Chw u WHERE u.supervisorName = :supervisorName")
})
@TypeDef(name = "jsonb-node", typeClass = JsonNodeBinaryType.class)
public class Chw implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "username")
	private String username;
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
	

	public Chw() {
	}

	public Chw(String username) {
		this.username = username;
	}
	
	public Chw(String Username, String ContactId, String ChwName, String BranchName, String ChwPhone, String Country, String SupervisorName) {
		this.username = Username;
		this.contactId = ContactId;
		this.chwName = ChwName;
		this.branchName = BranchName;
		this.chwPhone = ChwPhone;
		this.country = Country;
		this.supervisorName = SupervisorName;
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
	


	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof Chw)) {
			return false;
		}
		Chw other = (Chw) object;
		if ((this.username == null && other.username != null) || (this.username != null && !this.username.equals(other.username))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "org.goods.living.tech.health.device.jpa.dao.Chw[ id=" + username + " ]";
	}

}
