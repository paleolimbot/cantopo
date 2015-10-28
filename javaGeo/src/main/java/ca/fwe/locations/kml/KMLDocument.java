package ca.fwe.locations.kml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class KMLDocument extends KMLEncloser {
	public static final String KML_HEADER_V1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" ;

	public KMLDocument() {
		super("Document") ;
	}
	
	public KMLDocument(String name, String description, ArrayList<Placemark> contents) {
		super("Document", name, description, contents);
		
	}
	
	public void write(BufferedWriter w) throws IOException {
		write(0, w) ;
	}
	
	protected void writeStartTag(int level, BufferedWriter w) throws IOException {
		w.write(KML_HEADER_V1) ;
		super.writeStartTag(level, w) ;
	}
	
	protected void writeEndTag(int level, BufferedWriter w) throws IOException {
		super.writeEndTag(level, w) ;
		w.write("</kml>") ;
		w.flush() ;
		w.close() ;
	}

	public void writeFile(File f) throws IOException {
		BufferedWriter w = new BufferedWriter(new FileWriter(f)) ;
		this.write(w);
		w.close();
	}
	
}
