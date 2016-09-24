package fwe.locations.gpx;

import fwe.locations.geometry.LatLon;

public class GPXTrackpoint extends GPXWaypoint {

	public GPXTrackpoint(LatLon latlon, long time) {
		super(latlon, time);
		tagName = "trkpt" ;
	}

}
