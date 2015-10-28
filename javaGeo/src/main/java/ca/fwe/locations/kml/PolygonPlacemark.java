package ca.fwe.locations.kml;

import java.io.BufferedWriter;
import java.io.IOException;

import ca.fwe.locations.geometry.Polygon;
import ca.fwe.locations.geometry.XY;

public class PolygonPlacemark extends Placemark {

	private Polygon polygon ;
	
	@Override
	public void write(int level, BufferedWriter w) throws IOException {
		writeTag(w, level, "Placemark", null, null) ;
		this.writeNameAndDescription(level+1, w) ;
		writeTag(w, level+1, "StyleUrl", null, "#default") ;
		writeTag(w, level+1, "Polygon", null, null) ;
		writeTag(w, level+2, "outerBoundaryIs", null, null) ;
		writeTag(w, level+3, "LinearRing", null, null) ;
		
		XY[] points = new XY[polygon.getCount() + 1] ;
		for(int i=0; i<polygon.getCount(); i++) {
			points[i] = polygon.getPoint(i) ;
		}
		points[polygon.getCount()] = points[0] ;
		
		writeCoordinates(w, level+4, points) ;
		closeTag(w, level+3, "LinearRing") ;
		closeTag(w, level+2, "outerBoundaryIs") ;
		closeTag(w, level+1, "Polygon") ;
		closeTag(w, level, "Placemark") ;
		
	}
	
	public Polygon getPolygon() {
		return polygon;
	}

	public void setPolygon(Polygon polygon) {
		this.polygon = polygon;
	}

}
