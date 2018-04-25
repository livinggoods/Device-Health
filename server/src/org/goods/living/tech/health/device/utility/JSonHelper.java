package org.goods.living.tech.health.device.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JSonHelper {

	static Logger logger = LogManager.getLogger();// .getName());

	public static JSONObject getObject(InputStream incomingData) {
		StringBuilder jsonElements = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsonElements.append(line);
			}
			logger.debug("Data Received: " + jsonElements.toString());

			JSONObject jObject;
			jObject = new JSONObject(jsonElements.toString());

			Iterator<?> keys = jObject.keys();

			while (keys.hasNext()) {
				String key = (String) keys.next();
				String value = jObject.getString(key);
				logger.debug("key: " + key + "\t value: " + value);
			}
			return jObject;
		} catch (Exception e) {
			logger.error("Error ",e);
			return null;
		}
	}
}
