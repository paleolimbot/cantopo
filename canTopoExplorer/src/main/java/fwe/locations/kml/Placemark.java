package fwe.locations.kml;

import java.io.BufferedWriter;
import java.io.IOException;

import fwe.locations.geometry.XY;

public abstract class Placemark {
	private String name ;
	private String description ;
	//LookAt, styleUrl not implemented
	
	public Placemark() {
		
	}
	
	public void setName(String name) {
		this.name = name ;
	}
	
	public void setDescription(String description) {
		this.description = description ; 
	}
	
	public Placemark(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
	
	public void writeNameAndDescription(int level, BufferedWriter w) throws IOException {
		if(this.getName() != null)
			writeTag(w, level, "name", null, this.getName()) ;
		if(this.getDescription() != null) ;
			writeTag(w, level, "description", null, "<![CDATA[" + this.getDescription() + "]]>") ;
	}
	
	public abstract void write(int level, BufferedWriter w) throws IOException ;
	
	public static void writeTag(BufferedWriter w, int level, String name, String[][] attributes, String value) throws IOException {
		indent(level, w) ;
		if(attributes == null || attributes.length == 0) {
			w.write("<" + name + ">") ;
		} else {
			w.write("<" + name ) ;
			for(String[] attribute: attributes) {
				w.write(" " + attribute[0] + "=\"" + attribute[1] + "\"") ;
			}
			w.write(">") ;
		}
		if(value != null) {
			w.write(value) ;
			closeTag(w, 0, name) ;
		} else {
			w.write("\n") ;
		}
	}
	
	public static void writeTag(BufferedWriter w, int level, String name, String[][] attributes, double value) throws IOException {
		writeTag(w, level, name, attributes, Double.valueOf(value).toString()) ;
	}
	
	public static void closeTag(BufferedWriter w, int level, String name) throws IOException {
		indent(level, w) ;
		w.write("</" + name + ">\n") ;
	}
	
	public static void indent(int level, BufferedWriter w) throws IOException {
		for(int i=0; i<level; i++)
			w.write("\t") ;
	}
	
	public static void writeCoordinates(BufferedWriter w, int level, XY[] xys) throws IOException {
		writeTag(w, level, "coordinates", null, null) ;
		indent(level+1, w) ;
		String sp = "" ;
		for(XY xy: xys) {
			w.write(sp) ;
			writeXY(w, xy) ;
			sp = " " ;
		}
		closeTag(w, level, "coordinates") ;		
	}
	
	private static void writeXY(BufferedWriter w, XY xy) throws IOException {
		w.write(xy.x() + "," + xy.y() + "," + xy.z()) ;
	}
	
}
