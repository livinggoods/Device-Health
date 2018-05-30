package org.goods.living.tech.health.device.jpa;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Named
@ApplicationScoped
public class DatabaseManager {

	/**
	 * The factory that produces entity manager.
	 */

	@PersistenceUnitQualifier(PersistenceUnitEnum.POSTGRES_DEVICE_HEALTH)
	@PersistenceContext(unitName = "postgresClientDBDeviceHealth")
	EntityManagerFactory entityManagerFactoryDH;

	@PersistenceUnitQualifier(PersistenceUnitEnum.POSTGRES_MM)
	@PersistenceContext(unitName = "postgresClientDBMM")
	EntityManagerFactory entityManagerFactoryMM;

	Logger logger = LogManager.getLogger();

	public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
		logger.info("DatabaseManager was succesfully initialized on start-up!");
	}

	public DatabaseManager() {
		logger.info("DatabaseManager constructor is called ...!");
		// entityManagerFactory = Persistence.createEntityManagerFactory("fhsc_PU");
		try {
			// this.setUp();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	// @Produces
	// @PersistenceUnitQualifier(PersistenceUnitEnum.POSTGRES_DEVICE_HEALTH)
	// public EntityManagerFactory getEntityManagerFactoryDH() {
	// logger.info("getEntityManagerFactoryDH() is called ...!");
	// return entityManagerFactoryDH;
	// }
	//
	// @Produces
	// @PersistenceUnitQualifier(PersistenceUnitEnum.POSTGRES_MM)
	// public EntityManagerFactory getEntityManagerFactoryMM() {
	// logger.info("getEntityManagerFactoryMM() is called ...!");
	// return entityManagerFactoryMM;
	// }

	// private void setUp() throws Exception {
	// entityManagerFactoryDH =
	// Persistence.createEntityManagerFactory("postgresClientDBDeviceHealth");
	// entityManagerFactoryMM =
	// Persistence.createEntityManagerFactory("postgresClientDBMM");
	// }

}