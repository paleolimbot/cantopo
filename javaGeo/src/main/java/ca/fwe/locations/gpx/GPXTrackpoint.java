package ca.fwe.locations.gpx;

import ca.fwe.locations.geometry.LatLon;

public class GPXTrackpoint extends GPXWaypoint {

	public GPXTrackpoint(LatLon latlon, long time) {
		super(latlon, time);
		tagName = "trkpt" ;
	}

}
