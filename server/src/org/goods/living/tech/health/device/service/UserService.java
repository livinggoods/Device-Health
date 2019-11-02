package org.goods.living.tech.health.device.service;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.goods.living.tech.health.device.jpa.controllers.AdminUsersJpaController;
import org.goods.living.tech.health.device.jpa.controllers.ChwJpaController;
import org.goods.living.tech.health.device.jpa.controllers.MedicJpaController;
import org.goods.living.tech.health.device.jpa.controllers.UsersJpaController;
import org.goods.living.tech.health.device.jpa.dao.Chw;
import org.goods.living.tech.health.device.jpa.dao.MedicUser;
import org.goods.living.tech.health.device.jpa.dao.Users;
import org.goods.living.tech.health.device.models.Result;
import org.goods.living.tech.health.device.service.security.qualifier.Secured;
import org.goods.living.tech.health.device.service.security.qualifier.UserCategory;
import org.goods.living.tech.health.device.utility.Constants;
import org.goods.living.tech.health.device.utility.JSonHelper;
import org.goods.living.tech.health.device.utility.Utils;

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

//https://dzone.com/articles/lets-compare-jax-rs-vs-spring-for-rest-endpoints

@Path(Constants.URL.USER)
@Named
@RequestScoped
public class UserService extends BaseService {

	// @Inject
	// @PersistenceUnitQualifier(PersistenceUnitEnum.POSTGRES_MANAGEMENT)
	// private EntityManagerFactory copd_management_emf;

	// @Inject
	// private ApplicationParameters applicationParameters;

	@Inject
	UsersJpaController usersJpaController;
	
	@Inject
	ChwJpaController chwJpaController;

	@Inject
	MedicJpaController medicJpaController;

	@Inject
	AdminUsersJpaController adminUsersJpaController;

	Integer DEFAULT_UPDATE_INTERVAL = 300;

