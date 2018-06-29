/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.goods.living.tech.health.device.jpa.controllers;

import org.codehaus.jackson.map.ObjectMapper;
import org.goods.living.tech.health.device.jpa.dao.AdminUsers;
import org.hibernate.annotations.NamedNativeQuery;
import org.mindrot.jbcrypt.BCrypt;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
}
