package org.goods.living.tech.health.device.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//import org.goods.living.tech.health.device.jpa.qualifier.PersistenceUnitQualifier;
//import org.goods.living.tech.health.device.jpa.utility.PersistenceUnitEnum;
//import org.goods.living.tech.health.device.service.security.qualifier.Secured;
import org.goods.living.tech.health.device.utility.ApplicationParameters;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/new")
@Named
@RequestScoped
public class NewService extends BaseService {

	// @Inject
	// @PersistenceUnitQualifier(PersistenceUnitEnum.POSTGRES_MANAGEMENT)
	// private EntityManagerFactory copd_management_emf;

	@Inject
	private ApplicationParameters applicationParameters;

	private String serviceMessage = "This is the Test service";

	public NewService() {
	}

	// http://localhost:8080/appname/rest/test/about
	@GET
	@Path("/about")
	public Response about() {

		String result = "{ \"result\":" + "\"" + serviceMessage + " " + applicationParameters.getHashKey() + "\" }";
		System.out.println(result);

		return Response.status(200).entity(result).build();
	}

	// http://localhost:8080/appname/rest/test/dataUpload

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/dataUpload")
	public Response dataUpload(InputStream incomingData) {
		StringBuilder jsonElements = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsonElements.append(line);
			}
		} catch (IOException e) {
			System.out.println("Error Parsing: - " + e);
		}

		System.out.println("Data Received: " + jsonElements.toString());

		JSONObject jObject;
		try {
			jObject = new JSONObject(jsonElements.toString());

			Iterator<?> keys = jObject.keys();

			while (keys.hasNext()) {
				String key = (String) keys.next();
				String value = jObject.getString(key);
				System.out.println("key: " + key + "\t value: " + value);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String result = "{ \"result\":" + "\"" + jsonElements.toString() + "\" }";
		System.out.println(result);

		return Response.status(200).entity(result).build();
	}

	// @Secured
	// @GET
	// @Path("/secureAbout")
	// public Response secureAbout() {
	// String result = "{ \"result\":" + "\"" + serviceMessage + "\" }";
	// System.out.println(result);
	//
	// return Response.status(200).entity(result).build();
	// }
}