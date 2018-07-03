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
	EntityManagerFactory entityManagerFactoryMMKE;
	EntityManagerFactory entityManagerFactoryMMUG;

	@Produces
	@org.goods.living.tech.health.device.jpa.PersistenceUnitQualifier(org.goods.living.tech.health.device.jpa.PersistenceUnitEnum.POSTGRES_DEVICE_HEALTH)
	public EntityManagerFactory getEntityManagerFactoryDH() {
		logger.debug("entityManagerFactoryDH() is called ...!");
		return entityManagerFactoryDH;
	}

	@Produces
	@org.goods.living.tech.health.device.jpa.PersistenceUnitQualifier(org.goods.living.tech.health.device.jpa.PersistenceUnitEnum.POSTGRES_MMKE)
	public EntityManagerFactory getEntityManagerFactoryMMKE() {
		logger.debug("entityManagerFactoryMMKE() is called ...!");
		return entityManagerFactoryMMKE;
	}

	@Produces
	@org.goods.living.tech.health.device.jpa.PersistenceUnitQualifier(org.goods.living.tech.health.device.jpa.PersistenceUnitEnum.POSTGRES_MMUG)
	public EntityManagerFactory getEntityManagerFactoryMMUG() {
		logger.debug("entityManagerFactoryMMUG() is called ...!");
		return entityManagerFactoryMMUG;
	}

	@Produces
	public DataBalanceJpaController getDataBalanceJpaController() {
		return new DataBalanceJpaController(entityManagerFactoryDH);
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
		return new MedicJpaController(entityManagerFactoryMMKE, entityManagerFactoryMMUG);
	}

	@Produces
	public AdminUsersJpaController getAdminUsersJpaController() {
		return new AdminUsersJpaController(entityManagerFactoryDH);
	}

	void setUp() throws Exception {
		entityManagerFactoryDH = Persistence.createEntityManagerFactory("postgresClientDBDeviceHealth");
		entityManagerFactoryMMKE = Persistence.createEntityManagerFactory("postgresClientDBMMKE");
		entityManagerFactoryMMUG = Persistence.createEntityManagerFactory("postgresClientDBMMUG");
	}

}
