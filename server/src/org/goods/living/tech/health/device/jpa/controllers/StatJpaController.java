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
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.goods.living.tech.health.device.jpa.controllers.exceptions.NonexistentEntityException;
import org.goods.living.tech.health.device.jpa.dao.Stat;
import org.goods.living.tech.health.device.jpa.dao.User;

/**
 *
 * @author bensonbundi
 */
public class StatJpaController implements Serializable {

    public StatJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Stat stat) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            User userId = stat.getUserId();
            if (userId != null) {
                userId = em.getReference(userId.getClass(), userId.getId());
                stat.setUserId(userId);
            }
            em.persist(stat);
            if (userId != null) {
                userId.getStatCollection().add(stat);
                userId = em.merge(userId);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Stat stat) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Stat persistentStat = em.find(Stat.class, stat.getId());
            User userIdOld = persistentStat.getUserId();
            User userIdNew = stat.getUserId();
            if (userIdNew != null) {
                userIdNew = em.getReference(userIdNew.getClass(), userIdNew.getId());
                stat.setUserId(userIdNew);
            }
            stat = em.merge(stat);
            if (userIdOld != null && !userIdOld.equals(userIdNew)) {
                userIdOld.getStatCollection().remove(stat);
                userIdOld = em.merge(userIdOld);
            }
            if (userIdNew != null && !userIdNew.equals(userIdOld)) {
                userIdNew.getStatCollection().add(stat);
                userIdNew = em.merge(userIdNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = stat.getId();
                if (findStat(id) == null) {
                    throw new NonexistentEntityException("The stat with id " + id + " no longer exists.");
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
            Stat stat;
            try {
                stat = em.getReference(Stat.class, id);
                stat.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The stat with id " + id + " no longer exists.", enfe);
            }
            User userId = stat.getUserId();
            if (userId != null) {
                userId.getStatCollection().remove(stat);
                userId = em.merge(userId);
            }
            em.remove(stat);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Stat> findStatEntities() {
        return findStatEntities(true, -1, -1);
    }

    public List<Stat> findStatEntities(int maxResults, int firstResult) {
        return findStatEntities(false, maxResults, firstResult);
    }

    private List<Stat> findStatEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Stat.class));
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

    public Stat findStat(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Stat.class, id);
        } finally {
            em.close();
        }
    }

    public int getStatCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Stat> rt = cq.from(Stat.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
