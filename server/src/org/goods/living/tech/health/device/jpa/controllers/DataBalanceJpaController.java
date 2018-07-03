/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.goods.living.tech.health.device.jpa.controllers;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.goods.living.tech.health.device.jpa.controllers.exceptions.NonexistentEntityException;
import org.goods.living.tech.health.device.jpa.dao.DataBalance;
import org.goods.living.tech.health.device.jpa.dao.Users;

/**
 *
 * @author bensonbundi
 */
public class DataBalanceJpaController implements Serializable {

	public DataBalanceJpaController(EntityManagerFactory emf) {
		this.emf = emf;
	}

	private EntityManagerFactory emf = null;

	public EntityManager getEntityManager() {
		return emf.createEntityManager();
	}

	public void create(DataBalance dataBalance) {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			Users userId = dataBalance.getUserId();
			if (userId != null) {
				userId = em.getReference(userId.getClass(), userId.getId());
				dataBalance.setUserId(userId);
			}
			em.persist(dataBalance);
			if (userId != null) {
				userId.getDataBalanceCollection().add(dataBalance);
				userId = em.merge(userId);
			}
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void edit(DataBalance dataBalance) throws NonexistentEntityException, Exception {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			DataBalance persistentDataBalance = em.find(DataBalance.class, dataBalance.getId());
			Users userIdOld = persistentDataBalance.getUserId();
			Users userIdNew = dataBalance.getUserId();
			if (userIdNew != null) {
				userIdNew = em.getReference(userIdNew.getClass(), userIdNew.getId());
				dataBalance.setUserId(userIdNew);
			}
			dataBalance = em.merge(dataBalance);
			if (userIdOld != null && !userIdOld.equals(userIdNew)) {
				userIdOld.getDataBalanceCollection().remove(dataBalance);
				userIdOld = em.merge(userIdOld);
			}
			if (userIdNew != null && !userIdNew.equals(userIdOld)) {
				userIdNew.getDataBalanceCollection().add(dataBalance);
				userIdNew = em.merge(userIdNew);
			}
			em.getTransaction().commit();
		} catch (Exception ex) {
			String msg = ex.getLocalizedMessage();
			if (msg == null || msg.length() == 0) {
				Long id = dataBalance.getId();
				if (findDataBalance(id) == null) {
					throw new NonexistentEntityException("The dataBalance with id " + id + " no longer exists.");
				}
			}
			throw ex;
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void destroy(Long id) throws NonexistentEntityException {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			DataBalance dataBalance;
			try {
				dataBalance = em.getReference(DataBalance.class, id);
				dataBalance.getId();
			} catch (EntityNotFoundException enfe) {
				throw new NonexistentEntityException("The dataBalance with id " + id + " no longer exists.", enfe);
			}
			Users userId = dataBalance.getUserId();
			if (userId != null) {
				userId.getDataBalanceCollection().remove(dataBalance);
				userId = em.merge(userId);
			}
			em.remove(dataBalance);
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public List<DataBalance> findDataBalanceEntities() {
		return findDataBalanceEntities(true, -1, -1);
	}

	public List<DataBalance> findDataBalanceEntities(int maxResults, int firstResult) {
		return findDataBalanceEntities(false, maxResults, firstResult);
	}

	private List<DataBalance> findDataBalanceEntities(boolean all, int maxResults, int firstResult) {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			cq.select(cq.from(DataBalance.class));
			Query q = em.createQuery(cq);
			if (!all) {
				q.setMaxResults(maxResults);
				q.setFirstResult(firstResult);
			}
			return q.getResultList();
		} finally {
			em.close();
		}
	}

	public DataBalance findDataBalance(Long id) {
		EntityManager em = getEntityManager();
		try {
			return em.find(DataBalance.class, id);
		} finally {
			em.close();
		}
	}

	public int getDataBalanceCount() {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			Root<DataBalance> rt = cq.from(DataBalance.class);
			cq.select(em.getCriteriaBuilder().count(rt));
			Query q = em.createQuery(cq);
			return ((Long) q.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}

}
