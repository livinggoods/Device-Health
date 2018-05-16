package org.goods.living.tech.health.device.service;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goods.living.tech.health.device.models.Result;
import org.goods.living.tech.health.device.utility.ApplicationParameters;

public class BaseService {

	Logger logger = LogManager.getLogger();

	String serviceMessage = getClass().getSimpleName();

	@Inject
	ApplicationParameters applicationParameters;

	public BaseService() {
	}

	@GET
	@Produces("application/json")
	public Result<String> index() {// Response
		logger.debug("index page of " + serviceMessage + " hash: " + applicationParameters.getHashKey());

		Result<String> result = new Result(true, "", serviceMessage + " hash: " + applicationParameters.getHashKey());
		return result;// Response.status(200).entity(result).build();
	}
}