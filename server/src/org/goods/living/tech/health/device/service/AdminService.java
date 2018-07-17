package org.goods.living.tech.health.device.service;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.HtmlEmail;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.codehaus.jackson.JsonNode;
import org.goods.living.tech.health.device.jpa.controllers.AdminUsersJpaController;
import org.goods.living.tech.health.device.jpa.dao.AdminUsers;
import org.goods.living.tech.health.device.service.security.qualifier.Secured;
import org.goods.living.tech.health.device.service.security.qualifier.UserCategory;
import org.goods.living.tech.health.device.utility.Constants;
import org.goods.living.tech.health.device.utility.JSonHelper;
import org.json.simple.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Path(Constants.URL.ADMIN)
@Named
@RequestScoped
public class AdminService extends BaseService {

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
			// retrieve user from db
			AdminUsers user;
			try {
				user = adminUsersJpaController.find(email);
			} catch (Exception e) {
				user = null;
				e.printStackTrace();
			}

			if (user != null && BCrypt.checkpw(password, user.getPassword())) {
				String token = getJWT(user);

				response.put("status", "200");
				response.put("token", token);
				response.put("Message", "Login Successful");

			} else {
				response.put("status", "404"); // Add meaningful application codes
				response.put("Message", "Login not Successful");

			}
		} else {
			response.put("status", "404"); // Add meaningful application codes
			response.put("Message", "Login not Successful");

		}
		return response;

	}

	@Secured(value = UserCategory.ADMIN)
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	@Path(Constants.URL.NEWACC)
	public JSONObject create(InputStream incomingData) throws Exception {

		JsonNode data = JSonHelper.getJsonNode(incomingData);
		String email = data.has("email") ? data.get("email").asText() : null;
		String password = data.has("password") ? data.get("password").asText() : null;
		String name = data.has("name") ? data.get("name").asText() : null;
		JSONObject response = new JSONObject();
		if (email != null && password != null && name != null) {
			AdminUsers admin = new AdminUsers();
			admin.setName(name);
			admin.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
			admin.setCreatedAt(new Timestamp(System.currentTimeMillis()));
			admin.setEmail(email);
			boolean status = adminUsersJpaController.create(admin);

			if (status == true) {
				response.put("status", "200");
				response.put("Message", "Registration Successful");
			}

		} else {
			response.put("status", "404"); // Add meaningful application codes
			response.put("Message", "Registration not Successful");

		}
		return response;

	}

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	@Path(Constants.URL.PASSWORDRESET)
	public JSONObject resetPassword(InputStream incomingData) throws Exception {

		JsonNode data = JSonHelper.getJsonNode(incomingData);
		String email = data.has("email") ? data.get("email").asText() : null;
		JSONObject response = new JSONObject();
		if (email != null) {
			// retrieve user from db
			AdminUsers user;
			try {
				user = adminUsersJpaController.find(email);
			} catch (Exception e) {
				user = null;
				e.printStackTrace();
			}

			if (user != null) {
				String forgotToken = UUID.randomUUID().toString();

				user.setForgotToken(forgotToken);

				Boolean status = adminUsersJpaController.update(user);

				// Send Email

				if (status == true) {

					// VelocityEngine ve = new VelocityEngine();
					// ve.setApplicationAttribute("resource.loader", "class");
					// ve.setApplicationAttribute("class.resource.loader.class",
					// org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader.class);
					// // ve.setApplicationAttribute("class.resource.loader.path", "templates/");
					// ve.setApplicationAttribute("resource.loader", "webapp");
					// ve.setApplicationAttribute("webapp.resource.loader.class",
					// org.apache.velocity.runtime.resource.loader.ebapp.class);
					// ve.setApplicationAttribute("velocimacro.library.autoreload", true);
					// ve.setApplicationAttribute("file.resource.loader.path",
					// "/WEB-INF/classes/templates");
					// ve.setApplicationAttribute("velocimacro.permissions.allow.inline.to.replace.global",
					// true);
					// ve.init();

					VelocityEngine ve = new VelocityEngine();
					ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
					ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
					ve.init();

					org.apache.velocity.Template t = ve.getTemplate("templates/resetEmail.vm");

					// Set parameters for my template.
					VelocityContext context = new VelocityContext();
					context.put("token", URLEncoder.encode(user.getForgotToken(), "UTF-8"));
					context.put("user", user.getName());
					context.put("url", applicationParameters.getUrl());

					StringWriter writer = new StringWriter();
					t.merge(context, writer);
					String output = writer.toString().trim();

					Email passworResetEmail = new HtmlEmail();
					passworResetEmail.setHostName("smtp.office365.com");
					passworResetEmail.setSmtpPort(587);
					passworResetEmail.setAuthenticator(new DefaultAuthenticator(applicationParameters.getEmail(),
							applicationParameters.getEmailPassword()));
					passworResetEmail.setStartTLSEnabled(true);
					passworResetEmail.setFrom(applicationParameters.getEmail());
					passworResetEmail.setSubject("Device Health Admin Password Reset");
					((HtmlEmail) passworResetEmail).setHtmlMsg(output);
					passworResetEmail.addTo(user.getEmail());
					passworResetEmail.send();
					response.put("status", "200");
					response.put("Message", "Password Reset Token Sent");
				} else {
					response.put("status", "500"); // Add meaningful application codes
					response.put("Message", "User update failed");

				}

			} else {
				response.put("status", "404"); // Add meaningful application codes
				response.put("Message", "User not found");

			}
		} else {
			response.put("status", "404"); // Add meaningful application codes
			response.put("Message", "Login not Successful");

		}
		return response;

	}

	@GET
	@Produces("application/json")
	@Path(Constants.URL.PASSWORDRESET + Constants.URL.VERIFY)
	public JSONObject verifyToken(@QueryParam("token") String token) throws Exception {
		AdminUsers user = adminUsersJpaController.findByToken(token.trim());
		if (user != null) {
			adminUsersJpaController.invalidateToken(user);
		}
		JSONObject response = new JSONObject();
		response.put("status", "200");
		response.put("email", user.getEmail());
		response.put("Message", "Password Reset Token verification Successful");

		return response;

	}

	@POST
	@Produces("application/json")
	@Path(Constants.URL.PASSWORDRESET + Constants.URL.COMPLETE)
	public JSONObject completePasswordReset(InputStream incomingData) throws Exception {
		JsonNode data = JSonHelper.getJsonNode(incomingData);
		String email = data.has("email") ? data.get("email").asText() : null;
		String password = data.has("password") ? data.get("password").asText() : null;
		JSONObject response = new JSONObject();

		if (email != null) {
			AdminUsers user = adminUsersJpaController.findByEmail(email);
			if (user != null && password != null) {
				user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
				adminUsersJpaController.updatePassword(user);
			}
			response.put("status", "200");
			response.put("Message", "Password Reset Successful");
		} else {
			response.put("status", "404"); // Add meaningful application codes
			response.put("Message", "Password reset not Successful");
		}

		return response;

	}

	private String getJWT(AdminUsers user) {
		long tokenLife;
		try {
			tokenLife = Integer.parseInt(applicationParameters.getTokenLife()) * 1000;
		} catch (NumberFormatException nfe) {
			tokenLife = 3600 * 1000;
		}

		long nowMillis = System.currentTimeMillis();

		long expMillis = nowMillis + tokenLife;
		Date expireDate = new Date(expMillis);

		return Jwts.builder().setSubject(user.getEmail()).setId(user.getId().toString())
				.claim("roles", UserCategory.ADMIN).claim("name", user.getName()).claim("site", "site")
				.setIssuedAt(new Date()).signWith(SignatureAlgorithm.HS256, applicationParameters.getHashKey())
				.setExpiration(expireDate).compact();

	}
}