package org.goods.living.tech.health.device.service;

import java.text.SimpleDateFormat;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goods.living.tech.health.device.jpa.controllers.UsersJpaController;
import org.goods.living.tech.health.device.jpa.dao.Users;
import org.goods.living.tech.health.device.models.Result;
import org.goods.living.tech.health.device.service.security.ValidUser;
import org.goods.living.tech.health.device.utility.ApplicationParameters;

public class BaseService {

	Logger logger = LogManager.getLogger();

	String serviceMessage = getClass().getSimpleName();

	SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss Z");

	@Inject
	ApplicationParameters applicationParameters;

	@Context
	SecurityContext securityContext;

	@Inject
	UsersJpaController usersJpaController;

	public BaseService() {
	}

	@GET
	@Produces("application/json")
	public Result<String> index() {// Response
		logger.debug("index page of " + serviceMessage + " hash: " + applicationParameters.getHashKey());

		Result<String> result = new Result(true, "", serviceMessage + " hash: " + applicationParameters.getHashKey());
		return result;// Response.status(200).entity(result).build();
	}

	public ValidUser getValidUser() {// Response
		logger.debug("getValidUser");

		return (ValidUser) securityContext.getUserPrincipal();
	}

	public Users getCurrentUser() {// Response
		logger.debug("getCurrentUser");
		ValidUser ValidUser = getValidUser();
		if (ValidUser != null) {
			Users users = usersJpaController.findUsers(ValidUser.getId());
			return users;
		}
		return null;
	}

}