package net.fishandwhistle.ctexplorer.maptools;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Vibrator;
import android.widget.Toast;
import fwe.locations.geometry.LatLon;
import fwe.locations.geometry.Vector;
import fwe.locations.utm.UTMRef;
import fwe.locations.utm.UTMZone;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class MeasureAreaTool extends MapTool {

	private List<LatLng> points ;
	private List<UTMRef> utm ;
	private Polygon mapPoly ;
	private OnNewMeasuredAreaListener listen ;
	private UTMZone zone ;
	private MarkerOptions startMarkerOptions ;
	private Marker startMarker ;
	private PolygonOptions baseOptions ;

	public interface OnNewMeasuredAreaListener {
		public void onNewMeasuredArea(double areaKm2, List<LatLng> points) ;
	}

	public MeasureAreaTool(Context c, GoogleMap m) {
		super(c, m);
		points = new ArrayList<LatLng>() ;
		utm = new ArrayList<UTMRef>() ;
		startMarkerOptions = new MarkerOptions() ;
		baseOptions = new PolygonOptions() ;
	}

	public MarkerOptions getStartMarkerOptions() {
		return startMarkerOptions ;
	}

	public PolygonOptions getPolygonOptions() {
		return baseOptions ;
	}

	public void setOnNewMeasuredAreaListener(OnNewMeasuredAreaListener l) {
		listen = l ;
	}

	@Override
	protected void onSetEnabled(boolean flag) {
		if(!flag) {
			this.reset();
		}
	}

	@Override
	public void onMapClick(LatLng arg0) {
		LatLon ll = new LatLon(arg0.latitude, arg0.longitude) ;
		UTMRef utmRef = ll.toUTM() ;
		if(zone != null) {
			if(!zone.equals(utmRef.getUTMZone())) {
				Toast.makeText(context, "Cannot calculate areas with multiple UTM Zones. Sorry!", Toast.LENGTH_SHORT).show();
			} else {
				points.add(arg0) ;
				utm.add(utmRef) ;
				if(utm.size() >= 3) {
					recalculateArea() ;
				}
				
				if(mapPoly != null) {
					mapPoly.setPoints(points);
				} else {
					PolygonOptions plo = new PolygonOptions() ;
					plo.fillColor(baseOptions.getFillColor()) ;
					plo.strokeColor(baseOptions.getStrokeColor()) ;
					plo.strokeWidth(baseOptions.getStrokeWidth()) ;
					plo.zIndex(baseOptions.getZIndex()) ;
					plo.addAll(points) ;
					mapPoly = map.addPolygon(plo) ;
				}
				
			}
		} else {
			//new series, display marker
			zone = utmRef.getUTMZone() ;
			points.add(arg0) ;
			utm.add(utmRef) ;
			
			if(startMarkerOptions.getIcon() != null) {
				startMarkerOptions.position(arg0) ;
				startMarker = map.addMarker(startMarkerOptions) ;
			}
		}
	}

	private void recalculateArea() {
		double cumSum = 0 ;
		for(int i=0; i<utm.size(); i++) {
			UTMRef pt1 = utm.get(i) ;
			UTMRef pt2 ;
			if(i == utm.size()-1)
				pt2 = utm.get(0) ;
			else
				pt2 = utm.get(i+1) ;
			Vector v1 = new Vector(pt1) ;
			Vector v2 = new Vector(pt2) ;
			double toAdd = Vector.crossProduct(v1, v2).k() ;
			cumSum += toAdd ;
		}
		cumSum = Math.abs(cumSum) * 0.5 ;
		if(listen != null)
			listen.onNewMeasuredArea(cumSum / 1e6, points);
	}

	@Override
	public void onMapLongClick(LatLng arg0) {
		this.reset();
		Vibrator v = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE) ;
		v.vibrate(100);
	}

	@Override
	public boolean onMarkerClick(Marker ll) {
		return false;
	}

	private void reset() {
		points.clear();
		utm.clear();
		zone = null ;
		if(mapPoly != null) {
			mapPoly.remove();
			mapPoly = null ;
		}
		if(startMarker != null) {
			startMarker.remove();
			startMarker = null ;
		}
		if(listen != null)
			listen.onNewMeasuredArea(0, points);

	}

}
