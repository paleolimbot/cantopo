package ca.fwe.locations.utm;

import java.io.Serializable;


public class UTMZone implements Serializable {

	private static final long serialVersionUID = 7545588814719178029L;
	
	private int zone ;
	private String hemisphere ;

	private static final int MIN_ZONE = 1 ;
	private static final int MAX_ZONE = 60 ;
	public static final String HEMISPHERE_N = "N" ;
	public static final String HEMISPHERE_S = "S" ;

	public UTMZone(int zone, String hemisphere) {
		if(zone >= MIN_ZONE && zone <= MAX_ZONE && (hemisphere.equals(HEMISPHERE_N) || hemisphere.equals(HEMISPHERE_S))) {
			this.zone = zone ;
			this.hemisphere = hemisphere.toUpperCase() ;
		} else {
			throw new IllegalArgumentException() ;
		}
	}

	public boolean equals(Object otherObject) {
		if(otherObject instanceof UTMZone) {
			if(this.toString().equals(otherObject.toString())) {
				return true ;
			} else {
				return false ;
			}
		} else {
			return false ;
		}
	}

	public int getZone() {
		return this.zone ;
	}

	public String getHemisphere() {
		return this.hemisphere ;
	}

	public String toString() {
		return zone + hemisphere ;
	}

	public static UTMZone valueOf(String zoneString) {
		if(zoneString != null) {
			String trimmed = zoneString.trim() ;
			if(trimmed.length() == 2) {
				try {
					int zone = new Integer(trimmed.substring(0, 1)) ;
					String hemisphere = trimmed.substring(1,2) ;
					return new UTMZone(zone, hemisphere) ;
				} catch (NumberFormatException e) {
					return null ;
				}
			} else if(trimmed.length() == 3) {
				try {
					int zone = new Integer(trimmed.substring(0,2)) ;
					String hemisphere = trimmed.substring(2,3) ;
					return new UTMZone(zone, hemisphere) ;
				} catch (NumberFormatException e) {
					return null ;
				}
			} else {
				return null ;
			}
		} else {
			return null ;
		}
	}
}
