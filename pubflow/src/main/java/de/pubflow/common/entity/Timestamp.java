package de.pubflow.common.entity;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Timestamp implements StringSerializable {

	private long timeInMillis;
	private static Logger myLogger;

	static {
		myLogger = LoggerFactory.getLogger(Timestamp.class);
	}

	public Timestamp() {
		timeInMillis = Calendar.getInstance().getTimeInMillis();
	}

	@Override
	public String transformToString() {
		return timeInMillis + "";
	}

	@Override
	public void initFromString(String content) {
		try {
			timeInMillis = Long.parseLong(content);
		} catch (Exception e) {
			myLogger.error("An Error occured while parsing the timestamp ...");
		}
	}

}
