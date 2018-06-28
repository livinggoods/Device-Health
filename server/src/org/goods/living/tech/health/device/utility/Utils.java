package org.goods.living.tech.health.device.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Utils {

	public final static String DATE_FORMAT_TIMEZONE = "MM-dd-yyyy HH:mm:ss Z";// "MM-dd-yyyy HH:mm:ss"
	static SimpleDateFormat dateFormatWithTimezone = new SimpleDateFormat(DATE_FORMAT_TIMEZONE, Locale.getDefault());
	public final static String TIMEZONE_UTC = "UTC";

	static Logger logger = LogManager.getLogger();// .getName());

	public static Date getDateFromTimeStampWithTimezone(String date, TimeZone timezone) {

		try {
			if (date == null)
				return null;
			dateFormatWithTimezone.setTimeZone(timezone);
			return dateFormatWithTimezone.parse(date);
		} catch (Exception e) {
			logger.error(e);
			return null;
		}

	}

	/**
	 * Converting from Date to String
	 **/
	public static String getStringTimeStampWithTimezoneFromDate(Date date, TimeZone timezone) {
		try {
			if (date == null)
				return null;
			dateFormatWithTimezone.setTimeZone(timezone);// TimeZone.getTimeZone("UTC"
			return dateFormatWithTimezone.format(date);
		} catch (Exception e) {
			logger.error(e);
			return null;
		}

	}
}