	public UserService() {
	}

	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.CREATE)
	public Result<JsonNode> create(InputStream incomingData) throws Exception {
		logger.debug("create");
		Result<JsonNode> result = null;
		JsonNode data = JSonHelper.getJsonNode(incomingData);
		//JsonNode existing = JSonHelper.getJsonNode(incomingData);
		
		//System.out.print("incomingData" + incomingData);
		
		logger.debug("incomingData From Postman " + data);
		Users users;
		//String id = data.has("id") ? data.get("id").asText() : null;
		String username = data.has("username") ? data.get("username").asText() : null;
		String androidId = data.has("androidId") ? data.get("androidId").asText() : null;
		String deviceTimeStr = data.has("deviceTime") ? data.get("deviceTime").asText() : null;
		String country = data.has("country") ? data.get("country").asText() : null;
		String token = data.has("token") ? data.get("token").asText() : null;
		String fcmToken = data.has("fcmToken") ? data.get("fcmToken").asText() : null;
		String phone = data.has("phone") ? data.get("phone").asText() : null;

		Date deviceTime = Utils.getDateFromTimeStampWithTimezone(deviceTimeStr,
				TimeZone.getTimeZone(Utils.TIMEZONE_UTC));// at sync/toJSONObject time set this - we can use it to get
															// the clock drift

		Long clockDrift = null;// clock drift from server time in seconds
		Date serverTime = new Date();
		if (deviceTime != null) {
			clockDrift = Duration.between(serverTime.toInstant(), deviceTime.toInstant()).getSeconds();
		}

		//users = usersJpaController.findByUserNameAndAndroidId(username, androidId);
		//users = usersJpaController.findByUserName(username);
		
		Users user = usersJpaController.findByUserName(username);
		
		if (user == null) {
			users = new Users();
			
			logger.debug("user does not exist on Users Table: NULL ");
			Chw chw = chwJpaController.findByUserName(username);
			
				if(chw == null) {
				
					logger.debug("user does not exist on CHW Table: NULL ");
					
					users.setUsername(username);
					users.setPassword(data.has("password") ? data.get("password").asText() : null);
					users.setAndroidId(androidId);
					users.setCountry(country);
					users.setFcmToken(fcmToken);
					users.setPhone(phone);
					
					//IF NULL ON USERS AND CHW THEN INSERT
					
					int versionCode = data.has("versionCode") ? Integer.valueOf(data.get("versionCode").asText()) : 1;
					users.setVersionCode(versionCode);
					users.setVersionName(data.has("versionName") ? data.get("versionName").asText() : null);
					users.setDeviceTime(deviceTime);

					if (data.has("deviceInfo")) {
						// JacksonUtil.toJsonNode(); JsonNode entity = new
						// ObjectMapper().readTree(data.get("deviceInfo").toString());
						com.fasterxml.jackson.databind.JsonNode entity = JacksonUtil.toJsonNode(data.get("deviceInfo").toString());
						users.setDeviceInfo(entity);
					}
					if (data.has("setting")) {
						// JacksonUtil.toJsonNode(); JsonNode entity = new
						// ObjectMapper().readTree(data.get("deviceInfo").toString());
						com.fasterxml.jackson.databind.JsonNode settingEntity = JacksonUtil
								.toJsonNode(data.get("setting").toString());
						users.setSetting(settingEntity);
					}

					if (data.has("recordedAt")) {
						Date recordedAt = Utils.getDateFromTimeStampWithTimezone(data.get("recordedAt").asText(),
								TimeZone.getTimeZone(Utils.TIMEZONE_UTC));// dateFormat.parse(data.get("recordedAt").asText());

						users.setRecordedAt(recordedAt);
					}

					logger.debug("create new user");
					users.setCreatedAt(new Date());
					
					usersJpaController.create(users);
					
					
					// generate token
					if (token == null) {
						token = getJWT(users);

					}

					ObjectNode o = (ObjectNode) data;
					o.put("masterId", users.getId());
					o.put("phone", users.getPhone());
					o.put("chvId", users.getChvId());
					o.put("name", users.getName());
					o.put("branch", users.getBranch());
					o.put("country", users.getCountry());
					if (token != null)
						o.put("token", token);
					if (clockDrift != null)
						o.put("clockDrift", clockDrift);

					o.put("serverApi", applicationParameters.getServerApi());

					result = new Result<JsonNode>(true, "", o);
				
					
			}else {
				
				logger.debug("user FOUND ON CHW Table: NULL ");
				//I think we need to insert the users table with new record from chw table
				users.setChvId(chw.getContactId());
				users.setBranch(chw.getBranchName());
				users.setName(chw.getChwName());
				if(chw.getSupervisorName() != "Unassigned Supervisor" ) {
				   users.setSupervisor(true);
				}else {
					users.setSupervisor(false);
				}
				
				ObjectNode o = (ObjectNode) data;
				o.put("masterId", chw.getId());

				//boolean shouldforceupdate = shouldForceUpdate(users.getVersionName(), versionCode);
				o.put("serverApi", applicationParameters.getServerApi());
				//o.put("forceUpdate", shouldforceupdate);
				o.put("chvId", chw.getContactId());
				o.put("name", chw.getChwName());
				o.put("branch", chw.getBranchName());
				//o.put("country", chw.getCountry());

				result = new Result<JsonNode>(true, "", o);
			}

			
		}else {
			//Update the users table with details including name and branch
			Chw chw = chwJpaController.findByUserName(username);
			logger.debug("found existing user: " + user.getUsername());
			user.setChvId(user.getChvId());
			user.setBranch(user.getBranch());
			user.setName(user.getName());
			user.setSupervisor(user.getSupervisor());
			
			if(chw != null) {
				user.setChvId(chw.getContactId());
				user.setPhone(chw.getChwPhone());
				user.setName(chw.getChwName());
				user.setBranch(chw.getBranchName());
				if(chw.getSupervisorName() != "Unassigned Supervisor" ) {
					   user.setSupervisor(true);
				}else {
						user.setSupervisor(false);
				}
				usersJpaController.update(user);
			}
			
			ObjectNode o = (ObjectNode) data;
			if(user != null) {
				o.put("masterId", user.getId());
				//boolean shouldforceupdate = shouldForceUpdate(users.getVersionName(), versionCode);
				o.put("serverApi", applicationParameters.getServerApi());
				//o.put("forceUpdate", shouldforceupdate);
				o.put("chvId", user.getChvId());
				o.put("name", user.getName());
				o.put("branch", user.getBranch());
				o.put("message", "user exists");
			}else {
				o.put("masterId", user.getId());
				//boolean shouldforceupdate = shouldForceUpdate(users.getVersionName(), versionCode);
				o.put("serverApi", applicationParameters.getServerApi());
				//o.put("forceUpdate", shouldforceupdate);
				o.put("chvId", chw.getContactId());
				o.put("name", chw.getChwName());
				o.put("branch", chw.getBranchName());
				o.put("message", "user exists");
				//o.put("country", chw.getCountry());
			}
		
			result = new Result<JsonNode>(true, "", o);
			//return result;
		}
		
		return result;
	}

	/**
	 * 
	 * @deprecated just here to force old api devices to upgrade
	 * @param incomingData
	 * @return
	 * @throws Exception
	 */
	@POST
	@Secured(value = UserCategory.USER)
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.UPDATE)
	public Result<JsonNode> update(InputStream incomingData) throws Exception {

		Result<JsonNode> result = null;
		// try {

		logger.debug("update");
		JsonNode data = JSonHelper.getJsonNode(incomingData);

		Users users = getCurrentUser();
		// TODO: temp fix for old versions remove by august 15?
		if (users == null) {
			String username = data.has("username") ? data.get("username").asText() : null;
			String androidId = data.has("androidId") ? data.get("androidId").asText() : null;
			users = usersJpaController.findByUserNameAndAndroidId(username, androidId);

		}

		String deviceTimeStr = data.has("deviceTime") ? data.get("deviceTime").asText() : null;

		Date deviceTime = Utils.getDateFromTimeStampWithTimezone(deviceTimeStr,
				TimeZone.getTimeZone(Utils.TIMEZONE_UTC));// at sync/toJSONObject time set this - we can use it to
															// get
		String fcmToken = data.has("fcmToken") ? data.get("fcmToken").asText() : null;

		if (data.has("setting")) {
			// JacksonUtil.toJsonNode(); JsonNode entity = new
			// ObjectMapper().readTree(data.get("deviceInfo").toString());
			com.fasterxml.jackson.databind.JsonNode settingEntity = JacksonUtil
					.toJsonNode(data.get("setting").toString());
			users.setSetting(settingEntity);
		}

		users.setFcmToken(fcmToken);
		users.setDeviceTime(deviceTime);
		int versionCode = data.has("versionCode") ? Integer.valueOf(data.get("versionCode").asText()) : 1;
		users.setVersionCode(versionCode);
		users.setVersionName(data.has("versionName") ? data.get("versionName").asText() : null);
		users.setUpdatedAt(new Date());
		usersJpaController.update(users);

		ObjectNode o = (ObjectNode) data;
		o.put("masterId", users.getId());

		boolean shouldforceupdate = shouldForceUpdate(users.getVersionName(), versionCode);
		o.put("serverApi", applicationParameters.getServerApi());
		o.put("forceUpdate", shouldforceupdate);
		o.put("chvId", users.getChvId());
		o.put("name", users.getName());
		o.put("branch", users.getBranch());
		o.put("country", users.getCountry());

		result = new Result<JsonNode>(true, "", o);

		// } catch (Exception ex) {

		// System.out.println(ex.getMessage());

		// } finally {
		// System.out.println("Finally is being reached...you can get me exception from
		// here");
		// }

		return result;

	}

	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.USERS_REFRESH_TOKEN)
	public Result<JsonNode> refreshToken(InputStream incomingData) throws Exception {
		logger.debug("refresh token");
		JsonNode data = JSonHelper.getJsonNode(incomingData);

		String username = data.has("username") ? data.get("username").asText() : null;
		String androidId = data.has("androidId") ? data.get("androidId").asText() : null;
		String token;

		Users users = usersJpaController.findByUserNameAndAndroidId(username, androidId);

		ObjectNode o = (ObjectNode) data;
		if (users != null) {
			token = getJWT(users);
			o.put("token", token);
		} else {
			logger.debug("refresh token failed: " + username);
			o.remove("token");
		}

		Result<JsonNode> result = new Result<JsonNode>(true, "", o);
		return result;

	}

	// use the @Secured annotation for your secured methods
	// use the @RolesAllowed({"ADMIN"}) annotation to specify what level of
	// authority has access to your resources

	// Roles can be found at
	// org.goods.living.tech.health.device.service.security.qualifier

	@Secured(value = UserCategory.ADMIN)
	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.FIND)
	public Result<JsonNode> find(InputStream incomingData) throws Exception {
		logger.debug("find");
		JsonNode data = JSonHelper.getJsonNode(incomingData);

		String username = data.has("username") ? data.get("username").asText() : null;
		String country = data.has("country") ? data.get("country").asText() : null;

		List<MedicUser> lists = medicJpaController.findByNameLike(country, username);

		// List<Users> list = usersJpaController.findByUserNameLike(username);
		// ObjectMapper mapper = new ObjectMapper();
		// ArrayNode array = mapper.valueToTree(names);
		List<JsonNode> results = new ArrayList<>();
		// ObjectMapper mapper = new ObjectMapper();
		// ArrayNode array = mapper.valueToTree(list);
		ObjectMapper mapper = new ObjectMapper();
		for (MedicUser u : lists) {
			ObjectNode root = mapper.createObjectNode();
			root.put("username", u.getUsername());
			root.put("uuid", u.getUuid());
			root.put("name", u.getName());
			root.put("branch", u.getBranch());
			root.put("phone", u.getPhone());
			results.add(root);
		}

		ObjectNode node = JsonNodeFactory.instance.objectNode();

		node.putArray("users").addAll(results);

		Result<JsonNode> result = new Result<JsonNode>(true, "", node);
		return result;

	}

	@Secured(value = UserCategory.USER)
	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.SETTING)
	public Result<JsonNode> setting(InputStream incomingData) throws Exception {
		logger.debug("setting");
		JsonNode data = JSonHelper.getJsonNode(incomingData);
		// String username = data.has("username") ? data.get("username").asText() :
		// null;
		String username = data.has("username") ? data.get("username").asText() : null;
		Integer versionCode = data.has("versionCode") ? data.get("versionCode").asInt() : null;
		String network = data.has("network") ? data.get("network").asText() : null;

		// Users user = getCurrentUser();

		ObjectNode node = JsonNodeFactory.instance.objectNode();

		node.put("locationUpdateInterval", applicationParameters.getLocationUpdateInterval());// DEFAULT_UPDATE_INTERVAL);

		if (applicationParameters.shouldUpdateUSSDBalanceCodes()) {
			String ussd = applicationParameters.getUSSDBalanceCode(network);

			node.put("ussd", ussd);
		}

		node.put("disableDatabalanceCheck", applicationParameters.getDisableDatabalanceCheck());
		node.put("databalanceCheckTime", applicationParameters.getDataBalanceCheckTime());
		boolean shouldforceupdate = shouldForceUpdate(username, versionCode);
		node.put("serverApi", applicationParameters.getServerApi());
		node.put("forceUpdate", shouldforceupdate);
		node.put("disableSync", false);

		Result<JsonNode> result = new Result<JsonNode>(true, "", node);
		return result;

	}

	boolean shouldForceUpdate(String username, int deviceVersion) {

		int serverApi = applicationParameters.getServerApi();

		if (serverApi > deviceVersion) {
			logger.debug("forcing an update for user " + username);

			return true;

		}

		return false;

	}

	@SuppressWarnings("unused")
	@XmlRootElement
	private static class LoginResponse {

		public String token;

		public LoginResponse(final String token) {
			this.token = token;
		}
	}

	String getJWT(Users user) {
		long tokenLife;
		try {
			tokenLife = Integer.parseInt(applicationParameters.getTokenLife()) * 1000;
		} catch (NumberFormatException nfe) {
			tokenLife = 3600 * 1000;
		}

		long nowMillis = System.currentTimeMillis();

		long expMillis = nowMillis + tokenLife;
		Date expireDate = new Date(expMillis);

		logger.debug("token expirely  userid:expire  " + user.getId() + " : " + expireDate.toString());

		return Jwts.builder().setSubject(user.getUsername()).setId(user.getId().toString())
				.claim("roles", UserCategory.USER).claim("name", user.getName()).claim("chvId", user.getChvId())
				.claim("site", "site").setIssuedAt(new Date())
				.signWith(SignatureAlgorithm.HS256, applicationParameters.getHashKey()).setExpiration(expireDate)
				.compact();

	}

}