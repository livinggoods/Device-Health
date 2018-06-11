/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.goods.living.tech.health.device.jpa.controllers;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goods.living.tech.health.device.jpa.controllers.exceptions.NonexistentEntityException;
import org.goods.living.tech.health.device.jpa.dao.Stats;
import org.goods.living.tech.health.device.jpa.dao.Users;

public class StatsJpaController implements Serializable {

	public StatsJpaController(EntityManagerFactory emf) {
		this.emf = emf;
	}
	Logger logger = LogManager.getLogger();


	private EntityManagerFactory emf = null;

	public EntityManager getEntityManager() {
		return emf.createEntityManager();
	}

	public void create(Stats stats) {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			Users userId = stats.getUserId();
			if (userId != null) {
				userId = em.getReference(userId.getClass(), userId.getId());
				stats.setUserId(userId);
			}
			em.persist(stats);
			if (userId != null) {
				userId.getStatsCollection().add(stats);
				userId = em.merge(userId);
			}
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void edit(Stats stats) throws NonexistentEntityException, Exception {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			Stats persistentStats = em.find(Stats.class, stats.getId());
			Users userIdOld = persistentStats.getUserId();
			Users userIdNew = stats.getUserId();
			if (userIdNew != null) {
				userIdNew = em.getReference(userIdNew.getClass(), userIdNew.getId());
				stats.setUserId(userIdNew);
			}
			stats = em.merge(stats);
			if (userIdOld != null && !userIdOld.equals(userIdNew)) {
				userIdOld.getStatsCollection().remove(stats);
				userIdOld = em.merge(userIdOld);
			}
			if (userIdNew != null && !userIdNew.equals(userIdOld)) {
				userIdNew.getStatsCollection().add(stats);
				userIdNew = em.merge(userIdNew);
			}
			em.getTransaction().commit();
		} catch (Exception ex) {
			String msg = ex.getLocalizedMessage();
			if (msg == null || msg.length() == 0) {
				Long id = stats.getId();
				if (findStats(id) == null) {
					throw new NonexistentEntityException("The stats with id " + id + " no longer exists.");
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
			Stats stats;
			try {
				stats = em.getReference(Stats.class, id);
				stats.getId();
			} catch (EntityNotFoundException enfe) {
				throw new NonexistentEntityException("The stats with id " + id + " no longer exists.", enfe);
			}
			Users userId = stats.getUserId();
			if (userId != null) {
				userId.getStatsCollection().remove(stats);
				userId = em.merge(userId);
			}
			em.remove(stats);
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public List<Stats> findStatsEntities() {
		return findStatsEntities(true, -1, -1);
	}

	public List<Stats> findStatsEntities(int maxResults, int firstResult) {
		return findStatsEntities(false, maxResults, firstResult);
	}

	private List<Stats> findStatsEntities(boolean all, int maxResults, int firstResult) {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			cq.select(cq.from(Stats.class));
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

	public Stats findStats(Long id) {
		EntityManager em = getEntityManager();
		try {
			return em.find(Stats.class, id);
		} finally {
			em.close();
		}
	}

	public int getStatsCount() {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			Root<Stats> rt = cq.from(Stats.class);
			cq.select(em.getCriteriaBuilder().count(rt));
			Query q = em.createQuery(cq);
			return ((Long) q.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}

	public List<Stats> fetchStats(String userId, Date from, Date to) {
		long fromDate=from.getTime();
		long toDate=to.getTime();
		EntityManager em = getEntityManager();
		try {
			// CriteriaBuilder cb = em.getCriteriaBuilder();
			// CriteriaQuery<Stats> q = cb.//createQuery(Stats.class);
			// Root<Stats> c = q.from(Stats.class);

			Query q = em.createQuery(
					"SELECT s from Stats s WHERE s.userId.id = :userId and s.recordedAt >= :fromDate and s.recordedAt <= :toDate");
			q.setParameter("userId", userId).setParameter("fromDate", fromDate).setParameter("toDate", toDate);
			q.getResultList();
			return q.getResultList();

		} finally {
			em.close();
		}
	}
}
