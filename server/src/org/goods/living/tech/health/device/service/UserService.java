package org.goods.living.tech.health.device.service;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
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
import org.goods.living.tech.health.device.jpa.controllers.MedicJpaController;
import org.goods.living.tech.health.device.jpa.controllers.UsersJpaController;
import org.goods.living.tech.health.device.jpa.dao.AdminUsers;
import org.goods.living.tech.health.device.jpa.dao.MedicUser;
import org.goods.living.tech.health.device.jpa.dao.Users;
import org.goods.living.tech.health.device.models.Result;
import org.goods.living.tech.health.device.utility.Constants;
import org.goods.living.tech.health.device.utility.JSonHelper;
import org.goods.living.tech.health.device.utility.Utils;
import org.json.simple.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

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

	SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

	@Inject
	UsersJpaController usersJpaController;

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
		JsonNode data = JSonHelper.getJsonNode(incomingData);

		Users users;
		String username = data.has("username") ? data.get("username").asText() : null;
		String androidId = data.has("androidId") ? data.get("androidId").asText() : null;
		String deviceTimeStr = data.has("deviceTime") ? data.get("deviceTime").asText() : null;
		String country = data.has("country") ? data.get("country").asText() : null;

		Date deviceTime = Utils.getDateFromTimeStampWithTimezone(deviceTimeStr,
				TimeZone.getTimeZone(Utils.TIMEZONE_UTC));// at sync/toJSONObject time set this - we can use it to get
															// the clock drift

		Long clockDrift = null;// clock drift from server time in seconds
		Date serverTime = new Date();
		if (deviceTime != null) {
			clockDrift = Duration.between(serverTime.toInstant(), deviceTime.toInstant()).getSeconds();
		}

		users = usersJpaController.findByUserNameAndAndroidId(username, androidId);
		if (users == null) {
			users = new Users();
			users.setUpdateInterval(DEFAULT_UPDATE_INTERVAL);
			users.setUsername(username);
			users.setPassword(data.has("password") ? data.get("password").asText() : null);
			users.setAndroidId(androidId);
			users.setCountry(country);

			// set chvId - retrieve from medic
			MedicUser mu = medicJpaController.findByUsername(country, username);
			users.setChvId(mu == null ? null : mu.getUuid());
			users.setPhone(data.has("phone") ? data.get("phone").asText() : null);

		}

		int versionCode = data.has("versionCode") ? Integer.valueOf(data.get("versionCode").asText()) : 1;
		users.setVersionCode(versionCode);
		users.setVersionName(data.has("versionName") ? data.get("versionName").asText() : null);
		users.setDeviceTime(deviceTime);

		// TODO: data.has("deviceInfo") ? data.get("deviceInfo").asText() : null;

		if (data.has("recordedAt")) {
			Date recordedAt = dateFormat.parse(data.get("recordedAt").asText());

			users.setRecordedAt(recordedAt);
		}

		// new or update
		if (users.getId() == null) {
			logger.debug("create new user");
			users.setCreatedAt(new Date());
			usersJpaController.create(users);
		} else { // update?
			logger.debug("ignore create: update user");

			// set chvId if blank - retrieve from medic
			if (users.getChvId() == null) {
				MedicUser mu = medicJpaController.findByUsername(country, username);
				users.setChvId(mu == null ? null : mu.getUuid());
				users.setBranch(mu == null ? null : mu.getBranch());
				users.setName(mu == null ? null : mu.getName());

			}
			users.setUpdatedAt(new Date());
			users = usersJpaController.update(users);
		}

		// ObjectMapper mapper = new ObjectMapper();
		// ObjectNode o = (ObjectNode) data;
		// mapper.convertValue(users, JsonNode.class);
		//// ObjectNode root = mapper.createObjectNode();

		ObjectNode o = (ObjectNode) data;
		o.put("masterId", users.getId());
		o.put("updateInterval", users.getUpdateInterval());// DEFAULT_UPDATE_INTERVAL);
		o.put("phone", users.getPhone());
		o.put("chvId", users.getChvId());
		o.put("ussdBalanceCode", users.getUssdBalanceCode());

		if (clockDrift != null)
			o.put("clockDrift", clockDrift);

		// NodeBean toValue = mapper.convertValue(node, NodeBean.cla

		boolean shouldforceupdate = shouldForceUpdate(username, versionCode);
		o.put("serverApi", applicationParameters.getServerApi());
		o.put("forceUpdate", shouldforceupdate);

		Result<JsonNode> result = new Result<JsonNode>(true, "", o);
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
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.UPDATE)
	public Result<JsonNode> update(InputStream incomingData) throws Exception {
		logger.debug("update");
		JsonNode data = JSonHelper.getJsonNode(incomingData);

		Users users;
		String username = data.has("username") ? data.get("username").asText() : null;
		String androidId = data.has("androidId") ? data.get("androidId").asText() : null;
		users = usersJpaController.findByUserNameAndAndroidId(username, androidId);

		ObjectNode o = (ObjectNode) data;
		o.put("masterId", users.getId());
		o.put("updateInterval", users.getUpdateInterval());// DEFAULT_UPDATE_INTERVAL);

		// NodeBean toValue = mapper.convertValue(node, NodeBean.cla

		boolean shouldforceupdate = true;// shouldForceUpdate(username, versionCode);
		o.put("serverApi", applicationParameters.getServerApi());
		o.put("forceUpdate", shouldforceupdate);

		Result<JsonNode> result = new Result<JsonNode>(true, "", o);
		return result;

	}

	// use the @Secured annotation for your secured methods
	// use the @RolesAllowed({"ADMIN"}) annotation to specify what level of
	// authority has access to your resources

	// Roles can be found at
	// org.goods.living.tech.health.device.service.security.qualifier

	// @Secured
	// @RolesAllowed({"ADMIN"})
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

}