package org.goods.living.tech.health.device.utility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class JSonHelper {

	static Logger logger = LogManager.getLogger();// .getName());

	static ObjectMapper objectMapper = new ObjectMapper();
	{
		// objectMapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	public static JsonNode getJsonNode(InputStream incomingData) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));

			// JsonFactory factory = new JsonFactory();
			// factory.createJsonParser(in).
			JsonNode jsonNode = objectMapper.readTree(in);
			logger.debug(jsonNode);
			return jsonNode;
		} catch (Exception e) {
			logger.error("Error ", e);
			return null;
		}
	}
}
