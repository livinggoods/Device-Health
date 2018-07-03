package org.goods.living.tech.health.device.service.security;

import java.security.Principal;

/*
 *
 * @author:  Asif Akram
 * @purpose: getters and setters for the claims present in the context (token header).
 * 
 */
public class ValidUser implements Principal {

	private String username;
	private String apiToken;
	private String role;
	private String site;
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	private String jwtToken;

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	private String lastName;

	public String getUserName() {
		return this.username;
	}

	public void setUserName(String username) {
		this.username = username;
	}

	public String getApiToken() {
		return this.apiToken;
	}

	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}

	public String getRole() {
		return this.role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getSite() {
		return this.site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getJwtToken() {
		return jwtToken;
	}

	public void setJwtToken(String jwtToken) {
		this.jwtToken = jwtToken;
	}

	@Override
	public String getName() {
		return this.firstName + " " + this.lastName; // To change body of generated methods, choose Tools | Templates.
	}

}