package ca.fwe.locations.gpx;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class GPXWritable {
	
	private static final SimpleDateFormat POINT_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	public abstract void write(int level, BufferedWriter w) throws IOException ;
	protected static void tabs(int num, BufferedWriter w) throws IOException {
		for(int i=0; i<num; i++)
			w.write("\t") ;
	}
	
	protected static String formatTime(long time) {
		return POINT_DATE_FORMATTER.format(new Date(time)) ;
	}
}
