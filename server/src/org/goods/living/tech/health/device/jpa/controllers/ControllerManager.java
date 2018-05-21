package org.goods.living.tech.health.device.jpa.controllers;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goods.living.tech.health.device.jpa.PersistenceUnitEnum;
import org.goods.living.tech.health.device.jpa.PersistenceUnitQualifier;

@Named
@RequestScoped
public class ControllerManager {

	Logger logger = LogManager.getLogger();

	@Inject
	@PersistenceUnitQualifier(PersistenceUnitEnum.POSTGRES_DEVICE_HEALTH)
	EntityManagerFactory entityManagerFactory;

	@Produces
	public UsersJpaController getUsersJpaController() {
		return new UsersJpaController(entityManagerFactory);
	}

	@Produces
	public StatsJpaController getStatsJpaController() {
		return new StatsJpaController(entityManagerFactory);
	}
}
