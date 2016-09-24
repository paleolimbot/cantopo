package fwe.locations.geometry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import fwe.fwx.FWXUtils;
import jcoord.LatLng;
import fwe.fwx.FWXObject;
import fwe.locations.utm.UTMRef;
import fwe.mathutil.FweMath;

public class LatLon extends XY implements FWXObject {
	
	public static final String TAG_NAME = "latlon" ;
	public static final String ATTRIBUTE_LAT = "lat" ;
	public static final String ATTRIBUTE_LON = "lon" ;
	public static final String ATTRIBUTE_ELEVATION = "elevation" ;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9135189774267099978L;
	private static final double MIN_LAT = -90.0 ;
	private static final double MAX_LAT = 90.0 ;
	private static final double MIN_LON = -180.0 ;
	private static final double MAX_LON = 180.0 ;
	private static final DecimalFormat STRING_NUMBER_FORMAT = new DecimalFormat("###.000") ;
	
	public LatLon(double lat, double lon) {
		super(lon, lat) ;
		if(!(isValidLat(lat) && isValidLon(lon))) {
			throw new IllegalArgumentException() ;
		}
	}
	
	public LatLon(double lat, double lon, double elevation) {
		super(lon, lat, elevation) ;
		if(!(isValidLat(lat) && isValidLon(lon))) {
			throw new IllegalArgumentException() ;
		}
	}

	public LatLon(XY point) {
		super(point.x(), point.y(), point.z()) ;
	}

	public double getLat() {
		return this.y() ;
	}

	public double getLon() {
		return this.x() ;
	}

	public double getElevation() {
		return this.z() ;
	}
	
	public double distanceTo(LatLon otherPoint) {
		return distance(this, otherPoint) ;
	}
	
	public boolean equals(Object o) {
		if(o instanceof LatLon) {
			return super.equals(o) ;
		} else {
			return false ;
		}
	}
	
	public UTMRef toUTM() {
		jcoord.UTMRef jUTM = new LatLng(this.getLat(), this.getLon()).toUTMRef() ;
		if(this.getLat() >= 0) {
			return new UTMRef(jUTM.getEasting(), jUTM.getNorthing(), jUTM.getLngZone(), UTMRef.HEMISPHERE_N) ;
		} else {
			return new UTMRef(jUTM.getEasting(), jUTM.getNorthing(), jUTM.getLngZone(), UTMRef.HEMISPHERE_S) ;
		}
	}

	private static boolean isValidLat(double lat) {
		if(lat >= MIN_LAT && lat <= MAX_LAT) {
			return true ;
		} else {
			return false ;
		}
	}

	private static boolean isValidLon(double lon) {
		if(lon >= MIN_LON && lon <= MAX_LON) {
			return true ;
		} else {
			return false ;
		}
	}

	public static boolean isValid(LatLon latlon) {
		if(latlon != null) {
			if(isValidLat(latlon.getLat()) && isValidLon(latlon.getLon())) {
				return true ;
			} else {
				return false ;
			}
		} else {
			return false ;
		}
	}

	public static LatLon valueOf(String string) {
		if(string != null) {
			String[] parts = string.trim().split(",") ;
			if(parts.length == 2) {
				try {
					double lat = new Double(parts[0]) ;
					double lon = new Double(parts[1]) ;
					return new LatLon(lat, lon) ;
				} catch(NumberFormatException e) {
					return null ;
				}
			} else {
				return null ;
			}
		} else {
			return null ;
		}
	}

	public String toString() {
		return getStringLatDec() + ", " + getStringLonDec() ;
	}
	
	public String toStringDMS() {
		return getStringLatDMS() + " " + getStringLonDMS() ;
	}
	
	public String getStringLatDMS() {
		String hemisphereLat ;
		if(getLat() >= 0) hemisphereLat = " N"; else hemisphereLat = " S" ;
		return dms(getLat()) + hemisphereLat ;
	}
	
	public String getStringLonDMS() {
		String hemisphereLon ;
		if(getLon() >=0) hemisphereLon = " E"; else hemisphereLon = " W" ;
		return dms(getLon()) + hemisphereLon ;
	}
	
	public String getStringLatDec() {
		return STRING_NUMBER_FORMAT.format(getLat()) ;
	}
	
	public String getStringLonDec() {
		return STRING_NUMBER_FORMAT.format(getLon()) ;
	}
	
	public int getLatDegrees() {
		return degrees(this.getLat()) ;
	}
	
	public int getLonDegrees() {
		return degrees(this.getLon()) ;
	}
	
	public int getLatMinutes() {
		return minutes(this.getLat()) ;
	}
	
	public int getLonMinutes() {
		return minutes(this.getLon()) ;
	}
	
	public int getLatSeconds() {
		return seconds(this.getLat()) ;
	}
	
