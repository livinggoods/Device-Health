/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.goods.living.tech.health.device.jpa.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goods.living.tech.health.device.jpa.dao.MedicUser;

/**
 *
 * @author bensonbundi
 */
public class MedicJpaController implements Serializable {

	Logger logger = LogManager.getLogger();

	public MedicJpaController(EntityManagerFactory emf) {
		this.emf = emf;
	}

	private EntityManagerFactory emf = null;

	public EntityManager getEntityManager() {
		return emf.createEntityManager();
	}

	public List<MedicUser> findByNameLike(String username) {

		EntityManager em = getEntityManager();
		try {
			// CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			// Root<Users> rt = cq.from(Users.class);
			// cq.select(em.getCriteriaBuilder().count(rt));
			// Query q = em.createQuery(cq);

			List<Object[]> list = em.createNativeQuery("SELECT "
					+ "doc->>'name' AS username, doc->>'contact_id' contact_id, chw_name , branch_name, chw_phone "
					+ "FROM couchdb left join contactview_hierarchy ch on ch.chw_uuid=doc->>'contact_id' WHERE doc->>'type' = 'user-settings' "
					+ "and chw_name like :name").setParameter("name", "%" + username + "%").getResultList();
			List<MedicUser> l = new ArrayList<>();
			for (Object[] o : list) {
				MedicUser mu = new MedicUser();
				mu.setUsername((String) o[0]);
				mu.setUuid((String) o[1]);
				mu.setName((String) o[2]);
				mu.setBranch((String) o[3]);
				mu.setPhone((String) o[4]);
				l.add(mu);
			}

			return l;
		} finally {
			em.close();
		}

	}

}
