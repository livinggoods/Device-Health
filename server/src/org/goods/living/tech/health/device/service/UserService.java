package org.goods.living.tech.health.device.service;

import java.io.InputStream;
import java.util.Date;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.goods.living.tech.health.device.jpa.controllers.UsersJpaController;
import org.goods.living.tech.health.device.jpa.dao.Users;
import org.goods.living.tech.health.device.models.Result;
import org.goods.living.tech.health.device.utility.ApplicationParameters;
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

	@Inject
	UsersJpaController usersJpaController;

	@Inject
	private ApplicationParameters applicationParameters;

	Integer DEFAULT_UPDATE_INTERVAL = 60;

	public UserService() {
	}

	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.CREATE)
	public Result<JsonNode> create(InputStream incomingData) {
		logger.debug("create");
		JsonNode data = JSonHelper.getJsonNode(incomingData);

		Users users = new Users();

		int versionCode = data.has("versionCode") ? Integer.valueOf(data.get("versionCode").asText()) : 1;
		users.setVersionCode(versionCode);
		users.setVersionName(data.has("versionName") ? data.get("versionName").asText() : null);
		String username = data.has("username") ? data.get("username").asText() : null;
		users.setUsername(username);
		users.setPassword(data.has("password") ? data.get("password").asText() : null);
		users.setAndroidId(data.get("androidId").asText());
		users.setChvId(data.has("chvId") ? data.get("chvId").asText() : null);
		users.setPhone(data.has("phone") ? data.get("phone").asText() : null);

		users.setUpdateInterval(DEFAULT_UPDATE_INTERVAL);

		users.setCreatedAt(new Date());

		usersJpaController.create(users);

		ObjectNode o = (ObjectNode) data;
		o.put("masterId", users.getId());
		o.put("updateInterval", applicationParameters.getLocationUpdateInterval());// DEFAULT_UPDATE_INTERVAL);

		// NodeBean toValue = mapper.convertValue(node, NodeBean.cla

		boolean shouldforceupdate = shouldForceUpdate(username, versionCode);
		o.put("serverApi", applicationParameters.getServerApi());
		o.put("forceUpdate", shouldforceupdate);

		Result<JsonNode> result = new Result<JsonNode>(true, "", o);
		return result;

	}

	boolean shouldForceUpdate(String username, int deviceVersion) {

		int serverApi = applicationParameters.getServerApi();

		if (serverApi > deviceVersion) {
			logger.debug("forcing an up update for user " + username);

			return true;

		}

		return false;

	}

	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.UPDATE)
	public Result<JsonNode> update(InputStream incomingData) {
		logger.debug("update");
		JsonNode data = JSonHelper.getJsonNode(incomingData);

		int versionCode = data.has("versionCode") ? Integer.valueOf(data.get("versionCode").asText()) : 1;
		String username = data.has("username") ? data.get("username").asText() : null;

		boolean shouldforceupdate = shouldForceUpdate(username, versionCode);

		ObjectNode o = (ObjectNode) data;
		o.put("serverApi", applicationParameters.getServerApi());
		o.put("forceUpdate", shouldforceupdate);

		Result<JsonNode> result = new Result<JsonNode>(true, "", o);
		return result;

	}

}