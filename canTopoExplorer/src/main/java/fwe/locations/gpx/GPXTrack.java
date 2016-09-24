package fwe.locations.gpx;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GPXTrack extends GPXWritable {

	private static final String NAME_FORMAT = "<name>%s</name>\n" ;
	private static final String DESC_FORMAT = "<desc>%s</desc>\n" ;
	
	public String name ;
	public String description ;
	public List<GPXTrackpoint> points = new ArrayList<GPXTrackpoint>() ;
	
	@Override
	public void write(int level, BufferedWriter w) throws IOException {
		tabs(level, w) ;
		w.write("<trk>\n") ;
		if(name != null) {
			tabs(level+1, w) ;
			w.write(String.format(NAME_FORMAT, name)) ;
		}
		if(description != null) {
			tabs(level+1, w) ;
			w.write(String.format(DESC_FORMAT, description)) ;
		}
		if(points.size() > 0) {
			tabs(level+1, w) ;
			w.write("<trkseg>\n") ;
			for(GPXTrackpoint t: points) {
				t.write(level+2, w);
			}
			tabs(level+1, w) ;
			w.write("</trkseg>\n") ;
		}
		tabs(level, w) ;
		w.write("</trk>\n") ;
	}
	
}
