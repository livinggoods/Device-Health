package org.goods.living.tech.health.device.service;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.goods.living.tech.health.device.jpa.controllers.StatsJpaController;
import org.goods.living.tech.health.device.jpa.controllers.UsersJpaController;
import org.goods.living.tech.health.device.jpa.dao.Stats;
import org.goods.living.tech.health.device.jpa.dao.Users;
import org.goods.living.tech.health.device.models.Result;
import org.goods.living.tech.health.device.utility.Constants;
import org.goods.living.tech.health.device.utility.JSonHelper;

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
	UsersJpaController usersJpaController;

	/// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddd mmm
	/// dd/MM/yyyy HH:mm:ss");// Mon Apr 30 08:46:10 GMT+03:00 2018
	SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	// String formattedDate = dateFormat.format(date);

	public StatsService() {
	}

	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Constants.URL.CREATE)
	public Result<String> create(InputStream incomingData) throws ParseException {
		logger.debug("create");
		// JSONObject response = new JSONObject(responseString);
		List<JsonNode> list = JSonHelper.getJsonNodeArray(incomingData);

		Long userId = null;// TODO: get this from session
		if (list.size() > 0) {
			userId = list.get(0).has("userMasterId") ? list.get(0).get("userMasterId").asLong() : null;
		}

		Users user = usersJpaController.findUsers(userId);
		for (JsonNode j : list) {
			logger.debug(j);

			Stats stats = new Stats();
			stats.setUserId(user);
			stats.setAccuracy(j.has("accuracy") ? j.get("accuracy").asDouble() : null);
			stats.setLatitude(j.has("latitude") ? j.get("latitude").asDouble() : null);
			stats.setLongitude(j.has("longitude") ? j.get("longitude").asDouble() : null);
			stats.setProvider(j.has("provider") ? j.get("provider").asText() : null);
			if (j.has("recordedAt")) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
				Date recordedAt = dateFormat.parse(j.get("recordedAt").asText());

				stats.setRecordedAt(recordedAt);
			}

			stats.setCreatedAt(new Date());
			statsJpaController.create(stats);
		}

		// NodeBean toValue = mapper.convertValue(node, NodeBean.cla

		Result<String> result = new Result<String>(true, "", null);
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

}