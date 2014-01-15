package de.pubflow.wfCompUnits.ocn.entity;

import de.pubflow.wfCompUnits.ocn.entity.abstractClass.PubJect;


public class Bottle extends PubJect{
	
	public static final String LATITUDE = "bottle_latitude";
	public static final String LONGITUDE = "bottle_longitude"; 
	public static final String WATERDEPTH = "bottle_waterdepth";
	public static final String TIME = "bottle_time";
	public static final String STATION = "bottle_station";
	public static final String LABEL = "bottle_label";
	public static final String ID = "bottle_id";
	public static final String SAMPLELIST = "sampleList";

	public static final String[] c_BOTTLETABLE = {LATITUDE, LONGITUDE, WATERDEPTH, TIME, STATION, LABEL, ID};
}