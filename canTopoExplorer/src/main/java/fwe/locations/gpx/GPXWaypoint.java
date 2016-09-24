package fwe.locations.gpx;

import java.io.BufferedWriter;
import java.io.IOException;

import fwe.locations.geometry.LatLon;

public class GPXWaypoint extends GPXWritable {

	private static final String OPEN_FORMAT = "<%s lat=\"%s\" lon=\"%s\">\n" ;
	private static final String ELE_FORMAT = "<ele>%s</ele>\n" ;
	private static final String NAME_FORMAT = "<name>%s</name>\n" ;
	private static final String DESC_FORMAT = "<desc>%s</desc>\n" ;
	private static final String TIME_FORMAT = "<time>%s</time>\n" ;
	
	public String name ;
	public String description ;
	public LatLon point ;
	public long time ;
	protected String tagName ;
	
	public GPXWaypoint(LatLon latlon, long time) {
		tagName = "wpt" ;
		name = null ;
		description = null ;
		point = latlon ;
		this.time = time ;
	}
	
	@Override
	public void write(int level, BufferedWriter w) throws IOException {
		tabs(level, w);
		w.write(String.format(OPEN_FORMAT, tagName, point.getLat(), point.getLon())) ;
		tabs(level+1, w) ;
		String date = formatTime(time) ;
		w.write(String.format(TIME_FORMAT, date)) ;
		if(point.hasZ()) {
			tabs(level+1, w) ;
			w.write(String.format(ELE_FORMAT, point.getElevation())) ;
		}
		if(name != null) {
			tabs(level+1, w) ;
			w.write(String.format(NAME_FORMAT, name)) ;
		}
		if(description != null) {
			tabs(level+1, w) ;
			w.write(String.format(DESC_FORMAT, description)) ;
		}
		tabs(level, w) ;
		w.write("</"+ tagName + ">\n") ;
	}
	
}
