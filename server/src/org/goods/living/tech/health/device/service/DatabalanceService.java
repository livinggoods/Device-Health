package org.goods.living.tech.health.device.service;

import java.io.InputStream;
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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.goods.living.tech.health.device.jpa.controllers.DataBalanceJpaController;
import org.goods.living.tech.health.device.jpa.controllers.MedicJpaController;
import org.goods.living.tech.health.device.jpa.controllers.UsersJpaController;
import org.goods.living.tech.health.device.jpa.dao.Branch;
import org.goods.living.tech.health.device.jpa.dao.DataBalance;
import org.goods.living.tech.health.device.jpa.dao.Users;
import org.goods.living.tech.health.device.models.Result;
import org.goods.living.tech.health.device.service.security.qualifier.Secured;
import org.goods.living.tech.health.device.service.security.qualifier.UserCategory;
import org.goods.living.tech.health.device.utility.Constants;
import org.goods.living.tech.health.device.utility.JSonHelper;
import org.goods.living.tech.health.device.utility.Utils;

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;

//https://dzone.com/articles/lets-compare-jax-rs-vs-spring-for-rest-endpoints

@Path(Constants.URL.DATABALANCE)
@Named
@RequestScoped
public class DatabalanceService extends BaseService {

	// @Inject
	// @PersistenceUnitQualifier(PersistenceUnitEnum.POSTGRES_MANAGEMENT)
	// private EntityManagerFactory copd_management_emf;

	// @Inject
	// private ApplicationParameters applicationParameters;

	@Inject
	DataBalanceJpaController dataBalanceJpaController;

	@Inject
	UsersJpaController usersJpaController;

	@Inject
	MedicJpaController medicJpaController;

	// String formattedDate = dateFormat.format(date);

	public DatabalanceService() {
	}

	@Secured(value = UserCategory.USER)
	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.CREATE)
	public Result<String> create(InputStream incomingData) throws Exception {
		logger.debug("create");
		// JSONObject response = new JSONObject(responseString);
		List<JsonNode> list = JSonHelper.getJsonNodeArray(incomingData);

		Users user = getCurrentUser();

		for (JsonNode j : list) {
			logger.debug(j);

			DataBalance model = new DataBalance();
			model.setUserId(user);
			model.setBalance(j.has("balance") ? j.get("balance").asDouble() : null);
			model.setBalanceMessage(j.has("balanceMessage") ? j.get("balanceMessage").asText() : null);
			model.setMessage(j.has("message") ? j.get("message").asText() : null);

			if (j.has("info")) {
				// JacksonUtil.toJsonNode();
				com.fasterxml.jackson.databind.JsonNode entity = JacksonUtil.toJsonNode(j.get("info").toString());
				model.setInfo(entity);
			}

			if (j.has("recordedAt")) {
				Date date = Utils.getDateFromTimeStampWithTimezone(j.get("recordedAt").asText(),
						TimeZone.getTimeZone(Utils.TIMEZONE_UTC));
				model.setRecordedAt(date);
			}
			if (j.has("expiryDate")) {
				Date date = Utils.getDateFromTimeStampWithTimezone(j.get("expiryDate").asText(),
						TimeZone.getTimeZone(Utils.TIMEZONE_UTC));
				model.setExpiryDate(date);
			}

			model.setCreatedAt(new Date());
			dataBalanceJpaController.create(model);
		}

		// NodeBean toValue = mapper.convertValue(node, NodeBean.cla

		Result<String> result = new Result<String>(true, "", null);
		return result;

	}

	@Secured(value = UserCategory.USER)
	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.DATABALANCE_USSDCODES)
	public Result<String> ussdCodes(InputStream incomingData) {
		logger.debug("ussdCodes");

		JsonNode data = JSonHelper.getJsonNode(incomingData);

		// if (data.has("setting")) {
		// JacksonUtil.toJsonNode(); JsonNode entity = new
		// ObjectMapper().readTree(data.get("deviceInfo").toString());
		com.fasterxml.jackson.databind.JsonNode settingEntity = JacksonUtil.toJsonNode(data.get("setting").toString());

		String network = settingEntity.get("network").asText();

		// }

		// Users user = getCurrentUser();

		String ussd = applicationParameters.getUSSDBalanceCode(network);

		Result<String> result = new Result<String>(true, "", ussd);
		return result;

	}

	@Secured(value = UserCategory.ADMIN)
	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.BRANCHES + "" + Constants.URL.FIND)
	public Result<JsonNode> findBranch(InputStream incomingData) throws Exception {
		logger.debug("findBranches");
		JsonNode data = JSonHelper.getJsonNode(incomingData);

		String branchName = data.has("branchName") ? data.get("branchName").asText() : null;

		List<Branch> branches = branchName == null ? null : dataBalanceJpaController.findBranchMatching(branchName);

		if (branches == null) {
			Result<JsonNode> result = new Result<JsonNode>(false, "", null);
			return result;
		}

		List<JsonNode> results = new ArrayList<>();

		ObjectMapper mapper = new ObjectMapper();
		for (Branch branch : branches) {
			ObjectNode root = mapper.createObjectNode();
			root.put("branch", branch.getName());
			root.put("uuid", branch.getUuid());
			results.add(root);
		}

		ObjectNode node = JsonNodeFactory.instance.objectNode();

		node.putArray("branches").addAll(results);

		Result<JsonNode> result = new Result<JsonNode>(true, "", node);
		return result;

	}

	@Secured(value = UserCategory.ADMIN)
	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.STATS + "" + Constants.URL.FIND)
	public Result<JsonNode> fetchBalances(InputStream incomingData) throws Exception {
		logger.debug("findStats");
		JsonNode data = JSonHelper.getJsonNode(incomingData);

		String branchName = data.has("branchName") ? data.get("branchName").asText() : null;
		String chvName = data.has("chvName") ? data.get("chvName").asText() : null;
		String operator = data.has("operator") ? data.get("operator").asText() : null;
		String value = data.has("value") ? data.get("value").asText() : null;
		String page = data.has("page") ? data.get("page").asText() : null;

		List<Object[]> balances = dataBalanceJpaController.fetchBalances(branchName, chvName, operator, value, page);

		if (balances == null) {
			Result<JsonNode> result = new Result<JsonNode>(false, "", null);
			return result;
		}

		List<JsonNode> results = new ArrayList<>();

		ObjectMapper mapper = new ObjectMapper();
		for (Object[] balance : balances) {
			ObjectNode root = mapper.createObjectNode();
			root.put("username", balance[1] != null ? balance[1].toString() : null);
			root.put("name", balance[2] != null ? balance[2].toString() : null);
			root.put("androidId", balance[3] != null ? balance[3].toString() : null);
			root.put("branch", balance[4] != null ? balance[4].toString() : null);
			root.put("version_code", balance[5] != null ? balance[5].toString() : null);
			root.put("balance", balance[6] != null ? balance[6].toString() : null);
			root.put("balance_message", balance[7] != null ? balance[7].toString() : null);
			root.put("date", balance[8] != null ? balance[8].toString() : null);
			results.add(root);
		}

		ObjectNode node = JsonNodeFactory.instance.objectNode();

		node.putArray("balances").addAll(results);

		Result<JsonNode> result = new Result<JsonNode>(true, "", node);
		return result;

	}

}