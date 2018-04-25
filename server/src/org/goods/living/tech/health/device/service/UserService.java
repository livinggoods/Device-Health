package org.goods.living.tech.health.device.service;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import javax.ws.rs.core.Response.Status;

//import org.goods.living.tech.health.device.jpa.qualifier.PersistenceUnitQualifier;
//import org.goods.living.tech.health.device.jpa.utility.PersistenceUnitEnum;
//import org.goods.living.tech.health.device.service.security.qualifier.Secured;
import org.goods.living.tech.health.device.utility.ApplicationParameters;
import org.goods.living.tech.health.device.utility.JSonHelper;
import org.json.JSONException;
import org.json.JSONObject;


//https://dzone.com/articles/lets-compare-jax-rs-vs-spring-for-rest-endpoints

@Path("/user")
@Named
@RequestScoped
public class UserService  extends BaseService{

//	@Inject
//	@PersistenceUnitQualifier(PersistenceUnitEnum.POSTGRES_MANAGEMENT)
//	private EntityManagerFactory copd_management_emf;
	
	//@Inject
	//private ApplicationParameters applicationParameters;


	public UserService() {
	}

	// http://localhost:8080/appname/rest/user/add
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/add")
	public Response add(InputStream incomingData) {
		try {
			JSONObject jObject = JSonHelper.getObject(incomingData);

			return Response.status(200).entity(jObject).build();
			
		} catch (Exception e) {
			
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("").build();
		}

	}
	
	@POST
	@Path("/get")
	@Produces("application/json")
	public JSONObject get(InputStream incomingData) { //@PathVariable("id") String id
		try {
			JSONObject jObject = JSonHelper.getObject(incomingData);

			return jObject;
			
		} catch (Exception e) {
			logger.error("Error",e);
			return null;
		}

	}

	@GET
	//@Produces("application/json")
	public JSONObject index(InputStream incomingData) { //@PathVariable("id") String id
		try {
			logger.debug("index hit");
			JSONObject jObject = JSonHelper.getObject(incomingData);

			return jObject;
			
		} catch (Exception e) {
			logger.error("Error",e);
			return null;
		}

	}
//	@Secured
//	@GET
//	@Path("/secureAbout")
//	public Response secureAbout() {
//		String result = "{ \"result\":" + "\"" + serviceMessage + "\" }";
//		System.out.println(result);
//
//		return Response.status(200).entity(result).build();
//	}
}