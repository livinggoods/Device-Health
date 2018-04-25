package org.goods.living.tech.health.device.service;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.jboss.resteasy.logging.Logger;
//import org.goods.living.tech.health.device.jpa.qualifier.PersistenceUnitQualifier;
//import org.goods.living.tech.health.device.jpa.utility.PersistenceUnitEnum;
//import org.goods.living.tech.health.device.service.security.qualifier.Secured;
import org.goods.living.tech.health.device.utility.ApplicationParameters;

public class BaseService {

	// @Inject
	// @PersistenceUnitQualifier(PersistenceUnitEnum.POSTGRES_MANAGEMENT)
	// private EntityManagerFactory copd_management_emf;

	@Inject
	ApplicationParameters applicationParameters;

	Logger logger = LogManager.getLogger();// .getName());
	// Logger logger = LogManager.getLogger(MyApp.class);
	// Logger logger = Logger.getLogger(getClass());

	public BaseService() {
	}

}