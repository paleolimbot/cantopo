package net.fishandwhistle.ctexplorer.webres;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import android.net.Uri;
import android.util.Log;
import ca.fwe.locations.geometry.Bounds;

public class WebGeocoder {

	private static final String REQUEST_PATTERN = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=AIzaSyCxeQKSja_ioW9Myc1DfWUUczGWCcIxldk" ;
	private static final String BOUNDS_PATTERN = "&bounds=%s,%s|%s,%s" ;
	private static HashMap<String, String> recentGeocodes = new HashMap<String, String>() ;
	private static int totalRequests = 0 ;

	public WebGeocoder() {

	}

	public JSON geocode(String locationName, int maxResults) throws IOException {
		return geocode(locationName, maxResults, null) ;
	}

	public JSON geocode(String locationName, int maxResults, Bounds b) throws IOException {

		if(recentGeocodes.containsKey(locationName)) {
			String s = recentGeocodes.get(locationName) ;
			Log.i("WebGeocoder", "returning previously cached info for " + locationName) ;
			try {
				return JSON.parse(s) ;
			} catch(IllegalStateException e) {
				Log.e("WebGeocoder", "JSON parse error", e) ;
				return null ;
			}
		} else {
			String bds = "" ;
			if(b != null)
				bds = String.format(BOUNDS_PATTERN, b.getMinY(), b.getMinX(), b.getMaxY(), b.getMaxX()) ;
			String encodedName = Uri.encode(locationName) ;
			String urlRequest = String.format(REQUEST_PATTERN, encodedName, bds) ;

			URL url = new URL(urlRequest) ;
			InputStream input = new BufferedInputStream(url.openStream(), 8192);
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			byte data[] = new byte[1024];
			int count;
			int totalCount = 0 ;
			while ((count = input.read(data)) != -1) {
				output.write(data, 0, count);
				totalCount += count ;
			}

			output.flush();
			totalRequests++ ;
			Log.i("WebGeocoder", "reponse returned " + totalCount + " bytes, total requests in session: " + totalRequests) ;
			String response = output.toString("UTF-8") ;
			output.close();
			input.close();
			try {
				JSON results = JSON.parse(response) ;
				recentGeocodes.put(locationName, response) ;
				return results ;
			} catch(IllegalStateException e) {
				Log.e("WebGeocoder", "JSON parse error", e) ;
				return null ;
			}
		}
	}

}
