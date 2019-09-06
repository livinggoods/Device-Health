package org.goods.living.tech.health.device.jpa.controllers;

/**
 *
 * @author Chebet
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.goods.living.tech.health.device.jpa.dao.Chw;
import org.goods.living.tech.health.device.jpa.dao.Stats;
import org.goods.living.tech.health.device.jpa.dao.Users;

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
			// CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			// Root<Users> rt = cq.from(Users.class);
			// cq.select(em.getCriteriaBuilder().count(rt));
			// Query q = em.createQuery(cq);

			List<Chw> list = em.createNamedQuery("Chw.findByUsername").setParameter("username", username)
					.getResultList();

			return list.size() > 0 ? list.get(0) : null;
		} finally {
			em.close();
		}

	}
}
