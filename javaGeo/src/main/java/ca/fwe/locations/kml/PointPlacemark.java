package ca.fwe.locations.kml;

import java.io.BufferedWriter;
import java.io.IOException;

import ca.fwe.locations.geometry.LatLon;

public class PointPlacemark extends Placemark {
	private LatLon point ;
	
	public PointPlacemark(String name, String description, LatLon point) {
		super(name, description);
		this.point = point ;
		if(point == null)
			throw new IllegalArgumentException() ;
	}
	
	public LatLon getPoint() {
		return point ;
	}
	
	public void write(int level, BufferedWriter w) throws IOException {
		writeTag(w, level, "Placemark", null, null) ;
		this.writeNameAndDescription(level+1, w) ;
		writeTag(w, level+1, "Point", null, null) ;
		writeTag(w, level+2, "coordinates", null, this.getPoint().getLon() + "," + this.getPoint().getLat() + "," + this.getPoint().getElevation()) ;
		closeTag(w, level+1, "Point") ;
		closeTag(w, level, "Placemark") ;
	}
}
