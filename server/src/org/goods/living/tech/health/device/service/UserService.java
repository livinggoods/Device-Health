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

		int API = data.get("api").asInt();

		Users users = new Users();
		users.setApi(data.has("api") ? Short.valueOf(data.get("api").asText()) : null);
		users.setUsername(data.has("username") ? data.get("username").asText() : null);
		users.setPassword(data.has("password") ? data.get("password").asText() : null);
		users.setAndroidId(data.get("androidId").asText());
		users.setChvId(data.has("chvId") ? data.get("chvId").asText() : null);
		users.setPhone(data.has("phone") ? data.get("phone").asText() : null);

		users.setUpdateInterval(DEFAULT_UPDATE_INTERVAL);

		users.setCreatedAt(new Date());

		usersJpaController.create(users);

		ObjectNode o = (ObjectNode) data;
		o.put("masterId", users.getId());
		o.put("updateInterval", DEFAULT_UPDATE_INTERVAL);
		// NodeBean toValue = mapper.convertValue(node, NodeBean.cla

		Result<JsonNode> result = new Result<JsonNode>(true, "", o);
		return result;

	}

	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.READ)
	public Result<JsonNode> read(InputStream incomingData) {
		logger.debug("read");
		JsonNode data = JSonHelper.getJsonNode(incomingData);

		Result<JsonNode> result = new Result<JsonNode>(true, "", data);
		return result;

	}

	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.UPDATE)
	public Result<JsonNode> update(InputStream incomingData) {
		logger.debug("update");
		JsonNode data = JSonHelper.getJsonNode(incomingData);

		int API = data.get("api").asInt();

		ObjectNode o = (ObjectNode) data;
		o.put("masterId", 1);
		o.put("updateInterval", 30);
		// NodeBean toValue = mapper.convertValue(node, NodeBean.cla

		Result<JsonNode> result = new Result<JsonNode>(true, "", o);
		return result;

	}

	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.DELETE)
	public Result<JsonNode> delete(InputStream incomingData) {
		logger.debug("delete");
		JsonNode data = JSonHelper.getJsonNode(incomingData);

		Result<JsonNode> result = new Result<JsonNode>(true, "", data);
		return result;

	}

}