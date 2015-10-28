package net.fishandwhistle.ctexplorer.backend;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GPSLocationFetcher {
	public static final int updateFrequency = 2000 ; //every 2 seconds, used for GPS
	
	private LocationManager locationManager ;
	private LocationListener activityListener ;
	private Location lastFix ;
	
	public GPSLocationFetcher(Context context, boolean useNetwork) {
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);	
		Location lastNetworkFix = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) ;
		Location lastGPSFix = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ;
		
		this.activityListener = new LocationListener() { //blank listener
			public void onLocationChanged(Location location) {}
			public void onProviderDisabled(String provider) {}
			public void onProviderEnabled(String provider) {}
			public void onStatusChanged(String provider, int status, Bundle extras) {}
		} ;
		
		if(lastGPSFix != null && lastNetworkFix != null) {
			if(lastGPSFix.getTime() > lastNetworkFix.getTime()) {
				//use most recent update
				lastFix = lastGPSFix ;
			} else {
				lastFix = lastNetworkFix ;
			}
		}
	}
	
	public void setLocationListener(LocationListener listener) {
		this.activityListener = listener ;
	}
	
	private LocationListener gpsLocationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			disableNetworkUpdates() ;
			updateLocation(location) ;
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {}

		public void onProviderEnabled(String provider) {
			
		}

		public void onProviderDisabled(String provider) {
			//to make sure network updates are now used
			lastFix = null ;
			enableNetworkUpdates() ;
		}
	};
	
	private LocationListener networkLocationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			updateLocation(location) ;
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {}

		public void onProviderEnabled(String provider) {}

		public void onProviderDisabled(String provider) {}
	};
	
	private synchronized void updateLocation(Location location) {
		if(lastFix != null) {
			if(location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
				//always use GPS fix
				lastFix = location ;
				activityListener.onLocationChanged(location) ;
			} else {
				//network listener: use only if last fix was network
				if(lastFix.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
					lastFix = location ;
					activityListener.onLocationChanged(location) ;
				}
			}
		} else {
			lastFix = location ;
			activityListener.onLocationChanged(location) ;
		}
		
	}
	
	public void enableUpdates() {	
		enableGPSUpdates() ;
		enableNetworkUpdates() ;
	}
	
	private void enableGPSUpdates() {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateFrequency, 0, gpsLocationListener);

	}
	
	private void enableNetworkUpdates() {
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, updateFrequency, 0, networkLocationListener) ;

	}
	
	private void disableGPSUpdates() {
		locationManager.removeUpdates(gpsLocationListener) ;
	}
	
	private void disableNetworkUpdates() {
		locationManager.removeUpdates(networkLocationListener) ;

	}
	
	public void disableUpdates() {
		disableGPSUpdates() ;
		disableNetworkUpdates() ;
	}
	
	public Location getFix() {
		return lastFix ;
	}
	
}
