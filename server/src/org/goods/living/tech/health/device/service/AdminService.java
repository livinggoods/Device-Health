package org.goods.living.tech.health.device.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.codehaus.jackson.JsonNode;
import org.goods.living.tech.health.device.jpa.controllers.AdminUsersJpaController;
import org.goods.living.tech.health.device.jpa.dao.AdminUsers;
import org.goods.living.tech.health.device.utility.Constants;
import org.goods.living.tech.health.device.utility.JSonHelper;
import org.json.simple.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


@Path(Constants.URL.ADMIN)
@Named
@RequestScoped
public class AdminService extends BaseService {


	SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

	@Inject
	AdminUsersJpaController adminUsersJpaController;

	Integer DEFAULT_UPDATE_INTERVAL = 300;

	public AdminService() {
	}
        
        @POST
	@Consumes("application/json")
	@Produces("application/json")
	@Path(Constants.URL.LOGIN)
	public JSONObject login(InputStream incomingData) throws Exception {

			JsonNode data = JSonHelper.getJsonNode(incomingData);
			String email = data.has("email") ? data.get("email").asText() : null;
			String password = data.has("password") ? data.get("password").asText() : null;
			JSONObject response = new JSONObject();
			if (email != null && password != null) {
//			retrieve user from db
			AdminUsers user;
			try{
				 user= adminUsersJpaController.find(email);
			}
			catch (Exception e){
				user=null;
				e.printStackTrace();
			}


			if(user!=null && BCrypt.checkpw(password, user.getPassword())){
				String token = getJWT(email, password);
				response.put("status", "200");
				response.put("token", token);
				response.put("Message", "Login Successful");

			}else {
				response.put("status", "404"); //Add meaningful application codes
				response.put("Message", "Login not Successful");

			}
		} else {
				response.put("status", "404"); //Add meaningful application codes
				response.put("Message", "Login not Successful");

		}
		return response;

	}
        
        private String getJWT(String email, String password) {
			long tokenLife;
			try {
				tokenLife = Integer.parseInt(applicationParameters.getTokenLife()) * 1000;
			} catch (NumberFormatException nfe) {
				tokenLife = 3600 * 1000;
			}

			long nowMillis = System.currentTimeMillis();

			long expMillis = nowMillis + tokenLife;
			Date expireDate = new Date(expMillis);

			System.out.println(expireDate.toString());

			return Jwts.builder().setSubject(email).setId("Unique_ID")
					.claim("roles", "ADMIN").claim("first name", "firstName")
					.claim("site", "site").setIssuedAt(new Date())
					.signWith(SignatureAlgorithm.HS256, applicationParameters.getHashKey()).setExpiration(expireDate).compact();

	}
}