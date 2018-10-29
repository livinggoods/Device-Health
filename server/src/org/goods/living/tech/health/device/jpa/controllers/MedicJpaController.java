/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.goods.living.tech.health.device.jpa.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goods.living.tech.health.device.jpa.dao.ChvActivity;
import org.goods.living.tech.health.device.jpa.dao.MedicUser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author bensonbundi
 */
public class MedicJpaController implements Serializable {

	Logger logger = LogManager.getLogger();
	private EntityManagerFactory emf = null;

	public static String EM_KE = "KE";
	public static String EM_UG = "UG";

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

	public EntityManager getEntityManager(String db) {
		if (db != null && db.toUpperCase().equals(EM_KE)) {
			return getEntityManagerKE();
		}
		return getEntityManagerUG();
	}

	public List<MedicUser> findByNameLike(String db, String username) {
		EntityManager em = getEntityManager(db);
		try {
			List<Object[]> list = em.createNativeQuery("SELECT "
					+ "doc->>'name' AS username, doc->>'contact_id' contact_id, chw_name , branch_name, chw_phone "
					+ "FROM couchdb left join contactview_hierarchy ch on ch.chw_uuid=doc->>'contact_id' WHERE doc->>'type' = 'user-settings' "
					+ "and chw_name ilike :name").setParameter("name", "%" + username + "%").getResultList();
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

			// CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			// Root<Users> rt = cq.from(Users.class);
			// cq.select(em.getCriteriaBuilder().count(rt));
			// Query q = em.createQuery(cq);

			return l;
		} catch (Exception e) {
			logger.error(e);
			return null;
		} finally {
			em.close();
		}

	}

	public MedicUser findByUsername(String db, String username) {

		EntityManager em = getEntityManager(db);
		try {

			Object[] o = (Object[]) em.createNativeQuery("select * from (\n"
					+ "	SELECT doc->>'name' AS username, doc->>'contact_id' contact_id, chw_name , branch_name, chw_phone ,false as supervisor\n"
					+ "FROM couchdb left join contactview_hierarchy ch on ch.chw_uuid=doc->>'contact_id'  WHERE doc->>'type' = 'user-settings'\n"
					+ "union (\n" + "	SELECT \n" + "    form.doc->>'name' AS username, \n"
					+ "    form.doc->>'contact_id' contact_id, \n" + "    cmeta.name,\n"
					+ "    branch.\"name\" branch_name,\n" + "    branch.uuid,\n" + "	true as supervisor\n"
					+ "    FROM  \n" + "    contactview_branch branch\n"
					+ "    INNER JOIN contactview_metadata cmeta ON (cmeta.parent_uuid = branch.uuid)\n"
					+ "    LEFT  JOIN couchdb form  on cmeta.uuid=form.doc->>'contact_id'\n"
					+ "    WHERE doc->>'type' = 'user-settings'\n" + "	 )\n" + ") as tab\n"
					+ "	 where  username ILIKE :name").setParameter("name", "" + username + "").getSingleResult();
			// List<MedicUser> l = new ArrayList<>();
			// for (Object[] o : list) {
			MedicUser mu = new MedicUser();
			mu.setUsername((String) o[0]);
			mu.setUuid((String) o[1]);
			mu.setName((String) o[2]);
			mu.setBranch((String) o[3]);
			mu.setPhone((String) o[4]);
			mu.setSupervisor((Boolean) o[5]);
			// l.add(mu);
			// }
			return mu;
		} catch (NoResultException e) {
			logger.error(e);
			return new MedicUser();
		} catch (Exception e) {
			logger.error(e);
			return null;
		} finally {
			em.close();
		}

	}

	public List<ChvActivity> findChvActivities(String db, String chvId, Date from, Date to) {

		EntityManager em = getEntityManager(db);
		try {

			logger.info("starting query execution");
			long dateFrom = from.getTime() / 1000;
			long dateTo = to.getTime() / 1000;

			List<Object[]> queryResult = em.createNativeQuery(
					"SELECT cast(uuid as text) as recordId, cast(chw as text) as chwId,  formname as formname, extract(epoch from reported) as reported, cast(doc as text) as jsonRecord from (SELECT * from form_metadata fm where chw = :chvId and extract(epoch from reported) between :fromDate and :toDate) as a inner join couchdb cb on (a.uuid = cb.doc->>'_id')")
					.setParameter("chvId", chvId).setParameter("fromDate", dateFrom).setParameter("toDate", dateTo)
					.getResultList();

			List<ChvActivity> activities = new ArrayList<>();
			for (Object[] object : queryResult) {
				ChvActivity activity = new ChvActivity();
				JSONParser parser = new JSONParser();
				Object obj = null;
				try {
					obj = parser.parse((String) object[4]);
				} catch (ParseException e) {

				}
				JSONObject chvActivty = (JSONObject) obj;
				JSONObject fields = (JSONObject) chvActivty.get("fields");
				JSONObject inputs = (JSONObject) fields.get("inputs");
				JSONObject contact = (JSONObject) inputs.get("contact");
				JSONObject inputMetadata = (JSONObject) inputs.get("meta");
				JSONObject coordinates = (JSONObject) inputMetadata.get("location");
				String clientName = null;
				try {
					clientName = (String) contact.get("name");
				} catch (Exception e) {
					e.printStackTrace();
				}

				activity.setActivityType((String) object[2]);
				activity.setActivityId((String) object[0]);
				activity.setClientName(clientName);
				activity.setMedicCoordinates(coordinates);

				activity.setChvUuid((String) object[1]);
				Date date = new Date(((Double) object[3]).longValue() * 1000);

				activity.setReportedDate(date);
				activities.add(activity);
			}
			return activities;

		} catch (Exception e) {
			logger.error(e);
			return null;
		} finally {

		}

	}

}
