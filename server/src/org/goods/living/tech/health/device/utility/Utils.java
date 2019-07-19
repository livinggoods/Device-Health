package org.goods.living.tech.health.device.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Utils {

	public final static String DATE_FORMAT_TIMEZONE = "dd-MM-yyyy'T'HH:mm:ss'Z'";// "dd-mm-yyyy HH:mm:ss"
	static SimpleDateFormat dateFormatWithTimezone = new SimpleDateFormat(DATE_FORMAT_TIMEZONE, Locale.getDefault());
	static SimpleDateFormat oldDateFormatWithTimezone = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss Z",
			Locale.getDefault());
	public final static String TIMEZONE_UTC = "UTC";

	static Logger logger = LogManager.getLogger();// .getName());

	public static Date getDateFromTimeStampWithTimezone(String date, TimeZone timezone) {

		try {
			if (date == null)
				return null;
			dateFormatWithTimezone.setTimeZone(timezone);
			return dateFormatWithTimezone.parse(date);
		} catch (Exception e) {
			try {
				logger.error("exception mapping date... attempting old approach", e);

				oldDateFormatWithTimezone.setTimeZone(timezone);
				return oldDateFormatWithTimezone.parse(date);
			} catch (Exception ee) {
				logger.error(ee);
				return null;
			}
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
