package ca.fwe.locations.utm;

import java.io.Serializable;

import uk.me.jstott.jcoord.LatLng;
import ca.fwe.locations.geometry.LatLon;
import ca.fwe.locations.geometry.XY;
import ca.fwe.mathutil.FweMath;

public class UTMRef extends XY implements Serializable {

	private static final long serialVersionUID = 7218727558600713993L;
	
	public static final String HEMISPHERE_N = UTMZone.HEMISPHERE_N ;
	public static final String HEMISPHERE_S = UTMZone.HEMISPHERE_S ;
	private static final double MIN_EASTING = 0.0 ;
	private static final double MAX_EASTING = 1000000.0 ;
	private static final double MIN_NORTHING = 0.0 ;
	private static final double MAX_NORTHING = 10000000.0 ;


	private UTMZone zone ;

	public UTMRef(double easting, double northing, int zone, String hemisphere) {
		this(easting, northing, new UTMZone(zone, hemisphere)) ;
	}

	public UTMRef(double easting, double northing, UTMZone utmZone) {
		super(easting, northing) ;
		this.zone = utmZone ;
		if(zone != null) {
			this.zone = utmZone ;
			if(!(isValidEasting(easting) && isValidNorthing(northing))) {
				throw new IllegalArgumentException("Illegal easting or northing: " + easting + ", " + northing) ;
			}
		} else {
			throw new IllegalArgumentException() ;
		}
	}

	public LatLon toLatLon() {
		char lngZone ;
		if(this.getHemisphere().equals(UTMZone.HEMISPHERE_N)) {
			//first northern hemisphere zone
			lngZone = 'N' ;
		} else {
			//first southern hemisphere zone
			lngZone = 'M' ;
		}

		LatLng jstotRef =  new uk.me.jstott.jcoord.UTMRef(this.getEasting(), this.getNorthing(), lngZone, this.getZone()).toLatLng() ;
		return new LatLon (jstotRef.getLat(), jstotRef.getLng()) ;
	}

	public UTMZone getUTMZone() {
		return zone ;
	}

	public int getZone() {
		return this.getUTMZone().getZone() ;
	}

	public String getHemisphere() {
		return this.getUTMZone().getHemisphere() ;
	}

	public double getEasting() {
		return this.x();
	}

	public double getNorthing() {
		return this.y();
	}
	
	public boolean equals(Object o) {
		if(o instanceof UTMRef) {
			if(super.equals(o) && ((UTMRef)o).getUTMZone().equals(this.getUTMZone())) {
				return true ;
			} else {
				return false ;
			}
		} else {
			return false ;
		}
	}
	
	public String toString() {
		String strEast = new Long(Math.round(getEasting())).toString() ;
		String strNorth = new Long(Math.round(getNorthing())).toString() ;
		return strEast + "E " + strNorth + "N" + " " + this.getUTMZone().toString() ;
	}
	
	public String getHtml() {
		return bold(getStringEasting()) + "E " + bold(getStringNorthing()) + "N " +  getUTMZone().toString() ;
	}
	
	public String getStringEasting() {
		return new Long(Math.round(getEasting())).toString() ;
	}
	
	public String getStringNorthing() {
		return new Long(Math.round(getNorthing())).toString() ;
	}
	
	public static String bold(String eastingOrNorthing) {
		if(eastingOrNorthing.length() >= 5) {
			int length = eastingOrNorthing.length() ;
			String first = eastingOrNorthing.substring(0, length - 5) ;
			String km = eastingOrNorthing.substring(length - 5, length-3) ;
			String m = eastingOrNorthing.substring(length - 3) ;
			return first + "<b>" + km + "</b>" + m ;
		} else {
			return bold(FweMath.addZeros(eastingOrNorthing, 5)) ;
		}
	}
	
	public static String addSpaces(String eastingOrNorthing) {
		if(eastingOrNorthing.length() >= 5) {
			int length = eastingOrNorthing.length() ;
			String first = eastingOrNorthing.substring(0, length - 5) ;
			String km = eastingOrNorthing.substring(length - 5, length-3) ;
			String m = eastingOrNorthing.substring(length - 3) ;
			return first + " " + km + " " + m ;
		} else {
			return bold(FweMath.addZeros(eastingOrNorthing, 5)) ;
		}
	}
	
	public String encode() {
		return this.getUTMZone().toString() + " " + this.getEasting() + "E " + this.getNorthing() + "N" ;
	}


	public static boolean isValidEasting(double easting) {
		if(easting >= MIN_EASTING && easting <= MAX_EASTING) {
			return true ;
		} else {
			return false ;
		}
	}

	public static boolean isValidNorthing(double northing) {
		if(northing >= MIN_NORTHING && northing <= MAX_NORTHING) {
			return true ;
		} else {
			return false ;
		}
	}

	public static UTMRef valueOf(String stringReference) {
		// 11N 702111E 5661430N 
		if(stringReference != null) {
			String[] firstSplit = stringReference.split(" ") ;
			if(firstSplit.length == 3) {
				String zoneString = firstSplit[0] ;
				String eastingString = firstSplit[1] ;
				String northingString = firstSplit[2] ;

				UTMZone utmZone = UTMZone.valueOf(zoneString) ;
				if(utmZone != null) {
					try {
						double easting = new Double(eastingString.substring(0, eastingString.length() - 1)) ;
						double northing = new Double(northingString.substring(0, northingString.length() - 1)) ;
						return new UTMRef(easting, northing, utmZone) ;
					} catch (NumberFormatException e) {
						return null ;
					}
				} else {
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
