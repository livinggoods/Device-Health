package org.goods.living.tech.health.device.service;

import java.io.InputStream;
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
					Email passworResetEmail = new HtmlEmail();
					passworResetEmail.setHostName("smtp.office365.com");
					passworResetEmail.setSmtpPort(587);
					passworResetEmail.setAuthenticator(new DefaultAuthenticator(applicationParameters.getEmail(),
							applicationParameters.getEmailPassword()));
					passworResetEmail.setStartTLSEnabled(true);
					passworResetEmail.setFrom("itsupport@livinggoods.org");
					passworResetEmail.setSubject("Device Health Admin Password Reset");
					((HtmlEmail) passworResetEmail).setHtmlMsg("<body style=\"background: #FFFFFF;\">   "
							+ " <div class=\"mj-container\" style=\"background-color:#FFFFFF;\"><!--[if mso | IE]>     "
							+ " <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\" "
							+ "align=\"center\" style=\"width:600px;\">        <tr>          <td style=\"line-height:0px;"
							+ "font-size:0px;mso-line-height-rule:exactly;\">      <![endif]--><div style=\"margin:0px auto;"
							+ "max-width:600px;\"><table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" "
							+ "style=\"font-size:0px;width:100%;\" align=\"center\" border=\"0\"><tbody><tr><td "
							+ "style=\"text-align:center;vertical-align:top;direction:ltr;font-size:0px;padding:9px 0px 9px 0px;\"><!--[if mso | IE]>   "
							+ "   <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">        "
							+ "<tr>          <td style=\"vertical-align:top;width:600px;\">     "
							+ " <![endif]--><div class=\"mj-column-per-100 outlook-group-fix\" "
							+ "style=\"vertical-align:top;display:inline-block;direction:ltr;font-size:13px;text-align:left;width:100%;\">"
							+ "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">"
							+ "<tbody><tr><td style=\"word-wrap:break-word;font-size:0px;padding:0px 20px 0px 20px;\" align=\"center\">"
							+ "<div style=\"cursor:auto;color:#000000;font-family:Ubuntu, Helvetica, Arial, sans-serif;font-size:11px;line-height:22px;text-align:center;\"><h1 style=\"font-family: &apos;Cabin&apos;, sans-serif; line-height: 100%;\">Device Health - Living Goods</h1></div></td></tr><tr><td style=\"word-wrap:break-word;font-size:0px;padding:0px 20px 0px 20px;\" align=\"left\"><div style=\"cursor:auto;color:#000000;font-family:Ubuntu, Helvetica, Arial, sans-serif;font-size:11px;line-height:22px;text-align:left;\"><p><span style=\"font-size:14px;\">Dear "
							+ user.getName()
							+ ",</span></p><p><span style=\"font-size:14px;\">We have received a password reset request from you. Please click on the button below to reset your password</span></p></div></td></tr><tr><td style=\"word-wrap:break-word;font-size:0px;padding:10px 25px 10px 25px;padding-top:10px;padding-left:25px;\" align=\"center\"><table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:separate;\" align=\"center\" border=\"0\"><tbody><tr><td style=\"border:none;border-radius:24px;color:#fff;cursor:auto;padding:10px 25px;\" align=\"center\" valign=\"middle\" bgcolor=\"#005084\">"
							+ "<a href=\"" + applicationParameters.getUrl() + "/password/reset/verify?token="
							+ URLEncoder.encode(user.getForgotToken(), "UTF-8") + "\""
							+ " style=\"text-decoration:none;background:#005084;color:#fff;font-family:Ubuntu, Helvetica, Arial, sans-serif, Helvetica, Arial, sans-serif;font-size:13px;font-weight:normal;line-height:120%;text-transform:none;margin:0px;\" target=\"_blank\">Reset Password</a></td></tr></tbody></table></td></tr></tbody></table></div><!--[if mso | IE]>      </td></tr></table>      <![endif]--></td></tr></tbody></table></div><!--[if mso | IE]>      </td></tr></table>      <![endif]--></div></body>");
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