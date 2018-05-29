package org.goods.living.tech.health.device.service.app;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ApplicationPath("/api")
public class RestApplication extends Application {

	Logger logger = LogManager.getLogger();// .getName());

	/**
	 * Set<Class<?>> classes is required for CDI
	 */
	// private Set<Class<?>> classes = new HashSet<>();
	// private Set<Object> singletons;

	public RestApplication() {
		logger.debug("TestRestApplication() called");

		// classes.add(FormService.class);
	}

}