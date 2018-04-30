/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.goods.living.tech.health.device.jpa.controllers;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.goods.living.tech.health.device.jpa.dao.Stat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.goods.living.tech.health.device.jpa.controllers.exceptions.IllegalOrphanException;
import org.goods.living.tech.health.device.jpa.controllers.exceptions.NonexistentEntityException;
import org.goods.living.tech.health.device.jpa.dao.User;

/**
 *
 * @author bensonbundi
 */
public class UserJpaController implements Serializable {

    public UserJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(User user) {
        if (user.getStatCollection() == null) {
            user.setStatCollection(new ArrayList<Stat>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Stat> attachedStatCollection = new ArrayList<Stat>();
            for (Stat statCollectionStatToAttach : user.getStatCollection()) {
                statCollectionStatToAttach = em.getReference(statCollectionStatToAttach.getClass(), statCollectionStatToAttach.getId());
                attachedStatCollection.add(statCollectionStatToAttach);
            }
            user.setStatCollection(attachedStatCollection);
            em.persist(user);
            for (Stat statCollectionStat : user.getStatCollection()) {
                User oldUserIdOfStatCollectionStat = statCollectionStat.getUserId();
                statCollectionStat.setUserId(user);
                statCollectionStat = em.merge(statCollectionStat);
                if (oldUserIdOfStatCollectionStat != null) {
                    oldUserIdOfStatCollectionStat.getStatCollection().remove(statCollectionStat);
                    oldUserIdOfStatCollectionStat = em.merge(oldUserIdOfStatCollectionStat);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(User user) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            User persistentUser = em.find(User.class, user.getId());
            Collection<Stat> statCollectionOld = persistentUser.getStatCollection();
            Collection<Stat> statCollectionNew = user.getStatCollection();
            List<String> illegalOrphanMessages = null;
            for (Stat statCollectionOldStat : statCollectionOld) {
                if (!statCollectionNew.contains(statCollectionOldStat)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Stat " + statCollectionOldStat + " since its userId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Stat> attachedStatCollectionNew = new ArrayList<Stat>();
            for (Stat statCollectionNewStatToAttach : statCollectionNew) {
                statCollectionNewStatToAttach = em.getReference(statCollectionNewStatToAttach.getClass(), statCollectionNewStatToAttach.getId());
                attachedStatCollectionNew.add(statCollectionNewStatToAttach);
            }
            statCollectionNew = attachedStatCollectionNew;
            user.setStatCollection(statCollectionNew);
            user = em.merge(user);
            for (Stat statCollectionNewStat : statCollectionNew) {
                if (!statCollectionOld.contains(statCollectionNewStat)) {
                    User oldUserIdOfStatCollectionNewStat = statCollectionNewStat.getUserId();
                    statCollectionNewStat.setUserId(user);
                    statCollectionNewStat = em.merge(statCollectionNewStat);
                    if (oldUserIdOfStatCollectionNewStat != null && !oldUserIdOfStatCollectionNewStat.equals(user)) {
                        oldUserIdOfStatCollectionNewStat.getStatCollection().remove(statCollectionNewStat);
                        oldUserIdOfStatCollectionNewStat = em.merge(oldUserIdOfStatCollectionNewStat);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = user.getId();
                if (findUser(id) == null) {
                    throw new NonexistentEntityException("The user with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Long id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            User user;
            try {
                user = em.getReference(User.class, id);
                user.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The user with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Stat> statCollectionOrphanCheck = user.getStatCollection();
            for (Stat statCollectionOrphanCheckStat : statCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This User (" + user + ") cannot be destroyed since the Stat " + statCollectionOrphanCheckStat + " in its statCollection field has a non-nullable userId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(user);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<User> findUserEntities() {
        return findUserEntities(true, -1, -1);
    }

    public List<User> findUserEntities(int maxResults, int firstResult) {
        return findUserEntities(false, maxResults, firstResult);
    }

    private List<User> findUserEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(User.class));
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

    public User findUser(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }

    public int getUserCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<User> rt = cq.from(User.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
