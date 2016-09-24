package fwe.locations.kml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import fwe.locations.geometry.XY;

public class LinePlacemark extends Placemark {

	private List<? extends XY> pts ;
	
	@Override
	public void write(int level, BufferedWriter w) throws IOException {
		writeTag(w, level, "Placemark", null, null) ;
		this.writeNameAndDescription(level+1, w) ;
		writeTag(w, level+1, "StyleUrl", null, "#default") ;
		writeTag(w, level+1, "LineString", null, null) ;
		
		XY[] points = pts.toArray(new XY[0]) ;
		
		writeCoordinates(w, level+2, points) ;
		closeTag(w, level+1, "LineString") ;
		closeTag(w, level, "Placemark") ;
		
	}
	
	public List<? extends XY> getPoints() {
		return pts;
	}

	public void setPoints(List<? extends XY> points) {
		this.pts = points;
	}

}
