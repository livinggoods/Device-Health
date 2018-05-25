package org.goods.living.tech.health.device.service;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.goods.living.tech.health.device.jpa.controllers.UsersJpaController;
import org.goods.living.tech.health.device.jpa.dao.Users;
import org.goods.living.tech.health.device.models.Result;
import org.goods.living.tech.health.device.utility.Constants;
import org.goods.living.tech.health.device.utility.JSonHelper;

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

		users = usersJpaController.findByUserNameAndAndroidId(username, androidId);
		if (users == null) {
			users = new Users();
		}

		int versionCode = data.has("versionCode") ? Integer.valueOf(data.get("versionCode").asText()) : 1;
		users.setVersionCode(versionCode);
		users.setVersionName(data.has("versionName") ? data.get("versionName").asText() : null);

		users.setUsername(username);
		users.setPassword(data.has("password") ? data.get("password").asText() : null);

		users.setAndroidId(androidId);
		users.setChvId(data.has("chvId") ? data.get("chvId").asText() : null);
		users.setPhone(data.has("phone") ? data.get("phone").asText() : null);

		users.setUpdateInterval(DEFAULT_UPDATE_INTERVAL);

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
			users.setUpdatedAt(new Date());
			users = usersJpaController.update(users);
		}

		ObjectNode o = (ObjectNode) data;
		o.put("masterId", users.getId());
		o.put("disableSync", users.getDisableSync());
		o.put("updateInterval", applicationParameters.getLocationUpdateInterval());// DEFAULT_UPDATE_INTERVAL);

		// NodeBean toValue = mapper.convertValue(node, NodeBean.cla

		boolean shouldforceupdate = shouldForceUpdate(username, versionCode);
		o.put("serverApi", applicationParameters.getServerApi());
		o.put("forceUpdate", shouldforceupdate);

		Result<JsonNode> result = new Result<JsonNode>(true, "", o);
		return result;

	}

	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.FIND)
	public Result<JsonNode> find(InputStream incomingData) throws Exception {
		logger.debug("find");
		JsonNode data = JSonHelper.getJsonNode(incomingData);

		String username = data.has("username") ? data.get("username").asText() : null;

		List<Users> list = usersJpaController.findByUserNameLike(username);
		List<String> names = new ArrayList<String>();
		for (Users u : list) {
			names.add(u.getUsername());
		}

		ObjectMapper mapper = new ObjectMapper();
		ArrayNode array = mapper.valueToTree(names);

		ObjectNode node = JsonNodeFactory.instance.objectNode();

		node.putArray("users").addAll(array);

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

}