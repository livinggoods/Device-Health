/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.goods.living.tech.health.device.jpa.controllers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goods.living.tech.health.device.jpa.dao.ChvActivity;
import org.goods.living.tech.health.device.jpa.dao.MedicUser;

/**
 * @author bensonbundi
 */
public class MedicJpaController implements Serializable {

	Logger logger = LogManager.getLogger();
	private EntityManagerFactory emf = null;

	public MedicJpaController(EntityManagerFactory emfKE, EntityManagerFactory emfUG) {
		this.emfKE = emfKE;
		this.emfUG = emfUG;
	}

	EntityManagerFactory emfUG = null;
	EntityManagerFactory emfKE = null;

	public EntityManager getEntityManagerUG() {
		return emfUG.createEntityManager();
	}

	public EntityManager getEntityManagerKE() {
		return emfKE.createEntityManager();
	}

	public List<MedicUser> findByNameLike(String username) {

		List<Object[]> list = this.getEntityManagerUG().createNativeQuery("SELECT "
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
		EntityManager em = getEntityManagerUG();
		try {
			// CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			// Root<Users> rt = cq.from(Users.class);
			// cq.select(em.getCriteriaBuilder().count(rt));
			// Query q = em.createQuery(cq);

			return l;
		} finally {
			em.close();
		}

	}

	public MedicUser findByUsername(String username) {

		EntityManager em = getEntityManagerUG();
		try {
			Object[] o = (Object[]) this.getEntityManagerUG().createNativeQuery("SELECT "
					+ "doc->>'name' AS username, doc->>'contact_id' contact_id, chw_name , branch_name, chw_phone "
					+ "FROM couchdb left join contactview_hierarchy ch on ch.chw_uuid=doc->>'contact_id' WHERE doc->>'type' = 'user-settings' "
					+ "and doc->>'name' ILIKE :name").setParameter("name", "" + username + "").getSingleResult();
			// List<MedicUser> l = new ArrayList<>();
			// for (Object[] o : list) {
			MedicUser mu = new MedicUser();
			mu.setUsername((String) o[0]);
			mu.setUuid((String) o[1]);
			mu.setName((String) o[2]);
			mu.setBranch((String) o[3]);
			mu.setPhone((String) o[4]);
			// l.add(mu);
			// }

			return mu;
		} finally {
			em.close();
		}

	}

	public List<ChvActivity> findChvActivities(String uuid, Date from, Date to) {
		long dateFrom = from.getTime()/1000;
		long dateTo = to.getTime()/1000;
		EntityManager em = getEntityManagerUG();
		logger.debug("date From: "+ dateFrom);
		logger.debug("date To: "+ dateTo);
		System.out.println(dateFrom);

		try {
//            List<Object[]> queryResult = em.createNativeQuery("SELECT doc->'contact'->>'name'  FROM couchdb  WHERE doc->'contact'->>'_id' = :userId AND cast(doc->>'reported_date' as float) " +
//                    "BETWEEN :dateFrom AND :dateTo ").setParameter("userId", uuid)
//                    .setParameter("dateFrom", dateFrom).setParameter("dateTo", dateTo).getResultList();
			logger.info("starting query execution");
//            List<Object[]> queryResult = em.createNativeQuery("SELECT doc->'contact'->>'name' AS chv_name, " +
//                    "doc->>'form' as activity, doc->'fields'->'inputs'->'contact'->>'name' as client ,cast(doc->>'reported_date' AS float) " +
//                    "AS reported_date FROM couchdb WHERE doc #>>'{contact,_id}' = :userId AND cast(doc->>'reported_date' as float) " +
//                    "BETWEEN :dateFrom AND :dateTo LIMIT 3").setParameter("userId", uuid)
//                    .setParameter("dateFrom", dateFrom).setParameter("dateTo", dateTo).getResultList();

//            List<Object[]> queryResult = em.createNativeQuery("SELECT doc->'contact'->>'name' AS chv_name, doc->>'form' as activity, doc->'fields'->'inputs'->'contact'->>'name' as client ,doc->>'reported_date'" +
//                    " AS reported_date FROM couchdb  WHERE doc #>>'{contact,_id}' = :uuid AND cast(doc->>'reported_date' as float) BETWEEN :fromDate AND :toDate LIMIT 6")
//                    .setParameter("uuid", uuid).setParameter("fromDate", dateFrom).setParameter("toDate", dateTo).getResultList();

			List<Object[]> queryResult = em.createNativeQuery("SELECT * from (SELECT doc->'contact'->>'name' AS " +
					"chv_name, doc->>'form' as activity, doc->'fields'->'inputs'->'contact'->>'name' as client, cast(LEFT (doc->>'reported_date' ,-3) as numeric) as reported_date " +
					"FROM couchdb  WHERE doc #>>'{contact,_id}' = :uuid) as a where reported_date > :fromDate and reported_date < :toDate")
					.setMaxResults(70).setParameter("uuid", uuid).setParameter("fromDate", 1498744631)
					.setParameter("toDate", 1509744631).getResultList();



			List<ChvActivity> activities = new ArrayList<>();
			for (Object[] object : queryResult) {
				String reportedDate=(String)object[3];
				ChvActivity activity = new ChvActivity();
				activity.setActivityType((String) object[1]);
				activity.setContactPerson((String) object[2]);
				activity.setUuid(uuid);
//                activity.setReportedDate(Long.parseLong((Long)object[3]));
				activities.add(activity);
			}
			return activities;

		} finally {

		}

	}

}

