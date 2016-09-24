package net.fishandwhistle.ctexplorer.tiles;

import android.content.Context;
import fwe.locations.geometry.Bounds;
import fwe.nts.NTSMapSheet;

import com.google.android.gms.maps.GoogleMap;

public class NTSGrid extends NTSTileManager {

	protected static final int WIDTH_MINOR = 2 ;
	protected static final int WIDTH_MAJOR = 6 ;
	
	protected static final double MAX_SCALE_50K = 2e6 ;
	protected static final double MAX_SCALE_250K = 8e6 ;
	protected static final double MAX_SCALE_SERIES = 5e7 ;
	
	public NTSGrid(Context context, GoogleMap map) {
		super(context, map);
	}

	@Override
	protected int getScale(Bounds bounds, double invScale) {
		if(invScale <= MAX_SCALE_50K) {
			return NTSMapSheet.SCALE_50K ;
		} else if(invScale <= MAX_SCALE_250K) {
			return NTSMapSheet.SCALE_250K ;
		} else if(invScale <= MAX_SCALE_SERIES) {
			return NTSMapSheet.SCALE_SERIES ;
		} else {
			return -1 ;
		}
	}

	@Override
	protected void addToMap(GoogleMap map, GeoMapSheet sheet) {
		sheet.addMarker(context, map) ;
		sheet.getPolylineOptions().width(WIDTH_MINOR) ;
		sheet.addPolyline(map) ;
	}

}
