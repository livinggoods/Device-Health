package org.goods.living.tech.health.device.jpa.controllers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Named
@ApplicationScoped
public class ControllerManager {

	Logger logger = LogManager.getLogger();

	public ControllerManager() throws Exception {
		logger.debug("setting up ControllerManager");
		this.setUp();
	}

	// @Inject
	// @PersistenceUnit(unitName = "postgresClientDBDeviceHealth")
	// @PersistenceUnitQualifier(PersistenceUnitEnum.POSTGRES_DEVICE_HEALTH)
	EntityManagerFactory entityManagerFactoryDH;

	// @Inject
	// @PersistenceUnitQualifier(PersistenceUnitEnum.POSTGRES_MM)
	// @PersistenceContext(unitName = "postgresClientDBMM")
	EntityManagerFactory entityManagerFactoryMM;

	@Produces
	@org.goods.living.tech.health.device.jpa.PersistenceUnitQualifier(org.goods.living.tech.health.device.jpa.PersistenceUnitEnum.POSTGRES_DEVICE_HEALTH)
	public EntityManagerFactory getEntityManagerFactoryDH() {
		logger.debug("entityManagerFactoryDH() is called ...!");
		return entityManagerFactoryDH;
	}

	@Produces
	@org.goods.living.tech.health.device.jpa.PersistenceUnitQualifier(org.goods.living.tech.health.device.jpa.PersistenceUnitEnum.POSTGRES_MM)
	public EntityManagerFactory getEntityManagerFactoryMM() {
		logger.debug("entityManagerFactoryMM() is called ...!");
		return entityManagerFactoryMM;
	}

	@Produces
	public UsersJpaController getUsersJpaController() {
		return new UsersJpaController(entityManagerFactoryDH);
	}

	@Produces
	public StatsJpaController getStatsJpaController() {
		return new StatsJpaController(entityManagerFactoryDH);
	}

	@Produces
	public MedicJpaController getMedicJpaController() {
		return new MedicJpaController(entityManagerFactoryMM);
	}

	void setUp() throws Exception {
		entityManagerFactoryDH = Persistence.createEntityManagerFactory("postgresClientDBDeviceHealth");
		entityManagerFactoryMM = Persistence.createEntityManagerFactory("postgresClientDBMM");
	}

}
