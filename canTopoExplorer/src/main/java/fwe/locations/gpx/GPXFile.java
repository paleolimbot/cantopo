package fwe.locations.gpx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GPXFile {

	public List<GPXWritable> content = new ArrayList<GPXWritable>() ;

	public void write(BufferedWriter w) throws IOException {
		w.write("<?xml version=\"1.0\"?>\n<gpx version=\"1.1\" creator=\"JavaGeo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/1\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n") ;
		for(GPXWritable gpw: content) {
			gpw.write(0, w);
		}
		w.write("</gpx>") ;
	}
	
	public void writeFile(File f) throws IOException {
		BufferedWriter w = new BufferedWriter(new FileWriter(f)) ;
		this.write(w);
		w.close();
	}
	
}
