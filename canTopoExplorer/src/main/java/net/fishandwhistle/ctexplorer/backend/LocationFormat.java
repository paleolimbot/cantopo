package net.fishandwhistle.ctexplorer.backend;

import java.text.DecimalFormat;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.Html;
import fwe.locations.geometry.LatLon;
import fwe.locations.utm.UTMRef;

public class LocationFormat {
	
	public static String formatLocationHtml(Context context, double lat, double lon) {
		String pref = PreferenceManager.getDefaultSharedPreferences(context).getString("xml_location_format", "0") ;
		if(pref.equals("0")) {
			//utm
			UTMRef utm = new LatLon(lat, lon).toUTM() ;
			String utmHtml = utm.getHtml() ;
			return utmHtml ;
		} else if(pref.equals("1")) {
			//degrees/dec min
			double[] degminLat = degmin(lat) ;
			double[] degminLon = degmin(lon) ;
			String latString = formatDegMin(lat, degminLat, new String[] {"S", "N"}) ;
			String lonString = formatDegMin(lon, degminLon, new String[] {"W", "E"}) ;
			return latString + " " + lonString ;
		} else if(pref.equals("2")) {
			//decimal degrees
			DecimalFormat f = new DecimalFormat("0.0000") ;
			return String.format("Lat: %s Lon: %s", f.format(lat), f.format(lon)) ;
		}
		return null ;
	}
	
	public static CharSequence formatLocation(Context context, double lat, double lon) {
		return Html.fromHtml(formatLocationHtml(context, lat, lon)) ;
	}

	private static String formatDegMin(double num, double[] degmin, String[] hemispheres) {
		int ind = 0 ;
		if(num >= 0)
			ind = 1 ;
		return String.format("%s°%s'%s", DecimalFormat.getIntegerInstance().format(degmin[0]),
				new DecimalFormat("00.00").format(degmin[1]), hemispheres[ind]) ;
	}
	
	private static double[] degmin(double num) {
		double pos = Math.abs(num) ;
		double deg = Math.floor(pos) ;
		double min = (pos - deg)*60.0 ;
		return new double[] {deg, min} ;
	}
}
