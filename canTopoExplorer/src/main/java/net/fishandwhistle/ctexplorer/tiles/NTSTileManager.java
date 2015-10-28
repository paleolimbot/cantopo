package net.fishandwhistle.ctexplorer.tiles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import ca.fwe.locations.geometry.Bounds;
import ca.fwe.locations.geometry.XY;
import ca.fwe.nts.NTSMapSheet;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public abstract class NTSTileManager {

	private static final String TAG = "NTSTileManager" ;

	protected Context context ;
	protected GoogleMap map ;
	protected List<GeoMapSheet> sheets ;
	protected Set<String> sheetIds ;
	private Bounds lastBounds ;
	private double lastInvScale ;
	private int currentScaleConstant ;
	private boolean enabled ;

	private AsyncTask<Void, GeoMapSheet, Void> loadTask ;
	private AsyncTask<Void, GeoMapSheet, Void> cleanTask ;

	public NTSTileManager(Context context, GoogleMap map) {
		this.context = context ;
		this.map = map ;
		this.sheets = new ArrayList<GeoMapSheet>() ;
		this.sheetIds = new HashSet<String>() ;
		currentScaleConstant = -1 ;
		enabled = false ;
	}

	protected abstract int getScale(Bounds bounds, double invScale) ;

	protected void prepareForMap(GoogleMap map, GeoMapSheet sheet) {
		//default: do nothing. will not run on UI thread
	}

	protected abstract void addToMap(GoogleMap map, GeoMapSheet sheet) ;

	protected void removeFromMap(GoogleMap map, GeoMapSheet sheet) {
		sheet.removeAll();
	}

	public void onBoundsUpdated(final Bounds newBounds, double invScale) {
		if(loadTask != null)
			loadTask.cancel(false) ;
		if(cleanTask != null)
			cleanTask.cancel(false) ;

		lastBounds = newBounds ;
		lastInvScale = invScale ;
		final int scale = this.getScale(newBounds, invScale) ;
		if(scale != currentScaleConstant) {
			currentScaleConstant = scale ;
			if(loadTask != null)
				loadTask.cancel(false) ;
			this.removeAllSheets();
			Log.i(TAG, "changing to scale " + scale) ;
		}

		if(enabled && scale != -1) {
			loadTask = new AsyncTask<Void, GeoMapSheet, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					List<NTSMapSheet> sheetsInArea = NTSMapSheet.getSheetsByBounds(scale, newBounds) ;
					for(NTSMapSheet ns: sheetsInArea) {
						if(this.isCancelled())
							break ;
						if(!containsNTSSheet(ns)) {
							GeoMapSheet gs = new GeoMapSheet(ns) ;
							prepareForMap(map, gs) ;
							this.publishProgress(gs);
						}
					}
					return null ;
				}

				@Override
				protected void onProgressUpdate(GeoMapSheet... values) {
					if(!this.isCancelled()) {
						sheets.add(values[0]) ;
						sheetIds.add(values[0].getNTSSheet().getNtsId()) ;
						addToMap(map, values[0]) ;
					}
				}

				@Override
				protected void onPostExecute(Void result) {
					cleanSheets(newBounds) ;
				}



			} ;
			loadTask.execute() ;
		}
	}

	public void setEnabled(boolean flag) {
		if(flag != enabled) {
			enabled = flag ;
			if(!enabled) {
				if(loadTask != null)
					loadTask.cancel(false) ;
				this.removeAllSheets();
			} else {
				if(lastBounds != null)
					this.refreshSheets();
			}
		}
	}

	public boolean isEnabled() {
		return enabled ;
	}

	public void refreshSheets() {
		this.removeAllSheets();
		this.onBoundsUpdated(lastBounds, lastInvScale);
	}

	public int getCurrentScaleConstant() {
		return currentScaleConstant ;
	}

	public GeoMapSheet getGeoMapSheet(NTSMapSheet sheet) {
		for(GeoMapSheet s: sheets) {
			if(s.getNTSSheet().equals(sheet))
				return s ;
		}
		return null ;
	}

	public GeoMapSheet getGeoMapSheet(Marker marker) {
		for(GeoMapSheet s: sheets) {
			if(marker.equals(s.getMarker()))
				return s ;
		}
		return null ;
	}

	private void removeAllSheets() {
		for(GeoMapSheet s: sheets) {
			this.removeFromMap(map, s);
		}
		sheets.clear();
		sheetIds.clear();
	}

	private boolean containsNTSSheet(NTSMapSheet sheet) {
		return sheetIds.contains(sheet.getNtsId()) ;
	}

	private void cleanSheets(final Bounds b) {
		if(cleanTask != null)
			cleanTask.cancel(false) ;
		cleanTask = new AsyncTask<Void, GeoMapSheet, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				List<GeoMapSheet> toRemove = new ArrayList<GeoMapSheet>() ;
				XY centre = b.getCentre() ;
				double cx = centre.x() ;
				double cy = centre.y();
				double height = b.height()*2 ;
				double width = b.width()* 2 ;
				Bounds newBounds = new Bounds(cx-width/2, cx+width/2, cy-height/2, cy+height/2) ;

				for(GeoMapSheet s: sheets) {
					if(!boundsIntersect(newBounds, s.getNTSSheet().getBounds())) {
						toRemove.add(s) ;
					}
				}

				for(GeoMapSheet s: toRemove) {
					if(this.isCancelled())
						break ;
					this.publishProgress(s);
				}

				Log.i(TAG, "cleaned " + toRemove.size() + " sheets") ;
				return null ;
			}

			@Override
			protected void onProgressUpdate(GeoMapSheet... values) {
				if(!this.isCancelled()) {
					removeFromMap(map, values[0]) ;
					sheetIds.remove(values[0].getNTSSheet().getNtsId()) ;
					sheets.remove(values[0]) ;
				}
			}

		} ;
		cleanTask.execute() ; 

	}

	private boolean boundsIntersect(Bounds b1, Bounds b2) {
		if (b1.getMinX() < b2.getMaxX() && b1.getMaxX() > b2.getMinX() &&
				b1.getMinY() < b2.getMaxY() && b1.getMaxY() > b2.getMinY())
			return true ;
		else
			return false ;
	}

}