	public int getLonSeconds() {
		return seconds(this.getLon()) ;
	}
	
	public double getLonSecondsDouble() {
		return secondsDouble(this.getLon()) ;
	}
	
	public double getLatSecondsDouble() {
		return secondsDouble(this.getLat()) ;
	}
	
	public static String dms(double number) {
		number = Math.abs(number) ;
		int deg = (int)Math.floor(number) ;
		double decimalPortion = number - deg ;
		double minutes = 60 * decimalPortion ;
		int min = (int)Math.floor(minutes) ;
		decimalPortion = minutes - min ;
		double seconds = 60 * decimalPortion ;
		int sec = (int) Math.round(seconds) ;
		
		return deg + "° " + FweMath.addZeros(min,2) + "' " + FweMath.addZeros(sec, 2) + "\"" ;
	}
	
	private static int degrees(double number) {
		number = Math.abs(number) ;
		int deg = (int)Math.floor(number) ;
		return deg ;
	}
	
	private static int minutes(double number) {
		number = Math.abs(number) ;
		int deg = (int)Math.floor(number) ;
		double decimalPortion = number - deg ;
		double minutes = 60 * decimalPortion ;
		int min = (int)Math.floor(minutes) ;
		return min ;
	}
	
	private static int seconds(double number) {
		number = Math.abs(number) ;
		int deg = (int)Math.floor(number) ;
		double decimalPortion = number - deg ;
		double minutes = 60 * decimalPortion ;
		int min = (int)Math.floor(minutes) ;
		decimalPortion = minutes - min ;
		double seconds = 60 * decimalPortion ;
		int sec = (int) Math.round(seconds) ;
		return sec ;
	}
	
	private static double secondsDouble(double number) {
		number = Math.abs(number) ;
		int deg = (int)Math.floor(number) ;
		double decimalPortion = number - deg ;
		double minutes = 60 * decimalPortion ;
		int min = (int)Math.floor(minutes) ;
		decimalPortion = minutes - min ;
		double seconds = 60 * decimalPortion ;
		return seconds ;
	}
	
	public String encode() {
		return this.getLat() + ", " + this.getLon() ;
	}
	
	public static LatLon from(int latDeg, int latMin, int latSec, int lonDeg, int lonMin,
			int lonSec) {
		if(validDeg(latMin) && validDeg(latSec) && validDeg(lonMin) && validDeg(lonSec)) {
			double lat = Math.abs(latDeg) + (latMin / 60.0) + (latSec / 3600.0) ;
			double lon = Math.abs(lonDeg) + (lonMin / 60.0) + (lonSec / 3600.0) ;
			if(latDeg < 0)
				lat *= -1 ;
			if(lonDeg < 0)
				lon *= -1 ;
			return new LatLon(lat, lon) ;
		} else {
			throw new IllegalArgumentException() ;
		}
	}

	private static boolean validDeg(int number) {
		if(number <= 60 && number >= 0) {
			return true ;
		} else {
			return false ;
		}
	}
	
	private static double distance(LatLon a, LatLon b) {
		LatLng p1 = new LatLng(a.getLat(), a.getLon()) ;
		LatLng p2 = new LatLng(b.getLat(), b.getLon()) ;
		return p1.distance(p2) ;
	}

	@Override
	public void write(int level, BufferedWriter w) throws IOException {
		FWXUtils.indent(level, w) ;
		FWXUtils.open(this, w) ;
		w.write(" ") ;
		FWXUtils.writeAttribute(ATTRIBUTE_LAT, getLat(), w) ;
		w.write(" ") ;
		FWXUtils.writeAttribute(ATTRIBUTE_LON, getLon(), w) ;
		if(hasZ()) {
			FWXUtils.writeAttribute(ATTRIBUTE_ELEVATION, getElevation(), w) ;
			w.write(" ") ;
		}
		w.write(" />\n") ; 
	}

	@Override
	public FWXHandler getSAXHandler() {
		return new FWXObject.FWXHandler() {
			
			@Override
			public void onValue(String value) {}
			
			@Override
			public void onProperty(String propertyName, String value) {}
			
			@Override
			public void onAttribute(String name, String value) {
				if(name.equals(ATTRIBUTE_LAT)) {
					LatLon.super.getSAXHandler().onAttribute(ATTRIBUTE_Y, value) ;
				} else if(name.equals(ATTRIBUTE_LON)) {
					LatLon.super.getSAXHandler().onAttribute(ATTRIBUTE_X, value) ;
				} else if(name.equals(ATTRIBUTE_ELEVATION)) {
					LatLon.super.getSAXHandler().onAttribute(ATTRIBUTE_Z, value) ;
				}
			}
			public FWXObject getObject(String tagName) {return null ;} 
		};
	}

	@Override
	public String getTagName() {
		return TAG_NAME ;
	}
	
}
