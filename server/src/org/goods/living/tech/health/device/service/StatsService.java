package org.goods.living.tech.health.device.service;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.goods.living.tech.health.device.jpa.controllers.MedicJpaController;
import org.goods.living.tech.health.device.jpa.controllers.StatsJpaController;
import org.goods.living.tech.health.device.jpa.controllers.UsersJpaController;
import org.goods.living.tech.health.device.jpa.dao.ChvActivity;
import org.goods.living.tech.health.device.jpa.dao.Stats;
import org.goods.living.tech.health.device.jpa.dao.Users;
import org.goods.living.tech.health.device.models.Result;
import org.goods.living.tech.health.device.service.security.qualifier.Secured;
import org.goods.living.tech.health.device.service.security.qualifier.UserCategory;
import org.goods.living.tech.health.device.utility.Constants;
import org.goods.living.tech.health.device.utility.JSonHelper;
import org.goods.living.tech.health.device.utility.Utils;

//https://dzone.com/articles/lets-compare-jax-rs-vs-spring-for-rest-endpoints

@Path(Constants.URL.STATS)
@Named
@RequestScoped
public class StatsService extends BaseService {

	// @Inject
	// @PersistenceUnitQualifier(PersistenceUnitEnum.POSTGRES_MANAGEMENT)
	// private EntityManagerFactory copd_management_emf;

	// @Inject
	// private ApplicationParameters applicationParameters;
	@Inject
	StatsJpaController statsJpaController;

	@Inject
	MedicJpaController medicJpaController;

	@Inject
	UsersJpaController usersJpaController;

	public StatsService() {
	}

	@Secured(value = UserCategory.USER)
	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.CREATE)
	public Result<String> create(InputStream incomingData) throws ParseException {
		logger.debug("create");
		// JSONObject response = new JSONObject(responseString);
		List<JsonNode> list = JSonHelper.getJsonNodeArray(incomingData);

		Users user = getCurrentUser();
		// TODO: temp fix for old versions remove by august 15?
		if (user == null) {
			JsonNode JsonNode = list.size() > 0 ? list.get(0) : null;
			Long userId = JsonNode.has("userId") ? JsonNode.get("userId").asLong() : null;
			user = usersJpaController.findUsers(userId);
		}

		for (JsonNode j : list) {
			logger.debug(j);

			Stats stats = new Stats();
			stats.setUserId(user);
			stats.setAccuracy(j.has("accuracy") ? j.get("accuracy").asDouble() : null);
			stats.setLatitude(j.has("latitude") ? j.get("latitude").asDouble() : null);
			stats.setLongitude(j.has("longitude") ? j.get("longitude").asDouble() : null);
			stats.setProvider(j.has("provider") ? j.get("provider").asText() : null);

			stats.setBatteryLevel(j.has("batteryLevel") ? j.get("batteryLevel").asDouble() : null);
			stats.setBrightness(j.has("brightness") ? j.get("brightness").asDouble() : null);
			if (j.has("recordedAt")) {

				Date recordedAt = Utils.getDateFromTimeStampWithTimezone(j.get("recordedAt").asText(),
						TimeZone.getTimeZone(Utils.TIMEZONE_UTC));

				stats.setRecordedAt(recordedAt);
			}

			stats.setCreatedAt(new Date());
			statsJpaController.create(stats);
		}

		// NodeBean toValue = mapper.convertValue(node, NodeBean.cla

		Result<String> result = new Result<String>(true, "", null);
		return result;

	}

	@Secured(value = UserCategory.ADMIN)
	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.FIND)
	public Result<JsonNode> find(InputStream incomingData) throws Exception {
		logger.debug("find");
		JsonNode data = JSonHelper.getJsonNode(incomingData);

		String uuid = data.has("uuid") ? data.get("uuid").asText() : null;

		String username = data.has("username") ? data.get("username").asText() : null;

		String country = data.has("country") ? data.get("country").asText() : null;

		String dateFromString = data.has("from") ? data.get("from").asText() : null;
		String dateToString = data.has("to") ? data.get("to").asText() : null;
		SimpleDateFormat dFormat = new SimpleDateFormat("MM-dd-yyyy");
		Date from = dateFromString == null ? null : dFormat.parse(dateFromString);
		Date to = dateToString == null ? null : dFormat.parse(dateToString);

		logger.info("initiating query execution");

		List<ChvActivity> chvActivities = uuid == null ? null
				: medicJpaController.findChvActivities(country, uuid, from, to);

		if (chvActivities == null) {
			logger.error("no activities found... " + username);
			Result<JsonNode> result = new Result<JsonNode>(false, "", null);
			return result;
		}

		List<JsonNode> results = new ArrayList<>();
		// ObjectMapper mapper = new ObjectMapper();
		// ArrayNode array = mapper.valueToTree(list);
		ObjectMapper mapper = new ObjectMapper();
		for (ChvActivity activity : chvActivities) {
			// get array of locations for the activity timestamp

			ChvActivity activityWithStats = statsJpaController.fetchLocationStatistics(uuid, activity);
			ObjectNode root = mapper.createObjectNode();

			root.put("timestamp", activityWithStats.getReportedDate().toString());
			root.put("longitude", activityWithStats.getLongitude());
			root.put("latitude", activityWithStats.getLatitude());
			root.put("activity", activityWithStats.getActivityType());
			root.put("client", activityWithStats.getClientName());
			root.put("medicCoordinates", activityWithStats.getMedicCoordinates().toJSONString());
			root.put("activityId", activityWithStats.getActivityId());
			results.add(root);
		}

		ObjectNode node = JsonNodeFactory.instance.objectNode();

		node.putArray("locations").addAll(results);

		Result<JsonNode> result = new Result<JsonNode>(true, "", node);
		return result;

	}

	//
	// @POST
	// // @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// @Path(Constants.URL.DELETE)
	// public Result<JsonNode> delete(InputStream incomingData) {
	// logger.debug("delete");
	// JsonNode data = JSonHelper.getJsonNode(incomingData);
	//
	// Result<JsonNode> result = new Result<JsonNode>(true, "", data);
	// return result;
	//
	// }

	// public static void conv(String[] args) {
	// final ObjectMapper mapper = new ObjectMapper();
	// final ObjectNode root = mapper.createObjectNode();
	// root.put("integer", mapper.convertValue(1, JsonNode.class));
	// root.set("string", mapper.convertValue("string", JsonNode.class));
	// root.set("bool", mapper.convertValue(true, JsonNode.class));
	// root.set("array", mapper.convertValue(Arrays.asList("a", "b", "c"),
	// JsonNode.class));
	// System.out.println(root);
	// }

}