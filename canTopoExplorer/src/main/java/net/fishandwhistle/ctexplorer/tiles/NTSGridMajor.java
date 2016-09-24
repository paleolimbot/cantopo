package net.fishandwhistle.ctexplorer.tiles;

import android.content.Context;
import fwe.locations.geometry.Bounds;
import fwe.nts.NTSMapSheet;

import com.google.android.gms.maps.GoogleMap;

public class NTSGridMajor extends NTSGrid {

	public NTSGridMajor(Context context, GoogleMap map) {
		super(context, map);
	}

	@Override
	protected int getScale(Bounds bounds, double invScale) {
		if(invScale <= MAX_SCALE_50K) {
			return NTSMapSheet.SCALE_250K ;
		} else if(invScale <= MAX_SCALE_250K) {
			return NTSMapSheet.SCALE_SERIES ;
		} else {
			return -1 ;
		}
	}

	@Override
	protected void addToMap(GoogleMap map, GeoMapSheet sheet) {
		sheet.getPolylineOptions().width(WIDTH_MAJOR) ;
		sheet.addPolyline(map) ;
	}
	
}
