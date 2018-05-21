package org.goods.living.tech.health.device.jpa;

public enum PersistenceUnitEnum {

	/*
	 * POSTGRES_MANAGEMENT is the COPD Web management database
	 * 
	 * POSTGRES_DATABASE_USER is the RedCap database
	 * 
	 * both databases will be independent of each other but can be accessed from the
	 * same web application
	 */

	POSTGRES_DEVICE_HEALTH, POSTGRES_DATABASE_USER
}
