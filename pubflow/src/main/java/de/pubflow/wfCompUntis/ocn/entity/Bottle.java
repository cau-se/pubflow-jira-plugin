package de.pubflow.wfCompUntis.ocn.entity;

import de.pubflow.wfCompUntis.ocn.entity.abstractClass.PubJect;


public class Bottle extends PubJect{
	
	public static final String c_latitude = "bottle_latitude";
	public static final String c_longitude = "bottle_longitude"; 
	public static final String c_waterdepth = "bottle_waterdepth";
	public static final String c_time = "bottle_time";
	public static final String c_station = "bottle_station";
	public static final String c_label = "bottle_label";
	public static final String c_id = "bottle_id";
	public static final String c_sampleList = "sampleList";

	public static final String[] c_BOTTLETABLE = {c_latitude, c_longitude, c_waterdepth, c_time, c_station, c_label, c_id};
}
