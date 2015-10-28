package ca.fwe.fwx;

import java.io.BufferedWriter;
import java.io.IOException;

public class FWXUtils {

	public static void indent(int num, BufferedWriter w) throws IOException {
		for(int i=0; i<num; i++)
			w.write("\t") ;
	}
	
	public static void close(FWXObject obj, BufferedWriter w) throws IOException {
		w.write("</" + obj.getTagName() + ">\n") ;
	}
	
	public static void open(FWXObject obj, BufferedWriter w) throws IOException {
		w.write("<" + obj.getTagName()) ;
	}
	
	public static void writeAttribute(String name, String value, BufferedWriter w) throws IOException {
		w.write(name + "=\"" + value + "\"") ;
	}
	
	public static void writeAttribute(String name, long value, BufferedWriter w) throws IOException {
		w.write(name + "=\"" + value + "\"") ;
	}
	
	public static void writeAttribute(String name, double value, BufferedWriter w) throws IOException {
		w.write(name + "=\"" + value + "\"") ;
	}
	
	public static void writeAttribute(String name, boolean value, BufferedWriter w) throws IOException {
		w.write(name + "=\"" + value + "\"") ;
	}
	
	public static void writeProperty(int level, String name, String value, BufferedWriter w) throws IOException {
		new FWXProperty(name, value).write(level, w) ;
	}
	
	public static void writeProperty(int level, String name, long value, BufferedWriter w) throws IOException {
		writeProperty(level, name, Long.valueOf(value).toString(), w) ;
	}
	
	public static void writeProperty(int level, String name, double value, BufferedWriter w) throws IOException {
		writeProperty(level, name, Double.valueOf(value).toString(), w) ;
	}
	
	public static void writeProperty(int level, String name, boolean value, BufferedWriter w) throws IOException {
		writeProperty(level, name, Boolean.valueOf(value).toString(), w) ;
	}
	
}
