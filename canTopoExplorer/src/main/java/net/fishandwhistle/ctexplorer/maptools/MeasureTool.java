package net.fishandwhistle.ctexplorer.maptools;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Vibrator;
import ca.fwe.locations.geometry.LatLon;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MeasureTool extends MapTool {

	List<LatLng> points ;
	LatLon lastPoint ;
	double cumulativeLength ;
	Polyline polyline ;
	PolylineOptions baseOptions ;
	MarkerOptions startMarkerOptions ;
	Marker startMarker ;
	private OnNewMeasuredDistanceListener listener ;
	
	public interface OnNewMeasuredDistanceListener {
		public void onNewMeasuredDistance(double distanceKm, List<LatLng> points) ;
	}
	
	public MeasureTool(Context c, GoogleMap m) {
		super(c, m);
		points = new ArrayList<LatLng>() ;
		baseOptions = new PolylineOptions() ;
		startMarkerOptions = new MarkerOptions() ;
		this.reset();
	}

	public MarkerOptions getStartMarkerOptions() {
		return startMarkerOptions ;
	}
	
	public PolylineOptions getPolylineOptions() {
		return baseOptions ;
	}
	
	public void setOnNewMeasuredDistanceListener(OnNewMeasuredDistanceListener l) {
		listener = l ;
	}
	
	@Override
	protected void onSetEnabled(boolean flag) {
		if(!flag && polyline != null) {
			this.reset();
		}
	}

	@Override
	public void onMapClick(LatLng arg0) {
		points.add(arg0) ;
		LatLon ll = new LatLon(arg0.latitude, arg0.longitude) ;
		if(lastPoint != null) {
			cumulativeLength += ll.distanceTo(lastPoint) ;
		} else {
			//new series, display marker
			if(startMarkerOptions.getIcon() != null) {
				startMarkerOptions.position(arg0) ;
				startMarker = map.addMarker(startMarkerOptions) ;
			}
		}
		lastPoint = ll ;

		if(polyline != null) {
			polyline.setPoints(points);
		} else {
			PolylineOptions plo = new PolylineOptions() ;
			plo.color(baseOptions.getColor()) ;
			plo.width(baseOptions.getWidth()) ;
			plo.zIndex(baseOptions.getZIndex()) ;
			plo.addAll(points) ;
			polyline = map.addPolyline(plo) ;
		}
		if(listener != null)
			listener.onNewMeasuredDistance(cumulativeLength, points);
	}

	@Override
	public void onMapLongClick(LatLng arg0) {
		this.reset();
		Vibrator v = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE) ;
		v.vibrate(100);
	}

	public void reset() {
		points.clear();
		cumulativeLength = 0 ;
		lastPoint = null ;
		if(polyline != null) {
			polyline.remove();
			polyline = null ;
		}
		if(startMarker != null) {
			startMarker.remove();
			startMarker = null ;
		}
		if(listener != null)
			listener.onNewMeasuredDistance(0, new ArrayList<LatLng>());
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		//ignore clicks to markers
		return false ;
	}

}
