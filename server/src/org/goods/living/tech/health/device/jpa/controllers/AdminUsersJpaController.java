/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.goods.living.tech.health.device.jpa.controllers;

import org.goods.living.tech.health.device.jpa.dao.AdminUsers;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.io.Serializable;
import java.math.BigInteger;


public class AdminUsersJpaController implements Serializable {

    EntityManagerFactory emf = null;

    public AdminUsersJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }


    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }


    public AdminUsers find(String email) {
        EntityManager em = getEntityManager();
//        String password=BCrypt.hashpw("password",BCrypt.gensalt());


        try {
            Object[] obj = (Object[])em.createNativeQuery("SELECT * from admin_users where email = :email")
                    .setParameter("email", email)
                    .getSingleResult();
            AdminUsers user= new AdminUsers();
            user.setEmail((String)obj[2]);
            user.setPassword((String)obj[3]);
            user.setId(((BigInteger)obj[0]).longValue());

            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean update(AdminUsers userObject) {
        EntityManager em = getEntityManager();
        AdminUsers user = em.find(AdminUsers.class, userObject.getId());
        em.getTransaction().begin();
        try {
            user.setForgotToken(userObject.getForgotToken());
            em.merge(user);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
    public AdminUsers findByEmail(String email) {
        EntityManager em = getEntityManager();
        AdminUsers user = (AdminUsers) em.createNamedQuery("AdminUsers.findByEmail").setParameter("email", email).getSingleResult();
        return user;
    }
    public AdminUsers findByToken(String token) {
        EntityManager em = getEntityManager();
        AdminUsers user = (AdminUsers) em.createNamedQuery("AdminUsers.findByForgotToken").setParameter("forgotToken", token).getSingleResult();
        return user;
    }
    public void invalidateToken(AdminUsers user) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        try{
            AdminUsers adminUser = em.find(AdminUsers.class, user.getId());
            user.setForgotToken(null);
            em.merge(user);
            em.getTransaction().commit();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    public void updatePassword(AdminUsers user) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        try{
            AdminUsers adminUser = em.find(AdminUsers.class, user.getId());
            adminUser.setPassword(user.getPassword());
            em.merge(user);
            em.getTransaction().commit();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
