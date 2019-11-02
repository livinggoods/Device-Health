package org.goods.living.tech.health.device.jpa.controllers;
/**
*
* @author patricia
*/
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.goods.living.tech.health.device.jpa.dao.Chw;

public class ChwJpaController implements Serializable{
	
	public ChwJpaController(EntityManagerFactory emf) {
		this.emf = emf;
	}

	private EntityManagerFactory emf = null;

	public EntityManager getEntityManager() {
		return emf.createEntityManager();
	}
	
	public Chw findByUserName(String username) {
		EntityManager em = getEntityManager();
		try {
			List<Chw> list = em.createNamedQuery("Chw.findByUsername").setParameter("username", username).getResultList();
			System.out.print("The user is found and " + list.toString());
			return list.size() > 0 ? list.get(0) : null;
		} finally {
			em.close();
		}

	}
}
