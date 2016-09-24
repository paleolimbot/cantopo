package fwe.locations.kml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KMLEncloser extends Placemark {
	private String tagName ;
	private List<Placemark> contents ;
	
	public KMLEncloser(String tagName) {
		this.tagName = tagName ;
		contents = new ArrayList<Placemark>() ;
	}
	
	public KMLEncloser(String tagName, String name, String description, List<Placemark> wpts) {
		super(name, description);
		this.contents = wpts ;
		this.tagName = tagName ;
	}
	
	public void write(int level, BufferedWriter w) throws IOException {
		this.writeStartTag(level, w) ;
		this.writeContents(level, w) ;
		this.writeEndTag(level, w) ;
	}
	
	public void add(Placemark p) {
		contents.add(p) ;
	}
	
	public void remove(Placemark p) {
		contents.remove(p) ;
	}
	
	public void remove(int index) {
		contents.remove(index) ;
	}
	
	public int size() {
		return contents.size() ;
	}
	
	public void clear() {
		contents.clear() ;
	}
	
	protected void writeStartTag(int level, BufferedWriter w) throws IOException {
		indent(level, w) ;
		w.write("<" + tagName + ">\n") ;
		this.writeNameAndDescription(level+1, w) ;
	}
	
	protected void writeContents(int level, BufferedWriter w) throws IOException {
		for(int i=0; i<contents.size(); i++) {
			Placemark p = contents.get(i) ;
			if(p != null)
				p.write(level+1, w) ;
		}
	}
	
	protected void writeEndTag(int level, BufferedWriter w) throws IOException {
		indent(level, w) ;
		closeTag(w, level, tagName) ;
	}

	
	
}
