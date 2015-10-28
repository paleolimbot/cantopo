package net.fishandwhistle.ctexplorer.tiles;

import net.fishandwhistle.ctexplorer.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ca.fwe.locations.geometry.Bounds;
import ca.fwe.locations.geometry.XY;
import ca.fwe.nts.NTSMapSheet;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class GeoMapSheet {

	private NTSMapSheet sheet ;

	private PolylineOptions plo ;
	private GroundOverlayOptions goo ;
	private MarkerOptions mo ;
	private PolygonOptions po ;

	private Polyline rectangle ;
	private GroundOverlay overlay ;
	private Marker marker ;
	private Polygon polygon ;

	public GeoMapSheet(NTSMapSheet sheet) {
		this.sheet = sheet ;
	}

	public NTSMapSheet getNTSSheet() {
		return sheet ;
	}

	public PolylineOptions getPolylineOptions() {
		if(plo == null)
			plo = setupPolyline(sheet.getBounds()) ;
		return plo ;
	}

	public GroundOverlayOptions getGroundOverlayOptions() {
		if(goo == null)
			goo = setupGroundOverlay(sheet.getBounds()) ;
		return goo ;
	}

	public MarkerOptions getMarkerOptions(Context context) {
		if(mo == null) {
			mo = setupMarker(context, sheet.getNtsId(), sheet.getBounds().getCentre()) ;
		}
		return mo ;
	}

	public PolygonOptions getPolygonOptions() {
		if(po == null)
			po = setupPolygon(sheet.getBounds()) ;
		return po ;
	}

	public Marker addMarker(Context context, GoogleMap map) {
		this.getMarkerOptions(context) ;
		marker = map.addMarker(mo) ;
		return marker ;
	}

	public Polyline addPolyline(GoogleMap map) {
		this.getPolylineOptions() ;
		rectangle = map.addPolyline(plo) ;
		return rectangle ;
	}

	public GroundOverlay addGroundOverlay(GoogleMap map) {
		this.getGroundOverlayOptions() ;
		try {
			overlay = map.addGroundOverlay(goo) ;
		} catch(NullPointerException e) {
			//TODO diagnostic throw because null pointer exception was thrown.
			throw new NullPointerException("NullPointerException on addGroundOverlay for sheet " + sheet.getNtsId() + " and goo " + goo) ;
		}
		return overlay ;
	}

	public Polygon addPolygon(GoogleMap map) {
		this.getPolygonOptions() ;
		polygon = map.addPolygon(po) ;
		return polygon ;
	}

	public boolean hasMarkerOptions() {
		return mo != null ;
	}

	public boolean hasPolylineOptions() {
		return plo != null ;
	}

	public boolean hasGroundOverlayOptions() {
		return goo != null ;
	}

	public boolean hasPolygonOptions() {
		return po != null ;
	}

	public void removeMarker() {
		if(marker != null) {
			marker.remove();
			marker = null ;
		}
	}

	public void removePolyline() {
		if(rectangle != null) {
			rectangle.remove();
			rectangle = null ;
		}
	}

	public void removeOverlay() {
		if(overlay != null) {
			overlay.remove();
			overlay = null ;
		}
	}

	public void removePolygon() {
		if(polygon != null) {
			polygon.remove();
			polygon = null ;
		}
	}

	public void removeAll() {
		this.removeMarker();
		this.removeOverlay();
		this.removePolyline();
		this.removePolygon();
	}

	public Marker getMarker() {
		return marker ;
	}

	public Polyline getPolyline() {
		return rectangle ;
	}

	public GroundOverlay getOverlay() {
		return overlay ;
	}

	public Polygon getPolygon() {
		return polygon ;
	}

	private static PolylineOptions setupPolyline(Bounds b) {
		PolylineOptions out = new PolylineOptions() ;
		LatLng p1 = new LatLng(b.getMinY(), b.getMinX()) ;
		LatLng p2 = new LatLng(b.getMinY(), b.getMaxX()) ;
		LatLng p3 = new LatLng(b.getMaxY(), b.getMaxX()) ;
		LatLng p4 = new LatLng(b.getMaxY(), b.getMinX()) ;
		out.add(p1, p2, p3, p4, p1) ;
		return out ;
	}

	private static GroundOverlayOptions setupGroundOverlay(Bounds b) {
		GroundOverlayOptions out = new GroundOverlayOptions() ;
		LatLng p1 = new LatLng(b.getMinY(), b.getMinX()) ;
		LatLng p3 = new LatLng(b.getMaxY(), b.getMaxX()) ;
		out.positionFromBounds(new LatLngBounds(p1, p3)) ;
		return out ;
	}

	private static MarkerOptions setupMarker(Context context, String label, XY centre) {
		MarkerOptions out = new MarkerOptions() ;
		out.position(new LatLng(centre.y(), centre.x())) ;
		Bitmap marker = createBitmapFromLayoutWithText(context, label) ;
		out.icon(BitmapDescriptorFactory.fromBitmap(marker)) ;
		out.anchor((float)0.5, (float)0.5) ;
		return out ;
	}

	private static PolygonOptions setupPolygon(Bounds b) {
		PolygonOptions out = new PolygonOptions() ;
		LatLng p1 = new LatLng(b.getMinY(), b.getMinX()) ;
		LatLng p2 = new LatLng(b.getMinY(), b.getMaxX()) ;
		LatLng p3 = new LatLng(b.getMaxY(), b.getMaxX()) ;
		LatLng p4 = new LatLng(b.getMaxY(), b.getMinX()) ;
		out.add(p1, p2, p3, p4, p1) ;
		return out ;
	}

	private static Bitmap createBitmapFromLayoutWithText(Context context, String text) {
		LayoutInflater  mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		//Inflate the layout into a view and configure it the way you like
		RelativeLayout view = new RelativeLayout(context);
		mInflater.inflate(R.layout.map_nts_label, view, true);
		TextView tv = (TextView)view.findViewById(R.id.map_ntslabel_text);
		tv.setText(text);

		//Provide it with a layout params. It should necessarily be wrapping the
		//content as we not really going to have a parent for it.
		view.setLayoutParams(new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT));

		//Pre-measure the view so that height and width don't remain null.
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		//Assign a size and position to the view and all of its descendants 
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

		//Create the bitmap
		Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), 
				view.getMeasuredHeight(), 
				Bitmap.Config.ARGB_8888);
		//Create a canvas with the specified bitmap to draw into
		Canvas c = new Canvas(bitmap);

		//Render this view (and all of its children) to the given Canvas
		view.draw(c);
		return bitmap;
	}

}


